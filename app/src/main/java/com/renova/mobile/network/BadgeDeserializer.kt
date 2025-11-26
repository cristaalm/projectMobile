package com.renova.mobile.network

import android.content.Context
import android.util.Log
import com.google.gson.*
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.renova.mobile.utils.SessionManager
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit

/**
 * Deserializador robusto para el campo 'badge' que maneja múltiples formatos:
 * - Array de enteros: [1, 2, 3]
 * - Objeto vacío: {}
 * - null
 * - String vacío: ""
 */
class BadgeCollectionDeserializer : JsonDeserializer<BadgeCollection?> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): BadgeCollection? {
        try {
            return when {
                json == null || json.isJsonNull -> {
                    android.util.Log.d("BadgeDeserializer", "Badge is null")
                    emptyList()
                }

                // Caso 1: Es un array [1, 2, 3]
                json.isJsonArray -> {
                    android.util.Log.d("BadgeDeserializer", "Badge is array: ${json.asJsonArray}")
                    json.asJsonArray.mapNotNull { element ->
                        try {
                            when {
                                element.isJsonPrimitive && element.asJsonPrimitive.isNumber -> {
                                    element.asInt
                                }
                                else -> null
                            }
                        } catch (e: Exception) {
                            android.util.Log.w("BadgeDeserializer", "Error parsing badge element: $element", e)
                            null
                        }
                    }
                }

                // Caso 2: Es un objeto {} (común cuando no hay badges)
                json.isJsonObject -> {
                    android.util.Log.d("BadgeDeserializer", "Badge is empty object: ${json.asJsonObject}")
                    emptyList()
                }

                // Caso 3: Es un string vacío ""
                json.isJsonPrimitive && json.asJsonPrimitive.isString -> {
                    android.util.Log.d("BadgeDeserializer", "Badge is empty string")
                    emptyList()
                }

                // Caso 4: Fallback
                else -> {
                    android.util.Log.w("BadgeDeserializer", "Unknown badge format: $json")
                    emptyList()
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("BadgeDeserializer", "Critical error deserializing badge", e)
            return emptyList()
        }
    }
}

/**
 * Deserializador robusto para ClaimBadgeResponse que maneja múltiples formatos de 'data':
 * - Objeto: { user: {...}, badge: {...} }
 * - Array: [1, 2, 3] (lista de badge IDs)
 * - null
 */
class ClaimBadgeResponseDeserializer : JsonDeserializer<ClaimBadgeResponse> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): ClaimBadgeResponse {
        try {
            if (json == null || !json.isJsonObject) {
                return ClaimBadgeResponse(
                    success = false,
                    message = "Invalid response format",
                    data = null,
                    errors = null,
                    code = 500
                )
            }

            val jsonObject = json.asJsonObject

            val success = jsonObject.get("success")?.asBoolean ?: false
            val message = jsonObject.get("message")?.asString ?: ""
            val code = jsonObject.get("code")?.asInt ?: 500
            val errors = jsonObject.get("errors")

            // Intentar parsear 'data' de forma segura
            val data: ClaimBadgeData? = try {
                val dataElement = jsonObject.get("data")

                when {
                    dataElement == null || dataElement.isJsonNull -> {
                        Log.d("ClaimBadgeDeserializer", "data is null")
                        null
                    }

                    // CASO 1: data es un OBJETO { user: {...}, badge: {...} }
                    dataElement.isJsonObject -> {
                        Log.d("ClaimBadgeDeserializer", "data is object, parsing normally")
                        context?.deserialize(dataElement, ClaimBadgeData::class.java)
                    }

                    // CASO 2: data es un ARRAY [1, 2, 3] (solo IDs de badges)
                    dataElement.isJsonArray -> {
                        Log.w("ClaimBadgeDeserializer", "⚠️ data is array (unexpected format): ${dataElement.asJsonArray}")
                        // No podemos construir ClaimBadgeData sin el usuario completo
                        null
                    }

                    else -> {
                        Log.w("ClaimBadgeDeserializer", "⚠️ Unknown data format: $dataElement")
                        null
                    }
                }
            } catch (e: Exception) {
                Log.e("ClaimBadgeDeserializer", "Error parsing data field", e)
                null
            }

            return ClaimBadgeResponse(
                success = success,
                message = message,
                data = data,
                errors = errors,
                code = code
            )

        } catch (e: Exception) {
            Log.e("ClaimBadgeDeserializer", "Critical error deserializing ClaimBadgeResponse", e)
            return ClaimBadgeResponse(
                success = false,
                message = "Deserialization error: ${e.message}",
                data = null,
                errors = null,
                code = 500
            )
        }
    }
}