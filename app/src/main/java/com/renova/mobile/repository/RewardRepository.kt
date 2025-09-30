package com.renova.mobile.repository

import com.renova.mobile.network.ApiClient
import com.renova.mobile.network.Reward
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RewardRepository {
    suspend fun getRewardsByAlliance(allianceId: Int): Result<List<Reward>> {
        return withContext(Dispatchers.IO) {
            try {
                // Llamamos a la nueva función del ApiService
                val response = ApiClient.apiService.getRewardsByAlliance(allianceId = allianceId)

                if (response.isSuccessful) {
                    val body = response.body()
                    // Extraemos la lista de la estructura de paginación
                    if (body?.success == true && body.data?.data != null) {
                        Result.success(body.data.data)
                    } else {
                        Result.failure(Exception(body?.message ?: "Respuesta de recompensas inválida"))
                    }
                } else {
                    Result.failure(Exception("Error HTTP: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}