package com.renova.mobile.ui.viewmodels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renova.mobile.network.*
import com.renova.mobile.screens.DocumentsData
import com.renova.mobile.screens.RegisterData
import com.renova.mobile.utils.SessionManager
import com.renova.mobile.utils.RegisterErrorMapper
import com.renova.mobile.utils.DocumentsErrorMapper
import com.renova.mobile.utils.SelfieErrorMapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

sealed class RegisterState {
    object Idle : RegisterState()
    object Loading : RegisterState()
    data class Success(val userId: Int, val token: String, val tokenType: String, val expiresAt: String) : RegisterState()
    data class Error(val message: String) : RegisterState()
}

sealed class UploadState {
    object Idle : UploadState()
    object Loading : UploadState()
    object Success : UploadState()
    data class Error(val message: String) : UploadState()
}

class RegisterViewModel : ViewModel() {
    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> = _registerState

    private val _uploadDocumentsState = MutableStateFlow<UploadState>(UploadState.Idle)
    val uploadDocumentsState: StateFlow<UploadState> = _uploadDocumentsState

    private val _uploadSelfieState = MutableStateFlow<UploadState>(UploadState.Idle)
    val uploadSelfieState: StateFlow<UploadState> = _uploadSelfieState

    var userId: Int = 0
        private set
    var authToken: String = ""
        private set
    var tokenType: String = ""
        private set
    var expiresAt: String = ""
        private set

    private var sessionManager: SessionManager? = null
    private var appContext: Context? = null

    fun setSessionManager(context: Context) {
        if (sessionManager == null) {
            sessionManager = SessionManager(context)
            appContext = context.applicationContext
        }
    }

