package com.renova.mobile.repository

import com.renova.mobile.network.ApiClient
import com.renova.mobile.network.HistoryResponse
import com.renova.mobile.network.TotalScansResponse
import com.renova.mobile.utils.SessionManager
import android.util.Log

class ActivityRepository {
    suspend fun getHistory(
        page: Int = 1,
        perPage: Int = 10,
        key: String = "created_at",
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

    suspend fun getUserPoints(sessionManager: SessionManager): Int {
        val token = sessionManager.getAuthToken()

        if (token == null) {
            return 0
        }

        Log.d("Tokenn", "Token: ${token}")

        val cleanToken = token.removePrefix("Bearer ").trim()

        val response = ApiClient.apiService.identifyUser(
            com.renova.mobile.network.IdentifyUserRequest(token = cleanToken)
        )

        return if (response.isSuccessful) {
            val body = response.body()
            val points = body?.data?.user?.total_points ?: 0
            Log.d("UserPoints", "total_points: $points")
            points
        } else {
            0
        }
    }
}