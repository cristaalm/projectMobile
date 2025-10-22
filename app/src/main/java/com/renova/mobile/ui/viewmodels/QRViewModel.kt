package com.renova.mobile.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.renova.mobile.repository.ActivityRepository
import com.renova.mobile.utils.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class QRState(
    val points: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)

class QRViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ActivityRepository()
    private val sessionManager = SessionManager(application)

    private val _state = MutableStateFlow(
        QRState(points = sessionManager.getUser()?.total_points ?: 0)
    )
    val state: StateFlow<QRState> = _state.asStateFlow()

    init {
        loadPoints()
    }

    fun loadPoints() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val pts = repository.getUserPoints(sessionManager)
                _state.update { it.copy(points = pts, isLoading = false) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun retry() {
        loadPoints()
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}