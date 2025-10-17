package com.renova.mobile.network

import android.content.Context
import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.renova.mobile.utils.SessionManager
import okhttp3.MultipartBody
import retrofit2.http.*

// ========== REGISTER ==========
data class RegisterRequest(
    val name: String,
    val last_name: String,
    val email: String,
    val phone: String,
    val curp: String,
    val password: String,
    val password_confirmation: String
)

data class RegisterResponse(
    val success: Boolean,
    val message: String,
    val data: RegisterResponseData?,
    val errors: Any?,
    val status: Int
)

data class RegisterResponseData(
    val access_token: String,
    val token_type: String,
    val expires_at: String,
    val user: RegisteredUser
)

data class RegisteredUser(
    val id: Int,
    val name: String,
    val last_name: String,
    val email: String,
    val phone: String,
    val curp: String,
    val email_verified_at: String?,
    val role_id: Int,
    val total_points: Int,
    val verification_status: Int,
    val two_factor_status: Boolean,
    val code_identity: String,
    val status: String,
    val created_at: String,
    val updated_at: String
)

// ========== UPLOAD DOCUMENTS ==========
data class UploadDocumentsResponse(
    val success: Boolean,
    val message: String,
    val data: DocumentResponseData?,
    val errors: Any?,
    val status: Int
)

data class DocumentResponseData(
    val id: Int,
    val user_id: Int,
    val ine_front_url: String,
    val ine_back_url: String,
    val selfie_url: String?,
    val status: Int,
    val rejection_reason: String?,
    val verified_by: Int?,
    val verified_at: String?,
    val created_at: String,
    val updated_at: String
)

data class UploadSelfieResponse(
    val success: Boolean,
    val message: String,
    val data: DocumentResponseData?,
    val errors: Any?,
    val status: Int
)

// ========== LOGIN ==========
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
    val code_identity: String?,
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

// ========== FORGOT PASSWORD ==========
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

// ========== VALIDATE TOKEN ==========
data class ValidateTokenRequest(
    val token: String
)

data class ValidateTokenResponse(
    val success: Boolean,
    val message: String,
    val data: User?,
    val errors: Any?,
    val status: Int
)

// ========== ALIANZAS ==========
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

// ========== TYPE SHOP ==========
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

// ========== REWARDS ==========
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

// ========== HISTORY ==========
data class HistoryResponse(
    val success: Boolean,
    val message: String,
    val data: PaginatedData,
    val errors: Any?,
    val status: Int
)

data class TotalScansResponse(
    val success: Boolean,
    val message: String,
    val data: TotalScansData,
    val errors: Any?,
    val code: Int
)

data class TotalScansData(
    val plastic: Int,
    val aluminum: Int
)

data class PaginatedData(
    val data: List<ActivityItem>,
    val current_page: Int,
    val first_page_url: String?,
    val from: Int?,
    val last_page: Int,
    val last_page_url: String?,
    val next_page_url: String?,
    val path: String?,
    val per_page: Int,
    val prev_page_url: String?,
    val to: Int?,
    val total: Int
)

data class ActivityItem(
    val id: Int,
    val user_id: Int,
    val type_history: Int,
    val material_type_id: Int?,
    val points: Int,
    val reward_id: Int,
    val alliance_id: Int?,
    val created_at: String,
    val updated_at: String,
    val alliance: Alliance?,
    val material_type: MaterialType?,
    val reward: HistoryReward?,
    val scan: Scan?
)

data class Alliance(
    val id: Int,
    val name: String,
    val contact_name: String?,
    val contact_email: String?,
    val phone: String?,
    val address: String?,
    val logo: Boolean,
    val type_shop_id: Int?,
    val ext: String?,
    val status: Int,
    val created_at: String?,
    val updated_at: String?
)

data class MaterialType(
    val id: Int,
    val name: String,
    val slug: String,
    val points: Int,
    val is_active: Boolean,
    val description: String?,
    val created_at: String?,
    val updated_at: String?
)

data class HistoryReward(
    val id: Int,
    val alliance_id: Int,
    val name: String,
    val description: String,
    val points_required: Int,
    val image: Boolean,
    val ext: String,
    val stock: Int,
    val single_use: Boolean,
    val code: String,
    val is_active: Boolean,
    val expires_at: String,
    val created_at: String,
    val updated_at: String
)

data class Scan(
    val id: Int,
    val user_id: Int,
    val container_id: Int?,
    val material_type_id: Int,
    val image: String?,
    val is_valid: Boolean,
    val points_awarded: Int,
    val scan_status: Int,
    val description: String?,
    val scanned_at: String?,
    val created_at: String,
    val updated_at: String,
    val is_crushed: Boolean
)

// ========== IDENTITY USER / PROFILE ==========
// Request para obtener perfil completo con documentos de verificación
data class IdentifyUserRequest(
    val token: String,
    val with_identity: Boolean = false
)

// Response del perfil de usuario
data class IdentifyUserResponse(
    val success: Boolean,
    val message: String,
    val data: IdentifyUserData?,
    val errors: Any?,
    val code: Int
)

