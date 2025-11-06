package com.renova.mobile.ui.viewmodels

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
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
import java.io.IOException

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
    companion object {
        private const val TAG = "RegisterViewModel"
        private const val MAX_IMAGE_SIZE_KB = 1024 // 1 MB
        private const val COMPRESSION_QUALITY = 85
        private const val MAX_IMAGE_DIMENSION = 1920
    }

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
                    Log.d(TAG, "=== DEBUG REGISTER ERROR ===")
                    Log.d(TAG, "Status Code: ${response.code()}")
                    Log.d(TAG, "Response Body: $responseBody")
                    Log.d(TAG, "Message: ${responseBody?.message}")
                    Log.d(TAG, "Errors: ${responseBody?.errors}")

                    val backendMessage = responseBody?.message ?: "Error al registrar usuario"
                    val statusCode = response.code()

                    val localizedMessage = if (statusCode == 422 && responseBody?.errors != null) {
                        val errors = responseBody.errors
                        if (errors is Map<*, *>) {
                            val errorsMap = errors as? Map<String, List<String>>
                            val firstError = errorsMap?.entries?.firstOrNull()

                            if (firstError != null && firstError.value.isNotEmpty()) {
                                val fieldName = firstError.key
                                val errorMessage = firstError.value.first()
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

                    _registerState.value = RegisterState.Error(localizedMessage)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception during registration", e)
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
                Log.d(TAG, "=== INICIANDO UPLOAD DE DOCUMENTOS ===")

                val frontFile = uriToCompressedFile(context, documentsData.ineFrontUri, "front.jpg")
                val backFile = uriToCompressedFile(context, documentsData.ineBackUri, "back.jpg")

                Log.d(TAG, "Tamaño archivo frontal: ${frontFile.length() / 1024} KB")
                Log.d(TAG, "Tamaño archivo trasero: ${backFile.length() / 1024} KB")

                val frontPart = MultipartBody.Part.createFormData(
                    "document_front",
                    frontFile.name,
                    frontFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                )

                val backPart = MultipartBody.Part.createFormData(
                    "document_back",
                    backFile.name,
                    backFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                )

                val response = ApiClient.apiService.uploadDocuments(
                    userId = userId,
                    document_front = frontPart,
                    document_back = backPart
                )

                if (response.isSuccessful && response.body()?.success == true) {
                    Log.d(TAG, "✅ Documentos subidos exitosamente")
                    _uploadDocumentsState.value = UploadState.Success
                } else {
                    val responseBody = response.body()
                    val errorBody = response.errorBody()?.string()

                    Log.e(TAG, "=== ERROR UPLOAD DOCUMENTOS ===")
                    Log.e(TAG, "Status Code: ${response.code()}")
                    Log.e(TAG, "Response Body: $responseBody")
                    Log.e(TAG, "Error Body: $errorBody")

                    val backendMessage = responseBody?.message ?: "Error al subir documentos"
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
                Log.e(TAG, "Exception durante upload de documentos", e)
                val localizedMessage = DocumentsErrorMapper.mapDocumentsError(
                    "Error: ${e.message}",
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
                Log.d(TAG, "=== INICIANDO UPLOAD DE SELFIE ===")
                Log.d(TAG, "URI: $selfieUri")

                val selfieFile = uriToCompressedFile(context, selfieUri, "selfie.jpg")
                Log.d(TAG, "Tamaño archivo selfie: ${selfieFile.length() / 1024} KB")

                if (!selfieFile.exists()) {
                    throw IOException("El archivo de selfie no existe")
                }

                val selfiePart = MultipartBody.Part.createFormData(
                    "selfie",
                    selfieFile.name,
                    selfieFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                )

                Log.d(TAG, "Enviando selfie al servidor...")
                val response = ApiClient.apiService.uploadSelfie(
                    userId = userId,
                    selfie = selfiePart
                )

                Log.d(TAG, "Respuesta recibida - Code: ${response.code()}")

                if (response.isSuccessful && response.body()?.success == true) {
                    Log.d(TAG, "✅ Selfie subida exitosamente")
                    _uploadSelfieState.value = UploadState.Success
                } else {
                    val responseBody = response.body()
                    val errorBody = response.errorBody()?.string()

                    Log.e(TAG, "=== ERROR UPLOAD SELFIE ===")
                    Log.e(TAG, "Status Code: ${response.code()}")
                    Log.e(TAG, "Response Body: $responseBody")
                    Log.e(TAG, "Error Body: $errorBody")
                    Log.e(TAG, "Message: ${responseBody?.message}")
                    Log.e(TAG, "Errors: ${responseBody?.errors}")

                    val backendMessage = responseBody?.message ?: errorBody ?: "Error al subir selfie"
                    val statusCode = response.code()

                    val localizedMessage = if (statusCode == 422 && responseBody?.errors != null) {
                        val errors = responseBody.errors
                        if (errors is Map<*, *>) {
                            val errorsMap = errors as? Map<String, List<String>>
                            val firstError = errorsMap?.entries?.firstOrNull()

                            if (firstError != null && firstError.value.isNotEmpty()) {
                                val errorMessage = firstError.value.first()
                                Log.e(TAG, "Error de validación: $errorMessage")
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
                Log.e(TAG, "Exception durante upload de selfie", e)
                Log.e(TAG, "Stack trace:", e)

                val localizedMessage = SelfieErrorMapper.mapSelfieError(
                    "Error: ${e.message}",
                    -1,
                    context
                )
                _uploadSelfieState.value = UploadState.Error(localizedMessage)
            }
        }
    }

    /**
     * Convierte un URI a un archivo comprimido para reducir el tamaño
     */
    private fun uriToCompressedFile(context: Context, uri: Uri, fileName: String): File {
        try {
            // Leer el bitmap original
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: throw IOException("No se pudo abrir el archivo")

            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            if (originalBitmap == null) {
                throw IOException("No se pudo decodificar la imagen")
            }

            Log.d(TAG, "Imagen original - Ancho: ${originalBitmap.width}, Alto: ${originalBitmap.height}")

            // Escalar si es necesario
            val scaledBitmap = if (originalBitmap.width > MAX_IMAGE_DIMENSION ||
                originalBitmap.height > MAX_IMAGE_DIMENSION) {
                val ratio = minOf(
                    MAX_IMAGE_DIMENSION.toFloat() / originalBitmap.width,
                    MAX_IMAGE_DIMENSION.toFloat() / originalBitmap.height
                )
                val newWidth = (originalBitmap.width * ratio).toInt()
                val newHeight = (originalBitmap.height * ratio).toInt()

                Log.d(TAG, "Escalando imagen a - Ancho: $newWidth, Alto: $newHeight")
                Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)
            } else {
                originalBitmap
            }

            // Comprimir y guardar
            val file = File(context.cacheDir, fileName)
            FileOutputStream(file).use { output ->
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY, output)
            }

            // Limpiar memoria
            if (scaledBitmap != originalBitmap) {
                originalBitmap.recycle()
            }
            scaledBitmap.recycle()

            Log.d(TAG, "Archivo comprimido creado: ${file.length() / 1024} KB")

            return file
        } catch (e: Exception) {
            Log.e(TAG, "Error al comprimir imagen", e)
            throw IOException("Error al procesar la imagen: ${e.message}", e)
        }
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