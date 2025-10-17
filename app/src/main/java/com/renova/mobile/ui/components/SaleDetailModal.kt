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
import com.renova.mobile.ui.theme.PoppinsFontFamily
import com.renova.mobile.ui.theme.RenovaColors

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
    Dialog(onDismissRequest = onClose) {
        Card(shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Encabezado con botón de cierre arriba a la derecha
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Detalle de compra",
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

                // Resumen general
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Eliminado: ID de la venta
                    if (summary.allianceName != null) {
                        Text(
                            text = "Comercio: ${summary.allianceName}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = PoppinsFontFamily)
                        )
                    }
                    if (summary.consumerName != null) {
                        Text(
                            text = "Cliente: ${summary.consumerName}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = PoppinsFontFamily)
                        )
                    }
                    Text(
                        text = "Total puntos: ${summary.totalPoints}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                Divider()

                // Lista de items
                LazyColumn(modifier = Modifier.heightIn(max = 280.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(summary.items) { item ->
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = item.name, style = MaterialTheme.typography.bodyMedium.copy(fontFamily = PoppinsFontFamily, fontWeight = FontWeight.SemiBold), color = Color.Black)
                                Text(text = "Puntos requeridos: ${item.pointsRequired}", style = MaterialTheme.typography.bodySmall.copy(fontFamily = PoppinsFontFamily), color = Color.DarkGray)
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
                        Text(text = "Imprimir")
                    }
                    Button(
                        onClick = onClose,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RenovaColors.Primary,
                            contentColor = Color.White
                        )
                    ) {
                        Text(text = "Cancelar")
                    }
                }
            }
        }
    }
}