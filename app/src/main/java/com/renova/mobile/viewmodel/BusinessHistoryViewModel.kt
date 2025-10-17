package com.renova.mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renova.mobile.network.ApiClient
import com.renova.mobile.network.ActivityItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BusinessHistoryState(
    val isLoading: Boolean = false,
    val activities: List<ActivityItem> = emptyList(),
    val error: String? = null,
    val currentPage: Int = 1,
    val totalPages: Int = 1
)

class BusinessHistoryViewModel : ViewModel() {
    private val _state = MutableStateFlow(BusinessHistoryState())
    val state: StateFlow<BusinessHistoryState> = _state.asStateFlow()

    // ID de la alianza del comercio (por ahora hardcodeado como 14)
    private val allianceId = 14

    init {
        loadHistory()
    }

    fun loadHistory(page: Int = 1) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            try {
                val response = ApiClient.apiService.getHistoryByAlliance(
                    allianceId = allianceId,
                    page = page,
                    perPage = 10
                )

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        _state.value = _state.value.copy(
                            activities = body.data.data,
                            currentPage = body.data.current_page,
                            totalPages = body.data.last_page,
                            isLoading = false
                        )
                    } else {
                        _state.value = _state.value.copy(
                            error = body?.message ?: "Error desconocido",
                            isLoading = false
                        )
                    }
                } else {
                    _state.value = _state.value.copy(
                        error = "Error ${response.code()}: ${response.message()}",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Error de conexión: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    fun retry() {
        loadHistory(1)
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    fun nextPage() {
        val current = _state.value.currentPage
        val total = _state.value.totalPages
        if (current < total && !_state.value.isLoading) {
            loadHistory(current + 1)
        }
    }

    fun previousPage() {
        val current = _state.value.currentPage
        if (current > 1 && !_state.value.isLoading) {
            loadHistory(current - 1)
        }
    }
}