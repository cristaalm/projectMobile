// ARCHIVO: ApiService.kt
// RUTA: app/kotlin+java/com.renova.mobile/network/ApiService.kt

package com.renova.mobile.network

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

data class LoginRequest(
    val email: String,
    val password: String,
    val remember_me: Boolean = false
)

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val data: LoginData?,
    val errors: Map<String, List<String>>?,
    val code: Int
)

data class LoginData(
    val token: String?,
    val user: User?
)

data class User(
    val id: Int,
    val name: String,
    val email: String
)

interface ApiService {
    @POST("api/auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>
}

object ApiClient {
    private const val BASE_URL = "https://renova-3q4h.onrender.com/"  // CAMBIA ESTA URL

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}