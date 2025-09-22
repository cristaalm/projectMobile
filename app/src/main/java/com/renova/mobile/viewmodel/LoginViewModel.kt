package com.renova.mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.renova.mobile.repository.LoginRepository
import com.renova.mobile.network.User
import java.net.UnknownHostException
import java.net.SocketTimeoutException
import retrofit2.HttpException

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
                        // El backend retornó success=false con mensaje específico
                        val specificError = categorizeBackendError(response.message)
                        _loginState.value = LoginState(error = specificError)
                    }
                }
                .onFailure { exception ->

                    val errorMessage = categorizeBackendError(exception.message)
                    _loginState.value = LoginState(error = errorMessage)
                }
        }
    }

    private fun categorizeBackendError(backendMessage: String?): String {
        return when {
            // Mensaje específico de tu backend actual
            backendMessage?.contains("Correo electrónico o contraseña incorrectos", ignoreCase = true) == true -> {
                "invalid password"
            }

            // Errores de usuario no encontrado
            backendMessage?.contains("usuario no encontrado", ignoreCase = true) == true ||
                    backendMessage?.contains("user not found", ignoreCase = true) == true ||
                    backendMessage?.contains("email no existe", ignoreCase = true) == true ||
                    backendMessage?.contains("email not registered", ignoreCase = true) == true ||
                    backendMessage?.contains("correo no registrado", ignoreCase = true) == true ||
                    backendMessage?.contains("no existe el usuario", ignoreCase = true) == true ->
                "user not found"

            // Errores de contraseña incorrecta específicos
            backendMessage?.contains("contraseña incorrecta", ignoreCase = true) == true ||
                    backendMessage?.contains("invalid password", ignoreCase = true) == true ||
                    backendMessage?.contains("wrong password", ignoreCase = true) == true ||
                    backendMessage?.contains("password mismatch", ignoreCase = true) == true ||
                    backendMessage?.contains("contraseña inválida", ignoreCase = true) == true ->
                "invalid password"

            // Errores de cuenta bloqueada
            backendMessage?.contains("cuenta bloqueada", ignoreCase = true) == true ||
                    backendMessage?.contains("account blocked", ignoreCase = true) == true ||
                    backendMessage?.contains("user blocked", ignoreCase = true) == true ||
                    backendMessage?.contains("account suspended", ignoreCase = true) == true ||
                    backendMessage?.contains("cuenta suspendida", ignoreCase = true) == true ||
                    backendMessage?.contains("usuario bloqueado", ignoreCase = true) == true ->
                "account blocked"

            // Errores de cuenta no verificada
            backendMessage?.contains("cuenta no verificada", ignoreCase = true) == true ||
                    backendMessage?.contains("email not verified", ignoreCase = true) == true ||
                    backendMessage?.contains("account not activated", ignoreCase = true) == true ||
                    backendMessage?.contains("correo no verificado", ignoreCase = true) == true ->
                "account not verified"

            // Errores del servidor
            backendMessage?.contains("error interno", ignoreCase = true) == true ||
                    backendMessage?.contains("server error", ignoreCase = true) == true ||
                    backendMessage?.contains("internal error", ignoreCase = true) == true ||
                    backendMessage?.contains("error del servidor", ignoreCase = true) == true ->
                "server error"

            // Errores de red específicos
            backendMessage?.contains("network connection error", ignoreCase = true) == true ||
                    backendMessage?.contains("sin conexión", ignoreCase = true) == true ||
                    backendMessage?.contains("no internet", ignoreCase = true) == true ->
                "network connection error"

            // Errores de timeout
            backendMessage?.contains("connection timeout", ignoreCase = true) == true ||
                    backendMessage?.contains("tiempo agotado", ignoreCase = true) == true ->
                "connection timeout"

            // Errores de mantenimiento
            backendMessage?.contains("mantenimiento", ignoreCase = true) == true ||
                    backendMessage?.contains("maintenance", ignoreCase = true) == true ||
                    backendMessage?.contains("servicio no disponible", ignoreCase = true) == true ->
                "server maintenance"

            // Errores de demasiados intentos
            backendMessage?.contains("too many attempts", ignoreCase = true) == true ||
                    backendMessage?.contains("demasiados intentos", ignoreCase = true) == true ||
                    backendMessage?.contains("muchos intentos", ignoreCase = true) == true ->
                "too many attempts"

            // Si no se puede categorizar, devolver el mensaje original o uno genérico
            else -> backendMessage ?: "Error desconocido"
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