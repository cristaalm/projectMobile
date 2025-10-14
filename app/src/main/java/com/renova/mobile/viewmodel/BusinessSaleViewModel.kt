package com.renova.mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renova.mobile.network.ApiClient
import com.renova.mobile.network.IdentifyUserByCodeRequest
import com.renova.mobile.network.UserData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Maneja el estado del cliente escaneado para el flujo de venta del comerciante
class BusinessSaleViewModel : ViewModel() {
    private val _scannedUser = MutableStateFlow<UserData?>(null)
    val scannedUser: StateFlow<UserData?> = _scannedUser

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // Estado de recompensas disponibles y ticket actual
    private val _isRewardLoading = MutableStateFlow(false)
    val isRewardLoading: StateFlow<Boolean> = _isRewardLoading

    private val _availableRewards = MutableStateFlow<List<com.renova.mobile.network.Reward>>(emptyList())
    val availableRewards: StateFlow<List<com.renova.mobile.network.Reward>> = _availableRewards

    private val _ticket = MutableStateFlow<List<com.renova.mobile.network.Reward>>(emptyList())
    val ticket: StateFlow<List<com.renova.mobile.network.Reward>> = _ticket

    private val _businessAllianceId = MutableStateFlow<Int?>(null)
    val businessAllianceId: StateFlow<Int?> = _businessAllianceId

    // Información del comercio identificado (alianza)
    private val _businessAlliance = MutableStateFlow<com.renova.mobile.network.Alianza?>(null)
    val businessAlliance: StateFlow<com.renova.mobile.network.Alianza?> = _businessAlliance

    // Última venta resumida para usar en Home y detalle
    private val _lastSaleSummary = MutableStateFlow<com.renova.mobile.ui.components.SaleSummary?>(null)
    val lastSaleSummary: StateFlow<com.renova.mobile.ui.components.SaleSummary?> = _lastSaleSummary

    fun setBusinessAllianceId(id: Int) {
        _businessAllianceId.value = id
        // Cargar recompensas del comercio
        loadRewardsForAlliance(id)
        // Cargar información de la alianza para mostrar nombre
        viewModelScope.launch {
            try {
                val alianzasResult = com.renova.mobile.repository.AlianzasRepository().getAllAlianzas()
                if (alianzasResult.isSuccess) {
                    _businessAlliance.value = alianzasResult.getOrDefault(emptyList()).firstOrNull { it.id == id }
                }
            } catch (_: Exception) {}
        }
    }

    fun clear() {
        _scannedUser.value = null
        _error.value = null
        _isLoading.value = false
        _isRewardLoading.value = false
        _availableRewards.value = emptyList()
        _ticket.value = emptyList()
        _businessAllianceId.value = null
        _businessAlliance.value = null
    }

