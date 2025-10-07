package com.renova.mobile.repository

import com.renova.mobile.network.ApiClient
import com.renova.mobile.network.HistoryResponse
import com.renova.mobile.network.TotalScansResponse

class ActivityRepository {
    suspend fun getHistory(
        page: Int = 1,
        perPage: Int = 10,
        key: String? = null,
        order: String = "desc"
    ): HistoryResponse {

        val response = ApiClient.apiService.getHistory(
            page = page,
            perPage = perPage,
            key = key,
            order = order
        )

        return if (response.isSuccessful) {
            response.body() ?: throw Exception("Response body is null")
        } else {
            val errorBody = response.errorBody()?.string()
            throw Exception("Error ${response.code()}: ${response.message()}. Body: $errorBody")
        }
    }

    suspend fun getTotalScans(): TotalScansResponse {

        val response = ApiClient.apiService.getTotalScans()

        return if (response.isSuccessful) {
            response.body() ?: throw Exception("Response body is null")
        } else {
            val errorBody = response.errorBody()?.string()
            throw Exception("Error ${response.code()}: ${response.message()}. Body: $errorBody")
        }
    }

    suspend fun getUserPoints(): Int {
        // Usa la versión sin parámetros: el interceptor añadirá el Authorization de la sesión actual
        val response = ApiClient.apiService.identifyUser()

        return if (response.isSuccessful) {
            response.body()?.data?.total_points ?: 0
        } else {
            val errorBody = response.errorBody()?.string()
            throw Exception("Error ${response.code()}: ${response.message()}. Body: $errorBody")
        }
    }
}