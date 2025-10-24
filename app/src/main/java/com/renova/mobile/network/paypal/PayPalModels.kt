package com.renova.mobile.network.paypal

import com.google.gson.annotations.SerializedName

// ============================================
// MODELOS PARA AUTENTICACIÓN
// ============================================

data class PayPalAuthResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String,
    @SerializedName("expires_in") val expiresIn: Int
)

// ============================================
// MODELOS PARA ENVIAR DINERO (PAYOUT)
// ============================================

data class PayPalPayoutRequest(
    @SerializedName("sender_batch_header") val senderBatchHeader: SenderBatchHeader,
    @SerializedName("items") val items: List<PayoutItem>
)

data class SenderBatchHeader(
    @SerializedName("sender_batch_id") val senderBatchId: String,
    @SerializedName("email_subject") val emailSubject: String = "Cobro de puntos Renova",
    @SerializedName("email_message") val emailMessage: String = "Tu cobro de puntos ha sido procesado exitosamente"
)

data class PayoutItem(
    @SerializedName("recipient_type") val recipientType: String = "EMAIL",
    @SerializedName("amount") val amount: PayoutAmount,
    @SerializedName("receiver") val receiver: String, // Email del comercio que RECIBE
    @SerializedName("note") val note: String = "Cobro de puntos Renova",
    @SerializedName("sender_item_id") val senderItemId: String
)

data class PayoutAmount(
    @SerializedName("value") val value: String, // Monto en string: "150.00"
    @SerializedName("currency") val currency: String = "MXN"
)

// ============================================
// RESPUESTA DE PAYPAL
// ============================================

data class PayPalPayoutResponse(
    @SerializedName("batch_header") val batchHeader: BatchHeader,
    @SerializedName("links") val links: List<PayPalLink>?
)

data class BatchHeader(
    @SerializedName("payout_batch_id") val payoutBatchId: String,
    @SerializedName("batch_status") val batchStatus: String,
    @SerializedName("sender_batch_header") val senderBatchHeader: SenderBatchHeader
)

data class PayPalLink(
    @SerializedName("href") val href: String,
    @SerializedName("rel") val rel: String,
    @SerializedName("method") val method: String
)

// ============================================
// ERROR RESPONSE
// ============================================

data class PayPalErrorResponse(
    @SerializedName("name") val name: String?,
    @SerializedName("message") val message: String?,
    @SerializedName("debug_id") val debugId: String?
)