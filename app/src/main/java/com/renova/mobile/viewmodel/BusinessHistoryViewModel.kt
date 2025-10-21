package com.renova.mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renova.mobile.network.ApiClient
import com.renova.mobile.network.ActivityItem
import com.renova.mobile.network.HistoryReward
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone


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

    // Umbral de agrupación: agrupa entradas del mismo usuario dentro de 5 minutos
    private val groupWindowMs = 5 * 60 * 1000L

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
                        // 1) Ordenar por fecha de creación descendente, usando parseo robusto
                        val sorted = body.data.data.sortedByDescending { parseToMillis(it.created_at) }
                        // 2) Agrupar reclamaciones (type_history==2) del mismo usuario dentro de una ventana corta
                        val grouped = groupRewardSales(sorted)

                        _state.value = _state.value.copy(
                            activities = grouped,
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

    private fun parseToMillis(dateString: String): Long {
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
                val sdf = SimpleDateFormat(p, Locale.getDefault())
                if (p.contains("'Z'") || p.contains("XXX")) {
                    sdf.timeZone = TimeZone.getTimeZone("UTC")
                }
                val d = sdf.parse(dateString)
                if (d != null) return d.time
            } catch (_: Exception) { }
        }
        return 0L
    }

    private fun groupRewardSales(sorted: List<ActivityItem>): List<ActivityItem> {
        val result = mutableListOf<ActivityItem>()
        var currentGroup = mutableListOf<ActivityItem>()

        fun flushGroup() {
            if (currentGroup.isEmpty()) return
            // Sumar puntos y usar el último timestamp como final de venta
            val totalPoints = currentGroup.sumOf { it.points }
            val lastItem = currentGroup.maxByOrNull { parseToMillis(it.created_at) } ?: currentGroup.last()
            val firstItem = currentGroup.minByOrNull { parseToMillis(it.created_at) } ?: currentGroup.first()

            // Construir un resumen de la venta dentro del campo reward para reflejar múltiples artículos
            val aggregatedReward: HistoryReward? = firstItem.reward?.let { r ->
                val countsByName = currentGroup.groupBy { it.reward?.name ?: "Recompensa" }
                    .mapValues { it.value.size }
                val summaryString = countsByName.entries.joinToString(", ") { "${it.value} x ${it.key}" }
                r.copy(
                    name = "Venta: ${currentGroup.size} recompensas",
                    description = summaryString
                )
            }

            val aggregated = ActivityItem(
                id = firstItem.id,
                user_id = firstItem.user_id,
                type_history = 2,
                material_type_id = null,
                points = totalPoints,
                reward_id = firstItem.reward_id,
                alliance_id = firstItem.alliance_id,
                created_at = lastItem.created_at,
                updated_at = lastItem.updated_at,
                scan_id = null,
                comerciant_id = null,
                description = null,
                quantity = currentGroup.size,
                alliance = lastItem.alliance,
                material_type = null,
                reward = aggregatedReward ?: firstItem.reward,
                scan = null
            )
            result.add(aggregated)
            currentGroup.clear()
        }

        for (item in sorted) {
            val isReward = item.type_history == 2
            val sameAlliance = item.alliance_id == allianceId
            if (isReward && sameAlliance) {
                if (currentGroup.isEmpty()) {
                    currentGroup.add(item)
                } else {
                    val lastTime = parseToMillis(currentGroup.last().created_at)
                    val thisTime = parseToMillis(item.created_at)
                    val sameUser = item.user_id == currentGroup.last().user_id
                    if (sameUser && (lastTime - thisTime).let { if (it < 0) -it else it } <= groupWindowMs) {
                        currentGroup.add(item)
                    } else {
                        flushGroup()
                        currentGroup.add(item)
                    }
                }
            } else {
                // Si entramos a otra actividad, vaciar grupo actual
                flushGroup()
                result.add(item)
            }
        }
        flushGroup()
        // Mantener el orden descendente por fecha
        return result.sortedByDescending { parseToMillis(it.created_at) }
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