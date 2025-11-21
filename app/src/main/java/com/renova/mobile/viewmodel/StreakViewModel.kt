package com.renova.mobile.ui.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.renova.mobile.network.ApiClient
import com.renova.mobile.network.BadgeCollection
import com.renova.mobile.network.ClaimBadgeRequestV2
import com.renova.mobile.network.IdentifyUserRequest
import com.renova.mobile.ui.screens.MonthlyBadge
import com.renova.mobile.ui.screens.calculateDaysSinceRegistration
import com.renova.mobile.ui.screens.getBadgeVisualConfig
import com.renova.mobile.utils.SessionManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

data class StreakState(
    val currentStreak: Int = 0,
    val isStreakActive: Boolean = false,
    val longestStreak: Int = 0,
    val totalRecyclingDays: Int = 0,
    val currentMonthPoints: Int = 0,
    val userId: Int = 0,
    val userBadges: BadgeCollection = BadgeCollection(),
    val weekData: List<Int> = emptyList(),
    val monthlyBadges: List<MonthlyBadge> = emptyList(),
    val badgesLoadError: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val hasLoadedOnce: Boolean = false,
    val isManualRefresh: Boolean = false,
    val isClaimingBadge: Boolean = false,
    val claimError: String? = null
)

class StreakViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)

    private val _state = MutableStateFlow(StreakState())
    val state: StateFlow<StreakState> = _state.asStateFlow()

    init {
        loadStreakData(isManualRefresh = false)
    }

    private fun getErrorMessage(e: Exception): String {
        return when {
            e.message?.contains("Unable to resolve host", ignoreCase = true) == true ||
                    e.message?.contains("timeout", ignoreCase = true) == true ||
                    e.message?.contains("Failed to connect", ignoreCase = true) == true ||
                    e.message?.contains("No address associated with hostname", ignoreCase = true) == true ->
                "ERROR_NO_INTERNET"

            e.message?.contains("401", ignoreCase = true) == true ||
                    e.message?.contains("Unauthorized", ignoreCase = true) == true ->
                "ERROR_SESSION_EXPIRED"

            else -> e.message ?: "ERROR_UNKNOWN"
        }
    }

    fun loadStreakData(isManualRefresh: Boolean = false) {
        viewModelScope.launch {
            try {
                _state.update {
                    it.copy(
                        isLoading = true,
                        error = null,
                        badgesLoadError = false,
                        isManualRefresh = isManualRefresh
                    )
                }

                val token = sessionManager.getAccessToken()

                if (token == null) {
                    _state.update {
                        it.copy(
                            error = "ERROR_SESSION_EXPIRED",
                            isLoading = false,
                            isManualRefresh = false
                        )
                    }
                    return@launch
                }

                supervisorScope {
                    var hasAnySuccess = false

                    // 1. Obtener datos del usuario
                    val userDataDeferred = async {
                        try {
                            val identityResponse = ApiClient.apiService.identifyUser(
                                IdentifyUserRequest(
                                    token = token,
                                    with_identity = false
                                )
                            )

                            if (identityResponse.isSuccessful && identityResponse.body()?.success == true) {
                                identityResponse.body()?.data?.user
                            } else null
                        } catch (e: Exception) {
                            Log.e("StreakViewModel", "Error fetching user data", e)
                            null
                        }
                    }

                    // 2. Obtener badges desde el backend
                    val badgesDeferred = async {
                        try {
                            val badgesResponse = ApiClient.apiService.getAllBadges(
                                perPage = 100,
                                status = 1
                            )

                            if (badgesResponse.isSuccessful && badgesResponse.body()?.success == true) {
                                badgesResponse.body()?.data?.data
                            } else null
                        } catch (e: Exception) {
                            Log.e("StreakViewModel", "Error fetching badges", e)
                            null
                        }
                    }

                    // 3. Obtener racha
                    val streakDeferred = async {
                        try {
                            val streakResponse = ApiClient.apiService.getStreak()
                            if (streakResponse.isSuccessful && streakResponse.body()?.success == true) {
                                streakResponse.body()?.data
                            } else null
                        } catch (e: Exception) {
                            Log.e("StreakViewModel", "Error fetching streak", e)
                            null
                        }
                    }

                    // 4. Obtener datos de la semana
                    val weekDataDeferred = async {
                        try {
                            val scansResponse = ApiClient.apiService.getScansByDayOfWeek()
                            if (scansResponse.isSuccessful && scansResponse.body()?.success == true) {
                                scansResponse.body()?.data?.map { it.scans_count }
                            } else null
                        } catch (e: Exception) {
                            Log.e("StreakViewModel", "Error fetching week data", e)
                            null
                        }
                    }

                    // Esperar todos los resultados
                    val userData = userDataDeferred.await()
                    val badgesData = badgesDeferred.await()
                    val streakData = streakDeferred.await()
                    val weekDataResult = weekDataDeferred.await()

                    // Procesar userData
                    var newUserId = _state.value.userId
                    var newCurrentMonthPoints = _state.value.currentMonthPoints
                    var newUserBadges = _state.value.userBadges
                    var newTotalRecyclingDays = _state.value.totalRecyclingDays

                    userData?.let { user ->
                        newUserId = user.id
                        newCurrentMonthPoints = user.points_month
                        newUserBadges = user.badge
                        newTotalRecyclingDays = calculateDaysSinceRegistration(user.created_at)
                        sessionManager.saveUser(user)
                        hasAnySuccess = true
                    }

                    // Procesar badges
                    var newMonthlyBadges = _state.value.monthlyBadges
                    var newBadgesLoadError = false

                    if (badgesData != null) {
                        newMonthlyBadges = badgesData.map { badge ->
                            val (iconRes, color, bgColor) = getBadgeVisualConfig(badge.name)
                            val isClaimed = newUserBadges.isClaimed(badge.name)

                            MonthlyBadge(
                                id = badge.id,
                                name = badge.name,
                                pointsRequired = badge.pointsRequired,
                                bonusPoints = badge.pointsAwarded,
                                iconRes = iconRes,
                                isActive = badge.status,
                                isUnlocked = newCurrentMonthPoints >= badge.pointsRequired,
                                isClaimed = isClaimed,
                                currentMonthProgress = newCurrentMonthPoints.coerceAtMost(badge.pointsRequired)
                            )
                        }.sortedBy { it.pointsRequired }
                        hasAnySuccess = true
                    } else {
                        newBadgesLoadError = true
                    }

                    // Procesar streak
                    var newCurrentStreak = _state.value.currentStreak
                    var newIsStreakActive = _state.value.isStreakActive
                    var newLongestStreak = _state.value.longestStreak

                    streakData?.let { streak ->
                        newCurrentStreak = streak.streak
                        newIsStreakActive = streak.is_active
                        if (newCurrentStreak > newLongestStreak) {
                            newLongestStreak = newCurrentStreak
                        }
                        hasAnySuccess = true
                    }

                    // Procesar weekData
                    var newWeekData = _state.value.weekData
                    weekDataResult?.let { week ->
                        newWeekData = week
                        hasAnySuccess = true
                    }

                    // Actualizar estado
                    if (hasAnySuccess) {
                        _state.update {
                            it.copy(
                                userId = newUserId,
                                currentMonthPoints = newCurrentMonthPoints,
                                userBadges = newUserBadges,
                                totalRecyclingDays = newTotalRecyclingDays,
                                monthlyBadges = newMonthlyBadges,
                                badgesLoadError = newBadgesLoadError,
                                currentStreak = newCurrentStreak,
                                isStreakActive = newIsStreakActive,
                                longestStreak = newLongestStreak,
                                weekData = newWeekData,
                                isLoading = false,
                                hasLoadedOnce = true,
                                error = null,
                                isManualRefresh = false
                            )
                        }
                    } else {
                        // Si nada funcionó
                        _state.update {
                            it.copy(
                                error = "ERROR_NO_INTERNET",
                                isLoading = false,
                                isManualRefresh = false
                            )
                        }
                    }
                }
            } catch (e: CancellationException) {
                Log.d("StreakViewModel", "Loading cancelled")
            } catch (e: Exception) {
                val errorMessage = getErrorMessage(e)
                Log.e("StreakViewModel", "Error loading streak data: $errorMessage", e)

                _state.update {
                    it.copy(
                        error = errorMessage,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun claimBadge(badgeId: Int) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isClaimingBadge = true, claimError = null) }

                val response = ApiClient.apiService.claimBadgeV2(
                    ClaimBadgeRequestV2(
                        userId = _state.value.userId,
                        badgeId = badgeId
                    )
                )

                if (response.isSuccessful && response.body()?.success == true) {
                    response.body()?.data?.user?.let { updatedUserData ->
                        sessionManager.saveUser(updatedUserData)

                        val currentUser = sessionManager.getUser()
                        currentUser?.let { user ->
                            val updatedUser = user.copy(
                                points_month = updatedUserData.points_month,
                                badge = updatedUserData.badge,
                                total_points = updatedUserData.total_points
                            )
                            sessionManager.saveSession(
                                sessionManager.getAccessToken() ?: "",
                                sessionManager.getTokenType(),
                                sessionManager.getExpiresAt(),
                                updatedUser,
                                sessionManager.hasRememberMe()
                            )
                        }

                        _state.update {
                            it.copy(
                                currentMonthPoints = updatedUserData.points_month,
                                userBadges = updatedUserData.badge,
                                monthlyBadges = it.monthlyBadges.map { badge ->
                                    if (badge.id == badgeId) {
                                        badge.copy(isClaimed = true)
                                    } else badge
                                },
                                isClaimingBadge = false,
                                claimError = null
                            )
                        }
                    }
                } else {
                    _state.update {
                        it.copy(
                            claimError = response.body()?.message ?: "ERROR_UNKNOWN",
                            isClaimingBadge = false
                        )
                    }
                }
            } catch (e: Exception) {
                val errorMessage = getErrorMessage(e)
                Log.e("StreakViewModel", "Error claiming badge: $errorMessage", e)

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
        loadStreakData(isManualRefresh = true)
    }

    fun clearError() {
        _state.update {
            it.copy(
                error = null,
                isManualRefresh = false
            )
        }
    }

    fun clearClaimError() {
        _state.update {
            it.copy(claimError = null)
        }
    }
}