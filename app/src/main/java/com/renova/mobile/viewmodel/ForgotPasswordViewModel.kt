package com.renova.mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.renova.mobile.repository.LoginRepository
import com.renova.mobile.repository.ForgotPasswordException
import com.renova.mobile.R
import java.net.SocketTimeoutException
import java.net.UnknownHostException

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
                            messageResId = R.string.recovery_link
                        )
                    } else {
                        _forgotPasswordState.value = ForgotPasswordState(
                            errorResId = mapStatusCodeToStringResource(500)
                        )
                    }
                }
                .onFailure { exception ->
                    val errorResId = when (exception) {
                        is ForgotPasswordException -> {
                            mapStatusCodeToStringResource(exception.statusCode)
                        }
                        is SocketTimeoutException -> R.string.connection_timeout
                        is UnknownHostException -> R.string.connection_internet_filed
                        else -> {
                            // Intentar mapear por el mensaje de la excepción
                            mapErrorMessageToStringResource(exception.message)
                        }
                    }

                    _forgotPasswordState.value = ForgotPasswordState(
                        errorResId = errorResId
                    )
                }
        }
    }

    private fun mapStatusCodeToStringResource(statusCode: Int): Int {
        return when (statusCode) {
            404 -> R.string.mail_not_registered       // "El correo electrónico no está registrado"
            422 -> R.string.mail_not_registered       // "Correo electrónico inválido o no registrado"
            500 -> R.string.error_server              // "Error del servidor"
            -1 -> R.string.connection_internet_filed  // Error de conexión
            else -> R.string.error_unknown
        }
    }

    private fun mapErrorMessageToStringResource(message: String?): Int {
        return when {
            message == null -> R.string.error_unknown

            // Errores de correo no registrado
            message.contains("no está registrado", ignoreCase = true) ||
                    message.contains("not registered", ignoreCase = true) ||
                    message.contains("can't find", ignoreCase = true) -> R.string.mail_not_registered

            // Errores de validación
            message.contains("inválido", ignoreCase = true) ||
                    message.contains("invalid", ignoreCase = true) -> R.string.email_invalid

            // Errores de cuenta bloqueada
            message.contains("bloqueada", ignoreCase = true) ||
                    message.contains("blocked", ignoreCase = true) ||
                    message.contains("desactivada", ignoreCase = true) -> R.string.blocked_account

            // Errores del servidor
            message.contains("servidor", ignoreCase = true) ||
                    message.contains("server", ignoreCase = true) ||
                    message.contains("inesperado", ignoreCase = true) -> R.string.error_server

            // Errores de conexión
            message.contains("conexión", ignoreCase = true) ||
                    message.contains("connection", ignoreCase = true) ||
                    message.contains("red", ignoreCase = true) ||
                    message.contains("network", ignoreCase = true) -> R.string.connection_internet_filed

            // Timeout
            message.contains("timeout", ignoreCase = true) ||
                    message.contains("timed out", ignoreCase = true) -> R.string.connection_timeout

            else -> R.string.error_unknown
        }
    }

    fun clearState() {
        _forgotPasswordState.value = ForgotPasswordState()
    }
}

data class ForgotPasswordState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorResId: Int? = null,      // ID del string resource para traducción
    val messageResId: Int? = null     // ID del string resource para mensajes de éxito
)