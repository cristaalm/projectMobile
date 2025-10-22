package com.renova.mobile.ui.screens.business.payments

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.renova.mobile.ui.components.BusinessSectionHeader
import com.renova.mobile.ui.theme.PoppinsFontFamily
import com.renova.mobile.ui.theme.RenovaColors
import java.text.NumberFormat
import java.util.Locale

private const val POINT_TO_MXN = 0.10

@Composable
fun PointsCashoutScreen(onLogout: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        BusinessSectionHeader(
            title = "Cobrar Puntos",
            onLogout = onLogout,
            textColor = Color.White
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Conversión de puntos a MXN",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold
                        ),
                        color = RenovaColors.Primary
                    )
                    Text(
                        text = "1 punto = $${POINT_TO_MXN} MXN",
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = PoppinsFontFamily),
                        color = Color.DarkGray
                    )

                    var pointsText by remember { mutableStateOf("") }
                    OutlinedTextField(
                        value = pointsText,
                        onValueChange = { input ->
                            // Permitir solo números
                            pointsText = input.filter { it.isDigit() }
                        },
                        label = { Text("Puntos a cobrar") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    val points = pointsText.toIntOrNull() ?: 0
                    val pesos = points * POINT_TO_MXN
                    val pesosFormatted = NumberFormat.getCurrencyInstance(Locale("es", "MX")).format(pesos)

                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Equivalente: $pesosFormatted",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = RenovaColors.Primary
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Button(
                            onClick = { /* TODO: Integrar flujo de cobro */ },
                            enabled = points > 0,
                            colors = ButtonDefaults.buttonColors(containerColor = RenovaColors.Primary)
                        ) {
                            Text(
                                text = "Generar cobro",
                                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = PoppinsFontFamily),
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}