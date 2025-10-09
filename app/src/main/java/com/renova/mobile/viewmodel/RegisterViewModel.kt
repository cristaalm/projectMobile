package com.renova.mobile.ui.viewmodels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renova.mobile.network.*
import com.renova.mobile.screens.DocumentsData
import com.renova.mobile.screens.RegisterData
import com.renova.mobile.utils.SessionManager
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

    // Guardar datos del registro para usarlos en pantallas siguientes
    var userId: Int = 0
        private set
    var authToken: String = ""
        private set
    var tokenType: String = ""
        private set
    var expiresAt: String = ""
        private set

    private var sessionManager: SessionManager? = null

    fun setSessionManager(context: Context) {
        if (sessionManager == null) {
            sessionManager = SessionManager(context)
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

                        // IMPORTANTE: Guardar el token temporalmente para las siguientes peticiones
                        sessionManager?.saveAuthToken(authToken, tokenType)

                        _registerState.value = RegisterState.Success(
                            userId = userId,
                            token = authToken,
                            tokenType = tokenType,
                            expiresAt = expiresAt
                        )
                    } else {
                        _registerState.value = RegisterState.Error("Error: datos de usuario no disponibles")
                    }
                } else {
                    val errorMessage = response.body()?.message ?: "Error al registrar usuario"
                    _registerState.value = RegisterState.Error(errorMessage)
                }
            } catch (e: Exception) {
                _registerState.value = RegisterState.Error("Error de conexión: ${e.message}")
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
                    val errorMessage = response.body()?.message ?: "Error al subir documentos"
                    _uploadDocumentsState.value = UploadState.Error(errorMessage)
                }
            } catch (e: Exception) {
                _uploadDocumentsState.value = UploadState.Error("Error de conexión: ${e.message}")
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
                    val errorMessage = response.body()?.message ?: "Error al subir selfie"
                    _uploadSelfieState.value = UploadState.Error(errorMessage)
                }
            } catch (e: Exception) {
                _uploadSelfieState.value = UploadState.Error("Error de conexión: ${e.message}")
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