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
    val error: String? = null
)

class ActivityViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = ActivityRepository()
    private val sessionManager = SessionManager(application)

    private val _state = MutableStateFlow(ActivityState())
    val state: StateFlow<ActivityState> = _state.asStateFlow()

    init {
        loadHistory(1)
    }

    fun loadHistory(page: Int = 1) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            try {
                val historyDeferred = async { repository.getHistory(page = page, perPage = 6) }
                val totalsDeferred = async {
                    if (_state.value.totalPlastic == 0 && _state.value.totalAluminum == 0) {
                        repository.getTotalScans()
                    } else null
                }
                val pointsDeferred = async { repository.getUserPoints(sessionManager) }

                val historyResponse = historyDeferred.await()
                val totalsResponse = totalsDeferred.await()
                val userPoints = pointsDeferred.await()

                if (historyResponse.success) {
                    // Ordenar las actividades por fecha de creación (más reciente primero)
                    val sortedActivities = historyResponse.data.data.sortedByDescending { activity ->
                        try {
                            parseActivityDate(activity.created_at)?.time ?: 0L
                        } catch (e: Exception) {
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
                            isLoading = false
                        )
                    }
                } else {
                    _state.update {
                        it.copy(
                            error = historyResponse.message,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception){
                _state.update {
                    it.copy(
                        error = "Error: ${e.message}",
                        isLoading = false
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
            loadHistory(_state.value.currentPage + 1)
        }
    }

    fun previousPage() {
        if (_state.value.currentPage > 1 && !_state.value.isLoading) {
            loadHistory(_state.value.currentPage - 1)
        }
    }

    fun retry() {
        loadHistory(1)
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}