package com.renova.mobile.repository

import android.util.Log
import com.renova.mobile.network.*
import com.renova.mobile.ui.viewmodels.WeekDayData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BadgeRepository {

    private var badgesCache: List<Badge>? = null
    private var cacheTimestamp: Long = 0
    private val CACHE_DURATION = 5 * 60 * 1000L

    data class BadgeState(
        val badge: Badge,
        val isClaimed: Boolean,
        val isUnlocked: Boolean,
        val canClaim: Boolean
    )

    sealed class BadgeResult {
        data class Success(
            val allBadges: List<BadgeState>,
            val nextClaimableBadge: BadgeState?,
            val currentMonthPoints: Int
        ) : BadgeResult()
        data class Error(val message: String) : BadgeResult()
    }

    sealed class ClaimResult {
        data class Success(
            val updatedUser: UserData,
            val claimedBadge: Badge,
            val newTotalPoints: Int,
            val bonusPointsAwarded: Int
        ) : ClaimResult()
        data class Error(val message: String) : ClaimResult()
    }

    sealed class WeekDataResult {
        data class Success(val weekData: List<WeekDayData>) : WeekDataResult()
        data class Error(val message: String) : WeekDataResult()
    }

    /**
     * ✅ NUEVA FUNCIÓN: Obtiene badges del servidor sin procesar estado del usuario
     */
    suspend fun getAllBadgesFromServer(): List<Badge> = withContext(Dispatchers.IO) {
        try {
            val now = System.currentTimeMillis()
            badgesCache?.let { cache ->
                if ((now - cacheTimestamp) < CACHE_DURATION) {
                    Log.d("BadgeRepository", "📦 Usando badges desde cache")
                    return@withContext cache
                }
            }

            Log.d("BadgeRepository", "📡 Obteniendo badges desde servidor...")
            val badgesResponse = ApiClient.apiService.getAllBadges(
                perPage = 100,
                status = 1
            )

            if (!badgesResponse.isSuccessful || badgesResponse.body()?.success != true) {
                throw Exception("Error al obtener badges del servidor")
            }

            val badges = badgesResponse.body()?.data?.data
                ?: throw Exception("No hay badges disponibles")

            badgesCache = badges
            cacheTimestamp = now

            Log.d("BadgeRepository", "✅ Badges obtenidos desde servidor: ${badges.size}")
            return@withContext badges
        } catch (e: Exception) {
            Log.e("BadgeRepository", "❌ Error en getAllBadgesFromServer", e)
            emptyList()
        }
    }

    suspend fun getBadgesWithState(
        token: String,
        userId: Int
    ): BadgeResult = withContext(Dispatchers.IO) {
        try {
            val userResponse = ApiClient.apiService.identifyUser(
                IdentifyUserRequest(token = token, with_identity = false)
            )

            if (!userResponse.isSuccessful || userResponse.body()?.success != true) {
                return@withContext BadgeResult.Error("Error al obtener datos del usuario")
            }

            val userData = userResponse.body()?.data?.user
                ?: return@withContext BadgeResult.Error("Datos de usuario no disponibles")

            val allBadges = getAllBadgesFromServer()
            val claimedBadgeIds = userData.badge.toSafeSet()
            val currentMonthPoints = userData.points_month

            val badgesWithState = allBadges
                .sortedBy { it.pointsRequired }
                .map { badge ->
                    val isClaimed = claimedBadgeIds.contains(badge.id)
                    val isUnlocked = currentMonthPoints >= badge.pointsRequired

                    val previousBadges = allBadges
                        .filter { it.pointsRequired < badge.pointsRequired }
                    val allPreviousClaimed = previousBadges.all {
                        claimedBadgeIds.contains(it.id)
                    }

                    val canClaim = isUnlocked && !isClaimed && allPreviousClaimed

                    BadgeState(
                        badge = badge,
                        isClaimed = isClaimed,
                        isUnlocked = isUnlocked,
                        canClaim = canClaim
                    )
                }

            val nextClaimable = badgesWithState.firstOrNull { it.canClaim }

            BadgeResult.Success(
                allBadges = badgesWithState,
                nextClaimableBadge = nextClaimable,
                currentMonthPoints = currentMonthPoints
            )

        } catch (e: Exception) {
            Log.e("BadgeRepository", "Error in getBadgesWithState", e)
            BadgeResult.Error(e.message ?: "Error desconocido")
        }
    }

    suspend fun getWeeklyActivity(): WeekDataResult = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.apiService.getScansByDayOfWeek()

            if (!response.isSuccessful) {
                return@withContext WeekDataResult.Success(generateEmptyWeekData())
            }

            if (response.body()?.success != true) {
                return@withContext WeekDataResult.Success(generateEmptyWeekData())
            }

            val scansByDay = response.body()?.data

            if (scansByDay.isNullOrEmpty()) {
                return@withContext WeekDataResult.Success(generateEmptyWeekData())
            }

            val dayMapSpanish = mapOf(
                "Lunes" to "L",
                "Martes" to "M",
                "Miércoles" to "M",
                "Jueves" to "J",
                "Viernes" to "V",
                "Sábado" to "S",
                "Domingo" to "D"
            )

            val dayMapEnglish = mapOf(
                "Monday" to "L",
                "Tuesday" to "M",
                "Wednesday" to "M",
                "Thursday" to "J",
                "Friday" to "V",
                "Saturday" to "S",
                "Sunday" to "D"
            )

            val orderedDaysSpanish = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")

            val weekData = orderedDaysSpanish.map { dayName ->
                val dayData = scansByDay.find { it.day == dayName }
                val scansCount = dayData?.scans_count ?: 0

                WeekDayData(
                    day = dayMapSpanish[dayName] ?: dayMapEnglish[dayName] ?: dayName.first().toString(),
                    isActive = scansCount > 0,
                    materialsCount = scansCount
                )
            }

            WeekDataResult.Success(weekData)

        } catch (e: Exception) {
            WeekDataResult.Success(generateEmptyWeekData())
        }
    }

    private fun generateEmptyWeekData(): List<WeekDayData> {
        val days = listOf("L", "M", "M", "J", "V", "S", "D")
        return days.map { day ->
            WeekDayData(
                day = day,
                isActive = false,
                materialsCount = 0
            )
        }
    }

    suspend fun claimBadge(
        userId: Int,
        badgeId: Int
    ): ClaimResult = withContext(Dispatchers.IO) {
        try {
            Log.d("BadgeRepository", "🎯 Iniciando reclamo de badge $badgeId para usuario $userId")

            val response = ApiClient.apiService.claimBadgeV2(
                ClaimBadgeRequestV2(
                    userId = userId,
                    badgeId = badgeId
                )
            )

            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string()
                Log.e("BadgeRepository", "❌ HTTP Error ${response.code()}: $errorBody")
                return@withContext ClaimResult.Error("Error HTTP: ${response.code()}")
            }

            val rawBody = response.body()
            Log.d("BadgeRepository", "📦 Raw response body: $rawBody")

            val isSuccess = rawBody?.success == true
            val message = rawBody?.message ?: "Sin mensaje"

            Log.d("BadgeRepository", "✅ Success field: $isSuccess, Message: $message")

            if (!isSuccess) {
                Log.e("BadgeRepository", "❌ Servidor reportó fallo: $message")
                return@withContext ClaimResult.Error(message)
            }

            try {
                val userData = rawBody.data?.user

                if (userData == null) {
                    Log.w("BadgeRepository", "⚠️ No se recibió userData, pero el reclamo fue exitoso")
                    val claimedBadge = badgesCache?.find { it.id == badgeId }
                        ?: getAllBadgesFromServer().find { it.id == badgeId }
                        ?: throw Exception("Badge no encontrado en cache")

                    return@withContext ClaimResult.Success(
                        updatedUser = createFallbackUser(userId, badgeId),
                        claimedBadge = claimedBadge,
                        newTotalPoints = 0,
                        bonusPointsAwarded = claimedBadge.pointsAwarded
                    )
                }

                val claimedBadge = badgesCache?.find { it.id == badgeId }
                    ?: getAllBadgesFromServer().find { it.id == badgeId }
                    ?: throw Exception("Badge no encontrado")

                Log.d("BadgeRepository", "✅ Badge reclamado: ${claimedBadge.name}, Puntos: ${claimedBadge.pointsAwarded}")

                ClaimResult.Success(
                    updatedUser = userData,
                    claimedBadge = claimedBadge,
                    newTotalPoints = userData.total_points,
                    bonusPointsAwarded = claimedBadge.pointsAwarded
                )

            } catch (parseException: Exception) {
                Log.w("BadgeRepository", "⚠️ Error parseando respuesta, pero reclamo fue exitoso", parseException)

                val claimedBadge = badgesCache?.find { it.id == badgeId }
                    ?: getAllBadgesFromServer().find { it.id == badgeId }
                    ?: throw Exception("Badge no encontrado")

                return@withContext ClaimResult.Success(
                    updatedUser = createFallbackUser(userId, badgeId),
                    claimedBadge = claimedBadge,
                    newTotalPoints = 0,
                    bonusPointsAwarded = claimedBadge.pointsAwarded
                )
            }

        } catch (e: Exception) {
            Log.e("BadgeRepository", "❌ Exception crítica en claimBadge", e)
            ClaimResult.Error(e.message ?: "Error desconocido")
        }
    }

    private fun createFallbackUser(userId: Int, newBadgeId: Int): UserData {
        return UserData(
            id = userId,
            name = "",
            last_name = "",
            email = "",
            phone = "",
            curp = "",
            total_points = 0,
            points_month = 0,
            streak = 0,
            tour = false,
            verification_status = 0,
            two_factor_status = false,
            code_identity = "",
            status = 1,
            alliance = null,
            badge = listOf(newBadgeId),
            created_at = "",
            updated_at = "",
            role = RoleData(
                id = 0,
                name = "",
                display_name = "",
                is_active = true
            )
        )
    }

    suspend fun canClaimBadge(
        token: String,
        userId: Int,
        badgeId: Int
    ): Boolean = withContext(Dispatchers.IO) {
        when (val result = getBadgesWithState(token, userId)) {
            is BadgeResult.Success -> {
                val badgeState = result.allBadges.find { it.badge.id == badgeId }
                badgeState?.canClaim == true
            }
            is BadgeResult.Error -> false
        }
    }

    suspend fun getNextBadgeProgress(
        token: String,
        userId: Int
    ): NextBadgeProgress? = withContext(Dispatchers.IO) {
        when (val result = getBadgesWithState(token, userId)) {
            is BadgeResult.Success -> {
                val nextBadge = result.allBadges.firstOrNull { !it.isClaimed }

                nextBadge?.let {
                    val pointsNeeded = (it.badge.pointsRequired - result.currentMonthPoints)
                        .coerceAtLeast(0)
                    val progress = (result.currentMonthPoints.toFloat() /
                            it.badge.pointsRequired.toFloat()).coerceIn(0f, 1f)

                    NextBadgeProgress(
                        badge = it.badge,
                        currentPoints = result.currentMonthPoints,
                        pointsNeeded = pointsNeeded,
                        progress = progress,
                        isUnlocked = it.isUnlocked
                    )
                }
            }
            is BadgeResult.Error -> null
        }
    }

    fun clearCache() {
        badgesCache = null
        cacheTimestamp = 0
        Log.d("BadgeRepository", "Badge cache cleared")
    }

    data class NextBadgeProgress(
        val badge: Badge,
        val currentPoints: Int,
        val pointsNeeded: Int,
        val progress: Float,
        val isUnlocked: Boolean
    )
}

// ============================================
// Extension function
// ============================================
fun Any?.toSafeSet(): Set<Int> {
    return when (this) {
        is List<*> -> this.filterIsInstance<Number>().map { it.toInt() }.toSet()
        is Int -> setOf(this)
        null -> emptySet()
        else -> emptySet()
    }
}