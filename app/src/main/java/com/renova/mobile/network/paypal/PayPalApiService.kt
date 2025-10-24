package com.renova.mobile.network.paypal

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

// ============================================
// RETROFIT SERVICE INTERFACE
// ============================================

interface PayPalApiService {

    /**
     * Obtener token de acceso OAuth2
     * Este token es necesario para todas las demás llamadas
     */
    @FormUrlEncoded
    @POST("v1/oauth2/token")
    @Headers("Accept: application/json")
    suspend fun getAccessToken(
        @Header("Authorization") basicAuth: String,
        @Field("grant_type") grantType: String = "client_credentials"
    ): Response<PayPalAuthResponse>

    /**
     * Crear payout - ENVIAR DINERO al comercio
     * Esto es lo que Renova usa para PAGARLE al comercio
     */
    @POST("v1/payments/payouts")
    @Headers("Content-Type: application/json")
    suspend fun createPayout(
        @Header("Authorization") bearerToken: String,
        @Body payoutRequest: PayPalPayoutRequest
    ): Response<PayPalPayoutResponse>

    /**
     * Obtener detalles de un payout específico
     * Para verificar el estado de la transferencia
     */
    @GET("v1/payments/payouts/{payout_batch_id}")
    suspend fun getPayoutDetails(
        @Header("Authorization") bearerToken: String,
        @Path("payout_batch_id") payoutBatchId: String
    ): Response<PayPalPayoutResponse>
}

// ============================================
// RETROFIT CLIENT SINGLETON
// ============================================

object PayPalRetrofitClient {

    // URL SANDBOX - Para pruebas (NO cobra dinero real)
    private const val SANDBOX_BASE_URL = "https://api-m.sandbox.paypal.com/"

    // URL PRODUCCIÓN - Para cuando vayas en vivo (COBRA DINERO REAL)
    // private const val PRODUCTION_BASE_URL = "https://api-m.paypal.com/"

    // Logger para ver las peticiones en Logcat
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Cliente HTTP con timeouts
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // Instancia de Retrofit
    private val retrofit = Retrofit.Builder()
        .baseUrl(SANDBOX_BASE_URL) // ⚠️ Cambiar a PRODUCTION cuando vayas en vivo
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // Service listo para usar
    val apiService: PayPalApiService = retrofit.create(PayPalApiService::class.java)
}