// Data del perfil (usuario + documentos de verificación)
data class IdentifyUserData(
    val user: UserData,
    val identityVerification: List<IdentityVerification>
)

// Usuario completo (usado en perfil y otras llamadas)
data class UserData(
    val id: Int,
    val name: String,
    val last_name: String,
    val email: String,
    val phone: String,
    @SerializedName("curp") val curp: String,
    val total_points: Int,
    val verification_status: Int, // 0=pendiente, 1=aprobado, 2=rechazado, 3=sin docs
    val two_factor_status: Boolean,
    val code_identity: String,
    val status: Int,
    val created_at: String,
    val updated_at: String,
    val role: RoleData
)

// Rol del usuario
data class RoleData(
    val id: Int,
    val name: String,
    val display_name: String,
    val is_active: Boolean
)

// Documentos de verificación de identidad
data class IdentityVerification(
    val id: Int,
    val user_id: Int,
    val ine_front_url: String?,
    val ine_back_url: String?,
    val selfie_url: String?,
    val status: Int,
    val rejection_reason: String?,
    val verified_by: Int?,
    val verified_at: String?,
    val created_at: String,
    val updated_at: String
)

// Request para identificar usuario por código (si se usa en otras partes)
data class IdentifyUserByCodeRequest(
    val code: String
)


// ========== REWARD CLAIM ==========
data class ClaimRewardRequest(
    val user_id: Int,
    val reward_id: Int,
    val alliance_id: Int
)

data class ClaimRewardResponse(
    val success: Boolean,
    val message: String,
    val data: ClaimRewardData?,
    val error: String?,
    val status: Int
)

data class ClaimRewardData(
    val id: Int,
    val user_id: Int,
    val reward_id: Int,
    val redeemed_at: String
)

interface ApiService {

    @POST("api/users/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    @Multipart
    @POST("api/users/uploadDocuments")
    suspend fun uploadDocuments(
        @Part("user_id") userId: Int,
        @Part document_front: MultipartBody.Part,
        @Part document_back: MultipartBody.Part
    ): Response<UploadDocumentsResponse>

    @Multipart
    @POST("api/users/uploadSelfie")
    suspend fun uploadSelfie(
        @Part("user_id") userId: Int,
        @Part selfie: MultipartBody.Part
    ): Response<UploadSelfieResponse>

    @POST("api/auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>

    @POST("api/auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<ForgotPasswordResponse>

    @POST("api/auth/validateToken")
    suspend fun validateToken(@Body request: ValidateTokenRequest): Response<ValidateTokenResponse>

    @GET("api/alianzas/getAll")
    suspend fun getAllAlianzas(
        @Query("status") status: Int
    ): Response<AlianzasResponse>

    @GET("api/typeShop/catalog")
    suspend fun getTypeShops(): Response<TypeShopResponse>

    @GET("api/reward/getAll")
    suspend fun getRewardsByAlliance(
        @Query("alliance_id") allianceId: Int,
        @Query("is_active") isActive: Int = 1
    ): Response<RewardResponse>

    @GET("api/history/getAll")
    suspend fun getHistory(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 10,
        @Query("key") key: String = "created_at",
        @Query("order") order: String = "desc"
    ): Response<HistoryResponse>

    @GET("api/history/getAll")
    suspend fun getHistoryByAlliance(
        @Query("id_alliance") allianceId: Int,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 10,
        @Query("key") key: String = "created_at",
        @Query("order") order: String = "desc"
    ): Response<HistoryResponse>

    @GET("api/scans/total-type-scans")
    suspend fun getTotalScans(): Response<TotalScansResponse>

    @POST("api/reward/claim")
    suspend fun claimReward(@Body request: ClaimRewardRequest): Response<ClaimRewardResponse>


    // Endpoint principal para obtener perfil de usuario con documentos
    @POST("api/users/identityUser")
    suspend fun identifyUser(@Body request: IdentifyUserRequest): Response<IdentifyUserResponse>

    // Endpoint para identificar usuario por código (mantener si otras partes lo usan)
    @POST("api/users/identityUserCode")
    suspend fun identifyUserByCode(@Body request: IdentifyUserByCodeRequest): Response<IdentifyUserResponse>
}

// ========== API CLIENT ==========
object ApiClient {
    private const val BASE_URL = "https://renova-3q4h.onrender.com/"

    private var sessionManager: SessionManager? = null

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client: OkHttpClient
        get() {
            if (sessionManager == null) {
                throw IllegalStateException("ApiClient no ha sido inicializado. Llama a ApiClient.init(context) en tu Application o Activity.")
            }

            return OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor { chain ->
                    val original = chain.request()
                    val builder = original.newBuilder()

                    val alreadyHasAuth = original.header("Authorization") != null
                    val token = sessionManager?.getAuthToken()
                    if (token != null && !alreadyHasAuth) {
                        builder.addHeader("Authorization", token)
                    }

                    chain.proceed(builder.build())
                }
                .build()
        }

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    fun init(context: Context) {
        if (sessionManager == null) {
            sessionManager = SessionManager(context.applicationContext)
        }
    }
}