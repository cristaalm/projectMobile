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
                val allAlianzas = mutableListOf<Alianza>()
                var currentPage = 1
                var hasMorePages = true

                while (hasMorePages) {
                    val response = ApiClient.apiService.getAllAlianzas(
                        status = 1,
                        page = currentPage,
                        per_page = 100 // Máximo permitido para obtener más datos por página
                    )

                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body?.success == true && body.data != null) {
                            val pageAlianzas = body.data.data
                            allAlianzas.addAll(pageAlianzas)

                            // Verificar si hay más páginas
                            val lastPage = body.data.last_page
                            hasMorePages = currentPage < lastPage
                            currentPage++

                        } else {
                            return@withContext Result.failure(Exception(body?.message ?: "Respuesta inválida del servidor"))
                        }
                    } else {
                        return@withContext Result.failure(Exception("Error HTTP: ${response.code()}"))
                    }
                }

                Result.success(allAlianzas)
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