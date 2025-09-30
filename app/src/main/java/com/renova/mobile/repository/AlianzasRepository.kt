package com.renova.mobile.repository

import com.renova.mobile.network.ApiClient
import com.renova.mobile.network.Alianza
import com.renova.mobile.network.TypeShop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AlianzasRepository {

    suspend fun getAllAlianzas(): Result<List<Alianza>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = ApiClient.apiService.getAllAlianzas(status = 1)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Result.success(body.data.data)
                    } else {
                        Result.failure(Exception(body?.message ?: "Respuesta inválida del servidor"))
                    }
                } else {
                    Result.failure(Exception("Error HTTP: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getTypeShops(): Result<List<TypeShop>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = ApiClient.apiService.getTypeShops()
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Result.success(body.data)
                    } else {
                        Result.failure(Exception(body?.message ?: "Respuesta de categorías inválida"))
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