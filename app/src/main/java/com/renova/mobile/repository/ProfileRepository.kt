package com.renova.mobile.data.repository

import android.content.Context
import android.util.Log
import com.renova.mobile.network.*
import com.renova.mobile.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import retrofit2.Response

class ProfileRepository(private val context: Context) {

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
                Log.d("Profile", "token: $cleanToken")
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

    suspend fun uploadDocuments(
        userId: Int,
        documentFront: MultipartBody.Part,
        documentBack: MultipartBody.Part
    ): Result<UploadDocumentsResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.uploadDocuments(
                userId = userId,
                document_front = documentFront,
                document_back = documentBack
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
            val token = sessionManager.getAuthToken()
            val response = apiService.getDocumentImage(type, userId)

            if (response.isSuccessful) {
                response.body()?.bytes()?.let { bytes ->
                    Result.success(bytes)
                } ?: run {
                    Result.failure(Exception("Imagen vacía"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // En tu archivo ProfileRepository (Document 2)

    suspend fun updateUserField(
        field: String,
        value: String
    ): Result<UpdateFieldResponse> = withContext(Dispatchers.IO) {
        try {
            val userId = sessionManager.getUserId()
            if (userId == null) {
                return@withContext Result.failure(Exception("No se encontró el ID del usuario"))
            }
            val request = UpdateFieldRequest(value = value)
            val response = apiService.updateUserField(field, userId, request)

            if (response.isSuccessful) {
                response.body()?.let {
                    if (it.success) {
                        Result.success(it)
                    } else {
                        Result.failure(Exception(it.message))
                    }
                } ?: Result.failure(Exception("Respuesta vacía"))
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resetVerificationStatus(
        userId: Int
    ): Result<UpdateFieldResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.toggleStatusPending(userId)

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

    suspend fun uploadSingleDocument(
        type: String,
        userId: Int,
        document: MultipartBody.Part
    ): Result<UploadDocumentsResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.uploadSingleDocument(type, userId, document)

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

    fun clearSession() {
        sessionManager.clearAuthToken()
    }
}