    fun registerUser(registerData: RegisterData) {
        viewModelScope.launch {
            _registerState.value = RegisterState.Loading
            try {
                val request = RegisterRequest(
                    name = registerData.firstName,
                    last_name = registerData.lastName,
                    email = registerData.email,
                    phone = registerData.phone,
                    curp = registerData.curp,
                    password = registerData.password,
                    password_confirmation = registerData.password
                )

                val response = ApiClient.apiService.register(request)

                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()?.data
                    if (data != null) {
                        userId = data.user.id
                        authToken = data.access_token
                        tokenType = data.token_type
                        expiresAt = data.expires_at

                        sessionManager?.saveAuthToken(authToken, tokenType)

                        _registerState.value = RegisterState.Success(
                            userId = userId,
                            token = authToken,
                            tokenType = tokenType,
                            expiresAt = expiresAt
                        )
                    } else {
                        val errorMsg = appContext?.let { ctx ->
                            RegisterErrorMapper.mapRegisterError(
                                "Error: datos de usuario no disponibles",
                                -1,
                                ctx
                            )
                        } ?: "Error: datos de usuario no disponibles"
                        _registerState.value = RegisterState.Error(errorMsg)
                    }
                } else {
                    val responseBody = response.body()

                    // 🔍 DEBUG: Ver qué está llegando
                    android.util.Log.d("RegisterViewModel", "=== DEBUG REGISTER ERROR ===")
                    android.util.Log.d("RegisterViewModel", "Status Code: ${response.code()}")
                    android.util.Log.d("RegisterViewModel", "Response Body: $responseBody")
                    android.util.Log.d("RegisterViewModel", "Message: ${responseBody?.message}")
                    android.util.Log.d("RegisterViewModel", "Errors: ${responseBody?.errors}")
                    android.util.Log.d("RegisterViewModel", "Errors Type: ${responseBody?.errors?.javaClass}")

                    // 🌍 Mapear el error al idioma actual
                    val backendMessage = responseBody?.message ?: "Error al registrar usuario"
                    val statusCode = response.code()

                    // Verificar si hay errores específicos de validación (422)
                    val localizedMessage = if (statusCode == 422 && responseBody?.errors != null) {
                        // Extraer el primer error específico del campo
                        val errors = responseBody.errors
                        android.util.Log.d("RegisterViewModel", "Errors is Map: ${errors is Map<*, *>}")

                        if (errors is Map<*, *>) {
                            val errorsMap = errors as? Map<String, List<String>>
                            android.util.Log.d("RegisterViewModel", "Errors Map: $errorsMap")

                            val firstError = errorsMap?.entries?.firstOrNull()
                            android.util.Log.d("RegisterViewModel", "First Error: $firstError")

                            if (firstError != null && firstError.value.isNotEmpty()) {
                                val fieldName = firstError.key
                                val errorMessage = firstError.value.first()

                                android.util.Log.d("RegisterViewModel", "Field: $fieldName, Message: $errorMessage")

                                appContext?.let { ctx ->
                                    RegisterErrorMapper.mapValidationError(fieldName, errorMessage, ctx)
                                } ?: errorMessage
                            } else {
                                appContext?.let { ctx ->
                                    RegisterErrorMapper.mapRegisterError(backendMessage, statusCode, ctx)
                                } ?: backendMessage
                            }
                        } else {
                            appContext?.let { ctx ->
                                RegisterErrorMapper.mapRegisterError(backendMessage, statusCode, ctx)
                            } ?: backendMessage
                        }
                    } else {
                        appContext?.let { ctx ->
                            RegisterErrorMapper.mapRegisterError(backendMessage, statusCode, ctx)
                        } ?: backendMessage
                    }

                    android.util.Log.d("RegisterViewModel", "Final Localized Message: $localizedMessage")

                    _registerState.value = RegisterState.Error(localizedMessage)
                }
            } catch (e: Exception) {
                val errorMsg = appContext?.let { ctx ->
                    RegisterErrorMapper.mapRegisterError(
                        "Error de conexión: ${e.message}",
                        -1,
                        ctx
                    )
                } ?: "Error de conexión: ${e.message}"
                _registerState.value = RegisterState.Error(errorMsg)
            }
        }
    }

    fun uploadDocuments(context: Context, documentsData: DocumentsData) {
        viewModelScope.launch {
            _uploadDocumentsState.value = UploadState.Loading
            try {
                val frontFile = uriToFile(context, documentsData.ineFrontUri, "front.jpg")
                val backFile = uriToFile(context, documentsData.ineBackUri, "back.jpg")

                val frontPart = MultipartBody.Part.createFormData(
                    "document_front",
                    frontFile.name,
                    frontFile.asRequestBody("image/*".toMediaTypeOrNull())
                )

                val backPart = MultipartBody.Part.createFormData(
                    "document_back",
                    backFile.name,
                    backFile.asRequestBody("image/*".toMediaTypeOrNull())
                )

                val response = ApiClient.apiService.uploadDocuments(
                    userId = userId,
                    document_front = frontPart,
                    document_back = backPart
                )

                if (response.isSuccessful && response.body()?.success == true) {
                    _uploadDocumentsState.value = UploadState.Success
                } else {
                    // 🌍 Mapear el error al idioma actual
                    val responseBody = response.body()
                    val backendMessage = responseBody?.message ?: "Error al subir documentos"
                    val statusCode = response.code()

                    // Verificar si hay errores específicos de validación (422)
                    val localizedMessage = if (statusCode == 422 && responseBody?.errors != null) {
                        val errors = responseBody.errors
                        if (errors is Map<*, *>) {
                            val errorsMap = errors as? Map<String, List<String>>
                            val firstError = errorsMap?.entries?.firstOrNull()

                            if (firstError != null && firstError.value.isNotEmpty()) {
                                val errorMessage = firstError.value.first()
                                DocumentsErrorMapper.mapDocumentValidationError(errorMessage, context)
                            } else {
                                DocumentsErrorMapper.mapDocumentsError(backendMessage, statusCode, context)
                            }
                        } else {
                            DocumentsErrorMapper.mapDocumentsError(backendMessage, statusCode, context)
                        }
                    } else {
                        DocumentsErrorMapper.mapDocumentsError(backendMessage, statusCode, context)
                    }

                    _uploadDocumentsState.value = UploadState.Error(localizedMessage)
                }
            } catch (e: Exception) {
                val localizedMessage = DocumentsErrorMapper.mapDocumentsError(
                    "Error de conexión: ${e.message}",
                    -1,
                    context
                )
                _uploadDocumentsState.value = UploadState.Error(localizedMessage)
            }
        }
    }

    fun uploadSelfie(context: Context, selfieUri: Uri) {
        viewModelScope.launch {
            _uploadSelfieState.value = UploadState.Loading
            try {
                val selfieFile = uriToFile(context, selfieUri, "selfie.jpg")

                val selfiePart = MultipartBody.Part.createFormData(
                    "selfie",
                    selfieFile.name,
                    selfieFile.asRequestBody("image/*".toMediaTypeOrNull())
                )

                val response = ApiClient.apiService.uploadSelfie(
                    userId = userId,
                    selfie = selfiePart
                )

                if (response.isSuccessful && response.body()?.success == true) {
                    _uploadSelfieState.value = UploadState.Success
                } else {
                    // 🌍 Mapear el error al idioma actual
                    val responseBody = response.body()
                    val backendMessage = responseBody?.message ?: "Error al subir selfie"
                    val statusCode = response.code()

                    // Verificar si hay errores específicos de validación (422)
                    val localizedMessage = if (statusCode == 422 && responseBody?.errors != null) {
                        val errors = responseBody.errors
                        if (errors is Map<*, *>) {
                            val errorsMap = errors as? Map<String, List<String>>
                            val firstError = errorsMap?.entries?.firstOrNull()

                            if (firstError != null && firstError.value.isNotEmpty()) {
                                val errorMessage = firstError.value.first()
                                SelfieErrorMapper.mapSelfieValidationError(errorMessage, context)
                            } else {
                                SelfieErrorMapper.mapSelfieError(backendMessage, statusCode, context)
                            }
                        } else {
                            SelfieErrorMapper.mapSelfieError(backendMessage, statusCode, context)
                        }
                    } else {
                        SelfieErrorMapper.mapSelfieError(backendMessage, statusCode, context)
                    }

                    _uploadSelfieState.value = UploadState.Error(localizedMessage)
                }
            } catch (e: Exception) {
                val localizedMessage = SelfieErrorMapper.mapSelfieError(
                    "Error de conexión: ${e.message}",
                    -1,
                    context
                )
                _uploadSelfieState.value = UploadState.Error(localizedMessage)
            }
        }
    }

    private fun uriToFile(context: Context, uri: Uri, fileName: String): File {
        val file = File(context.cacheDir, fileName)
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
        return file
    }

    fun resetStates() {
        _registerState.value = RegisterState.Idle
        _uploadDocumentsState.value = UploadState.Idle
        _uploadSelfieState.value = UploadState.Idle
    }

    fun clearSession() {
        sessionManager?.clearAuthToken()
        userId = 0
        authToken = ""
        tokenType = ""
        expiresAt = ""
    }
}