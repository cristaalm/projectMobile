package com.renova.mobile.ui.screens.business.payments

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import com.renova.mobile.ui.theme.PoppinsFontFamily
import com.renova.mobile.ui.theme.RenovaColors
import com.renova.mobile.ui.theme.LocalRenovaColors
import com.renova.mobile.network.paypal.PayPalRepository
import com.renova.mobile.network.ApiClient
import com.renova.mobile.utils.SessionManager
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

enum class WithdrawalStep {
    LOADING_POINTS,
    ACCOUNT_FORM,
    PROCESSING,
    SUCCESS
}

data class WithdrawalDetails(
    val transactionId: String,
    val businessId: Int,
    val userId: Int,
    val dateTime: String,
    val amount: Double,
    val method: String,
    val estimatedArrival: String,
    val paypalBatchId: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentSimulationModal(
    onDismiss: () -> Unit,
    onPaymentComplete: (String) -> Unit
) {
    val colors = LocalRenovaColors.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var currentStep by remember { mutableStateOf(WithdrawalStep.LOADING_POINTS) }
    var withdrawalDetails by remember { mutableStateOf<WithdrawalDetails?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    // Estado para los puntos obtenidos de la API
    var points by remember { mutableStateOf(0) }
    var loadingError by remember { mutableStateOf<String?>(null) }

    val paypalRepository = remember { PayPalRepository() }
    val sessionManager = remember { SessionManager(context) }

    val amountMXN = points * 0.10
    val amountFormatted = NumberFormat.getCurrencyInstance(Locale("es", "MX")).format(amountMXN)
    val minimumAmount = 50.0
    val canWithdraw = amountMXN >= minimumAmount

    // Obtener puntos del mes actual desde la API
    LaunchedEffect(Unit) {
        val user = sessionManager.getUser()
        val allianceId = user?.alliance_id

        android.util.Log.d("PaymentModal", "Usuario: $user")
        android.util.Log.d("PaymentModal", "Alliance ID: $allianceId")

        if (allianceId == null) {
            loadingError = "No se encontró el ID de alianza del usuario"
            Toast.makeText(context, loadingError, Toast.LENGTH_LONG).show()
            onDismiss()
            return@LaunchedEffect
        }

        try {
            // Calcular fechas del mes actual
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)

            val firstDay = Calendar.getInstance().apply {
                set(year, month, 1)
            }
            val lastDay = Calendar.getInstance().apply {
                set(year, month, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
            }

            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dateStart = dateFormat.format(firstDay.time)
            val dateEnd = dateFormat.format(lastDay.time)

            android.util.Log.d("PaymentModal", "Llamando API con:")
            android.util.Log.d("PaymentModal", "  Alliance ID: $allianceId")
            android.util.Log.d("PaymentModal", "  Date Start: $dateStart")
            android.util.Log.d("PaymentModal", "  Date End: $dateEnd")

            // Llamar al endpoint
            val response = ApiClient.apiService.getTotalPointsByShop(
                allianceId = allianceId,
                dateStart = dateStart,
                dateEnd = dateEnd
            )

            android.util.Log.d("PaymentModal", "Response code: ${response.code()}")
            android.util.Log.d("PaymentModal", "Response body: ${response.body()}")

            if (response.isSuccessful && response.body()?.success == true) {
                points = response.body()?.data?.total_points ?: 0
                currentStep = WithdrawalStep.ACCOUNT_FORM
                android.util.Log.d("PaymentModal", "Puntos obtenidos: $points")
            } else {
                val errorMsg = response.body()?.message ?: response.errorBody()?.string() ?: "Error al obtener puntos"
                loadingError = errorMsg
                android.util.Log.e("PaymentModal", "Error API: $errorMsg")
                Toast.makeText(context, loadingError, Toast.LENGTH_LONG).show()
                onDismiss()
            }
        } catch (e: Exception) {
            loadingError = "Error de conexión: ${e.message}"
            android.util.Log.e("PaymentModal", "Excepción: ${e.message}", e)
            Toast.makeText(context, loadingError, Toast.LENGTH_LONG).show()
            onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colors.cardBackground,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (currentStep) {
                        WithdrawalStep.LOADING_POINTS -> "Cargando información..."
                        WithdrawalStep.ACCOUNT_FORM -> "Retiro con PayPal"
                        WithdrawalStep.PROCESSING -> "Procesando solicitud"
                        WithdrawalStep.SUCCESS -> "¡Retiro exitoso!"
                    },
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold
                    ),
                    color = colors.textPrimary
                )
                if (currentStep == WithdrawalStep.ACCOUNT_FORM) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = colors.textSecondary
                        )
                    }
                }
            }

            // Content según el step
            when (currentStep) {
                WithdrawalStep.LOADING_POINTS -> {
                    LoadingPoints(colors = colors)
                }

                WithdrawalStep.ACCOUNT_FORM -> {
                    // Amount card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = RenovaColors.Primary.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Monto a recibir",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = PoppinsFontFamily
                                ),
                                color = colors.textSecondary
                            )
                            Text(
                                text = amountFormatted,
                                style = MaterialTheme.typography.displaySmall.copy(
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = if (canWithdraw) RenovaColors.Primary else RenovaColors.Warning
                            )
                            Text(
                                text = "${NumberFormat.getIntegerInstance(Locale("es","MX")).format(points)} puntos canjeados",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = PoppinsFontFamily
                                ),
                                color = colors.textSecondary
                            )

                            if (!canWithdraw) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "⚠️ Monto mínimo: ${NumberFormat.getCurrencyInstance(Locale("es", "MX")).format(minimumAmount)}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = RenovaColors.Warning,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    WithdrawalAccountForm(
                        colors = colors,
                        canWithdraw = canWithdraw,
                        onSubmit = { email ->
                            scope.launch {
                                try {
                                    isProcessing = true
                                    currentStep = WithdrawalStep.PROCESSING

                                    val transactionId = "WTH-${System.currentTimeMillis()}"
                                    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale("es", "MX"))

                                    val result = paypalRepository.sendPayoutToMerchant(
                                        recipientEmail = email,
                                        amount = amountMXN,
                                        transactionId = transactionId
                                    )

                                    if (result.isSuccess) {
                                        val payoutResponse = result.getOrNull()!!
                                        withdrawalDetails = WithdrawalDetails(
                                            transactionId = transactionId,
                                            businessId = sessionManager.getUser()?.alliance_id ?: 0,
                                            userId = sessionManager.getUserId() ?: 0,
                                            dateTime = dateFormat.format(Date()),
                                            amount = amountMXN,
                                            method = "PayPal",
                                            estimatedArrival = "1-2 días hábiles",
                                            paypalBatchId = payoutResponse.batchHeader.payoutBatchId
                                        )
                                        currentStep = WithdrawalStep.SUCCESS
                                    } else {
                                        val error = result.exceptionOrNull()
                                        val errorMessage = when {
                                            error?.message?.contains("PERMISSION_DENIED", ignoreCase = true) == true ->
                                                "Error: Permisos insuficientes. Verifica tu configuración de PayPal."
                                            error?.message?.contains("INVALID_REQUEST", ignoreCase = true) == true ->
                                                "Error: Solicitud inválida. Verifica el formato del email."
                                            error?.message?.contains("INSUFFICIENT_FUNDS", ignoreCase = true) == true ->
                                                "Error: Fondos insuficientes en la cuenta de Renova."
                                            else -> "Error PayPal: ${error?.message}"
                                        }
                                        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                                        currentStep = WithdrawalStep.ACCOUNT_FORM
                                        isProcessing = false
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(
                                        context,
                                        "Error inesperado: ${e.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    currentStep = WithdrawalStep.ACCOUNT_FORM
                                } finally {
                                    isProcessing = false
                                }
                            }
                        }
                    )
                }

                WithdrawalStep.PROCESSING -> {
                    ProcessingWithdrawal(colors = colors)
                }

                WithdrawalStep.SUCCESS -> {
                    withdrawalDetails?.let { details ->
                        WithdrawalSuccess(
                            withdrawalDetails = details,
                            colors = colors,
                            onDone = {
                                onPaymentComplete(details.transactionId)
                                onDismiss()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingPoints(
    colors: com.renova.mobile.ui.theme.RenovaColorScheme
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(64.dp),
            color = RenovaColors.Primary,
            strokeWidth = 6.dp
        )

        Text(
            text = "Obteniendo puntos del mes...",
            style = MaterialTheme.typography.titleLarge.copy(
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.SemiBold
            ),
            color = RenovaColors.Primary,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Conectando con el servidor...",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = PoppinsFontFamily
            ),
            color = colors.textSecondary
        )
    }
}

@Composable
private fun WithdrawalAccountForm(
    colors: com.renova.mobile.ui.theme.RenovaColorScheme,
    canWithdraw: Boolean,
    onSubmit: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // PayPal Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF0070BA).copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFF0070BA), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "P",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Column {
                    Text(
                        text = "PayPal",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold
                        ),
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Transferencia segura y rápida",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = PoppinsFontFamily
                        ),
                        color = colors.textSecondary
                    )
                }
            }
        }

        Text(
            text = "Ingresa tu cuenta PayPal",
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.SemiBold
            ),
            color = colors.textPrimary
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email de PayPal", color = colors.textSecondary) },
            placeholder = { Text("tu-email@example.com", color = colors.textSecondary.copy(alpha = 0.5f)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = {
                Icon(Icons.Default.Email, contentDescription = null, tint = colors.textSecondary)
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = RenovaColors.Primary,
                unfocusedBorderColor = colors.textSecondary.copy(alpha = 0.5f),
                focusedTextColor = colors.textPrimary,
                unfocusedTextColor = colors.textPrimary,
                cursorColor = RenovaColors.Primary
            )
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = RenovaColors.Info.copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = RenovaColors.Info,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "El dinero será transferido directamente a tu cuenta de PayPal.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = PoppinsFontFamily
                    ),
                    color = colors.textPrimary
                )
            }
        }

        Button(
            onClick = { onSubmit(email) },
            enabled = email.isNotEmpty() && canWithdraw,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = RenovaColors.Primary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Solicitar retiro",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Medium
                ),
                color = Color.White,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun ProcessingWithdrawal(
    colors: com.renova.mobile.ui.theme.RenovaColorScheme
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(64.dp),
            color = RenovaColors.Primary,
            strokeWidth = 6.dp
        )

        Text(
            text = "Procesando solicitud de retiro...",
            style = MaterialTheme.typography.titleLarge.copy(
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.SemiBold
            ),
            color = RenovaColors.Primary,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Conectando con PayPal...",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = PoppinsFontFamily
            ),
            color = colors.textSecondary
        )
    }
}

