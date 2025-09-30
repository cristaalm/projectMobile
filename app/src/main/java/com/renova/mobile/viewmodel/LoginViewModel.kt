package com.renova.mobile.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.renova.mobile.network.User
import com.renova.mobile.repository.LoginRepository
import com.renova.mobile.utils.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Cambiado a AndroidViewModel para poder usar el Context
class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = LoginRepository()
    // Instancia de SessionManager para guardar la sesión
    private val sessionManager = SessionManager(application)

    private val _loginState = MutableStateFlow(LoginState())
    val loginState: StateFlow<LoginState> = _loginState

    fun login(email: String, password: String, rememberMe: Boolean = false) {
        viewModelScope.launch {
            _loginState.value = LoginState(isLoading = true)

            repository.login(email, password, rememberMe)
                .onSuccess { response ->
                    // Validamos que la respuesta sea exitosa y contenga los datos necesarios
                    if (response.success && response.data?.access_token != null && response.data.user != null) {

                        sessionManager.saveSession(
                            accessToken = response.data.access_token,
                            tokenType = response.data.token_type,
                            expiresAt = response.data.expires_at,
                            user = response.data.user
                        )

                        // Actualizamos el estado de la UI para notificar el éxito
                        _loginState.value = LoginState(
                            isSuccess = true,
                            user = response.data.user,
                            token = response.data.access_token,
                            message = "¡Bienvenido ${response.data.user.name}!"
                        )
                    } else {
                        // El backend retornó success=false o datos incompletos
                        val friendlyError = getFriendlyErrorMessage(response.message ?: "Error de inicio de sesión")
                        _loginState.value = LoginState(error = friendlyError)
                    }
                }
                .onFailure { exception ->
                    // Error en la petición de red o parsing
                    val friendlyError = getFriendlyErrorMessage(exception.message ?: "Error inesperado")
                    _loginState.value = LoginState(error = friendlyError)
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
    val token: String? = null,
    val tokenType: String? = null,
    val expiresAt: String? = null,
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