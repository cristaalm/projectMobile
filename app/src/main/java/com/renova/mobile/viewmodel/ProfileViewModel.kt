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

    // NUEVO: Método para reintentar la carga
    fun retry() {
        loadProfile()
    }

    // NUEVO: Método para limpiar el error
    fun clearError() {
        // Si hay un error, volver a cargar automáticamente
        if (_uiState.value is ProfileUiState.Error) {
            loadProfile()
        }
    }

    fun getVerificationStatus(): VerificationStatus {
        return when (val state = _uiState.value) {
            is ProfileUiState.Success -> {
                VerificationStatus.fromCode(state.user.verification_status)
            }
            else -> VerificationStatus.NO_DOCS
        }
    }

    fun logout() {
        repository.clearSession()
    }
}