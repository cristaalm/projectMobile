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
import java.util.concurrent.TimeUnit
import com.renova.mobile.data.model.AllianceStatsResponse
import com.renova.mobile.data.model.ActivityByDayResponse
import com.renova.mobile.data.model.TopRewardsResponse

data class CashCutResponse(
    val success: Boolean,
    val message: String,
    val data: CashCutData?,
    val errors: Any?,
    val code: Int
)

data class CashCutData(
    val total_points: Int,
    val cash_out: Double
)

// Total puntos (DE DEVELOP)
data class TotalPointsResponse(
    val success: Boolean,
    val message: String,
    val data: TotalPointsData,
    val errors: Any?,
    val status: Int
)

data class TotalPointsData(
    val total_points: Int
)

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
    val points_month: Int = 0,
    @SerializedName("badge") val badge: BadgeCollection? = null,
    val code_identity: String?,
    val role: Role?,
    val alliance_id: Int?,
    val tour_completed: Boolean?,
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
){
    fun getLogoUrl(): String? {
        return if (logo == true && ext != null) {
            "https://renova-3q4h.onrender.com/storage/alliances/$id/logo.$ext"
        } else {
            null
        }
    }
}

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
    val reward_id: Int?,
    val alliance_id: Int?,
    val created_at: String,
    val updated_at: String,
    val scan_id: Int?,
    val comerciant_id: Int?,
    val description: String?,
    val quantity: Int?,
    val alliance: Alliance?, // Versión de DEVELOP (más completa)
    val user: UserData?,
    val material_type: MaterialType?,
    val reward: HistoryReward?,
    val scan: Scan?
)

data class Alliance( // Versión de DEVELOP (más completa)
    val id: Int,
    val name: String,
    val contact_name: String?,
    val contact_email: String?,
    val phone: String?,
    val address: String?,
    val logo: Boolean,
    val type_shop_id: Int?,
    val type_shop: TypeShop?, // Esta línea es la diferencia
    val ext: String?,
    val status: Int,
    val created_at: String?,
    val updated_at: String?
){
    fun getLogoUrl(): String? {
        return if (logo == true && ext != null) {
            "https://renova-3q4h.onrender.com/storage/alliances/$id/logo.$ext"
        } else {
            null
        }
    }
}

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

data class BadgeInfo(
    val name: String,
    val claimed: Boolean = false,
    val unlocked: Boolean = false,
    val claimed_at: String? = null
)

// Data del perfil (usuario + documentos de verificación)
data class IdentifyUserData(
    val user: UserData,
    val identityVerification: List<IdentityVerification>
)

// Usuario completo (usado en perfil y otras llamadas)
data class UserData( // Versión de DEVELOP (más completa)
    val id: Int,
    val name: String,
    val last_name: String,
    val email: String,
    val phone: String,
    @SerializedName("curp") val curp: String,
    val total_points: Int,
    val points_month: Int,
    val streak: Int = 0,
    val tour: Boolean = false,
    val verification_status: Int, // 0=pendiente, 1=aprobado, 2=rechazado, 3=sin docs
    val two_factor_status: Boolean,
    val code_identity: String,
    val status: Int,
    val alliance: Alliance?,
    @SerializedName("badge") val badge: BadgeCollection? = null,
    val created_at: String,
    val updated_at: String,
    val role: RoleData
)

typealias BadgeCollection = List<Int>

fun BadgeCollection?.containsBadge(badgeId: Int): Boolean {
    return this?.contains(badgeId) ?: false
}

fun BadgeCollection?.toSafeSet(): Set<Int> {
    return this?.toSet() ?: emptySet()
}

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

data class StreakResponse(
    val success: Boolean,
    val message: String,
    val data: StreakData?,
    val errors: Any?,
    val status: Int
)

data class StreakData(
    val streak: Int,
    val is_active: Boolean
)

data class ScansByDayResponse(
    val success: Boolean,
    val message: String,
    val data: List<DayScanData>,
    val errors: Any?,
    val code: Int
)

data class DayScanData(
    val day: String,
    val date: String,
    val scans_count: Int
)

data class ClaimBadgeResponse(
    val success: Boolean,
    val message: String,
    val data: ClaimBadgeData?,
    val errors: Any?,
    val code: Int
)

data class ClaimBadgeData(
    val user: UserData,
    val badge: Badge
)

// ========== REWARD CLAIM (Versión de DEVELOP) ==========
data class ClaimRewardRequest(
    @SerializedName("user_id") val user_id: Int,
    @SerializedName("reward_id") val reward_id: Int,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("token") val token: String? = null
)

data class ClaimRewardResponse(
    val success: Boolean,
    val message: String,
    val data: ClaimRewardData?,
    val error: String?,
    val status: Int
)

data class ClaimRewardData(
    val reward: ClaimRewardInfo?,
    val notifications: ClaimNotifications?
)

data class ClaimRewardInfo(
    val id: Int,
    val user_id: Int,
    val reward_id: Int,
    val quantity: Int
)

data class ClaimNotifications(
    val client: NotificationResult?,
    val merchant: NotificationResult?
)

