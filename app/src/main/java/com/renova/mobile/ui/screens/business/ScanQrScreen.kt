package com.renova.mobile.ui.screens.business

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.renova.mobile.ui.components.BusinessHeader
import com.renova.mobile.viewmodel.BusinessSaleViewModel
import com.renova.mobile.ui.theme.PoppinsFontFamily
import com.renova.mobile.ui.theme.RenovaColors
import com.journeyapps.barcodescanner.CaptureActivity
import com.journeyapps.barcodescanner.ScanOptions
import com.journeyapps.barcodescanner.ScanContract
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun BusinessQRScreen(onLogout: () -> Unit, vm: BusinessSaleViewModel = viewModel()) {
    val context = LocalContext.current

    val scannedUser by vm.scannedUser.collectAsState()
    val isLoading by vm.isLoading.collectAsState()
    val error by vm.error.collectAsState()

    val launcher = rememberLauncherForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            vm.identifyUserByToken(result.contents)
        } else {
            // No se obtuvo contenido del escaneo
            // Mostrar un mensaje amigable
        }
    }

    fun startScanner() {
        val options = ScanOptions().apply {
            setDesiredBarcodeFormats(ScanOptions.QR_CODE, ScanOptions.CODE_128, ScanOptions.CODE_39, ScanOptions.EAN_13, ScanOptions.EAN_8)
            setPrompt("Escanea el código")
            setBeepEnabled(true)
            setOrientationLocked(false)
            captureActivity = CaptureActivity::class.java
        }
        launcher.launch(options)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        BusinessHeader(title = "QR", onLogout = onLogout)

        // Botón para iniciar el escaneo
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Button(onClick = { startScanner() }) {
                Text(text = "Iniciar escaneo")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Card de datos del cliente (similar estilo a InstructionSection)
        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = RenovaColors.Primary)
            }
        }

        if (error != null) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(text = error ?: "", color = Color.Red)
            }
        }

        if (scannedUser != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .heightIn(max = 180.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (androidx.compose.foundation.isSystemInDarkTheme()) Color.Black else RenovaColors.Light.Surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Consumidor",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = RenovaColors.Primary,
                    )

                    // Nombre y puntos actuales
                    Text(
                        text = "Nombre: ${scannedUser!!.name} ${scannedUser!!.last_name ?: ""}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = PoppinsFontFamily)
                    )
                    Text(
                        text = "Puntos disponibles: ${scannedUser!!.total_points}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = PoppinsFontFamily)
                    )
                }
            }
        }
    }
}

