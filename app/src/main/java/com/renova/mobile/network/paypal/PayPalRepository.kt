package com.renova.mobile.network.paypal

import android.util.Base64
import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

class PayPalRepository {

    private val apiService = PayPalRetrofitClient.apiService
    private val gson = Gson()

    // https://developer.paypal.com/dashboard/applications/sandbox
    private val clientId = "AThNUpnQi_MZ9LRx8PxEOWohM-ltsTk8_YXt8gixL48zAOPz-fqLrIzW3wWx0ZHpxA8vC-I0H3OU1jUR"
    private val secret = "ENreADRRUNpj4Cr5qY6042JafhNRYLEPJ51jCZAOMJwVnP-L19I_eEORjZn5fRNMu9ZSRp9FnQLyRFeD"

    companion object {
        private const val TAG = "PayPalRepository"
        private const val MXN_TO_USD_RATE = 0.05 // Tasa aproximada MXN a USD (ajústala según necesites)
    }

    /**
     * Paso 1: Obtener token de autenticación
     * Este token expira en ~9 horas
     */
    private suspend fun getAccessToken(): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🔐 Solicitando token de acceso...")

            // Crear credenciales en formato Base64
            val credentials = "$clientId:$secret"
            val encodedCredentials = Base64.encodeToString(
                credentials.toByteArray(),
                Base64.NO_WRAP
            )
            val basicAuth = "Basic $encodedCredentials"

            val response = apiService.getAccessToken(basicAuth)

            if (response.isSuccessful && response.body() != null) {
                val token = response.body()!!.accessToken
                Log.d(TAG, "✅ Token obtenido exitosamente")
                Result.success(token)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "❌ Error al autenticar: ${response.code()} - $errorBody")
                Result.failure(Exception("Error de autenticación: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Excepción al autenticar: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Paso 2: Enviar dinero al comercio (PAYOUT)
     * Esto es lo que Renova usa para PAGARLE al comercio sus puntos
     *
     * @param recipientEmail Email de PayPal del comercio que RECIBE el dinero
     * @param amount Cantidad en MXN a transferir (se convertirá automáticamente a USD)
     * @param transactionId ID único de la transacción de Renova
     */
    suspend fun sendPayoutToMerchant(
        recipientEmail: String,
        amount: Double,
        transactionId: String
    ): Result<PayPalPayoutResponse> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "💸 Iniciando payout...")
            Log.d(TAG, "   📧 Destinatario: $recipientEmail")
            Log.d(TAG, "   💰 Monto MXN: $$amount MXN")

            // Convertir MXN a USD para PayPal
            val amountUSD = amount * MXN_TO_USD_RATE
            Log.d(TAG, "   💵 Monto USD: $${String.format("%.2f", amountUSD)} USD")
            Log.d(TAG, "   🆔 ID Transacción: $transactionId")

            // 1. Obtener token de acceso
            val tokenResult = getAccessToken()
            if (tokenResult.isFailure) {
                return@withContext Result.failure(
                    tokenResult.exceptionOrNull() ?: Exception("Error desconocido al autenticar")
                )
            }

            val accessToken = tokenResult.getOrNull()!!
            val bearerToken = "Bearer $accessToken"

            // 2. Crear el request de payout EN USD
            val payoutRequest = PayPalPayoutRequest(
                senderBatchHeader = SenderBatchHeader(
                    senderBatchId = "Renova-$transactionId",
                    emailSubject = "Renova - Has recibido tu cobro de puntos",
                    emailMessage = "Tu cobro de \$${String.format("%.2f", amount)} MXN (aprox. \$${String.format("%.2f", amountUSD)} USD) por puntos Renova ha sido procesado exitosamente."
                ),
                items = listOf(
                    PayoutItem(
                        recipientType = "EMAIL",
                        amount = PayoutAmount(
                            value = String.format(Locale.US, "%.2f", amountUSD),
                            currency = "USD"
                        ),
                        receiver = recipientEmail,
                        note = "Cobro de puntos Renova - ID: $transactionId",
                        senderItemId = transactionId
                    )
                )
            )

            Log.d(TAG, "📤 Enviando payout a PayPal...")

            // 3. Enviar el payout
            val response = apiService.createPayout(bearerToken, payoutRequest)

            if (response.isSuccessful && response.body() != null) {
                val payoutResponse = response.body()!!
                Log.d(TAG, "✅ Payout creado exitosamente!")
                Log.d(TAG, "   🆔 Batch ID: ${payoutResponse.batchHeader.payoutBatchId}")
                Log.d(TAG, "   📊 Estado: ${payoutResponse.batchHeader.batchStatus}")
                Result.success(payoutResponse)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "❌ Error al crear payout: ${response.code()}")
                Log.e(TAG, "   📄 Detalle: $errorBody")

                // Intentar parsear el error de PayPal
                try {
                    val error = gson.fromJson(errorBody, PayPalErrorResponse::class.java)
                    Result.failure(Exception("PayPal Error: ${error.message} (${error.name})"))
                } catch (e: Exception) {
                    Result.failure(Exception("Error al crear payout: ${response.code()} - $errorBody"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Excepción al crear payout: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Verificar el estado de un payout
     * Útil para confirmar que el dinero fue enviado
     */
    suspend fun getPayoutStatus(
        payoutBatchId: String
    ): Result<PayPalPayoutResponse> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🔍 Verificando estado del payout: $payoutBatchId")

            val tokenResult = getAccessToken()
            if (tokenResult.isFailure) {
                return@withContext Result.failure(tokenResult.exceptionOrNull()!!)
            }

            val accessToken = tokenResult.getOrNull()!!
            val bearerToken = "Bearer $accessToken"

            val response = apiService.getPayoutDetails(bearerToken, payoutBatchId)

            if (response.isSuccessful && response.body() != null) {
                val details = response.body()!!
                Log.d(TAG, "✅ Estado obtenido: ${details.batchHeader.batchStatus}")
                Result.success(details)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "❌ Error al obtener estado: ${response.code()} - $errorBody")
                Result.failure(Exception("Error al obtener estado: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Excepción al verificar estado: ${e.message}", e)
            Result.failure(e)
        }
    }
}

/**
 * NOTA IMPORTANTE SOBRE MONEDAS Y PAYOUTS:
 *
 * - En Sandbox, USD funciona universalmente sin restricciones de región
 * - MXN puede tener restricciones según tu ubicación en Sandbox
 * - La conversión MXN -> USD es aproximada (0.05 = 1 MXN ≈ 0.05 USD)
 * - En producción, verifica las tasas de cambio reales
 *
 * Estados de Payout:
 * - PENDING: El payout está siendo procesado
 * - PROCESSING: PayPal está procesando los pagos
 * - SUCCESS: Todos los pagos fueron exitosos
 * - DENIED: El payout fue rechazado (cuenta no válida, restricciones, etc.)
 *
 * En SANDBOX todos los payouts se aprueban automáticamente
 * En PRODUCCIÓN puede tardar 1-2 días hábiles en llegar el dinero
 */