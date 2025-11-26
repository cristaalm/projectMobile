package com.renova.mobile.repository

import android.util.Log
import com.renova.mobile.network.ApiClient
import com.renova.mobile.network.Badge
import com.renova.mobile.network.ClaimBadgeRequestV2
import com.renova.mobile.network.IdentifyUserRequest
import com.renova.mobile.network.UserData
import com.renova.mobile.network.toSafeSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repositorio que maneja la lógica de negocio de los badges
 */
class BadgeRepository {

    // Cache de badges para evitar llamadas innecesarias
    private var badgesCache: List<Badge>? = null
    private var cacheTimestamp: Long = 0
    private val CACHE_DURATION = 5 * 60 * 1000L // 5 minutos

    /**
     * Representa el estado procesado de un badge para la UI
     */
    data class BadgeState(
        val badge: Badge,
        val isClaimed: Boolean,
        val isUnlocked: Boolean,
        val canClaim: Boolean
    )

    /**
     * Resultado de la obtención de badges
     */
    sealed class BadgeResult {
        data class Success(
            val allBadges: List<BadgeState>,
            val nextClaimableBadge: BadgeState?,
            val currentMonthPoints: Int
        ) : BadgeResult()

        data class Error(val message: String) : BadgeResult()
    }

    /**
     * Resultado del reclamo de un badge
     */
    sealed class ClaimResult {
        data class Success(
            val updatedUser: UserData,
            val claimedBadge: Badge,
            val newTotalPoints: Int,
            val bonusPointsAwarded: Int
        ) : ClaimResult()

        data class Error(val message: String) : ClaimResult()
    }

    /**
     * Obtiene todos los badges con su estado actual
     */
    suspend fun getBadgesWithState(
        token: String,
        userId: Int
    ): BadgeResult = withContext(Dispatchers.IO) {
        try {
            // 1. Obtener datos del usuario
            val userResponse = ApiClient.apiService.identifyUser(
                IdentifyUserRequest(token = token, with_identity = false)
            )

            if (!userResponse.isSuccessful || userResponse.body()?.success != true) {
                return@withContext BadgeResult.Error("Error al obtener datos del usuario")
            }

            val userData = userResponse.body()?.data?.user
                ?: return@withContext BadgeResult.Error("Datos de usuario no disponibles")

            // 2. Obtener todos los badges disponibles (con cache)
            val allBadges = getCachedBadges()

            // 3. Procesar badges
            val claimedBadgeIds = userData.badge.toSafeSet()
            val currentMonthPoints = userData.points_month

            val badgesWithState = allBadges
                .sortedBy { it.pointsRequired }
                .map { badge ->
                    val isClaimed = claimedBadgeIds.contains(badge.id)
                    val isUnlocked = currentMonthPoints >= badge.pointsRequired

                    // Verificar que todos los badges anteriores estén reclamados
                    val previousBadges = allBadges
                        .filter { it.pointsRequired < badge.pointsRequired }
                    val allPreviousClaimed = previousBadges.all {
                        claimedBadgeIds.contains(it.id)
                    }

                    // Un badge se puede reclamar si:
                    // - Está desbloqueado (tiene suficientes puntos)
                    // - NO ha sido reclamado
                    // - Todos los badges anteriores YA fueron reclamados
                    val canClaim = isUnlocked && !isClaimed && allPreviousClaimed

                    BadgeState(
                        badge = badge,
                        isClaimed = isClaimed,
                        isUnlocked = isUnlocked,
                        canClaim = canClaim
                    )
                }

            // 4. Encontrar el siguiente badge reclamable
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

    /**
     * Obtiene badges con cache para reducir llamadas al API
     */
    private suspend fun getCachedBadges(): List<Badge> {
        val now = System.currentTimeMillis()

        // Si el cache es válido, retornarlo
        badgesCache?.let { cache ->
            if ((now - cacheTimestamp) < CACHE_DURATION) {
                Log.d("BadgeRepository", "Using cached badges")
                return cache
            }
        }

        // Cache inválido o no existe, hacer llamada al API
        Log.d("BadgeRepository", "Fetching badges from API")
        val badgesResponse = ApiClient.apiService.getAllBadges(
            perPage = 100,
            status = 1
        )

        if (!badgesResponse.isSuccessful || badgesResponse.body()?.success != true) {
            throw Exception("Error al obtener badges del servidor")
        }

        val badges = badgesResponse.body()?.data?.data
            ?: throw Exception("No hay badges disponibles")

        // Actualizar cache
        badgesCache = badges
        cacheTimestamp = now

        return badges
    }

    /**
     * Reclama un badge específico
     */
    suspend fun claimBadge(
        userId: Int,
        badgeId: Int
    ): ClaimResult = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.apiService.claimBadgeV2(
                ClaimBadgeRequestV2(
                    userId = userId,
                    badgeId = badgeId
                )
            )

            if (!response.isSuccessful || response.body()?.success != true) {
                val errorMsg = response.body()?.message ?: "Error al reclamar badge"
                return@withContext ClaimResult.Error(errorMsg)
            }

            val updatedUser = response.body()?.data?.user
                ?: return@withContext ClaimResult.Error("No se recibieron datos actualizados")

            // Buscar el badge en el cache (mucho más eficiente)
            val claimedBadge = badgesCache?.find { it.id == badgeId }
                ?: run {
                    // Si no está en cache, hacer llamada (fallback)
                    Log.w("BadgeRepository", "Badge not in cache, fetching from API")
                    val badges = getCachedBadges()
                    badges.find { it.id == badgeId }
                } ?: return@withContext ClaimResult.Error("Badge no encontrado")

            ClaimResult.Success(
                updatedUser = updatedUser,
                claimedBadge = claimedBadge,
                newTotalPoints = updatedUser.total_points,
                bonusPointsAwarded = claimedBadge.pointsAwarded
            )

        } catch (e: Exception) {
            Log.e("BadgeRepository", "Error in claimBadge", e)
            ClaimResult.Error(e.message ?: "Error desconocido")
        }
    }

    /**
     * Verifica si un badge específico puede ser reclamado
     */
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

    /**
     * Obtiene el progreso hacia el siguiente badge
     */
    suspend fun getNextBadgeProgress(
        token: String,
        userId: Int
    ): NextBadgeProgress? = withContext(Dispatchers.IO) {
        when (val result = getBadgesWithState(token, userId)) {
            is BadgeResult.Success -> {
                // Buscar el primer badge no reclamado
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

    /**
     * Limpia el cache de badges (útil después de reclamar)
     */
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