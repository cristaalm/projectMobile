package com.renova.mobile.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.renova.mobile.network.ApiClient
import com.renova.mobile.network.IdentifyUserRequest
import com.renova.mobile.repository.BadgeRepository
import com.renova.mobile.ui.screens.MonthlyBadge
import com.renova.mobile.ui.screens.calculateDaysSinceRegistration
import com.renova.mobile.ui.screens.getBadgeVisualConfig
import com.renova.mobile.utils.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WeekDayData(
    val day: String,
    val isActive: Boolean
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

                // 1. Obtener datos del usuario
                val identityResponse = ApiClient.apiService.identifyUser(
                    IdentifyUserRequest(token = token, with_identity = false)
                )

                if (identityResponse.isSuccessful && identityResponse.body()?.success == true) {
                    val userData = identityResponse.body()?.data?.user

                    if (userData != null) {
                        // Calcular días desde registro
                        val daysSinceRegistration = calculateDaysSinceRegistration(userData.created_at)

                        _state.update {
                            it.copy(
                                currentStreak = userData.streak,
                                isStreakActive = userData.streak > 0,
                                currentMonthPoints = userData.points_month,
                                userId = userData.id
                            )
                        }

                        sessionManager.saveUser(userData)
                    }
                }

                // 2. Cargar badges
                loadBadges()

                // 3. Generar datos de la semana (simulados por ahora)
                val weekData = generateWeekData()

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

    private fun generateWeekData(): List<WeekDayData> {
        val days = listOf("L", "M", "M", "J", "V", "S", "D")
        // Por ahora, generamos datos aleatorios
        // En producción, esto vendría del backend
        return days.map { day ->
            WeekDayData(
                day = day,
                isActive = (0..1).random() == 1
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
                            claimError = "Usuario no identificado",
                            isClaimingBadge = false
                        )
                    }
                    return@launch
                }

                when (val result = badgeRepository.claimBadge(userId, badgeId)) {
                    is BadgeRepository.ClaimResult.Success -> {
                        sessionManager.saveUser(result.updatedUser)

                        _state.update {
                            it.copy(
                                currentMonthPoints = result.updatedUser.points_month,
                                monthlyBadges = it.monthlyBadges.map { badge ->
                                    if (badge.id == badgeId) badge.copy(isClaimed = true)
                                    else badge
                                },
                                isClaimingBadge = false
                            )
                        }

                        // Recargar datos para reflejar cambios
                        loadStreakData(isManualRefresh = false)
                    }
                    is BadgeRepository.ClaimResult.Error -> {
                        _state.update {
                            it.copy(
                                claimError = result.message,
                                isClaimingBadge = false
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                val errorMessage = when {
                    e.message?.contains("Unable to resolve host", ignoreCase = true) == true ||
                            e.message?.contains("timeout", ignoreCase = true) == true ||
                            e.message?.contains("Failed to connect", ignoreCase = true) == true ||
                            e.message?.contains("No address associated with hostname", ignoreCase = true) == true ->
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