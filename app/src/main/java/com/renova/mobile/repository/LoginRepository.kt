// ARCHIVO: LoginRepository.kt
// RUTA: app/kotlin+java/com.renova.mobile/repository/LoginRepository.kt

package com.renova.mobile.repository

import com.renova.mobile.network.ApiClient
import com.renova.mobile.network.LoginRequest
import com.renova.mobile.network.LoginResponse

class LoginRepository {
    private val apiService = ApiClient.apiService

    suspend fun login(email: String, password: String): Result<LoginResponse> {
        return try {
            val response = apiService.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    Result.success(body)
                } ?: Result.failure(Exception("Respuesta vacía del servidor"))
            } else {
                Result.failure(Exception("Error HTTP: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}