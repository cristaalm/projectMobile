package com.renova.mobile.network

import android.content.Context
import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.renova.mobile.utils.SessionManager
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

data class AlianzasResponse(
    val success: Boolean,
    val message: String,
    val data: AlianzasData?,
    val errors: Any?,
    val status: Int
)

data class AlianzasData(
    val data: List<Alianza>,
    val last_page: Int,
    val total: Int
)

data class Alianza(
    val id: Int,
    val name: String,
    val contact_name: String,
    val contact_email: String,
    val phone: String?,
    val address: String?,
    val type_shop_id: Int,
    val logo: Boolean?,
    val ext: String?,
    val status: Int,
    val created_at: String,
    val updated_at: String
)

data class TypeShopResponse(
    val success: Boolean,
    val message: String,
    val data: List<TypeShop>,
    val errors: Any?,
    val status: Int
)

data class TypeShop(
    val id: Int,
    val name: String
)

data class RewardResponse(
    val success: Boolean,
    val message: String,
    val data: RewardPaginationData?
)

data class RewardPaginationData(
    val data: List<Reward>
)

data class Reward(
    val id: Int,
    @SerializedName("alliance_id") val allianceId: Int,
    val name: String,
    val description: String,
    @SerializedName("points_required") val pointsRequired: Int,
    val image: Boolean?,
    val stock: Int?,
    val code: String?,
    @SerializedName("is_active") val isActive: Boolean,
    @SerializedName("expires_at") val expiresAt: String?,
    val alliance: Alianza?
)

interface ApiService {
    @POST("api/auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>

    @POST("api/auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<ForgotPasswordResponse>

    @GET("api/alianzas/getAll")
    suspend fun getAllAlianzas(
        @Query("status") status: Int // Esto agregará "?status=1" a la URL
    ): Response<AlianzasResponse>

    @GET("api/typeShop/catalog")
    suspend fun getTypeShops(): Response<TypeShopResponse>

    @GET("api/reward/getAll")
    suspend fun getRewardsByAlliance(
        @Query("alliance_id") allianceId: Int,
        @Query("is_active") isActive: Int = 1
    ): Response<RewardResponse>

}


object ApiClient {
    private const val BASE_URL = "https://renova-3q4h.onrender.com/"


    // instancia de SessionManager que usará el interceptor
    private var sessionManager: SessionManager? = null

    private val client: OkHttpClient
        get() {
            //Validar que el ApiClient haya sido inicializado
            if (sessionManager == null) {
                throw IllegalStateException("ApiClient no ha sido inicializado. Llama a ApiClient.init(context) en tu Application o Activity.")
            }

            return OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val original = chain.request()
                    val builder = original.newBuilder()

                    // 4. Obtenemos el token desde SessionManager en cada petición
                    val token = sessionManager?.getAccessToken()
                    if (token != null) {
                        builder.addHeader("Authorization", "Bearer $token")
                    }

                    chain.proceed(builder.build())
                }
                .build()
        }


    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client) // Usamos el cliente con el interceptor actualizado
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    // metdo para inicializar el SessionManager
    fun init(context: Context) {
        if (sessionManager == null) {
            sessionManager = SessionManager(context.applicationContext)
        }
    }
}