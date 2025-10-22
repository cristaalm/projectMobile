package com.renova.mobile.ui.viewmodels

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.renova.mobile.data.repository.ProfileRepository
import com.renova.mobile.network.IdentityVerification
import com.renova.mobile.network.UserData
import com.renova.mobile.ui.components.DocumentType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Success(
        val user: UserData,
        val identityVerification: IdentityVerification?
    ) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

enum class VerificationStatus(val code: Int) {
    PENDING(0),
    VERIFIED(1),
    REJECTED(2),
    NO_DOCS(3);

    companion object {
        fun fromCode(code: Int): VerificationStatus {
            return entries.find { it.code == code } ?: NO_DOCS
        }
    }
}

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ProfileRepository(application.applicationContext)

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _uploadState = MutableStateFlow<UploadState>(UploadState.Idle)
    val uploadState: StateFlow<UploadState> = _uploadState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _documentImages = MutableStateFlow<Map<String, ByteArray>>(emptyMap())
    val documentImages: StateFlow<Map<String, ByteArray>> = _documentImages.asStateFlow()

    private val _updateFieldState = MutableStateFlow<UpdateFieldState>(UpdateFieldState.Idle)
    val updateFieldState: StateFlow<UpdateFieldState> = _updateFieldState.asStateFlow()

    private val _verificationRequestState = MutableStateFlow<VerificationRequestState>(VerificationRequestState.Idle)
    val verificationRequestState: StateFlow<VerificationRequestState> = _verificationRequestState.asStateFlow()

    // Nuevo estado para controlar la subida de documentos individuales
    private val _documentUploadState = MutableStateFlow<DocumentUploadState>(DocumentUploadState.Idle)
    val documentUploadState: StateFlow<DocumentUploadState> = _documentUploadState.asStateFlow()

    sealed class UploadState {
        object Idle : UploadState()
        object Loading : UploadState()
        object Success : UploadState()
        data class Error(val message: String) : UploadState()
    }

    sealed class UpdateFieldState {
        object Idle : UpdateFieldState()
        object Loading : UpdateFieldState()
        object Success : UpdateFieldState()
        data class Error(val message: String) : UpdateFieldState()
    }

    sealed class VerificationRequestState {
        object Idle : VerificationRequestState()
        object Loading : VerificationRequestState()
        object Success : VerificationRequestState()
        data class Error(val message: String) : VerificationRequestState()
    }

    sealed class DocumentUploadState {
        object Idle : DocumentUploadState()
        data class Loading(val documentType: DocumentType) : DocumentUploadState()
        data class Success(val documentType: DocumentType) : DocumentUploadState()
        data class Error(val documentType: DocumentType, val message: String) : DocumentUploadState()
    }

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading

            repository.getProfile().fold(
                onSuccess = { response ->
                    response.data?.let { data ->
                        _uiState.value = ProfileUiState.Success(
                            user = data.user,
                            identityVerification = data.identityVerification.firstOrNull()
                        )
                    } ?: run {
                        _uiState.value = ProfileUiState.Error("No se encontraron datos del perfil")
                    }
                },
                onFailure = { exception ->
                    _uiState.value = ProfileUiState.Error(
                        exception.message ?: "Error desconocido al cargar el perfil"
                    )
                }
            )
        }
    }

    fun refreshProfile() {
        viewModelScope.launch {
            _isRefreshing.value = true

            repository.getProfile().fold(
                onSuccess = { response ->
                    response.data?.let { data ->
                        _uiState.value = ProfileUiState.Success(
                            user = data.user,
                            identityVerification = data.identityVerification.firstOrNull()
                        )
                    }
                },
                onFailure = { exception ->
                    _uiState.value = ProfileUiState.Error(
                        exception.message ?: "Error al refrescar el perfil"
                    )
                }
            )

            _isRefreshing.value = false
        }
    }

    fun retry() {
        loadProfile()
    }

    fun clearError() {
        if (_uiState.value is ProfileUiState.Error) {
            loadProfile()
        }
    }

    // Función para comprimir imagen
    private fun compressImage(context: Context, uri: Uri, maxSizeKB: Int = 4500): File? {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            var bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (bitmap == null) {
                return null
            }

            // Escalar imagen si es muy grande
            val maxDimension = 1920
            if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
                val scale = maxDimension.toFloat() / Math.max(bitmap.width, bitmap.height)
                val newWidth = (bitmap.width * scale).toInt()
                val newHeight = (bitmap.height * scale).toInt()
                bitmap = android.graphics.Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
            }

            // Comprimir iterativamente hasta que sea menor que maxSizeKB
            var quality = 90
            val file = File(context.cacheDir, "compressed_${System.currentTimeMillis()}.jpg")

            do {
                val outputStream = java.io.FileOutputStream(file)
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, quality, outputStream)
                outputStream.flush()
                outputStream.close()

                val fileSizeKB = file.length() / 1024

                if (fileSizeKB <= maxSizeKB) break
                quality -= 10
            } while (quality > 10)

            bitmap.recycle()

            val finalSizeKB = file.length() / 1024

            return file
        } catch (e: Exception) {
            return null
        }
    }

    // Función para subir documento individual automáticamente
    fun uploadDocument(documentType: DocumentType, uri: Uri, context: Context) {
        viewModelScope.launch {
            _documentUploadState.value = DocumentUploadState.Loading(documentType)

            val currentState = _uiState.value
            if (currentState !is ProfileUiState.Success) {
                _documentUploadState.value = DocumentUploadState.Error(
                    documentType,
                    "No se pudo obtener la información del usuario"
                )
                return@launch
            }

            val userId = currentState.user.id

            try {
                // Comprimir imagen antes de subir
                val file = compressImage(context, uri, maxSizeKB = 4500)

                if (file == null) {
                    _documentUploadState.value = DocumentUploadState.Error(
                        documentType,
                        "No se pudo procesar la imagen"
                    )
                    return@launch
                }
                val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("document", file.name, requestFile)

                // Determinar el tipo de documento para el endpoint
                val type = when (documentType) {
                    DocumentType.SELFIE -> "selfie"
                    DocumentType.INE_FRONT -> "ine_front"
                    DocumentType.INE_BACK -> "ine_back"
                }
                repository.uploadSingleDocument(type, userId, body).fold(
                    onSuccess = { response ->
                        // Limpiar archivo temporal
                        file.delete()
                        loadSingleDocumentImage(userId, type)

                        _documentUploadState.value = DocumentUploadState.Success(documentType)
                    },
                    onFailure = { exception ->
                        _documentUploadState.value = DocumentUploadState.Error(
                            documentType,
                            exception.message ?: "Error al subir documento"
                        )

                        file.delete()
                    }
                )
            } catch (e: Exception) {
                _documentUploadState.value = DocumentUploadState.Error(
                    documentType,
                    e.message ?: "Error al procesar la imagen"
                )
            }
        }
    }

    private fun loadSingleDocumentImage(userId: Int, type: String) {
        viewModelScope.launch {

            delay(500)

            val getType = when(type) {
                "ine_front" -> "front"
                "ine_back" -> "back"
                "selfie" -> "selfie"
                else -> type
            }
            repository.getDocumentImage(getType, userId)
                .onSuccess { bytes ->

                    val currentImages = _documentImages.value.toMutableMap()
                    currentImages[type] = bytes
                    _documentImages.value = currentImages.toMap()
                }
                .onFailure { }
        }
    }

    fun resetDocumentUploadState() {
        _documentUploadState.value = DocumentUploadState.Idle
    }

    fun updateName(newName: String) {
        viewModelScope.launch {
            _updateFieldState.value = UpdateFieldState.Loading

            repository.updateUserField("name", newName).fold(
                onSuccess = { response ->
                    if (response.success) {
                        _updateFieldState.value = UpdateFieldState.Success

                        val currentState = _uiState.value
                        if (currentState is ProfileUiState.Success) {
                            _uiState.value = currentState.copy(
                                user = currentState.user.copy(name = newName)
                            )
                        }
                    } else {
                        _updateFieldState.value = UpdateFieldState.Error(
                            response.message ?: "Error al actualizar nombre"
                        )
                    }
                },
                onFailure = { exception ->
                    _updateFieldState.value = UpdateFieldState.Error(
                        exception.message ?: "Error de conexión"
                    )
                }
            )
        }
    }

    fun updateLastName(newLastName: String) {
        viewModelScope.launch {
            _updateFieldState.value = UpdateFieldState.Loading

            repository.updateUserField("last_name", newLastName).fold(
                onSuccess = { response ->
                    if (response.success) {
                        _updateFieldState.value = UpdateFieldState.Success

                        val currentState = _uiState.value
                        if (currentState is ProfileUiState.Success) {
                            _uiState.value = currentState.copy(
                                user = currentState.user.copy(last_name = newLastName)
                            )
                        }
                    } else {
                        _updateFieldState.value = UpdateFieldState.Error(
                            response.message ?: "Error al actualizar apellido"
                        )
                    }
                },
                onFailure = { exception ->
                    _updateFieldState.value = UpdateFieldState.Error(
                        exception.message ?: "Error de conexión"
                    )
                }
            )
        }
    }

    fun updateEmail(newEmail: String) {
        viewModelScope.launch {
            _updateFieldState.value = UpdateFieldState.Loading

            repository.updateUserField("email", newEmail).fold(
                onSuccess = { response ->
                    if (response.success) {
                        _updateFieldState.value = UpdateFieldState.Success

                        val currentState = _uiState.value
                        if (currentState is ProfileUiState.Success) {
                            _uiState.value = currentState.copy(
                                user = currentState.user.copy(email = newEmail)
                            )
                        }
                    } else {
                        _updateFieldState.value = UpdateFieldState.Error(
                            response.message ?: "Error al actualizar email"
                        )
                    }
                },
                onFailure = { exception ->
                    _updateFieldState.value = UpdateFieldState.Error(
                        exception.message ?: "Error de conexión"
                    )
                }
            )
        }
    }

    fun updatePhone(newPhone: String) {
        viewModelScope.launch {
            _updateFieldState.value = UpdateFieldState.Loading

            repository.updateUserField("phone", newPhone).fold(
                onSuccess = { response ->
                    if (response.success) {
                        _updateFieldState.value = UpdateFieldState.Success

                        val currentState = _uiState.value
                        if (currentState is ProfileUiState.Success) {
                            _uiState.value = currentState.copy(
                                user = currentState.user.copy(phone = newPhone)
                            )
                        }
                    } else {
                        _updateFieldState.value = UpdateFieldState.Error(
                            response.message ?: "Error al actualizar teléfono"
                        )
                    }
                },
                onFailure = { exception ->
                    _updateFieldState.value = UpdateFieldState.Error(
                        exception.message ?: "Error de conexión"
                    )
                }
            )
        }
    }

    fun updateCurp(newCurp: String) {
        viewModelScope.launch {
            _updateFieldState.value = UpdateFieldState.Loading

            repository.updateUserField("curp", newCurp).fold(
                onSuccess = { response ->
                    if (response.success) {
                        _updateFieldState.value = UpdateFieldState.Success

                        val currentState = _uiState.value
                        if (currentState is ProfileUiState.Success) {
                            _uiState.value = currentState.copy(
                                user = currentState.user.copy(curp = newCurp)
                            )
                        }
                    } else {
                        _updateFieldState.value = UpdateFieldState.Error(
                            response.message ?: "Error al actualizar CURP"
                        )
                    }
                },
                onFailure = { exception ->
                    _updateFieldState.value = UpdateFieldState.Error(
                        exception.message ?: "Error de conexión"
                    )
                }
            )
        }
    }

    fun resetUpdateState() {
        _updateFieldState.value = UpdateFieldState.Idle
    }

    fun requestVerification() {
        viewModelScope.launch {
            _verificationRequestState.value = VerificationRequestState.Loading

            val currentState = _uiState.value
            if (currentState !is ProfileUiState.Success) {
                _verificationRequestState.value = VerificationRequestState.Error("No se pudo obtener la información del usuario")
                return@launch
            }

            val userId = currentState.user.id

            repository.resetVerificationStatus(userId).fold(
                onSuccess = { response ->
                    if (response.success) {
                        _verificationRequestState.value = VerificationRequestState.Success

                        // Actualizar el estado local del usuario a PENDING (0)
                        _uiState.value = currentState.copy(
                            user = currentState.user.copy(verification_status = 0)
                        )
                    } else {
                        _verificationRequestState.value = VerificationRequestState.Error(
                            response.message ?: "Error al solicitar verificación"
                        )
                    }
                },
                onFailure = { exception ->
                    _verificationRequestState.value = VerificationRequestState.Error(
                        exception.message ?: "Error de conexión"
                    )
                }
            )
        }
    }

    fun resetVerificationRequestState() {
        _verificationRequestState.value = VerificationRequestState.Idle
    }

    fun getVerificationStatus(): VerificationStatus {
        return when (val state = _uiState.value) {
            is ProfileUiState.Success -> {
                VerificationStatus.fromCode(state.user.verification_status)
            }
            else -> VerificationStatus.NO_DOCS
        }
    }

    fun loadDocumentImages(userId: Int, identityVerification: IdentityVerification?) {
        viewModelScope.launch {
            _documentImages.value = emptyMap()

            if (identityVerification == null) {
                return@launch
            }

            val images = mutableMapOf<String, ByteArray>()

            if (!identityVerification.selfie_url.isNullOrBlank()) {
                repository.getDocumentImage("selfie", userId)
                    .onSuccess { bytes ->
                        images["selfie"] = bytes
                        _documentImages.value = images.toMap()
                    }
                    .onFailure {
                    }
            }

            if (!identityVerification.ine_front_url.isNullOrBlank()) {
                repository.getDocumentImage("front", userId)
                    .onSuccess { bytes ->
                        images["ine_front"] = bytes
                        _documentImages.value = images.toMap()
                    }
                    .onFailure {}
            }

            if (!identityVerification.ine_back_url.isNullOrBlank()) {
                repository.getDocumentImage("back", userId)
                    .onSuccess { bytes ->
                        images["ine_back"] = bytes
                        _documentImages.value = images.toMap()
                    }
                    .onFailure {}
            }
        }
    }


    fun logout() {
        repository.clearSession()
    }
}