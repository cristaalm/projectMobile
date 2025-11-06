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
    summary: SaleSummary,
    onClose: () -> Unit,
    onPrint: () -> Unit,
) {
    val context = LocalContext.current
    Dialog(onDismissRequest = onClose) {
        Card(shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Encabezado con botón de cierre arriba a la derecha
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = context.getString(   R.string.purchase_detail),
                        color = RenovaColors.Primary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = PoppinsFontFamily,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onClose) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = "Cerrar", tint = RenovaColors.Primary)
                    }
                }

                // Hora de la venta debajo del título
                val timeText = runCatching {
                    val millis = summary.id.toLong()
                    val fmt = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                    fmt.format(java.util.Date(millis))
                }.getOrNull()
                if (timeText != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = context.getString(R.string.hist_detail_time_label, timeText),
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = PoppinsFontFamily),
                        color = Color.DarkGray
                    )
                }

                // Resumen general
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Eliminado: ID de la venta
                    if (summary.allianceName != null) {
                        Text(
                            text = context.getString(R.string.hist_detail_business_label, summary.allianceName),
                            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = PoppinsFontFamily),
                            color = RenovaColors.Primary
                        )
                    }
                    if (summary.consumerName != null) {
                        Text(
                            text = context.getString(R.string.hist_detail_client_label, summary.consumerName),
                            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = PoppinsFontFamily),
                            color = RenovaColors.Primary
                        )
                    }
                    Text(
                        text = context.getString(R.string.total_points_label, summary.totalPoints),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = RenovaColors.Primary
                    )
                }

                Divider()

                // Lista de items
                LazyColumn(modifier = Modifier.heightIn(max = 280.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(summary.items) { item ->
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = item.name, style = MaterialTheme.typography.bodyMedium.copy(fontFamily = PoppinsFontFamily, fontWeight = FontWeight.SemiBold), color = Color.Black)
                                Text(text = context.getString(R.string.points_required, item.pointsRequired), style = MaterialTheme.typography.bodySmall.copy(fontFamily = PoppinsFontFamily), color = Color.DarkGray)
                            }
                            Text(text = "x${item.quantity}", style = MaterialTheme.typography.titleSmall.copy(fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold), color = RenovaColors.Primary)
                        }
                    }
                }

                // Botón imprimir
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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