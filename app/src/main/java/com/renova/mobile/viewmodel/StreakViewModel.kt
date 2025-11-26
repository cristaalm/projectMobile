package com.renova.mobile.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.renova.mobile.network.ApiClient
import com.renova.mobile.network.IdentifyUserRequest
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
                _state.update {
                    it.copy(
                        isLoading = true,
                        error = null,
                        isManualRefresh = isManualRefresh
                    )
                }

                val token = sessionManager.getAccessToken()
                if (token == null) {
                    _state.update {
                        it.copy(
                            error = "ERROR_SESSION_EXPIRED",
                            isLoading = false
                        )
                    }
                    return@launch
                }

                // 1. Obtener datos básicos del usuario (puntos, badges)
                val identityResponse = ApiClient.apiService.identifyUser(
                    IdentifyUserRequest(token = token, with_identity = false)
                )

                if (!identityResponse.isSuccessful || identityResponse.body()?.success != true) {
                    _state.update {
                        it.copy(
                            error = "ERROR_UNKNOWN",
                            isLoading = false
                        )
                    }
                    return@launch
                }

                val userData = identityResponse.body()?.data?.user
                if (userData == null) {
                    _state.update {
                        it.copy(
                            error = "ERROR_UNKNOWN",
                            isLoading = false
                        )
                    }
                    return@launch
                }

                // 2. Obtener la racha desde el endpoint específico (OBLIGATORIO)
                var currentStreak = 0
                var isStreakActive = false

                try {
                    val streakResponse = ApiClient.apiService.getStreak()
                    if (streakResponse.isSuccessful && streakResponse.body()?.success == true) {
                        val streakData = streakResponse.body()?.data
                        currentStreak = streakData?.streak ?: 0
                        isStreakActive = streakData?.is_active ?: false
                    } else {
                        Log.w("StreakViewModel", "⚠️ Error obteniendo racha: ${streakResponse.code()}")
                    }
                } catch (e: Exception) {
                    Log.e("StreakViewModel", "❌ Error llamando a getStreak(): ${e.message}")
                }

                // 3. Actualizar estado con datos de usuario y racha
                _state.update {
                    it.copy(
                        currentStreak = currentStreak,
                        isStreakActive = isStreakActive,
                        currentMonthPoints = userData.points_month,
                        userId = userData.id
                    )
                }

                // Guardar usuario con racha actualizada
                sessionManager.saveUser(userData.copy(streak = currentStreak))

                // 4. Cargar badges
                loadBadges()

                // 5. Cargar datos de la semana desde el repositorio
                val weekData = loadWeekData()

                // 6. Actualizar estado final
                _state.update {
                    it.copy(
                        weekData = weekData,
                        isLoading = false,
                        hasLoadedOnce = true,
                        error = null
                    )
                }

            } catch (e: Exception) {
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
                        isLoading = false
                    )
                }
            }
        }
    }

    private suspend fun loadBadges() {
        try {
            val token = sessionManager.getAccessToken()
            if (token == null) {
                _state.update { it.copy(badgesLoadError = true) }
                return
            }

            val userId = _state.value.userId
            if (userId == 0) {
                _state.update { it.copy(badgesLoadError = true) }
                return
            }

            when (val result = badgeRepository.getBadgesWithState(token, userId)) {
                is BadgeRepository.BadgeResult.Success -> {
                    val badges = result.allBadges.map { badgeState ->
                        val (iconRes, _, _) = getBadgeVisualConfig(badgeState.badge.name)

                        MonthlyBadge(
                            id = badgeState.badge.id,
                            name = badgeState.badge.name,
                            pointsRequired = badgeState.badge.pointsRequired,
                            bonusPoints = badgeState.badge.pointsAwarded,
                            iconRes = iconRes,
                            isActive = badgeState.badge.status,
                            isUnlocked = badgeState.isUnlocked,
                            isClaimed = badgeState.isClaimed,
                            currentMonthProgress = result.currentMonthPoints.coerceAtMost(badgeState.badge.pointsRequired)
                        )
                    }

                    _state.update {
                        it.copy(
                            monthlyBadges = badges,
                            currentMonthPoints = result.currentMonthPoints,
                            badgesLoadError = false
                        )
                    }
                }
                is BadgeRepository.BadgeResult.Error -> {
                    _state.update { it.copy(badgesLoadError = true) }
                }
            }
        } catch (e: Exception) {
            _state.update { it.copy(badgesLoadError = true) }
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

    // En tu StreakViewModel.kt, reemplaza la función claimBadge con esta versión mejorada:

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
                        // Si falla, no importa porque ya tenemos los datos actualizados
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

    /**
     * Carga datos sin mostrar indicadores de loading
     * Útil para refrescar después de operaciones exitosas
     */
    private fun loadStreakDataSilently() {
        viewModelScope.launch {
            try {
                val token = sessionManager.getAccessToken() ?: return@launch

                // Intentar recargar badges silenciosamente
                val userId = _state.value.userId
                if (userId == 0) return@launch

                when (val result = badgeRepository.getBadgesWithState(token, userId)) {
                    is BadgeRepository.BadgeResult.Success -> {
                        val badges = result.allBadges.map { badgeState ->
                            val (iconRes, _, _) = getBadgeVisualConfig(badgeState.badge.name)

                            MonthlyBadge(
                                id = badgeState.badge.id,
                                name = badgeState.badge.name,
                                pointsRequired = badgeState.badge.pointsRequired,
                                bonusPoints = badgeState.badge.pointsAwarded,
                                iconRes = iconRes,
                                isActive = badgeState.badge.status,
                                isUnlocked = badgeState.isUnlocked,
                                isClaimed = badgeState.isClaimed,
                                currentMonthProgress = result.currentMonthPoints.coerceAtMost(badgeState.badge.pointsRequired)
                            )
                        }

                        _state.update {
                            it.copy(
                                monthlyBadges = badges,
                                currentMonthPoints = result.currentMonthPoints
                            )
                        }

                        Log.d("StreakViewModel", "✅ Datos recargados silenciosamente")
                    }
                    is BadgeRepository.BadgeResult.Error -> {
                        // Ignorar error silencioso, ya tenemos datos válidos
                        Log.w("StreakViewModel", "⚠️ Error en recarga silenciosa (ignorado): ${result.message}")
                    }
                }
            } catch (e: Exception) {
                // Ignorar excepciones en recarga silenciosa
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