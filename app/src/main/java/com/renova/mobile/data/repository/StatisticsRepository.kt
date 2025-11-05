package com.renova.mobile.data.repository

import com.renova.mobile.data.model.*
import com.renova.mobile.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log

sealed class StatisticsResult<out T> {
    data class Success<T>(val data: T) : StatisticsResult<T>()
    data class Error(val message: String, val code: Int? = null) : StatisticsResult<Nothing>()
    object Loading : StatisticsResult<Nothing>()
}

class StatisticsRepository {
    private val apiService = ApiClient.apiService

    suspend fun getAllianceStats(allianceId: Int): StatisticsResult<AllianceStatsData> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getAllianceStats(allianceId)

                when {
                    response.isSuccessful && response.body()?.success == true -> {
                        response.body()?.data?.let {
                            StatisticsResult.Success(it)
                        } ?: StatisticsResult.Error("No se recibieron datos", response.code())
                    }
                    response.code() == 404 -> {
                        StatisticsResult.Error("Alianza no encontrada", 404)
                    }
                    response.code() == 500 -> {
                        StatisticsResult.Error("Error interno del servidor", 500)
                    }
                    else -> {
                        val errorMsg = response.body()?.message ?: "Error desconocido"
                        StatisticsResult.Error(errorMsg, response.code())
                    }
                }
            } catch (e: Exception) {
                Log.e("StatisticsRepository", "Error getAllianceStats", e)
                StatisticsResult.Error("Error de conexión: ${e.message}")
            }
        }
    }

    suspend fun getActivityByDayOfWeek(allianceId: Int): StatisticsResult<ActivityByDayData> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getActivityByDayOfWeek(allianceId)

                when {
                    response.isSuccessful && response.body()?.success == true -> {
                        response.body()?.data?.let {
                            StatisticsResult.Success(it)
                        } ?: StatisticsResult.Error("No se recibieron datos", response.code())
                    }
                    response.code() == 404 -> {
                        StatisticsResult.Error("El comercio no existe", 404)
                    }
                    response.code() == 500 -> {
                        StatisticsResult.Error("Error al obtener actividad", 500)
                    }
                    else -> {
                        val errorMsg = response.body()?.message ?: "Error desconocido"
                        StatisticsResult.Error(errorMsg, response.code())
                    }
                }
            } catch (e: Exception) {
                Log.e("StatisticsRepository", "Error getActivityByDayOfWeek", e)
                StatisticsResult.Error("Error de conexión: ${e.message}")
            }
        }
    }

    suspend fun getTopRewards(allianceId: Int): StatisticsResult<List<TopReward>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getTopRewards(allianceId)

                when {
                    response.isSuccessful && response.body()?.success == true -> {
                        // API retorna [[{...}]], tomamos la primera lista
                        val rewards = response.body()?.data?.firstOrNull() ?: emptyList()
                        StatisticsResult.Success(rewards)
                    }
                    response.code() == 404 -> {
                        StatisticsResult.Error("La alianza no existe", 404)
                    }
                    response.code() == 500 -> {
                        StatisticsResult.Error("Error al obtener recompensas", 500)
                    }
                    else -> {
                        val errorMsg = response.body()?.message ?: "Error desconocido"
                        StatisticsResult.Error(errorMsg, response.code())
                    }
                }
            } catch (e: Exception) {
                Log.e("StatisticsRepository", "Error getTopRewards", e)
                StatisticsResult.Error("Error de conexión: ${e.message}")
            }
        }
    }
}