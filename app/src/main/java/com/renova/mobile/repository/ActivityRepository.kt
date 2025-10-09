package com.renova.mobile.repository

import com.renova.mobile.network.ApiClient
import com.renova.mobile.network.HistoryResponse
import com.renova.mobile.network.TotalScansResponse
import com.renova.mobile.utils.SessionManager

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

    suspend fun getUserPoints(sessionManager: SessionManager): Int {
        //android.util.Log.d("ActivityRepo", "Iniciando getUserPoints()")

        val token = sessionManager.getAuthToken()
        //android.util.Log.d("ActivityRepo", "Token completo: $token")

        if (token == null) {
            //android.util.Log.e("ActivityRepo", "Token es null!")
            return 0
        }

        val cleanToken = token.removePrefix("Bearer ").trim()
        //android.util.Log.d("ActivityRepo", "Token sin Bearer: ${cleanToken.take(20)}...")

        val response = ApiClient.apiService.identifyUser(
            com.renova.mobile.network.IdentifyUserRequest(token = cleanToken)
        )

        /*android.util.Log.d("ActivityRepo", "Response code: ${response.code()}")
        android.util.Log.d("ActivityRepo", "Response successful: ${response.isSuccessful}")*/

        return if (response.isSuccessful) {
            val body = response.body()
            /*android.util.Log.d("ActivityRepo", "Response body success: ${body?.success}")
            android.util.Log.d("ActivityRepo", "Response body message: ${body?.message}")
            android.util.Log.d("ActivityRepo", "Data exists: ${body?.data != null}")
            android.util.Log.d("ActivityRepo", "User exists: ${body?.data?.user != null}")
            android.util.Log.d("ActivityRepo", "Total points: ${body?.data?.user?.total_points}")*/

            body?.data?.user?.total_points ?: 0
        } else {
            val errorBody = response.errorBody()?.string()
            /*android.util.Log.e("ActivityRepo", "Error ${response.code()}: ${response.message()}")
            android.util.Log.e("ActivityRepo", "Error body: $errorBody")*/
            0
        }
    }
}