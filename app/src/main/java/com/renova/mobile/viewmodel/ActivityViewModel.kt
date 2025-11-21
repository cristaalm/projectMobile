package com.renova.mobile.ui.viewmodels

import android.util.Log
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.renova.mobile.network.ActivityItem
import com.renova.mobile.repository.ActivityRepository
import com.renova.mobile.utils.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.CancellationException
import java.text.SimpleDateFormat
import java.util.*

data class ActivityState(
    val activities: List<ActivityItem> = emptyList(),
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val totalPlastic: Int = 0,
    val totalAluminum: Int = 0,
    val totalPoints: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val hasLoadedOnce: Boolean = false,
    val isManualRefresh: Boolean = false  // ✅ NUEVO: indica si es refresh manual del usuario
)

class ActivityViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = ActivityRepository()
    private val sessionManager = SessionManager(application)

    private val _state = MutableStateFlow(ActivityState())
    val state: StateFlow<ActivityState> = _state.asStateFlow()

    init {
        loadHistory(1, isManualRefresh = false)  // ✅ Primera carga NO es manual
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

    fun loadHistory(page: Int = 1, isManualRefresh: Boolean = false) {  // ✅ Nuevo parámetro
        viewModelScope.launch {
            try {
                _state.update {
                    it.copy(
                        isLoading = true,
                        error = null,
                        isManualRefresh = isManualRefresh  // ✅ Guardar si es manual
                    )
                }

                supervisorScope {
                    try {
                        val historyDeferred = async {
                            try {
                                repository.getHistory(page = page, perPage = 6)
                            } catch (e: Exception) {
                                Log.e("ActivityViewModel", "Error fetching history", e)
                                throw e
                            }
                        }

                        val totalsDeferred = async {
                            try {
                                if (_state.value.totalPlastic == 0 && _state.value.totalAluminum == 0) {
                                    repository.getTotalScans()
                                } else null
                            } catch (e: Exception) {
                                Log.e("ActivityViewModel", "Error fetching totals", e)
                                null
                            }
                        }

                        val pointsDeferred = async {
                            try {
                                repository.getUserPoints(sessionManager)
                            } catch (e: Exception) {
                                Log.e("ActivityViewModel", "Error fetching points", e)
                                _state.value.totalPoints
                            }
                        }

                        val historyResponse = historyDeferred.await()
                        val totalsResponse = totalsDeferred.await()
                        val userPoints = pointsDeferred.await()

                        if (historyResponse.success) {
                            val sortedActivities = historyResponse.data.data.sortedByDescending { activity ->
                                try {
                                    parseActivityDate(activity.created_at)?.time ?: 0L
                                } catch (e: Exception) {
                                    Log.e("ActivityViewModel", "Error parsing date", e)
                                    0L
                                }
                            }

                            _state.update {
                                it.copy(
                                    activities = sortedActivities,
                                    currentPage = historyResponse.data.current_page,
                                    totalPages = historyResponse.data.last_page,
                                    totalPoints = userPoints,
                                    totalPlastic = totalsResponse?.data?.plastic ?: it.totalPlastic,
                                    totalAluminum = totalsResponse?.data?.aluminum ?: it.totalAluminum,
                                    isLoading = false,
                                    hasLoadedOnce = true,
                                    error = null,
                                    isManualRefresh = false  // ✅ Resetear después de éxito
                                )
                            }
                        } else {
                            _state.update {
                                it.copy(
                                    error = historyResponse.message,
                                    isLoading = false,
                                    isManualRefresh = false  // ✅ Resetear después de error
                                )
                            }
                        }
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        throw e
                    }
                }
            } catch (e: CancellationException) {
                Log.d("ActivityViewModel", "Loading cancelled")
            } catch (e: Exception) {
                val errorMessage = getErrorMessage(e)

                Log.e("ActivityViewModel", "Error loading history: $errorMessage", e)

                _state.update {
                    it.copy(
                        error = errorMessage,
                        isLoading = false
                        // ✅ NO resetear isManualRefresh aquí, lo necesitamos en la UI
                    )
                }
            }
        }
    }

    private fun parseActivityDate(dateString: String): Date? {
        val patterns = arrayOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX",
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ssXXX",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSS",
            "yyyy-MM-dd'T'HH:mm:ss.SSS",
            "yyyy-MM-dd'T'HH:mm:ss"
        )
        for (p in patterns) {
            try {
                val sdfInput = SimpleDateFormat(p, Locale.getDefault())
                if (p.contains("'Z'") || p.contains("XXX")) {
                    sdfInput.timeZone = TimeZone.getTimeZone("UTC")
                }
                val date = sdfInput.parse(dateString)
                if (date != null) return date
            } catch (_: Exception) {
            }
        }
        return null
    }

    fun nextPage() {
        if (_state.value.currentPage < _state.value.totalPages && !_state.value.isLoading) {
            loadHistory(_state.value.currentPage + 1, isManualRefresh = false)  // ✅ Paginación NO es manual
        }
    }

    fun previousPage() {
        if (_state.value.currentPage > 1 && !_state.value.isLoading) {
            loadHistory(_state.value.currentPage - 1, isManualRefresh = false)  // ✅ Paginación NO es manual
        }
    }

    fun retry() {
        loadHistory(_state.value.currentPage, isManualRefresh = true)  // ✅ Retry SI es manual
    }

    fun clearError() {
        _state.update {
            it.copy(
                error = null,
                isManualRefresh = false  // ✅ También resetear el flag manual
            )
        }
    }
}