package com.renova.mobile.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.renova.mobile.repository.BusinessProfileRepository
import com.renova.mobile.network.Reward
import com.renova.mobile.network.UserData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class BusinessProfileUiState {
    object Loading : BusinessProfileUiState()
    data class Success(
        val user: UserData,
        val rewards: List<Reward> = emptyList()
    ) : BusinessProfileUiState()
    data class Error(val message: String) : BusinessProfileUiState()
}

class BusinessProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BusinessProfileRepository(application.applicationContext)

    private val _uiState = MutableStateFlow<BusinessProfileUiState>(BusinessProfileUiState.Loading)
    val uiState: StateFlow<BusinessProfileUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()


    init {
        loadBusinessProfile()
    }

    fun loadBusinessProfile() {
        viewModelScope.launch {
            _uiState.value = BusinessProfileUiState.Loading

            repository.getProfile().fold(
                onSuccess = { response ->
                    response.data?.let { data ->
                        val allianceId = data.user.alliance?.id

                        if (allianceId != null) {
                            // Solo cargar recompensas (el logo ya viene en el objeto alliance)
                            loadRewards(allianceId, data.user)
                        } else {
                            _uiState.value = BusinessProfileUiState.Success(
                                user = data.user,
                                rewards = emptyList()
                            )
                        }
                    } ?: run {
                        _uiState.value = BusinessProfileUiState.Error("No se encontraron datos del perfil")
                    }
                },
                onFailure = { exception ->
                    _uiState.value = BusinessProfileUiState.Error(
                        exception.message ?: "Error desconocido al cargar el perfil"
                    )
                }
            )
        }
    }

    private fun loadRewards(allianceId: Int, user: UserData) {
        viewModelScope.launch {
            repository.getRewardsByAlliance(allianceId).fold(
                onSuccess = { response ->
                    val rewards = response.data?.data ?: emptyList()
                    _uiState.value = BusinessProfileUiState.Success(
                        user = user,
                        rewards = rewards
                    )
                },
                onFailure = {
                    // Si falla la carga de rewards, mostrar el perfil sin recompensas
                    _uiState.value = BusinessProfileUiState.Success(
                        user = user,
                        rewards = emptyList()
                    )
                }
            )
        }
    }

    fun refreshBusinessProfile() {
        viewModelScope.launch {
            _isRefreshing.value = true

            repository.getProfile().fold(
                onSuccess = { response ->
                    response.data?.let { data ->
                        val allianceId = data.user.alliance?.id

                        if (allianceId != null) {
                            // Solo cargar recompensas (el logo ya viene en el objeto alliance)
                            loadRewards(allianceId, data.user)
                        } else {
                            _uiState.value = BusinessProfileUiState.Success(
                                user = data.user,
                                rewards = emptyList()
                            )
                        }
                    }
                },
                onFailure = { exception ->
                    _uiState.value = BusinessProfileUiState.Error(
                        exception.message ?: "Error al refrescar el perfil"
                    )
                }
            )

            _isRefreshing.value = false
        }
    }

    fun retry() {
        loadBusinessProfile()
    }

    fun clearError() {
        if (_uiState.value is BusinessProfileUiState.Error) {
            loadBusinessProfile()
        }
    }

    fun logout() {
        repository.clearSession()
    }
}