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

    // ✅ NUEVO: Estados para almacenar los datos del formulario
    private val _formData = MutableStateFlow(RegisterData(
        firstName = "",
        lastName = "",
        email = "",
        phone = "",
        curp = "",
        password = ""
    ))
    val formData: StateFlow<RegisterData> = _formData

    // ✅ NUEVO: Estados para almacenar URIs de documentos
    private val _documentsUris = MutableStateFlow<Pair<Uri?, Uri?>>(null to null)
    val documentsUris: StateFlow<Pair<Uri?, Uri?>> = _documentsUris

    // ✅ NUEVO: Estado para almacenar URI de selfie
    private val _selfieUri = MutableStateFlow<Uri?>(null)
    val selfieUri: StateFlow<Uri?> = _selfieUri

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

    // ✅ NUEVO: Actualizar datos del formulario
    fun updateFormData(data: RegisterData) {
        _formData.value = data
        android.util.Log.d("RegisterViewModel", "Datos del formulario actualizados: ${data.email}")
    }

    // ✅ NUEVO: Guardar URIs de documentos
    fun updateDocumentsUris(frontUri: Uri?, backUri: Uri?) {
        _documentsUris.value = frontUri to backUri
        android.util.Log.d("RegisterViewModel", "URIs de documentos actualizados")
    }

    // ✅ NUEVO: Guardar URI de selfie
    fun updateSelfieUri(uri: Uri?) {
        _selfieUri.value = uri
        android.util.Log.d("RegisterViewModel", "URI de selfie actualizada")
    }

    fun registerUser(registerData: RegisterData) {
        // ✅ Prevenir múltiples llamadas
        if (_registerState.value is RegisterState.Loading) {
            android.util.Log.w("RegisterViewModel", "Ya hay un registro en progreso, ignorando...")
            return
        }

        viewModelScope.launch {
            _registerState.value = RegisterState.Loading

            android.util.Log.d("RegisterViewModel", "=== INICIANDO REGISTRO ===")
            android.util.Log.d("RegisterViewModel", "Email: ${registerData.email}")

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

                android.util.Log.d("RegisterViewModel", "Enviando petición al servidor...")
                val response = ApiClient.apiService.register(request)
                android.util.Log.d("RegisterViewModel", "Respuesta recibida: ${response.code()}")

                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()?.data
                    android.util.Log.d("RegisterViewModel", "✅ Registro exitoso")

                    if (data != null) {
                        userId = data.user.id
                        authToken = data.access_token
                        tokenType = data.token_type
                        expiresAt = data.expires_at

                        android.util.Log.d("RegisterViewModel", "UserId: $userId")
                        android.util.Log.d("RegisterViewModel", "Token guardado en SessionManager")

                        // Guardar token temporal
                        sessionManager?.saveAuthToken(authToken, tokenType)

                        _registerState.value = RegisterState.Success(
                            userId = userId,
                            token = authToken,
                            tokenType = tokenType,
                            expiresAt = expiresAt
                        )
                    } else {
                        android.util.Log.e("RegisterViewModel", "❌ Data es null")
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
                    val errorBody = response.errorBody()?.string()

                    android.util.Log.e("RegisterViewModel", "=== ERROR EN REGISTRO ===")
                    android.util.Log.e("RegisterViewModel", "Status Code: ${response.code()}")
                    android.util.Log.e("RegisterViewModel", "Response Body: $responseBody")
                    android.util.Log.e("RegisterViewModel", "Error Body: $errorBody")
                    android.util.Log.e("RegisterViewModel", "Message: ${responseBody?.message}")
                    android.util.Log.e("RegisterViewModel", "Errors: ${responseBody?.errors}")

                    val backendMessage = responseBody?.message ?: errorBody ?: "Error al registrar usuario"
                    val statusCode = response.code()

                    val localizedMessage = if (statusCode == 422 && responseBody?.errors != null) {
                        val errors = responseBody.errors

                        if (errors is Map<*, *>) {
                            val errorsMap = errors as? Map<String, List<String>>
                            val firstError = errorsMap?.entries?.firstOrNull()

                            if (firstError != null && firstError.value.isNotEmpty()) {
                                val fieldName = firstError.key
                                val errorMessage = firstError.value.first()

                                android.util.Log.d("RegisterViewModel", "Campo con error: $fieldName")
                                android.util.Log.d("RegisterViewModel", "Mensaje de error: $errorMessage")

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

                    android.util.Log.e("RegisterViewModel", "Mensaje final: $localizedMessage")
                    _registerState.value = RegisterState.Error(localizedMessage)
                }
            } catch (e: Exception) {
                android.util.Log.e("RegisterViewModel", "=== EXCEPCIÓN EN REGISTRO ===", e)
                android.util.Log.e("RegisterViewModel", "Tipo: ${e.javaClass.simpleName}")
                android.util.Log.e("RegisterViewModel", "Mensaje: ${e.message}")
                android.util.Log.e("RegisterViewModel", "Causa: ${e.cause}")

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
        // ✅ Prevenir múltiples llamadas
        if (_uploadDocumentsState.value is UploadState.Loading) {
            android.util.Log.w("RegisterViewModel", "Ya hay una subida en progreso, ignorando...")
            return
        }

        viewModelScope.launch {
            _uploadDocumentsState.value = UploadState.Loading

            android.util.Log.d("RegisterViewModel", "=== SUBIENDO DOCUMENTOS ===")
            android.util.Log.d("RegisterViewModel", "UserId: $userId")

            try {
                val frontFile = uriToFile(context, documentsData.ineFrontUri, "front.jpg")
                val backFile = uriToFile(context, documentsData.ineBackUri, "back.jpg")

                android.util.Log.d("RegisterViewModel", "Front file size: ${frontFile.length()} bytes")
                android.util.Log.d("RegisterViewModel", "Back file size: ${backFile.length()} bytes")

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

                android.util.Log.d("RegisterViewModel", "Respuesta documentos: ${response.code()}")

                if (response.isSuccessful && response.body()?.success == true) {
                    android.util.Log.d("RegisterViewModel", "✅ Documentos subidos exitosamente")
                    _uploadDocumentsState.value = UploadState.Success
                } else {
                    val responseBody = response.body()
                    val errorBody = response.errorBody()?.string()

                    android.util.Log.e("RegisterViewModel", "Error subiendo documentos")
                    android.util.Log.e("RegisterViewModel", "Response: $responseBody")
                    android.util.Log.e("RegisterViewModel", "Error Body: $errorBody")

                    val backendMessage = responseBody?.message ?: errorBody ?: "Error al subir documentos"
                    val statusCode = response.code()

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
                android.util.Log.e("RegisterViewModel", "Excepción subiendo documentos", e)
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
        // ✅ Prevenir múltiples llamadas
        if (_uploadSelfieState.value is UploadState.Loading) {
            android.util.Log.w("RegisterViewModel", "Ya hay una subida en progreso, ignorando...")
            return
        }

        viewModelScope.launch {
            _uploadSelfieState.value = UploadState.Loading

            android.util.Log.d("RegisterViewModel", "=== SUBIENDO SELFIE ===")
            android.util.Log.d("RegisterViewModel", "UserId: $userId")

            try {
                val selfieFile = uriToFile(context, selfieUri, "selfie.jpg")
                android.util.Log.d("RegisterViewModel", "Selfie file size: ${selfieFile.length()} bytes")

                val selfiePart = MultipartBody.Part.createFormData(
                    "selfie",
                    selfieFile.name,
                    selfieFile.asRequestBody("image/*".toMediaTypeOrNull())
                )

                val response = ApiClient.apiService.uploadSelfie(
                    userId = userId,
                    selfie = selfiePart
                )

                android.util.Log.d("RegisterViewModel", "Respuesta selfie: ${response.code()}")

                if (response.isSuccessful && response.body()?.success == true) {
                    android.util.Log.d("RegisterViewModel", "✅ Selfie subida exitosamente")
                    _uploadSelfieState.value = UploadState.Success
                } else {
                    val responseBody = response.body()
                    val errorBody = response.errorBody()?.string()

                    android.util.Log.e("RegisterViewModel", "Error subiendo selfie")
                    android.util.Log.e("RegisterViewModel", "Response: $responseBody")
                    android.util.Log.e("RegisterViewModel", "Error Body: $errorBody")

                    val backendMessage = responseBody?.message ?: errorBody ?: "Error al subir selfie"
                    val statusCode = response.code()

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
                android.util.Log.e("RegisterViewModel", "Excepción subiendo selfie", e)
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

    // ✅ Resetear solo estados de UI, mantener datos del formulario y userId/token
    fun resetUIStates() {
        android.util.Log.d("RegisterViewModel", "Reseteando solo estados de UI...")
        _registerState.value = RegisterState.Idle
        _uploadDocumentsState.value = UploadState.Idle
        _uploadSelfieState.value = UploadState.Idle
        // NO reseteamos formData, documentsUris, selfieUri, userId, authToken, etc.
    }

    // Resetear estados de UI solamente
    fun resetStates() {
        android.util.Log.d("RegisterViewModel", "Reseteando estados...")
        _registerState.value = RegisterState.Idle
        _uploadDocumentsState.value = UploadState.Idle
        _uploadSelfieState.value = UploadState.Idle
    }

    // ✅ Limpiar TODO incluyendo datos del formulario
    fun resetAll() {
        android.util.Log.d("RegisterViewModel", "Limpiando completamente...")
        _registerState.value = RegisterState.Idle
        _uploadDocumentsState.value = UploadState.Idle
        _uploadSelfieState.value = UploadState.Idle
        _formData.value = RegisterData("", "", "", "", "", "")
        _documentsUris.value = null to null
        _selfieUri.value = null
        clearSession()
    }

    fun clearSession() {
        android.util.Log.d("RegisterViewModel", "Limpiando sesión temporal...")
        sessionManager?.clearAuthToken()
        userId = 0
        authToken = ""
        tokenType = ""
        expiresAt = ""
    }
}