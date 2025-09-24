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

    fun login(email: String, password: String, rememberMe: Boolean = false) {
        viewModelScope.launch {
            _loginState.value = LoginState(isLoading = true)

            repository.login(email, password, rememberMe)
                .onSuccess { response ->
                    if (response.success) {
                        // Login exitoso
                        _loginState.value = LoginState(
                            isSuccess = true,
                            user = response.data?.user,
                            token = response.data?.access_token,
                            tokenType = response.data?.token_type,
                            expiresAt = response.data?.expires_at,
                            message = "¡Bienvenido ${response.data?.user?.name ?: ""}!"
                        )
                    } else {
                        // El backend retornó success=false
                        val friendlyError = getFriendlyErrorMessage(response.message ?: "Error de inicio de sesión")
                        _loginState.value = LoginState(
                            error = friendlyError
                        )
                    }
                }
                .onFailure { exception ->
                    // Error en la petición o parsing
                    val friendlyError = getFriendlyErrorMessage(exception.message ?: "Error inesperado")
                    _loginState.value = LoginState(
                        error = friendlyError
                    )
                }
        }
    }

    fun clearState() {
        _loginState.value = LoginState()
    }

    // Método para obtener mensaje de error amigable para la UI
    fun getFriendlyErrorMessage(error: String): String {
        return when {
            error.contains("Correo electrónico o contraseña incorrectos", ignoreCase = true) ->
                "Credenciales incorrectas. Verifica tu email y contraseña."

            error.contains("cuenta ha sido desactivada", ignoreCase = true) ->
                "Tu cuenta ha sido desactivada. Contacta al administrador."

            error.contains("correo electrónico no está registrado", ignoreCase = true) ->
                "El correo electrónico no está registrado en el sistema."

            error.contains("Error de conexión", ignoreCase = true) ->
                "Sin conexión a internet. Verifica tu conexión."

            error.contains("Error interno del servidor", ignoreCase = true) ->
                "Error del servidor. Intenta nuevamente más tarde."

            error.contains("selected email is invalid", ignoreCase = true) ->
                "El correo electrónico no está registrado en el sistema."

            else -> error
        }
    }

    // Método para determinar el tipo de error (útil para la UI)
    fun getErrorType(error: String): ErrorType {
        return when {
            error.contains("Correo electrónico o contraseña incorrectos", ignoreCase = true) ->
                ErrorType.INVALID_CREDENTIALS

            error.contains("cuenta ha sido desactivada", ignoreCase = true) ->
                ErrorType.ACCOUNT_DISABLED

            error.contains("correo electrónico no está registrado", ignoreCase = true) ->
                ErrorType.EMAIL_NOT_FOUND

            error.contains("Error de conexión", ignoreCase = true) ->
                ErrorType.NETWORK_ERROR

            error.contains("selected email is invalid", ignoreCase = true) ->
                ErrorType.EMAIL_NOT_FOUND

            else -> ErrorType.UNKNOWN
        }
    }
}

data class LoginState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val user: User? = null,
    val token: String? = null,           // access_token
    val tokenType: String? = null,       // token_type (Bearer)
    val expiresAt: String? = null,       // expires_at
    val error: String? = null,
    val message: String? = null
)

enum class ErrorType {
    INVALID_CREDENTIALS,
    ACCOUNT_DISABLED,
    EMAIL_NOT_FOUND,
    VALIDATION_ERROR,
    NETWORK_ERROR,
    UNKNOWN
}