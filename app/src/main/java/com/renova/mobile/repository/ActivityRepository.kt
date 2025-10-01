package com.renova.mobile.repository

import android.util.Log
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
        Log.d("ActivityRepository", "=== REQUEST DEBUG ===")
        Log.d("ActivityRepository", "page: $page")
        Log.d("ActivityRepository", "perPage: $perPage")
        Log.d("ActivityRepository", "key: $key")
        Log.d("ActivityRepository", "order: $order")

        val response = ApiClient.apiService.getHistory(
            page = page,
            perPage = perPage,
            key = key,
            order = order
        )

        Log.d("ActivityRepository", "Response Code: ${response.code()}")
        Log.d("ActivityRepository", "Response Message: ${response.message()}")

        return if (response.isSuccessful) {
            response.body() ?: throw Exception("Response body is null")
        } else {
            val errorBody = response.errorBody()?.string()
            Log.e("ActivityRepository", "Error Body: $errorBody")
            throw Exception("Error ${response.code()}: ${response.message()}. Body: $errorBody")
        }
    }

    suspend fun getTotalScans(): TotalScansResponse {
        Log.d("ActivityRepository", "=== TOTAL SCANS REQUEST ===")

        val response = ApiClient.apiService.getTotalScans()

        Log.d("ActivityRepository", "Response Code: ${response.code()}")

        return if (response.isSuccessful) {
            response.body() ?: throw Exception("Response body is null")
        } else {
            val errorBody = response.errorBody()?.string()
            Log.e("ActivityRepository", "Error Body: $errorBody")
            throw Exception("Error ${response.code()}: ${response.message()}. Body: $errorBody")
        }
    }
}