data class NotificationResult(
    val attempted: Int,
    val errors: List<String>?,
    val payload: NotificationPayload?,
    val tokens: List<String>?,
    val sent: List<SentNotification>?
)

data class NotificationPayload(
    val title: String,
    val body: String,
    val type: String,
    val reward_id: String
)

data class SentNotification(
    val token: String,
    val message_id: MessageId
)

data class MessageId(
    val name: String
)
// ========== FIN REWARD CLAIM (Versión de DEVELOP) ==========


data class UpdateFieldRequest(
    val value: String
)

data class UpdateFieldResponse(
    val success: Boolean,
    val message: String,
    val data: Any?,
    val error: String?,
    val status: Int
)

// ========== NOTIFICATIONS ==========

// ========== GENERAL ==========

data class ErrorResponse(
    val success: Boolean,
    val message: String,
    val data: ErrorResponseData?,
    val errors: Any?,
    val status: Int
)

data class ErrorResponseData(
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

data class SendNotificationRequest(
    @SerializedName("user_id") val userId: Int,
    val title: String,
    val message: String
)

data class SendNotificationResponse(
    val success: Boolean,
    val message: String
)

data class ResetPasswordRequest(
    @SerializedName("current_password") val current_password: String,
    @SerializedName("password") val password: String,
    @SerializedName("password_confirmation") val password_confirmation: String
)

// ======= FCM TOKEN REGISTER =======
data class RegisterFcmTokenRequest(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("token") val token: String,
    @SerializedName("platform") val platform: String = "android"
)

data class RegisterFcmTokenResponse(
    val success: Boolean,
    val message: String
)

// New: FCM token unregistration request (DE DEVELOP)
data class UnregisterFcmTokenRequest(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("token") val token: String,
    @SerializedName("platform") val platform: String = "android"
)

// ========== TOUR COMPLETE (DE TOUR) ==========
data class TourCompleteRequest(
    val user_id: Int
)

data class TourCompleteResponse(
    val success: Boolean,
    val message: String,
    val data: TourCompleteData?,
    val errors: Any?,
    val status: Int
)

data class TourCompleteData(
    val user: TourUser
)

data class TourUser(
    val id: Int,
    val alliance_id: Int?,
    val name: String,
    val last_name: String,
    val email: String,
    val phone: String?,
    val curp: String?,
    val email_verified_at: String?,
    val role_id: Int,
    val total_points: Int,
    val verification_status: Int,
    val two_factor_status: Boolean,
    val code_identity: String?,
    val status: String,
    val created_at: String,
    val updated_at: String
)


data class Badge(
    val id: Int,
    val name: String,
    @SerializedName("points_required") val pointsRequired: Int,
    @SerializedName("points_awared") val pointsAwarded: Int,
    val status: Boolean,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)

data class BadgesListResponse(
    val success: Boolean,
    val message: String,
    val data: BadgesPaginatedData?,
    val errors: Any?,
    val status: Int
)

data class BadgesPaginatedData(
    val data: List<Badge>,
    @SerializedName("current_page") val currentPage: Int,
    @SerializedName("first_page_url") val firstPageUrl: String?,
    val from: Int?,
    @SerializedName("last_page") val lastPage: Int,
    @SerializedName("last_page_url") val lastPageUrl: String?,
    @SerializedName("next_page_url") val nextPageUrl: String?,
    val path: String?,
    @SerializedName("per_page") val perPage: Int,
    @SerializedName("prev_page_url") val prevPageUrl: String?,
    val to: Int?,
    val total: Int
)

data class ClaimBadgeRequestV2(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("badge") val badgeId: Int
)

// ========== INTERFAZ ApiService (COMBINADA) ==========
interface ApiService {

    @GET("api/alianzas/stats/{alliance_id}")
    suspend fun getAllianceStats(
        @Path("alliance_id") allianceId: Int
    ): Response<AllianceStatsResponse>

    @GET("api/alianzas/activityByDayOfWeek/{alliance_id}")
    suspend fun getActivityByDayOfWeek(
        @Path("alliance_id") allianceId: Int
    ): Response<ActivityByDayResponse>

    @GET("api/alianzas/top-rewards/{alliance_id}")
    suspend fun getTopRewards(
        @Path("alliance_id") allianceId: Int
    ): Response<TopRewardsResponse>

    @GET("api/alianzas/cashCut/{alliance_id}")
    suspend fun getCashCut(
        @Path("alliance_id") allianceId: Int,
        @Query("only_return") onlyReturn: Boolean = true  // solo consulta
    ): Response<CashCutResponse>

    @GET("api/history/totalPointsByShop/{alliance_id}") // DE DEVELOP
    suspend fun getTotalPointsByShop(
        @Path("alliance_id") allianceId: Int,
        @Query("date_start") dateStart: String,
        @Query("date_end") dateEnd: String
    ): Response<TotalPointsResponse>

    @GET("api/badges/getAll")
    suspend fun getAllBadges(
        @Query("per_page") perPage: Int = 100,
        @Query("query") query: String? = null,
        @Query("key") key: String = "name",
        @Query("order") order: String = "asc",
        @Query("status") status: Int = 1,  // 1 = activos
        @Query("page") page: Int = 1
    ): Response<BadgesListResponse>

    @POST("api/badges/claimBadge")
    suspend fun claimBadgeV2(@Body request: ClaimBadgeRequestV2): Response<ClaimBadgeResponse>

    @POST("api/users/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    @Multipart
    @POST("api/users/uploadDocuments")
    suspend fun uploadDocuments(
        @Part("user_id") userId: Int,
        @Part document_front: MultipartBody.Part,
        @Part document_back: MultipartBody.Part
    ): Response<UploadDocumentsResponse>

    @POST("api/users/resetPassword")
    suspend fun resetPassword(
        @Body request: ResetPasswordRequest
    ): Response<UpdateFieldResponse>

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
        @Query("status") status: Int,
        @Query("page") page: Int? = null,
        @Query("per_page") per_page: Int? = null,
        @Query("query") query: String? = null,
        @Query("key") key: String? = null,
        @Query("order") order: String? = null
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

    // Endpoint para obtener imagen de documento
    @GET("api/users/documents/{type}/{userId}")
    suspend fun getDocumentImage(
        @Path("type") type: String,
        @Path("userId") userId: Int
    ): Response<okhttp3.ResponseBody>

    // Endpoint para actualizar campo individual del perfil
    @POST("api/users/updateField/{field}/{userId}")
    suspend fun updateUserField(
        @Path("field") field: String,
        @Path("userId") userId: Int,
        @Body request: UpdateFieldRequest
    ): Response<UpdateFieldResponse>

    // Endpoint para reiniciar estado de verificación a pendiente
    @POST("api/users/toggle-status-pending/{userId}")
    suspend fun toggleStatusPending(
        @Path("userId") userId: Int
    ): Response<UpdateFieldResponse>

    // Endpoint para subir documento individual
    @Multipart
    @POST("api/users/documents/{type}/{userId}")
    suspend fun uploadSingleDocument(
        @Path("type") type: String,
        @Path("userId") userId: Int,
        @Part document: MultipartBody.Part
    ): Response<UploadDocumentsResponse>

    @POST("api/notifications/send")
    suspend fun sendNotification(@Body request: SendNotificationRequest): Response<SendNotificationResponse>

    @POST("api/notifications/registerToken")
    suspend fun registerFcmToken(@Body request: RegisterFcmTokenRequest): Response<RegisterFcmTokenResponse>

    // New: unregister FCM token on logout (DE DEVELOP)
    @POST("api/notifications/unregisterToken")
    suspend fun unregisterFcmToken(@Body request: UnregisterFcmTokenRequest): Response<RegisterFcmTokenResponse>

    @POST("api/users/tourComplete/{userId}") // DE TOUR
    suspend fun completeTour(
        @Path("userId") userId: Int,
        @Body request: TourCompleteRequest
    ): Response<TourCompleteResponse>

    @GET("api/users/getStreak")
    suspend fun getStreak(): Response<StreakResponse>

    @GET("api/users/getScansByDayOfWeek")
    suspend fun getScansByDayOfWeek(): Response<ScansByDayResponse>
}

// ========== API CLIENT ==========
object ApiClient {
    private const val BASE_URL = "https://renova-3q4h.onrender.com/"
    const val STORAGE_URL = "${BASE_URL}storage/"

    private var sessionManager: SessionManager? = null

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // ✅ Lista de endpoints que NO requieren autenticación
    private val publicEndpoints = listOf(
        "/api/users/register",
        "/api/auth/login",
        "/api/auth/forgot-password",
        "/api/auth/reset-password",
        "/api/auth/validateToken"
    )

    private val client: OkHttpClient
        get() {
            if (sessionManager == null) {
                throw IllegalStateException("ApiClient no ha sido inicializado. Llama a ApiClient.init(context) en tu Application o Activity.")
            }

            return OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor { chain ->
                    val original = chain.request()
                    val url = original.url.toString()

                    // ✅ Verificar si es un endpoint público
                    val isPublicEndpoint = publicEndpoints.any { endpoint ->
                        url.contains(endpoint)
                    }

                    val builder = original.newBuilder()

                    // ✅ Solo añadir token si NO es un endpoint público
                    if (!isPublicEndpoint) {
                        val token = sessionManager?.getAuthToken()
                        val tokenType = sessionManager?.getTokenType() ?: "Bearer"

                        if (!token.isNullOrEmpty()) {
                            android.util.Log.d("ApiClient", "🔐 Endpoint protegido: ${original.url.encodedPath} - Añadiendo token")
                            builder.addHeader("Authorization", "$tokenType $token")
                        } else {
                            android.util.Log.w("ApiClient", "⚠️ Token no disponible para: ${original.url.encodedPath}")
                        }
                    } else {
                        android.util.Log.d("ApiClient", "🔓 Endpoint público: ${original.url.encodedPath} - Sin token")
                    }

                    chain.proceed(builder.build())
                }
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
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