    fun identifyUserByCode(scannedCode: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val code = scannedCode.trim()
                if (code.isEmpty()) {
                    _error.value = "No es un escaneo válido"
                    _scannedUser.value = null
                    return@launch
                }

                val response = ApiClient.apiService.identifyUserByCode(
                    IdentifyUserByCodeRequest(code = code)
                )

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data?.user != null) {
                        _scannedUser.value = body.data.user
                    } else {
                        val message = body?.message
                        _error.value = message ?: "Usuario no encontrado"
                        _scannedUser.value = null
                    }
                } else {
                    val errorBody = try { response.errorBody()?.string() } catch (e: Exception) { null }
                    _error.value = when (response.code()) {
                        401 -> "No es un escaneo válido"
                        404 -> "Usuario no encontrado"
                        else -> "Error ${response.code()}: ${errorBody ?: response.message()}"
                    }
                    _scannedUser.value = null
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Ocurrió un error"
                _scannedUser.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setError(message: String) {
        _error.value = message
        // No limpiar al usuario escaneado; debe permanecer visible hasta finalizar la compra
    }

    fun clearError() {
        _error.value = null
    }

    // Cargar recompensas para la alianza del comercio
    fun loadRewardsForAlliance(allianceId: Int) {
        viewModelScope.launch {
            _isRewardLoading.value = true
            try {
                val result = com.renova.mobile.repository.RewardRepository().getRewardsByAlliance(allianceId)
                if (result.isSuccess) {
                    _availableRewards.value = result.getOrDefault(emptyList())
                } else {
                    _error.value = result.exceptionOrNull()?.message ?: "No se pudieron cargar las recompensas"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Error al cargar recompensas"
            } finally {
                _isRewardLoading.value = false
            }
        }
    }

    // Agregar recompensa al ticket validando comercio, stock y estado
    fun addRewardByCode(code: String) {
        viewModelScope.launch {
            var allianceId = _businessAllianceId.value

            // Si no hay comercio identificado, intentar identificarlo buscando el código en todas las alianzas
            if (allianceId == null) {
                _isRewardLoading.value = true
                try {
                    val alianzasRes = com.renova.mobile.repository.AlianzasRepository().getAllAlianzas()
                    if (alianzasRes.isSuccess) {
                        val alianzas = alianzasRes.getOrDefault(emptyList())
                        var foundReward: com.renova.mobile.network.Reward? = null
                        var rewardsOfAlliance: List<com.renova.mobile.network.Reward> = emptyList()
                        for (a in alianzas) {
                            val rewardsRes = com.renova.mobile.repository.RewardRepository().getRewardsByAlliance(a.id)
                            if (rewardsRes.isSuccess) {
                                val rewards = rewardsRes.getOrDefault(emptyList())
                                val match = rewards.firstOrNull { it.code == code }
                                if (match != null) {
                                    foundReward = match
                                    rewardsOfAlliance = rewards
                                    _businessAllianceId.value = a.id
                                    _businessAlliance.value = a
                                    break
                                }
                            }
                        }
                        if (foundReward == null) {
                            _error.value = "No se pudo identificar el comercio para el código escaneado."
                            return@launch
                        } else {
                            // Establecer recompensas disponibles del comercio identificado
                            _availableRewards.value = rewardsOfAlliance
                            allianceId = _businessAllianceId.value
                        }
                    } else {
                        _error.value = alianzasRes.exceptionOrNull()?.message ?: "Error al cargar comercios"
                        return@launch
                    }
                } catch (e: Exception) {
                    _error.value = e.message ?: "Error al identificar comercio"
                    return@launch
                } finally {
                    _isRewardLoading.value = false
                }
            }

            // Validar contra recompensas disponibles
            if (_availableRewards.value.isEmpty() && allianceId != null) {
                loadRewardsForAlliance(allianceId!!)
            }

            val reward = _availableRewards.value.firstOrNull { it.code == code }
            if (reward == null) {
                _error.value = "Recompensa no encontrada para este comercio."
                return@launch
            }

            if (reward.allianceId != allianceId) {
                _error.value = "La recompensa no pertenece a este comercio."
                return@launch
            }

            if (!reward.isActive) {
                _error.value = "La recompensa no está activa."
                return@launch
            }

            val stock = reward.stock ?: 0
            if (stock <= 0) {
                _error.value = "No hay stock disponible para esta recompensa."
                return@launch
            }

            _ticket.value = _ticket.value + reward
            _error.value = null
        }
    }

    fun clearTicket() {
        _ticket.value = emptyList()
        _error.value = null
    }

    fun setLastSaleSummary(summary: com.renova.mobile.ui.components.SaleSummary) {
        _lastSaleSummary.value = summary
    }

    fun buildLastSaleSummaryFromTicket(): com.renova.mobile.ui.components.SaleSummary? {
        val currentTicket = _ticket.value
        if (currentTicket.isEmpty()) return null

        val grouped = currentTicket.groupBy { it.code ?: it.id?.toString() ?: it.name }
        val totalPoints = grouped.values.sumOf { group -> group.size * group.first().pointsRequired }
        val items = grouped.map { (_, items) ->
            val reward = items.first()
            com.renova.mobile.ui.components.SaleItem(name = reward.name, quantity = items.size, pointsRequired = reward.pointsRequired)
        }
        val summary = com.renova.mobile.ui.components.SaleSummary(
            id = System.currentTimeMillis().toString(),
            allianceName = _businessAlliance.value?.name,
            consumerName = _scannedUser.value?.name,
            totalPoints = totalPoints,
            items = items
        )
        _lastSaleSummary.value = summary
        return summary
    }

    fun finalizeSale() {
        // Al finalizar, limpiar ticket, errores y datos del consumidor
        _ticket.value = emptyList()
        _error.value = null
        _scannedUser.value = null
        // Mantener la alianza establecida para continuar vendiendo sin reconfigurar
        // No tocar _businessAllianceId ni _businessAlliance
        // Mantener el resumen de la última venta para mostrarlo en Home
    }
}