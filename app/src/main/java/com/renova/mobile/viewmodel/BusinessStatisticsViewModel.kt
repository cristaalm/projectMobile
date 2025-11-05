package com.renova.mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renova.mobile.data.model.*
import com.renova.mobile.data.repository.StatisticsRepository
import com.renova.mobile.data.repository.StatisticsResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log

data class StatisticsUiState(
    val isLoading: Boolean = false,
    val stats: AllianceStatsData? = null,
    val activityData: ActivityByDayData? = null,
    val topRewards: List<TopReward> = emptyList(),
    val error: String? = null
)

class BusinessStatisticsViewModel : ViewModel() {
    private val repository = StatisticsRepository()

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    fun loadStatistics(allianceId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            // Cargar los 3 endpoints en paralelo
            val statsDeferred = launch { loadStats(allianceId) }
            val activityDeferred = launch { loadActivity(allianceId) }
            val rewardsDeferred = launch { loadTopRewards(allianceId) }

            // Esperar a que todos terminen
            statsDeferred.join()
            activityDeferred.join()
            rewardsDeferred.join()

            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    private suspend fun loadStats(allianceId: Int) {
        when (val result = repository.getAllianceStats(allianceId)) {
            is StatisticsResult.Success -> {
                _uiState.value = _uiState.value.copy(stats = result.data)
                Log.d("StatisticsViewModel", "Stats cargadas: ${result.data}")
            }
            is StatisticsResult.Error -> {
                _uiState.value = _uiState.value.copy(
                    error = "Error al cargar estadísticas: ${result.message}"
                )
                Log.e("StatisticsViewModel", "Error stats: ${result.message}")
            }
            StatisticsResult.Loading -> { }
        }
    }

    private suspend fun loadActivity(allianceId: Int) {
        when (val result = repository.getActivityByDayOfWeek(allianceId)) {
            is StatisticsResult.Success -> {
                _uiState.value = _uiState.value.copy(activityData = result.data)
                Log.d("StatisticsViewModel", "Actividad cargada: ${result.data.statsToWeek.size} días")
            }
            is StatisticsResult.Error -> {
                Log.e("StatisticsViewModel", "Error actividad: ${result.message}")
            }
            StatisticsResult.Loading -> { }
        }
    }

    private suspend fun loadTopRewards(allianceId: Int) {
        when (val result = repository.getTopRewards(allianceId)) {
            is StatisticsResult.Success -> {
                _uiState.value = _uiState.value.copy(topRewards = result.data)
                Log.d("StatisticsViewModel", "Top rewards cargados: ${result.data.size} items")
            }
            is StatisticsResult.Error -> {
                Log.e("StatisticsViewModel", "Error rewards: ${result.message}")
            }
            StatisticsResult.Loading -> { }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}