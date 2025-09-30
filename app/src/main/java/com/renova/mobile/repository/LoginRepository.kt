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
                    Result.success(body)
                } ?: Result.failure(Exception("Respuesta vacía del servidor"))
            } else {
                // Manejar respuestas de error
                handleLoginError(response.code(), response.errorBody()?.string())
            }
        } catch (e: HttpException) {
            handleLoginError(e.code(), null)
        } catch (e: IOException) {
            Result.failure(Exception("Error de conexión de red"))
        } catch (e: Exception) {
            Result.failure(Exception("Error inesperado: ${e.message}"))
        }
    }

    private fun handleLoginError(statusCode: Int, errorBody: String?): Result<LoginResponse> {
        return if (!errorBody.isNullOrBlank()) {
            try {
                val gson = Gson()
                val errorResponse = gson.fromJson(errorBody, LoginResponse::class.java)

                // Usar el mensaje específico de la API
                val message = when {
                    statusCode == 422 -> {
                        // Error de validación - mostrar el mensaje real de la API
                        if (errorResponse.errors is Map<*, *>) {
                            val errorsMap = errorResponse.errors as? Map<String, List<String>>
                            errorsMap?.get("email")?.firstOrNull() ?: errorResponse.message
                        } else {
                            errorResponse.message
                        }
                    }
                    statusCode == 401 -> errorResponse.message // "Correo electrónico o contraseña incorrectos"
                    statusCode == 403 -> errorResponse.message // "Tu cuenta ha sido desactivada"
                    statusCode == 500 -> {
                        // Para errores 500, errors puede ser String
                        if (errorResponse.errors is String) {
                            errorResponse.errors as String
                        } else {
                            errorResponse.message
                        }
                    }
                    else -> errorResponse.message
                }

                Result.failure(Exception(message ?: "Error del servidor"))

            } catch (e: Exception) {
                // Si falla el parseo JSON, usar mensajes por defecto
                getDefaultErrorMessage(statusCode)
            }
        } else {
            // Sin errorBody, usar mensajes por defecto
            getDefaultErrorMessage(statusCode)
        }
    }

    private fun getDefaultErrorMessage(statusCode: Int): Result<LoginResponse> {
        val message = when (statusCode) {
            401 -> "Credenciales incorrectas"
            403 -> "Cuenta desactivada"
            422 -> "Datos de entrada inválidos"
            500 -> "Error interno del servidor"
            else -> "Error del servidor (código: $statusCode)"
        }
        return Result.failure(Exception(message))
    }

    suspend fun forgotPassword(email: String): Result<ForgotPasswordResponse> {
        return try {
            val response = apiService.forgotPassword(ForgotPasswordRequest(email))

            if (response.isSuccessful) {
                response.body()?.let { body ->
                    Result.success(body)
                } ?: Result.failure(Exception("Respuesta vacía del servidor"))
            } else {
                handleForgotPasswordError(response.code(), response.errorBody()?.string())
            }
        } catch (e: HttpException) {
            handleForgotPasswordError(e.code(), null)
        } catch (e: IOException) {
            Result.failure(Exception("Error de conexión de red"))
        } catch (e: Exception) {
            Result.failure(Exception("Error inesperado: ${e.message}"))
        }
    }

    private fun handleForgotPasswordError(statusCode: Int, errorBody: String?): Result<ForgotPasswordResponse> {
        return if (!errorBody.isNullOrBlank()) {
            try {
                val gson = Gson()
                val errorResponse = gson.fromJson(errorBody, ForgotPasswordResponse::class.java)

                val message = when {
                    statusCode == 422 -> {
                        // Error de validación
                        if (errorResponse.errors is Map<*, *>) {
                            val errorsMap = errorResponse.errors as? Map<String, List<String>>
                            errorsMap?.get("email")?.firstOrNull() ?: errorResponse.message
                        } else {
                            errorResponse.message
                        }
                    }
                    statusCode == 404 -> errorResponse.message // "El correo electrónico no está registrado"
                    statusCode == 500 -> {
                        if (errorResponse.errors is String) {
                            errorResponse.errors as String
                        } else {
                            errorResponse.message
                        }
                    }
                    else -> errorResponse.message
                }

                Result.failure(Exception(message ?: "Error del servidor"))

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
        return Result.failure(Exception(message))
    }

    /**
     * Genera un código único para el QR combinando access_token y user ID
     * @param accessToken Token de acceso del usuario
     * @param userId ID del usuario
     * @return String único que identifica al usuario para el QR
     */
    fun generateUniqueQRCode(accessToken: String, userId: Int): String {
        val combinedData = "${accessToken}_${userId}"
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(combinedData.toByteArray())
            // convertir a hexadecimal y tomar los primeros 16 caracteres para un código más manejable
            hashBytes.joinToString("") { "%02x".format(it) }.take(16).uppercase()
        } catch (e: Exception) {
            // fallback en caso de error con el hash
            "${userId}_${accessToken.take(8)}"
        }
    }

    /**
     * Extrae los datos necesarios del LoginResponse para generar el QR
     * @param loginResponse Respuesta exitosa del login
     * @return String único para el QR o null si faltan datos
     */
    fun extractQRDataFromLogin(loginResponse: LoginResponse): String? {
        return loginResponse.data?.let { loginData ->
            val accessToken = loginData.access_token
            val userId = loginData.user?.id
            
            if (!accessToken.isNullOrBlank() && userId != null) {
                generateUniqueQRCode(accessToken, userId)
            } else {
                null
            }
        }
    }
}