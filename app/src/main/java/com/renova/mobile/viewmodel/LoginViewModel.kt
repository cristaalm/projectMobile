package com.renova.mobile.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.renova.mobile.network.User
import com.renova.mobile.repository.LoginException
import com.renova.mobile.repository.LoginRepository
import com.renova.mobile.utils.SessionManager
import com.renova.mobile.screens.ErrorMessageMapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = LoginRepository()
    private val sessionManager = SessionManager(application)
    private val context = application.applicationContext

    private val _loginState = MutableStateFlow(LoginState())
    val loginState: StateFlow<LoginState> = _loginState

    fun login(email: String, password: String, rememberMe: Boolean = false) {
        viewModelScope.launch {
            _loginState.value = LoginState(isLoading = true)

            repository.login(email, password, rememberMe)
                .onSuccess { response ->
                    // Validar que la respuesta contenga los datos necesarios
                    if (response.success &&
                        response.data?.access_token != null &&
                        response.data.user != null) {

                        val user = response.data.user

                        sessionManager.saveSession(
                            accessToken = response.data.access_token,
                            tokenType = response.data.token_type ?: "Bearer",
                            expiresAt = response.data.expires_at,
                            user = user
                        )

                        _loginState.value = LoginState(
                            isSuccess = true,
                            user = user,
                            token = response.data.access_token,
                            tokenType = response.data.token_type,
                            expiresAt = response.data.expires_at,
                            message = "¡Bienvenido ${user.name}!"
                        )
                    } else {
                        // Respuesta exitosa pero sin datos completos
                        _loginState.value = LoginState(
                            error = response.message ?: "Error de inicio de sesión",
                            errorType = ErrorType.UNKNOWN
                        )
                    }
                }
                .onFailure { exception ->
                    val statusCode = (exception as? LoginException)?.statusCode ?: -1
                    val backendMessage = exception.message ?: "Error inesperado"

                    // DEBUG: Imprimir información
                    android.util.Log.d("LoginViewModel", "Status Code: $statusCode")
                    android.util.Log.d("LoginViewModel", "Backend Message: $backendMessage")
                    android.util.Log.d("LoginViewModel", "Current Locale: ${context.resources.configuration.locales[0]}")

                    // 🌍 Mapear el mensaje del backend al idioma actual
                    val localizedMessage = ErrorMessageMapper.mapLoginError(
                        backendMessage = backendMessage,
                        statusCode = statusCode,
                        context = context
                    )

                    android.util.Log.d("LoginViewModel", "Localized Message: $localizedMessage")

                    _loginState.value = LoginState(
                        error = localizedMessage, // ✅ Ahora está traducido
                        errorType = getErrorType(statusCode)
                    )
                }
        }
    }

    fun clearState() {
        _loginState.value = LoginState()
    }

    private fun getErrorType(statusCode: Int): ErrorType {
        return when (statusCode) {
            401 -> ErrorType.INVALID_CREDENTIALS
            403 -> ErrorType.ACCOUNT_DISABLED
            422 -> ErrorType.VALIDATION_ERROR
            500 -> ErrorType.SERVER_ERROR
            -1 -> ErrorType.NETWORK_ERROR
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
    val errorType: ErrorType? = null,
    val message: String? = null
)

enum class ErrorType {
    INVALID_CREDENTIALS,    // 401
    ACCOUNT_DISABLED,       // 403
    VALIDATION_ERROR,       // 422
    SERVER_ERROR,           // 500
    NETWORK_ERROR,          // Sin conexión
    UNKNOWN                 // Otros errores
}