package com.renova.mobile.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.platform.LocalContext
import com.renova.mobile.ui.theme.PoppinsFontFamily
import com.renova.mobile.ui.theme.RenovaColors
import com.renova.mobile.R
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.renova.mobile.viewmodel.BusinessSaleViewModel

// Modelo de detalle de compra reutilizable
data class SaleItem(
    val name: String,
    val quantity: Int,
    val pointsRequired: Int
)

data class SaleSummary(
    val id: String,
    val allianceName: String?,
    val consumerName: String?,
    val totalPoints: Int,
    val items: List<SaleItem>
)

@Composable
fun SaleDetailModal(
    viewModel: BusinessSaleViewModel,
    onClose: () -> Unit,
    onPrint: () -> Unit,
) {
    val context = LocalContext.current

    // ✅ Obtener datos directamente del ViewModel
    val summary by viewModel.lastSaleSummary.collectAsState()

    // Log de debug
    android.util.Log.d("SaleDetailModal", "=== Datos del ViewModel ===")
    android.util.Log.d("SaleDetailModal", "Summary: $summary")
    android.util.Log.d("SaleDetailModal", "Consumer: ${summary?.consumerName}")
    android.util.Log.d("SaleDetailModal", "Alliance: ${summary?.allianceName}")
    android.util.Log.d("SaleDetailModal", "Points: ${summary?.totalPoints}")
    android.util.Log.d("SaleDetailModal", "Items: ${summary?.items?.size}")

    if (summary == null) {
        // Si no hay datos, mostrar error y cerrar
        Dialog(onDismissRequest = onClose) {
            Card(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No hay datos de venta disponibles",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onClose) {
                        Text("Cerrar")
                    }
                }
            }
        }
        return
    }

    Dialog(onDismissRequest = onClose) {
        Card(
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Encabezado con botón de cierre
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = context.getString(R.string.purchase_detail),
                        color = RenovaColors.Primary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = PoppinsFontFamily,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Cerrar",
                            tint = RenovaColors.Primary
                        )
                    }
                }

                // Hora de la venta
                val timeText = runCatching {
                    val millis = summary!!.id.toLong()
                    val fmt = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                    fmt.format(java.util.Date(millis))
                }.getOrNull()

                if (timeText != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Hora: $timeText",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = PoppinsFontFamily),
                        color = Color.DarkGray
                    )
                }

                // Resumen general
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Mostrar alianza
                    summary!!.allianceName?.let { alliance ->
                        if (alliance.isNotBlank() && alliance != "N/A") {
                            Text(
                                text = "Comercio: $alliance",
                                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = PoppinsFontFamily),
                                color = RenovaColors.Primary
                            )
                        }
                    }

                    // ✅ Mostrar nombre del cliente
                    summary!!.consumerName?.let { consumer ->
                        if (consumer.isNotBlank()) {
                            Text(
                                text = "Cliente: $consumer",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = RenovaColors.Primary
                            )
                            android.util.Log.d("SaleDetailModal", "✅ Mostrando cliente: $consumer")
                        } else {
                            android.util.Log.w("SaleDetailModal", "⚠️ Consumer name está vacío")
                        }
                    } ?: run {
                        android.util.Log.w("SaleDetailModal", "⚠️ Consumer name es null")
                    }

                    // Total de puntos
                    Text(
                        text = "Total: ${summary!!.totalPoints} puntos",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold
                        ),
                        color = RenovaColors.Primary
                    )
                }

                HorizontalDivider()

                // Lista de items
                if (summary!!.items.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 280.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(summary!!.items) { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = Color.Black
                                    )
                                    Text(
                                        text = "${item.pointsRequired} puntos c/u",
                                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = PoppinsFontFamily),
                                        color = Color.DarkGray
                                    )
                                }
                                Text(
                                    text = "x${item.quantity}",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = RenovaColors.Primary
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        text = "No hay items para mostrar",
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = PoppinsFontFamily),
                        color = Color.Gray,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                // Botones de acción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onPrint,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = RenovaColors.Primary
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, RenovaColors.Primary)
                    ) {
                        Text(text = context.getString(R.string.print_ticket))
                    }
                    Button(
                        onClick = onClose,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RenovaColors.Primary,
                            contentColor = Color.White
                        )
                    ) {
                        Text(text = context.getString(R.string.action_cancel))
                    }
                }
            }
        }
    }
}