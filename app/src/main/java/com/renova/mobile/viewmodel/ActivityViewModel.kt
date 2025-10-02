package com.renova.mobile.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renova.mobile.network.ActivityItem
import com.renova.mobile.repository.ActivityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ActivityState(
    val activities: List<ActivityItem> = emptyList(),
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val totalPlastic: Int = 0,
    val totalAluminum: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)

class ActivityViewModel(
    private val repository: ActivityRepository = ActivityRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(ActivityState())
    val state: StateFlow<ActivityState> = _state.asStateFlow()

    fun loadHistory(page: Int = 1) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            try {
                // Cargar historial
                val response = repository.getHistory(
                    page = page,
                    perPage = 10
                )

                if (response.success) {
                    _state.update {
                        it.copy(
                            activities = response.data.data,
                            currentPage = response.data.current_page,
                            totalPages = response.data.last_page,
                            isLoading = false
                        )
                    }

                    // Cargar totales solo si aún no se han cargado
                    if (_state.value.totalPlastic == 0 && _state.value.totalAluminum == 0) {
                        loadTotals()
                    }
                } else {
                    _state.update {
                        it.copy(
                            error = response.message,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("ActivityViewModel", "Error loading history", e)
                _state.update {
                    it.copy(
                        error = "Error: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    private suspend fun loadTotals() {
        try {
            val totalsResponse = repository.getTotalScans()
            if (totalsResponse.success) {
                _state.update {
                    it.copy(
                        totalPlastic = totalsResponse.data.plastic,
                        totalAluminum = totalsResponse.data.aluminum
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("ActivityViewModel", "Error loading totals", e)
            // Si falla, dejar los totales en 0
        }
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