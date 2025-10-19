package com.renova.mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renova.mobile.network.ApiClient
import com.renova.mobile.network.IdentifyUserByCodeRequest
import com.renova.mobile.network.UserData
import com.renova.mobile.network.ClaimRewardRequest
import com.renova.mobile.network.SendNotificationRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BusinessSaleViewModel : ViewModel() {
    private val _scannedUser = MutableStateFlow<UserData?>(null)
    val scannedUser: StateFlow<UserData?> = _scannedUser

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _isRewardLoading = MutableStateFlow(false)
    val isRewardLoading: StateFlow<Boolean> = _isRewardLoading

    private val _availableRewards = MutableStateFlow<List<com.renova.mobile.network.Reward>>(emptyList())
    val availableRewards: StateFlow<List<com.renova.mobile.network.Reward>> = _availableRewards

    private val _ticket = MutableStateFlow<List<com.renova.mobile.network.Reward>>(emptyList())
    val ticket: StateFlow<List<com.renova.mobile.network.Reward>> = _ticket

    private val _businessAllianceId = MutableStateFlow<Int?>(null)
    val businessAllianceId: StateFlow<Int?> = _businessAllianceId

    private val _businessAlliance = MutableStateFlow<com.renova.mobile.network.Alianza?>(null)
    val businessAlliance: StateFlow<com.renova.mobile.network.Alianza?> = _businessAlliance

    private val _lastSaleSummary = MutableStateFlow<com.renova.mobile.ui.components.SaleSummary?>(null)
    val lastSaleSummary: StateFlow<com.renova.mobile.ui.components.SaleSummary?> = _lastSaleSummary


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

    fun identifyUserByCode(code: String) {
        viewModelScope.launch {
            try {
                val response = ApiClient.apiService.identifyUserByCode(IdentifyUserByCodeRequest(code = code))
                if (response.isSuccessful && response.body()?.data != null) {
                    val userData = response.body()!!.data!!.user
                    _scannedUser.value = userData
                } else {
                    _error.value = response.body()?.message ?: "No se pudo identificar al usuario"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Error de conexión al identificar usuario"
            }
        }
    }

    fun setError(message: String) {
        _error.value = message
    }

    fun clearError() {
        _error.value = null
    }

    fun loadRewardsForAlliance(allianceId: Int) {
        viewModelScope.launch {
            try {
                val response = com.renova.mobile.repository.RewardRepository().getRewardsByAlliance(allianceId)
                if (response.isSuccess) {
                    _availableRewards.value = response.getOrDefault(emptyList())
                } else {
                    _error.value = response.exceptionOrNull()?.message ?: "Error al cargar recompensas"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Error de conexión al cargar recompensas"
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

            // Validar puntos disponibles del cliente antes de agregar
            val currentUser = _scannedUser.value
            if (currentUser == null) {
                _error.value = "Primero escanee al consumidor para agregar recompensas"
                return@launch
            }
            val userPoints = currentUser.total_points
            val ticketPoints = _ticket.value.sumOf { it.pointsRequired }
            val neededPoints = reward.pointsRequired
            if (userPoints < ticketPoints + neededPoints) {
                _error.value = "Puntos insuficientes para agregar esta recompensa"
                return@launch
            }

            _ticket.value = _ticket.value + reward
            _error.value = null
        }
    }

    fun setLastSaleSummary(summary: com.renova.mobile.ui.components.SaleSummary) {
        _lastSaleSummary.value = summary
    }

    fun finalizeSale() {
        _ticket.value = emptyList()
        _error.value = null
        _scannedUser.value = null
        // Mantener la alianza y el último resumen para continuidad del flujo
    }

    // Función para reclamar recompensas via API
    fun claimRewards(
        merchantUserId: Int?,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
        onPushFeedback: (String) -> Unit
    ) {
        val currentUser = _scannedUser.value
        val currentTicket = _ticket.value
        val currentAllianceId = _businessAllianceId.value

        if (currentUser == null || currentTicket.isEmpty() || currentAllianceId == null) {
            onError("Datos incompletos para procesar la venta")
            return
        }

        viewModelScope.launch {
            try {
                // Agrupar por id de recompensa para obtener cantidades
                val grouped = currentTicket.groupBy { it.id }
                val results = mutableListOf<String>()

                for ((rewardId, group) in grouped) {
                    val req = ClaimRewardRequest(
                        user_id = currentUser.id,
                        reward_id = rewardId,
                        quantity = group.size
                    )
                    val resp = ApiClient.apiService.claimReward(req)
                    if (!resp.isSuccessful || resp.body()?.success != true) {
                        val err = resp.body()?.message ?: resp.message() ?: "Error al reclamar recompensa"
                        onError(err)
                        return@launch
                    } else {
                        val data = resp.body()?.data
                        val redeemedQty = data?.quantity ?: group.size
                        val rewardName = group.first().name
                        results.add("${redeemedQty} x ${rewardName}")
                    }
                }

                val consumerName = currentUser.name
                val allianceName = _businessAlliance.value?.name
                val msg = buildString {
                    append("Venta exitosa para ")
                    append(consumerName)
                    if (allianceName != null) {
                        append(" en ")
                        append(allianceName)
                    }
                    append(": ")
                    append(results.joinToString(", "))
                }

                // Enviar push al cliente comprador
                try {
                    val clientTitle = "Compra finalizada"
                    val clientMessage = buildString {
                        append("Has canjeado: ")
                        append(results.joinToString(", "))
                        append(". ¡Gracias por tu compra!")
                    }
                    val pushReq = SendNotificationRequest(
                        userId = currentUser.id,
                        title = clientTitle,
                        message = clientMessage
                    )
                    val pushResp = ApiClient.apiService.sendNotification(pushReq)
                    if (pushResp.isSuccessful && pushResp.body()?.success == true) {
                        onPushFeedback("Notificación enviada al cliente")
                    } else {
                        val errMsg = pushResp.body()?.message ?: pushResp.message() ?: "No se pudo enviar notificación al cliente"
                        _error.value = errMsg
                    }
                } catch (e: Exception) {
                    _error.value = e.message ?: "Error al enviar notificación al cliente"
                }

                // Enviar push al comercio (si tenemos su user_id)
                if (merchantUserId != null) {
                    try {
                        val title = "Venta finalizada"
                        val merchantMessage = buildString {
                            append("Se registró una venta para ")
                            append(consumerName)
                            append(": ")
                            append(results.joinToString(", "))
                        }
                        val mReq = SendNotificationRequest(
                            userId = merchantUserId,
                            title = title,
                            message = merchantMessage
                        )
                        val mResp = ApiClient.apiService.sendNotification(mReq)
                        if (mResp.isSuccessful && mResp.body()?.success == true) {
                            onPushFeedback("Notificación enviada al comercio")
                        } else {
                            val errMsg = mResp.body()?.message ?: mResp.message() ?: "No se pudo enviar notificación al comercio"
                            _error.value = errMsg
                        }
                    } catch (e: Exception) {
                        _error.value = e.message ?: "Error al enviar notificación al comercio"
                    }
                }

                onSuccess(msg)
            } catch (e: Exception) {
                onError("Error de conexión: ${e.message}")
            }
        }
    }
}