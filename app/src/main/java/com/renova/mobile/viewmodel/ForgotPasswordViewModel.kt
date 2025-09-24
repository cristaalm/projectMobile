package com.renova.mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.renova.mobile.repository.LoginRepository

class ForgotPasswordViewModel : ViewModel() {
    private val repository = LoginRepository()

    private val _forgotPasswordState = MutableStateFlow(ForgotPasswordState())
    val forgotPasswordState: StateFlow<ForgotPasswordState> = _forgotPasswordState

    fun forgotPassword(email: String) {
        viewModelScope.launch {
            _forgotPasswordState.value = ForgotPasswordState(isLoading = true)

            repository.forgotPassword(email)
                .onSuccess { response ->
                    if (response.success) {
                        _forgotPasswordState.value = ForgotPasswordState(
                            isSuccess = true,
                            message = response.message
                        )
                    } else {
                        _forgotPasswordState.value = ForgotPasswordState(
                            error = response.message
                        )
                    }
                }
                .onFailure { exception ->
                    _forgotPasswordState.value = ForgotPasswordState(
                        error = "${exception.message}"
                    )
                }
        }
    }

    fun clearState() {
        _forgotPasswordState.value = ForgotPasswordState()
    }
}

data class ForgotPasswordState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val message: String? = null
)