@Composable
private fun WithdrawalSuccess(
    withdrawalDetails: WithdrawalDetails,
    colors: com.renova.mobile.ui.theme.RenovaColorScheme,
    onDone: () -> Unit
) {
    val amountFormatted = NumberFormat.getCurrencyInstance(Locale("es", "MX")).format(withdrawalDetails.amount)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(RenovaColors.Success.copy(alpha = 0.1f), shape = RoundedCornerShape(40.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Éxito",
                tint = RenovaColors.Success,
                modifier = Modifier.size(56.dp)
            )
        }

        Text(
            text = "¡Solicitud de retiro exitosa!",
            style = MaterialTheme.typography.titleLarge.copy(
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Bold
            ),
            color = colors.textPrimary,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Tu dinero será transferido pronto",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = PoppinsFontFamily
            ),
            color = colors.textSecondary,
            textAlign = TextAlign.Center
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = colors.cardBackground
            ),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DetailRow("ID Transacción:", withdrawalDetails.transactionId, colors)

                withdrawalDetails.paypalBatchId?.let { batchId ->
                    DetailRow("PayPal Batch ID:", batchId, colors)
                }

                DetailRow("Fecha y hora:", withdrawalDetails.dateTime, colors)
                DetailRow("Cantidad:", amountFormatted, colors)
                DetailRow("Método:", withdrawalDetails.method, colors)

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = colors.textSecondary.copy(alpha = 0.2f)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Llegada estimada:",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Medium
                        ),
                        color = colors.textSecondary
                    )
                    Text(
                        text = withdrawalDetails.estimatedArrival,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold
                        ),
                        color = RenovaColors.Success
                    )
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = RenovaColors.Info.copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = RenovaColors.Info,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Recibirás una notificación cuando el dinero esté disponible en tu cuenta",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = PoppinsFontFamily
                    ),
                    color = colors.textPrimary
                )
            }
        }

        Button(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = RenovaColors.Primary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Entendido",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Medium
                ),
                color = Color.White,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    colors: com.renova.mobile.ui.theme.RenovaColorScheme
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Medium
            ),
            color = colors.textSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.SemiBold
            ),
            color = colors.textPrimary
        )
    }
}