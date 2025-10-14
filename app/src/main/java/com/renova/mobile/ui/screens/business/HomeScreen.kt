package com.renova.mobile.ui.screens.business

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.renova.mobile.R
import com.renova.mobile.ui.components.SectionHeader
import com.renova.mobile.ui.components.BusinessSectionHeader
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import com.renova.mobile.ui.components.SaleDetailModal
import com.renova.mobile.ui.components.SaleSummary
import com.renova.mobile.ui.components.SaleItem
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import com.renova.mobile.viewmodel.BusinessSaleViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.Button
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

@Composable
fun BusinessHomeScreen(onLogout: () -> Unit, vm: BusinessSaleViewModel = viewModel()) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val lastSaleSummary by vm.lastSaleSummary.collectAsState()
    var showDetail by remember { mutableStateOf(false) }

    // Muestra un resumen simple de la última venta (si existiera)
    Column(modifier = Modifier.fillMaxSize()) {
        BusinessSectionHeader(
            title = stringResource(id = R.string.bottom_nav_home),
            onLogout = onLogout,
            textColor = Color.White
        )
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopStart) {
            if (lastSaleSummary == null) {
                Text(text = "No hay ventas recientes", modifier = Modifier.padding(16.dp))
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "Última venta", fontWeight = FontWeight.Bold)
                        Text(text = "ID: ${lastSaleSummary!!.id}")
                        Text(text = "Comercio: ${lastSaleSummary!!.allianceName ?: "-"}")
                        Text(text = "Total de puntos: ${lastSaleSummary!!.totalPoints}")
                        Button(onClick = { showDetail = true }) {
                            Text("Ver detalle")
                        }
                    }
                }
            }
        }
    }

    if (showDetail && lastSaleSummary != null) {
        SaleDetailModal(
            summary = lastSaleSummary!!,
            onClose = { showDetail = false },
            onPrint = {
                scope.launch {
                    Toast.makeText(context, "Imprimiendo...", Toast.LENGTH_SHORT).show()
                    delay(1000)
                    Toast.makeText(context, "Ticket impreso", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}

