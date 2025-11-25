package com.renova.mobile.ui.viewmodels

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.renova.mobile.R
import com.renova.mobile.data.repository.ProfileRepository
import com.renova.mobile.network.IdentityVerification
import com.renova.mobile.network.UserData
import com.renova.mobile.ui.components.DocumentType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import android.util.Log

sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Success(
        val user: UserData,
        val identityVerification: IdentityVerification?,
        val isManualRefresh: Boolean = false
    ) : ProfileUiState()
    data class Error(
        val message: String,
        val isManualRefresh: Boolean = false
    ) : ProfileUiState()
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
    private val context = application.applicationContext

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

    private val _documentUploadState = MutableStateFlow<DocumentUploadState>(DocumentUploadState.Idle)
    val documentUploadState: StateFlow<DocumentUploadState> = _documentUploadState.asStateFlow()

    private val _passwordResetState = MutableStateFlow<PasswordResetState>(PasswordResetState.Idle)
    val passwordResetState: StateFlow<PasswordResetState> = _passwordResetState.asStateFlow()

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
        data class Error(
            val message: String,
            val field: String
        ) : UpdateFieldState()
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

    sealed class PasswordResetState {
        object Idle : PasswordResetState()
        object Loading : PasswordResetState()
        object Success : PasswordResetState()
        data class Error(val message: String) : PasswordResetState()
    }

    init {
        loadProfile(isManualRefresh = false)
    }

    private fun getErrorMessage(exception: Throwable): String {
        return when {
            exception.message?.contains("Unable to resolve host", ignoreCase = true) == true ||
                    exception.message?.contains("timeout", ignoreCase = true) == true ||
                    exception.message?.contains("Failed to connect", ignoreCase = true) == true ||
                    exception.message?.contains("No address associated with hostname", ignoreCase = true) == true ->
                "ERROR_NO_INTERNET"

            exception.message?.contains("401", ignoreCase = true) == true ||
                    exception.message?.contains("Unauthorized", ignoreCase = true) == true ->
                "ERROR_SESSION_EXPIRED"

            exception.message?.contains("403", ignoreCase = true) == true ||
                    exception.message?.contains("Forbidden", ignoreCase = true) == true ->
                "ERROR_FORBIDDEN"

            exception.message?.contains("404", ignoreCase = true) == true ->
                "ERROR_NOT_FOUND"

            exception.message?.contains("422", ignoreCase = true) == true -> {
                Log.d("ProfileViewModel", "Error de validacion del campo")
                when {
                    exception.message?.contains("email", ignoreCase = true) == true ||
                            exception.message?.contains("correo", ignoreCase = true) == true ->
                        "ERROR_EMAIL_ALREADY_EXISTS"

                    exception.message?.contains("phone", ignoreCase = true) == true ||
                            exception.message?.contains("teléfono", ignoreCase = true) == true ||
                            exception.message?.contains("telefono", ignoreCase = true) == true ->
                        "ERROR_PHONE_ALREADY_EXISTS"

                    exception.message?.contains("curp", ignoreCase = true) == true ->
                        "ERROR_CURP_ALREADY_EXISTS"

                    exception.message?.contains("already", ignoreCase = true) == true ||
                            exception.message?.contains("ya existe", ignoreCase = true) == true ||
                            exception.message?.contains("duplicate", ignoreCase = true) == true ->
                        "ERROR_DUPLICATE_DATA"

                    else -> "ERROR_VALIDATION"
                }
            }

            exception.message?.contains("500", ignoreCase = true) == true ||
                    exception.message?.contains("Internal Server Error", ignoreCase = true) == true ->
                "ERROR_SERVER"

            else -> exception.message ?: "ERROR_UNKNOWN"
        }
    }

    fun loadProfile(isManualRefresh: Boolean = false) {
        viewModelScope.launch {
            if (!isManualRefresh) {
                _uiState.value = ProfileUiState.Loading
            }

            try {
                repository.getProfile().fold(
                    onSuccess = { response ->
                        response.data?.let { data ->
                            _uiState.value = ProfileUiState.Success(
                                user = data.user,
                                identityVerification = data.identityVerification.firstOrNull(),
                                isManualRefresh = false
                            )
                        } ?: run {
                            _uiState.value = ProfileUiState.Error(
                                message = "ERROR_PROFILE_NOT_FOUND",
                                isManualRefresh = false
                            )
                        }
                    },
                    onFailure = { exception ->
                        _uiState.value = ProfileUiState.Error(
                            message = getErrorMessage(exception),
                            isManualRefresh = isManualRefresh
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error(
                    message = getErrorMessage(e),
                    isManualRefresh = isManualRefresh
                )
            }
        }
    }

    fun refreshProfile() {
        viewModelScope.launch {
            _isRefreshing.value = true

            try {
                repository.getProfile().fold(
                    onSuccess = { response ->
                        response.data?.let { data ->
                            _uiState.value = ProfileUiState.Success(
                                user = data.user,
                                identityVerification = data.identityVerification.firstOrNull(),
                                isManualRefresh = false
                            )
                        }
                    },
                    onFailure = { exception ->
                        val currentState = _uiState.value
                        if (currentState is ProfileUiState.Success) {
                            _uiState.value = currentState.copy(
                                isManualRefresh = true
                            )
                            _uiState.update {
                                ProfileUiState.Error(
                                    message = getErrorMessage(exception),
                                    isManualRefresh = true
                                )
                            }
                        } else {
                            _uiState.value = ProfileUiState.Error(
                                message = getErrorMessage(exception),
                                isManualRefresh = true
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                val currentState = _uiState.value
                if (currentState is ProfileUiState.Success) {
                    _uiState.update {
                        ProfileUiState.Error(
                            message = getErrorMessage(e),
                            isManualRefresh = true
                        )
                    }
                } else {
                    _uiState.value = ProfileUiState.Error(
                        message = getErrorMessage(e),
                        isManualRefresh = true
                    )
                }
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun retry() {
        loadProfile(isManualRefresh = true)
    }

    fun clearError() {
        _uiState.update { currentState ->
            when (currentState) {
                is ProfileUiState.Error -> {
                    loadProfile(isManualRefresh = false)
                    currentState
                }
                is ProfileUiState.Success -> {
                    currentState.copy(isManualRefresh = false)
                }
                else -> currentState
            }
        }
    }

    private fun compressImage(context: Context, uri: Uri, maxSizeKB: Int = 4500): File? {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            var bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (bitmap == null) {
                return null
            }

            bitmap = correctImageOrientation(context, uri, bitmap)

            val maxDimension = 1920
            if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
                val scale = maxDimension.toFloat() / Math.max(bitmap.width, bitmap.height)
                val newWidth = (bitmap.width * scale).toInt()
                val newHeight = (bitmap.height * scale).toInt()
                bitmap = android.graphics.Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
            }

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

            return file
        } catch (e: Exception) {
            return null
        }
    }

    private fun correctImageOrientation(context: Context, uri: Uri, bitmap: android.graphics.Bitmap): android.graphics.Bitmap {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val exif = androidx.exifinterface.media.ExifInterface(inputStream!!)
            inputStream.close()

            val orientation = exif.getAttributeInt(
                androidx.exifinterface.media.ExifInterface.TAG_ORIENTATION,
                androidx.exifinterface.media.ExifInterface.ORIENTATION_NORMAL
            )

            val matrix = android.graphics.Matrix()
            when (orientation) {
                androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                androidx.exifinterface.media.ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
                androidx.exifinterface.media.ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            }

            return android.graphics.Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } catch (e: Exception) {
            return bitmap
        }
    }

    fun uploadDocument(documentType: DocumentType, uri: Uri, context: Context) {
        viewModelScope.launch {
            _documentUploadState.value = DocumentUploadState.Loading(documentType)

            val currentState = _uiState.value
            if (currentState !is ProfileUiState.Success) {
                _documentUploadState.value = DocumentUploadState.Error(
                    documentType,
                    "ERROR_USER_INFO"
                )
                return@launch
            }

            val userId = currentState.user.id

            try {
                val file = compressImage(context, uri, maxSizeKB = 4500)

                if (file == null) {
                    _documentUploadState.value = DocumentUploadState.Error(
                        documentType,
                        "ERROR_PROCESS_IMAGE"
                    )
                    return@launch
                }

                val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("document", file.name, requestFile)

                val type = when (documentType) {
                    DocumentType.SELFIE -> "selfie"
                    DocumentType.INE_FRONT -> "ine_front"
                    DocumentType.INE_BACK -> "ine_back"
                }

                repository.uploadSingleDocument(type, userId, body).fold(
                    onSuccess = { response ->
                        file.delete()
                        loadSingleDocumentImage(userId, type)
                        _documentUploadState.value = DocumentUploadState.Success(documentType)
                    },
                    onFailure = { exception ->
                        _documentUploadState.value = DocumentUploadState.Error(
                            documentType,
                            getErrorMessage(exception)
                        )
                        file.delete()
                    }
                )
            } catch (e: Exception) {
                _documentUploadState.value = DocumentUploadState.Error(
                    documentType,
                    getErrorMessage(e)
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

            try {
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
                                message = "ERROR_UPDATE_NAME",
                                field = "name" // ⭐ AÑADIDO
                            )
                        }
                    },
                    onFailure = { exception ->
                        _updateFieldState.value = UpdateFieldState.Error(
                            message = getErrorMessage(exception),
                            field = "name" // ⭐ AÑADIDO
                        )
                    }
                )
            } catch (e: Exception) {
                _updateFieldState.value = UpdateFieldState.Error(
                    message = getErrorMessage(e),
                    field = "name" // ⭐ AÑADIDO
                )
            }
        }
    }

    fun updateLastName(newLastName: String) {
        viewModelScope.launch {
            _updateFieldState.value = UpdateFieldState.Loading

            try {
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
                                message = "ERROR_UPDATE_LASTNAME",
                                field = "last_name" // ⭐ AÑADIDO
                            )
                        }
                    },
                    onFailure = { exception ->
                        _updateFieldState.value = UpdateFieldState.Error(
                            message = getErrorMessage(exception),
                            field = "last_name" // ⭐ AÑADIDO
                        )
                    }
                )
            } catch (e: Exception) {
                _updateFieldState.value = UpdateFieldState.Error(
                    message = getErrorMessage(e),
                    field = "last_name" // ⭐ AÑADIDO
                )
            }
        }
    }

    fun updateEmail(newEmail: String) {
        viewModelScope.launch {
            _updateFieldState.value = UpdateFieldState.Loading
            Log.d("ProfileViewModel", "🔵 Iniciando actualización de email: $newEmail")

            try {
                repository.updateUserField("email", newEmail).fold(
                    onSuccess = { response ->
                        Log.d("ProfileViewModel", "🟢 Respuesta recibida: success=${response.success}")
                        Log.d("ProfileViewModel", "🟢 Message: ${response.message}")

                        if (response.success) {
                            _updateFieldState.value = UpdateFieldState.Success
                            val currentState = _uiState.value
                            if (currentState is ProfileUiState.Success) {
                                _uiState.value = currentState.copy(
                                    user = currentState.user.copy(email = newEmail)
                                )
                            }
                        } else {
                            val errorCode = when {
                                response.message?.contains("correo", ignoreCase = true) == true ||
                                        response.message?.contains("email", ignoreCase = true) == true ||
                                        response.message?.contains("registrado", ignoreCase = true) == true ->
                                    "ERROR_EMAIL_ALREADY_EXISTS"
                                else -> "ERROR_UPDATE_EMAIL"
                            }
                            Log.d("ProfileViewModel", "🔴 Error detectado: $errorCode")
                            _updateFieldState.value = UpdateFieldState.Error(
                                message = errorCode,
                                field = "email" // ⭐ AÑADIDO
                            )
                        }
                    },
                    onFailure = { exception ->
                        Log.d("ProfileViewModel", "🔴 Exception: ${exception.message}")
                        val errorCode = if (exception.message?.contains("422") == true) {
                            "ERROR_EMAIL_ALREADY_EXISTS"
                        } else {
                            getErrorMessage(exception)
                        }
                        _updateFieldState.value = UpdateFieldState.Error(
                            message = errorCode,
                            field = "email" // ⭐ AÑADIDO
                        )
                    }
                )
            } catch (e: Exception) {
                Log.d("ProfileViewModel", "🔴 Catch Exception: ${e.message}")
                val errorCode = if (e.message?.contains("422") == true) {
                    "ERROR_EMAIL_ALREADY_EXISTS"
                } else {
                    getErrorMessage(e)
                }
                _updateFieldState.value = UpdateFieldState.Error(
                    message = errorCode,
                    field = "email" // ⭐ AÑADIDO
                )
            }
        }
    }

    fun updatePhone(newPhone: String) {
        viewModelScope.launch {
            _updateFieldState.value = UpdateFieldState.Loading

            try {
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
                            val errorCode = when {
                                response.message?.contains("teléfono", ignoreCase = true) == true ||
                                        response.message?.contains("telefono", ignoreCase = true) == true ||
                                        response.message?.contains("phone", ignoreCase = true) == true ||
                                        response.message?.contains("registrado", ignoreCase = true) == true ->
                                    "ERROR_PHONE_ALREADY_EXISTS"
                                else -> "ERROR_UPDATE_PHONE"
                            }
                            _updateFieldState.value = UpdateFieldState.Error(
                                message = errorCode,
                                field = "phone" // ⭐ AÑADIDO
                            )
                        }
                    },
                    onFailure = { exception ->
                        val errorCode = if (exception.message?.contains("422") == true) {
                            "ERROR_PHONE_ALREADY_EXISTS"
                        } else {
                            getErrorMessage(exception)
                        }
                        _updateFieldState.value = UpdateFieldState.Error(
                            message = errorCode,
                            field = "phone" // ⭐ AÑADIDO
                        )
                    }
                )
            } catch (e: Exception) {
                val errorCode = if (e.message?.contains("422") == true) {
                    "ERROR_PHONE_ALREADY_EXISTS"
                } else {
                    getErrorMessage(e)
                }
                _updateFieldState.value = UpdateFieldState.Error(
                    message = errorCode,
                    field = "phone" // ⭐ AÑADIDO
                )
            }
        }
    }

    fun updateCurp(newCurp: String) {
        viewModelScope.launch {
            _updateFieldState.value = UpdateFieldState.Loading

            try {
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
                            val errorCode = when {
                                response.message?.contains("curp", ignoreCase = true) == true ||
                                        response.message?.contains("registrado", ignoreCase = true) == true ->
                                    "ERROR_CURP_ALREADY_EXISTS"
                                else -> "ERROR_UPDATE_CURP"
                            }
                            _updateFieldState.value = UpdateFieldState.Error(
                                message = errorCode,
                                field = "curp" // ⭐ AÑADIDO
                            )
                        }
                    },
                    onFailure = { exception ->
                        val errorCode = if (exception.message?.contains("422") == true) {
                            "ERROR_CURP_ALREADY_EXISTS"
                        } else {
                            getErrorMessage(exception)
                        }
                        _updateFieldState.value = UpdateFieldState.Error(
                            message = errorCode,
                            field = "curp" // ⭐ AÑADIDO
                        )
                    }
                )
            } catch (e: Exception) {
                val errorCode = if (e.message?.contains("422") == true) {
                    "ERROR_CURP_ALREADY_EXISTS"
                } else {
                    getErrorMessage(e)
                }
                _updateFieldState.value = UpdateFieldState.Error(
                    message = errorCode,
                    field = "curp" // ⭐ AÑADIDO
                )
            }
        }
    }

    fun resetPassword(currentPassword: String, newPassword: String, newPasswordConfirmation: String) {
        viewModelScope.launch {
            _passwordResetState.value = PasswordResetState.Loading

            try {
                repository.resetPassword(
                    currentPassword = currentPassword,
                    newPassword = newPassword,
                    newPasswordConfirmation = newPasswordConfirmation
                ).fold(
                    onSuccess = { response ->
                        if (response.success) {
                            _passwordResetState.value = PasswordResetState.Success
                        } else {
                            val errorCode = when {
                                response.message?.contains("actual incorrecta", ignoreCase = true) == true ||
                                        response.message?.contains("credenciales incorrectas", ignoreCase = true) == true ->
                                    "ERROR_CURRENT_PASSWORD_INCORRECT"

                                response.message?.contains("8 caracteres", ignoreCase = true) == true ->
                                    "ERROR_PASSWORD_TOO_SHORT"

                                response.message?.contains("número", ignoreCase = true) == true ->
                                    "ERROR_PASSWORD_NO_NUMBER"

                                response.message?.contains("especial", ignoreCase = true) == true ->
                                    "ERROR_PASSWORD_NO_SPECIAL"

                                response.message?.contains("no coincide", ignoreCase = true) == true ||
                                        response.message?.contains("confirmation", ignoreCase = true) == true ->
                                    "ERROR_PASSWORD_MISMATCH"

                                response.message?.contains("igual", ignoreCase = true) == true ->
                                    "ERROR_PASSWORD_SAME_AS_OLD"

                                else -> "ERROR_RESET_PASSWORD"
                            }
                            _passwordResetState.value = PasswordResetState.Error(errorCode)
                        }
                    },
                    onFailure = { exception ->
                        val errorCode = when {
                            exception.message?.contains("401") == true ->
                                "ERROR_CURRENT_PASSWORD_INCORRECT"

                            exception.message?.contains("422") == true ->
                                "ERROR_PASSWORD_VALIDATION_FAILED"

                            else -> getErrorMessage(exception)
                        }
                        _passwordResetState.value = PasswordResetState.Error(errorCode)
                    }
                )
            } catch (e: Exception) {
                _passwordResetState.value = PasswordResetState.Error(getErrorMessage(e))
            }
        }
    }

    fun resetPasswordResetState() {
        _passwordResetState.value = PasswordResetState.Idle
    }

    fun resetUpdateState() {
        _updateFieldState.value = UpdateFieldState.Idle
    }

    fun requestVerification() {
        viewModelScope.launch {
            _verificationRequestState.value = VerificationRequestState.Loading

            val currentState = _uiState.value
            if (currentState !is ProfileUiState.Success) {
                _verificationRequestState.value = VerificationRequestState.Error(
                    "ERROR_USER_INFO"
                )
                return@launch
            }

            val userId = currentState.user.id

            try {
                repository.resetVerificationStatus(userId).fold(
                    onSuccess = { response ->
                        if (response.success) {
                            _verificationRequestState.value = VerificationRequestState.Success

                            _uiState.value = currentState.copy(
                                user = currentState.user.copy(verification_status = 0)
                            )
                        } else {
                            _verificationRequestState.value = VerificationRequestState.Error(
                                "ERROR_REQUEST_VERIFICATION"
                            )
                        }
                    },
                    onFailure = { exception ->
                        _verificationRequestState.value = VerificationRequestState.Error(
                            getErrorMessage(exception)
                        )
                    }
                )
            } catch (e: Exception) {
                _verificationRequestState.value = VerificationRequestState.Error(getErrorMessage(e))
            }
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

            try {
                if (!identityVerification.selfie_url.isNullOrBlank()) {
                    repository.getDocumentImage("selfie", userId)
                        .onSuccess { bytes ->
                            images["selfie"] = bytes
                            _documentImages.value = images.toMap()
                        }
                        .onFailure { }
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
            } catch (e: Exception) {
                // Error silencioso en la carga de imágenes
            }
        }
    }

    fun logout() {
        repository.clearSession()
    }
}