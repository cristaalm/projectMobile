package com.renova.mobile.data.repository

import android.content.Context
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

    /**
     * Sube los documentos INE (frente y reverso)
     * Usa el endpoint existente de uploadDocuments
     */
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

    /**
     * Sube la selfie del usuario
     * Usa el endpoint existente de uploadSelfie
     */
    suspend fun uploadSelfie(
        userId: Int,
        selfie: MultipartBody.Part
    ): Result<UploadSelfieResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.uploadSelfie(
                userId = userId,
                selfie = selfie
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

    /**
     * TODO: Método para actualizar email cuando el endpoint esté disponible
     */
    suspend fun updateEmail(newEmail: String): Result<Any> = withContext(Dispatchers.IO) {
        try {
            // TODO: Implementar cuando el endpoint esté listo
            // val response = apiService.updateEmail(UpdateEmailRequest(email = newEmail))
            // ... manejar respuesta

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * TODO: Método para actualizar teléfono cuando el endpoint esté disponible
     */
    suspend fun updatePhone(newPhone: String): Result<Any> = withContext(Dispatchers.IO) {
        try {
            // TODO: Implementar cuando el endpoint esté listo
            // val response = apiService.updatePhone(UpdatePhoneRequest(phone = newPhone))
            // ... manejar respuesta

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun clearSession() {
        sessionManager.clearAuthToken()
    }
}