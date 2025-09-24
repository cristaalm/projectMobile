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
    val errors: Any?,
    val status: Int
)

data class LoginData(
    val access_token: String?,
    val token_type: String?,
    val expires_at: String?,
    val user: User?
)

data class User(
    val id: Int,
    val name: String,
    val last_name: String?,
    val phone: String?,
    val email: String,
    val status: Int?,
    val verification_status: Int?,
    val total_points: Int?,
    val role: Role?,
    val created_at: String?,
    val updated_at: String?
)

data class Role(
    val id: Int,
    val display_name: String,
    val name: String,
    val is_active: Boolean
)

data class ForgotPasswordRequest(
    val email: String
)

data class ForgotPasswordResponse(
    val success: Boolean,
    val message: String,
    val data: Any?,
    val errors: Any?,
    val status: Int
)

interface ApiService {
    @POST("api/auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>

    @POST("api/auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<ForgotPasswordResponse>
}

object ApiClient {
    private const val BASE_URL = "https://renova-3q4h.onrender.com/"

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}