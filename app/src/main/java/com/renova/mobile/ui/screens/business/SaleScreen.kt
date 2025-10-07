package com.renova.mobile.ui.screens.business

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.renova.mobile.ui.components.BusinessHeader
import com.renova.mobile.viewmodel.BusinessSaleViewModel
import com.renova.mobile.ui.theme.PoppinsFontFamily
import com.renova.mobile.ui.theme.RenovaColors

@Composable
fun BusinessStoreScreen(onLogout: () -> Unit, vm: BusinessSaleViewModel = viewModel()) {
    val user by vm.scannedUser.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        BusinessHeader(title = "Venta", onLogout = onLogout)

        if (user != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Nombre: ${user!!.name} ${user!!.last_name ?: ""}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color.Black
                    )
                    Text(
                        text = "Puntos disponibles: ${user!!.total_points}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = PoppinsFontFamily),
                        color = Color.Black
                    )
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(text = "Consumidor", style = MaterialTheme.typography.titleMedium.copy(fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold))
            }
        }

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Detalle de la venta (pendiente)")
        }
    }
}

