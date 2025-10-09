package com.renova.mobile.repository

import com.renova.mobile.network.ApiClient
import com.renova.mobile.network.LoginRequest
import com.renova.mobile.network.LoginResponse
import com.renova.mobile.network.ForgotPasswordRequest
import com.renova.mobile.network.ForgotPasswordResponse
import com.google.gson.Gson
import retrofit2.HttpException
import java.io.IOException
import java.security.MessageDigest

class LoginRepository {
    private val apiService = ApiClient.apiService

    suspend fun login(email: String, password: String, rememberMe: Boolean = false): Result<LoginResponse> {
        return try {
            val response = apiService.login(LoginRequest(email, password, rememberMe))

            if (response.isSuccessful) {
                response.body()?.let { body ->
                    // Verificar que success sea true
                    if (body.success) {
                        Result.success(body)
                    } else {
                        // El backend retornó success=false
                        Result.failure(LoginException(
                            message = body.message ?: "Error desconocido",
                            statusCode = body.status ?: 500
                        ))
                    }
                } ?: Result.failure(LoginException("Respuesta vacía del servidor", 500))
            } else {
                handleLoginError(response.code(), response.errorBody()?.string())
            }
        } catch (e: HttpException) {
            handleLoginError(e.code(), null)
        } catch (e: IOException) {
            Result.failure(LoginException("Error de conexión de red", -1))
        } catch (e: Exception) {
            Result.failure(LoginException("Error inesperado: ${e.message}", -1))
        }
    }

    private fun handleLoginError(statusCode: Int, errorBody: String?): Result<LoginResponse> {
        return if (!errorBody.isNullOrBlank()) {
            try {
                val gson = Gson()
                val errorResponse = gson.fromJson(errorBody, LoginResponse::class.java)

                // Usar directamente el mensaje del backend
                val message = when (statusCode) {
                    401, 403 -> errorResponse.message ?: getDefaultErrorMessage(statusCode)
                    422 -> {
                        // Si hay errores de validación específicos, usar esos
                        if (errorResponse.errors is Map<*, *>) {
                            val errorsMap = errorResponse.errors as? Map<String, List<String>>
                            errorsMap?.get("email")?.firstOrNull()
                                ?: errorResponse.message
                                ?: getDefaultErrorMessage(statusCode)
                        } else {
                            errorResponse.message ?: getDefaultErrorMessage(statusCode)
                        }
                    }
                    500 -> {
                        // Para errores 500, priorizar el mensaje de "errors" si existe
                        if (errorResponse.errors is String) {
                            errorResponse.errors as String
                        } else {
                            errorResponse.message ?: getDefaultErrorMessage(statusCode)
                        }
                    }
                    else -> errorResponse.message ?: getDefaultErrorMessage(statusCode)
                }

                Result.failure(LoginException(message, statusCode))

            } catch (e: Exception) {
                Result.failure(LoginException(getDefaultErrorMessage(statusCode), statusCode))
            }
        } else {
            Result.failure(LoginException(getDefaultErrorMessage(statusCode), statusCode))
        }
    }

    private fun getDefaultErrorMessage(statusCode: Int): String {
        return when (statusCode) {
            401 -> "Credenciales incorrectas"
            403 -> "Cuenta desactivada"
            422 -> "Datos de entrada inválidos"
            500 -> "Error interno del servidor"
            -1 -> "Error de conexión de red"
            else -> "Error del servidor (código: $statusCode)"
        }
    }

    suspend fun forgotPassword(email: String): Result<ForgotPasswordResponse> {
        return try {
            val response = apiService.forgotPassword(ForgotPasswordRequest(email))

            if (response.isSuccessful) {
                response.body()?.let { body ->
                    Result.success(body)
                } ?: Result.failure(ForgotPasswordException("Respuesta vacía del servidor", 500))
            } else {
                handleForgotPasswordError(response.code(), response.errorBody()?.string())
            }
        } catch (e: HttpException) {
            handleForgotPasswordError(e.code(), null)
        } catch (e: IOException) {
            Result.failure(ForgotPasswordException("Error de conexión de red", -1))
        } catch (e: Exception) {
            Result.failure(ForgotPasswordException("Error inesperado: ${e.message}", -1))
        }
    }

    private fun handleForgotPasswordError(statusCode: Int, errorBody: String?): Result<ForgotPasswordResponse> {
        return if (!errorBody.isNullOrBlank()) {
            try {
                val gson = Gson()
                val errorResponse = gson.fromJson(errorBody, ForgotPasswordResponse::class.java)

                val message = when (statusCode) {
                    422 -> {
                        if (errorResponse.errors is Map<*, *>) {
                            val errorsMap = errorResponse.errors as? Map<String, List<String>>
                            errorsMap?.get("email")?.firstOrNull() ?: errorResponse.message
                        } else {
                            errorResponse.message
                        }
                    }
                    404 -> errorResponse.message
                    500 -> {
                        if (errorResponse.errors is String) {
                            errorResponse.errors as String
                        } else {
                            errorResponse.message
                        }
                    }
                    else -> errorResponse.message
                }

                Result.failure(ForgotPasswordException(message ?: "Error del servidor", statusCode))

            } catch (e: Exception) {
                getForgotPasswordDefaultError(statusCode)
            }
        } else {
            getForgotPasswordDefaultError(statusCode)
        }
    }

    private fun getForgotPasswordDefaultError(statusCode: Int): Result<ForgotPasswordResponse> {
        val message = when (statusCode) {
            404 -> "Correo electrónico no registrado"
            422 -> "Correo electrónico inválido"
            500 -> "Error interno del servidor"
            else -> "Error del servidor (código: $statusCode)"
        }
        return Result.failure(ForgotPasswordException(message, statusCode))
    }

    fun generateUniqueQRCode(accessToken: String, userId: Int): String {
        val combinedData = "${accessToken}_${userId}"
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(combinedData.toByteArray())
            hashBytes.joinToString("") { "%02x".format(it) }.take(16).uppercase()
        } catch (e: Exception) {
            "${userId}_${accessToken.take(8)}"
        }
    }
}

// Custom Exception para Login con status code
class LoginException(
    message: String,
    val statusCode: Int
) : Exception(message)

// Custom Exception para Forgot Password
class ForgotPasswordException(
    message: String,
    val statusCode: Int
) : Exception(message)