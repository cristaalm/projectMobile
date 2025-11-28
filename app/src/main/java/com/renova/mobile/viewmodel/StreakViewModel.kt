package com.renova.mobile.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.renova.mobile.network.ApiClient
import com.renova.mobile.network.IdentifyUserRequest
import com.renova.mobile.network.UserData
import com.renova.mobile.network.Badge
import com.renova.mobile.repository.BadgeRepository
import com.renova.mobile.ui.screens.MonthlyBadge
import com.renova.mobile.ui.screens.getBadgeVisualConfig
import com.renova.mobile.utils.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import android.util.Log

data class WeekDayData(
    val day: String,
    val isActive: Boolean,
    val materialsCount: Int = 0
)

data class StreakState(
    val currentStreak: Int = 0,
    val isStreakActive: Boolean = false,
    val currentMonthPoints: Int = 0,
    val userId: Int = 0,
    val monthlyBadges: List<MonthlyBadge> = emptyList(),
    val weekData: List<WeekDayData> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val hasLoadedOnce: Boolean = false,
    val isClaimingBadge: Boolean = false,
    val claimError: String? = null,
    val badgesLoadError: Boolean = false,
    val isManualRefresh: Boolean = false
)

class StreakViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)
    private val badgeRepository = BadgeRepository()

    private val _state = MutableStateFlow(StreakState())
    val state: StateFlow<StreakState> = _state.asStateFlow()

    init {
        val user = sessionManager.getUser()
        if (user != null) {
            _state.update { it.copy(userId = user.id) }
        }
        loadStreakData(isManualRefresh = false)
    }

    fun loadStreakData(isManualRefresh: Boolean = false) {
        viewModelScope.launch {
            try {
                Log.d("StreakViewModel", "🔄 Iniciando loadStreakData - isManualRefresh: $isManualRefresh")

                _state.update {
                    it.copy(
                        isLoading = true,
                        error = null,
                        isManualRefresh = isManualRefresh
                    )
                }

                val token = sessionManager.getAccessToken()
                if (token == null) {
                    Log.e("StreakViewModel", "❌ Token no disponible")
                    _state.update {
                        it.copy(
                            error = "ERROR_SESSION_EXPIRED",
                            isLoading = false,
                            isManualRefresh = false
                        )
                    }
                    return@launch
                }

                // ============================================
                // 1. Obtener datos del usuario (ÚNICA VEZ)
                // ============================================
                Log.d("StreakViewModel", "📡 Llamando a identifyUser...")
                val identityResponse = ApiClient.apiService.identifyUser(
                    IdentifyUserRequest(token = token, with_identity = false)
                )

                if (!identityResponse.isSuccessful || identityResponse.body()?.success != true) {
                    Log.e("StreakViewModel", "❌ Error en identifyUser: ${identityResponse.code()}")
                    _state.update {
                        it.copy(
                            error = "ERROR_UNKNOWN",
                            isLoading = false,
                            isManualRefresh = false
                        )
                    }
                    return@launch
                }

                val userData = identityResponse.body()?.data?.user
                if (userData == null) {
                    Log.e("StreakViewModel", "❌ userData es null")
                    _state.update {
                        it.copy(
                            error = "ERROR_UNKNOWN",
                            isLoading = false,
                            isManualRefresh = false
                        )
                    }
                    return@launch
                }

                Log.d("StreakViewModel", "✅ Usuario obtenido - Points: ${userData.points_month}, Badges: ${userData.badge}")

                // ============================================
                // 2. Obtener la racha
                // ============================================
                var currentStreak = 0
                var isStreakActive = false

                try {
                    Log.d("StreakViewModel", "📡 Llamando a getStreak...")
                    val streakResponse = ApiClient.apiService.getStreak()
                    if (streakResponse.isSuccessful && streakResponse.body()?.success == true) {
                        val streakData = streakResponse.body()?.data
                        currentStreak = streakData?.streak ?: 0
                        isStreakActive = streakData?.is_active ?: false
                        Log.d("StreakViewModel", "✅ Racha obtenida - Streak: $currentStreak, Active: $isStreakActive")
                    } else {
                        Log.w("StreakViewModel", "⚠️ Error obteniendo racha: ${streakResponse.code()}")
                    }
                } catch (e: Exception) {
                    Log.e("StreakViewModel", "❌ Error llamando a getStreak(): ${e.message}")
                }

                // Guardar usuario con racha actualizada
                sessionManager.saveUser(userData.copy(streak = currentStreak))

                // ============================================
                // 3. Cargar badges usando los datos ya obtenidos
                // ============================================
                Log.d("StreakViewModel", "📡 Cargando badges...")
                val badges = loadBadgesFromUserData(userData)
                Log.d("StreakViewModel", "✅ Badges cargados: ${badges.size}")

                // ============================================
                // 4. Cargar datos de la semana
                // ============================================
                Log.d("StreakViewModel", "📡 Cargando weekData...")
                val weekData = loadWeekData()
                Log.d("StreakViewModel", "✅ WeekData cargado: ${weekData.size} días")

                // ============================================
                // 5. Actualizar estado final - TODO junto
                // ============================================
                _state.update {
                    it.copy(
                        currentStreak = currentStreak,
                        isStreakActive = isStreakActive,
                        currentMonthPoints = userData.points_month,
                        userId = userData.id,
                        monthlyBadges = badges,
                        weekData = weekData,
                        isLoading = false,
                        hasLoadedOnce = true,
                        error = null,
                        badgesLoadError = false,
                        isManualRefresh = false
                    )
                }

                Log.d("StreakViewModel", "✅✅✅ loadStreakData COMPLETADO EXITOSAMENTE")

            } catch (e: Exception) {
                Log.e("StreakViewModel", "❌ Error en loadStreakData", e)

                val errorMessage = when {
                    e.message?.contains("Unable to resolve host", ignoreCase = true) == true ||
                            e.message?.contains("timeout", ignoreCase = true) == true ||
                            e.message?.contains("Failed to connect", ignoreCase = true) == true ||
                            e.message?.contains("No address associated with hostname", ignoreCase = true) == true ->
                        "ERROR_NO_INTERNET"
                    e.message?.contains("401", ignoreCase = true) == true ||
                            e.message?.contains("Unauthorized", ignoreCase = true) == true ->
                        "ERROR_SESSION_EXPIRED"
                    else -> "ERROR_UNKNOWN"
                }

                _state.update {
                    it.copy(
                        error = errorMessage,
                        isLoading = false,
                        isManualRefresh = false
                    )
                }
            }
        }
    }

    private suspend fun loadBadgesFromUserData(userData: UserData): List<MonthlyBadge> {
        return try {
            // Obtener todos los badges del servidor
            val allBadges = getCachedBadgesFromRepository()
            val claimedBadgeIds = userData.badge.toSafeSet()
            val currentMonthPoints = userData.points_month

            Log.d("StreakViewModel", "📊 Procesando badges - Total: ${allBadges.size}, Claimed: ${claimedBadgeIds.size}, Points: $currentMonthPoints")

            // Procesar badges
            val badges = allBadges
                .sortedBy { it.pointsRequired }
                .map { badge ->
                    val isClaimed = claimedBadgeIds.contains(badge.id)
                    val isUnlocked = currentMonthPoints >= badge.pointsRequired

                    val (iconRes, _, _) = getBadgeVisualConfig(badge.id)

                    MonthlyBadge(
                        id = badge.id,
                        name = badge.name,
                        pointsRequired = badge.pointsRequired,
                        bonusPoints = badge.pointsAwarded,
                        iconRes = iconRes,
                        isActive = badge.status,
                        isUnlocked = isUnlocked,
                        isClaimed = isClaimed,
                        currentMonthProgress = currentMonthPoints.coerceAtMost(badge.pointsRequired)
                    )
                }

            badges
        } catch (e: Exception) {
            Log.e("StreakViewModel", "❌ Error procesando badges", e)
            emptyList()
        }
    }

    private suspend fun getCachedBadgesFromRepository(): List<Badge> {
        return try {
            badgeRepository.getAllBadgesFromServer()
        } catch (e: Exception) {
            Log.e("StreakViewModel", "❌ Error obteniendo badges", e)
            emptyList()
        }
    }

    private suspend fun loadWeekData(): List<WeekDayData> {
        return try {
            when (val result = badgeRepository.getWeeklyActivity()) {
                is BadgeRepository.WeekDataResult.Success -> {
                    result.weekData
                }
                is BadgeRepository.WeekDataResult.Error -> {
                    generateEmptyWeekData()
                }
            }
        } catch (e: Exception) {
            generateEmptyWeekData()
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

    fun claimBadge(badgeId: Int) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isClaimingBadge = true, claimError = null) }

                val userId = _state.value.userId
                if (userId == 0) {
                    _state.update {
                        it.copy(
                            claimError = "ERROR_UNKNOWN",
                            isClaimingBadge = false
                        )
                    }
                    return@launch
                }

                when (val result = badgeRepository.claimBadge(userId, badgeId)) {
                    is BadgeRepository.ClaimResult.Success -> {
                        Log.d("StreakViewModel", "✅ Badge reclamado exitosamente: ${result.claimedBadge.name}")

                        // Guardar usuario actualizado
                        sessionManager.saveUser(result.updatedUser)

                        // Actualizar UI inmediatamente (optimistic update)
                        _state.update {
                            it.copy(
                                currentMonthPoints = result.updatedUser.points_month,
                                monthlyBadges = it.monthlyBadges.map { badge ->
                                    if (badge.id == badgeId) {
                                        badge.copy(
                                            isClaimed = true,
                                            currentMonthProgress = result.updatedUser.points_month
                                        )
                                    } else {
                                        badge
                                    }
                                },
                                isClaimingBadge = false,
                                claimError = null
                            )
                        }

                        // Recargar datos en segundo plano (sin mostrar loading)
                        loadStreakDataSilently()
                    }
                    is BadgeRepository.ClaimResult.Error -> {
                        Log.e("StreakViewModel", "❌ Error al reclamar badge: ${result.message}")

                        _state.update {
                            it.copy(
                                claimError = when {
                                    result.message.contains("Unable to resolve host", ignoreCase = true) ||
                                            result.message.contains("timeout", ignoreCase = true) ||
                                            result.message.contains("Failed to connect", ignoreCase = true) ->
                                        "ERROR_NO_INTERNET"
                                    else -> "ERROR_UNKNOWN"
                                },
                                isClaimingBadge = false
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("StreakViewModel", "❌ Exception al reclamar badge", e)

                val errorMessage = when {
                    e.message?.contains("Unable to resolve host", ignoreCase = true) == true ||
                            e.message?.contains("timeout", ignoreCase = true) == true ||
                            e.message?.contains("Failed to connect", ignoreCase = true) == true ->
                        "ERROR_NO_INTERNET"
                    else -> "ERROR_UNKNOWN"
                }

                _state.update {
                    it.copy(
                        claimError = errorMessage,
                        isClaimingBadge = false
                    )
                }
            }
        }
    }

    private fun loadStreakDataSilently() {
        viewModelScope.launch {
            try {
                val token = sessionManager.getAccessToken() ?: return@launch
                val userId = _state.value.userId
                if (userId == 0) return@launch

                val identityResponse = ApiClient.apiService.identifyUser(
                    IdentifyUserRequest(token = token, with_identity = false)
                )

                if (identityResponse.isSuccessful && identityResponse.body()?.success == true) {
                    val userData = identityResponse.body()?.data?.user
                    if (userData != null) {
                        val badges = loadBadgesFromUserData(userData)

                        _state.update {
                            it.copy(
                                monthlyBadges = badges,
                                currentMonthPoints = userData.points_month
                            )
                        }

                        Log.d("StreakViewModel", "✅ Datos recargados silenciosamente")
                    }
                }
            } catch (e: Exception) {
                Log.w("StreakViewModel", "⚠️ Exception en recarga silenciosa (ignorada)", e)
            }
        }
    }

    fun retry() {
        loadStreakData(isManualRefresh = false)
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun clearClaimError() {
        _state.update { it.copy(claimError = null) }
    }
}

// ============================================
// Extension function para convertir badge a Set seguro
// ============================================
fun Any?.toSafeSet(): Set<Int> {
    return when (this) {
        is List<*> -> this.filterIsInstance<Number>().map { it.toInt() }.toSet()
        is Int -> setOf(this)
        null -> emptySet()
        else -> emptySet()
    }
}