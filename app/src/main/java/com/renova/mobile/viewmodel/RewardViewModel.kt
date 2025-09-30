package com.renova.mobile.ui.screens.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.renova.mobile.network.Reward
import com.renova.mobile.repository.RewardRepository
import kotlinx.coroutines.launch

data class RewardUiState(
    val isLoading: Boolean = false,
    val rewards: List<Reward> = emptyList(),
    val allianceName: String = "Recompensas",
    val error: String? = null
)

class RewardViewModel(private val allianceId: Int) : ViewModel() {

    private val repository = RewardRepository()
    var uiState by mutableStateOf(RewardUiState())
        private set

    init {
        loadRewards()
    }

    fun loadRewards() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            repository.getRewardsByAlliance(allianceId)
                .onSuccess { rewards ->
                    val name = rewards.firstOrNull()?.alliance?.name ?: "Recompensas"
                    uiState = uiState.copy(isLoading = false, rewards = rewards, allianceName = name)
                }
                .onFailure {
                    uiState = uiState.copy(isLoading = false, error = it.message)
                }
        }
    }
}

class RewardViewModelFactory(private val allianceId: Int) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RewardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RewardViewModel(allianceId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}