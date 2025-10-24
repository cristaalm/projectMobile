package com.renova.mobile.repository

import android.content.Context
import com.renova.mobile.network.*
import com.renova.mobile.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class BusinessProfileRepository(private val context: Context) {

    private val sessionManager = SessionManager(context)
    private val apiService = ApiClient.apiService

    /**
     * Obtiene el perfil completo del usuario con información de verificación
     */
    suspend fun getProfile(): Result<IdentifyUserResponse> = withContext(Dispatchers.IO) {
        try {
            val token = sessionManager.getAuthToken()

            if (token.isNullOrEmpty()) {
                return@withContext Result.failure(Exception("No hay token de autenticación"))
            }

            val cleanToken = token.replace("Bearer ", "")

            val request = IdentifyUserRequest(
                token = cleanToken,
                with_identity = true
            )

            val response: Response<IdentifyUserResponse> = apiService.identifyUser(request)

            if (response.isSuccessful) {
                response.body()?.let {
                    if (it.success) {
                        Result.success(it)
                    } else {
                        Result.failure(Exception(it.message))
                    }
                } ?: Result.failure(Exception("Respuesta vacía"))
            } else {
                Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRewardsByAlliance(
        allianceId: Int
    ): Result<RewardResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getRewardsByAlliance(
                allianceId = allianceId,
                isActive = 1
            )

            if (response.isSuccessful) {
                response.body()?.let {
                    if (it.success) {
                        Result.success(it)
                    } else {
                        Result.failure(Exception(it.message))
                    }
                } ?: Result.failure(Exception("Respuesta vacía"))
            } else {
                Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDocumentImage(
        type: String,
        userId: Int
    ): Result<ByteArray> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getDocumentImage(type, userId)

            if (response.isSuccessful) {
                response.body()?.bytes()?.let { bytes ->
                    Result.success(bytes)
                } ?: run {
                    Result.failure(Exception("Imagen vacía"))
                }
            } else {
                Result.failure(Exception("Error al obtener imagen: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun clearSession() {
        sessionManager.clearAuthToken()
    }
}