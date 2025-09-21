// ARCHIVO: LoginViewModel.kt
// RUTA: app/kotlin+java/com.renova.mobile/viewmodel/LoginViewModel.kt

package com.renova.mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.renova.mobile.repository.LoginRepository
import com.renova.mobile.network.User

class LoginViewModel : ViewModel() {
    private val repository = LoginRepository()

    private val _loginState = MutableStateFlow(LoginState())
    val loginState: StateFlow<LoginState> = _loginState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState(isLoading = true)

            repository.login(email, password)
                .onSuccess { response ->
                    if (response.success) {
                        _loginState.value = LoginState(
                            isSuccess = true,
                            user = response.data?.user,
                            message = "¡Bienvenido ${response.data?.user?.name ?: ""}!"
                        )
                    } else {
                        _loginState.value = LoginState(
                            error = response.message
                        )
                    }
                }
                .onFailure { exception ->
                    _loginState.value = LoginState(
                        error = "Error de conexión: Usuario o contraseña incorrectos"
                    )
                }
        }
    }

    fun clearState() {
        _loginState.value = LoginState()
    }
}

data class LoginState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val user: User? = null,
    val error: String? = null,
    val message: String? = null
)