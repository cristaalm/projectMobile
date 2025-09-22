package com.renova.mobile.repository

import com.renova.mobile.network.ApiClient
import com.renova.mobile.network.LoginRequest
import com.renova.mobile.network.LoginResponse
import com.google.gson.Gson
import retrofit2.HttpException
import java.io.IOException

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
                val errorBody = response.errorBody()?.string()

                if (!errorBody.isNullOrBlank()) {
                    try {
                        val gson = Gson()
                        val errorResponse = gson.fromJson(errorBody, LoginResponse::class.java)

                        // Determinar el error específico basado en código HTTP y errors
                        val specificMessage = when {
                            response.code() == 422 && errorResponse.errors?.containsKey("email") == true -> {
                                "user not found" // Email inválido/inexistente
                            }
                            response.code() == 401 && errorResponse.errors == null -> {
                                "invalid password" // Contraseña incorrecta
                            }
                            else -> errorResponse.message ?: "Error del servidor"
                        }

                        Result.failure(Exception(specificMessage))
                    } catch (e: Exception) {
                        // Si falla el parseo, usar código HTTP
                        val specificMessage = when (response.code()) {
                            422 -> "user not found"
                            401 -> "invalid password"
                            403 -> "account blocked"
                            else -> "Error del servidor"
                        }
                        Result.failure(Exception(specificMessage))
                    }
                } else {
                    val specificMessage = when (response.code()) {
                        422 -> "user not found"
                        401 -> "invalid password"
                        403 -> "account blocked"
                        else -> "Error del servidor"
                    }
                    Result.failure(Exception(specificMessage))
                }
            }
        } catch (e: HttpException) {
            val specificMessage = when (e.code()) {
                422 -> "user not found"
                401 -> "invalid password"
                403 -> "account blocked"
                else -> "network error"
            }
            Result.failure(Exception(specificMessage))
        } catch (e: IOException) {
            Result.failure(Exception("network connection error"))
        } catch (e: Exception) {
            Result.failure(Exception("network connection error"))
        }
    }
}