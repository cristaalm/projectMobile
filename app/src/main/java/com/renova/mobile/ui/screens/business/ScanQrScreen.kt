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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.stringResource
import com.renova.mobile.R
import androidx.navigation.NavController
import com.renova.mobile.navigation.NavigationItemBusiness
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import com.renova.mobile.ui.components.BusinessSectionHeader
import androidx.compose.ui.res.stringResource


@Composable
fun BusinessQRScreen(onLogout: () -> Unit, vm: BusinessSaleViewModel = viewModel(), navController: NavController) {
    val context = LocalContext.current

    val scannedUser by vm.scannedUser.collectAsState()
    val isLoading by vm.isLoading.collectAsState()
    val error by vm.error.collectAsState()

    // Navegar automáticamente a la pantalla de venta cuando se identifique al usuario
    LaunchedEffect(scannedUser) {
        if (scannedUser != null) {
            // Navegar a la pantalla de venta cuando se identifique al usuario
            navController.navigate(NavigationItemBusiness.Store.route)
        }
    }

    val launcher = rememberLauncherForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            // Si por alguna razón se detecta un QR, mostrar error y no continuar
            if (result.formatName == "QR_CODE") {
                vm.setError(context.getString(R.string.no_qr))
            } else {
                vm.identifyUserByCode(result.contents)
            }
        } else {
            vm.setError(context.getString(R.string.scan_barcode_error))
        }
    }

    fun startScanner() {
        val options = ScanOptions().apply {
            // Restringir el escaneo a solo códigos de barras (1D)
            setDesiredBarcodeFormats(ScanOptions.ONE_D_CODE_TYPES)
            setPrompt(context.getString(R.string.scan_barcode_prompt))
            setBeepEnabled(true)
            setOrientationLocked(true)
            setCaptureActivity(com.renova.mobile.scan.PortraitCaptureActivity::class.java)
        }
        launcher.launch(options)
    }

    // Animación sutil para el cuadro de escaneo (efecto "latido")
    val infiniteTransition = rememberInfiniteTransition()
    val glow by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Column(modifier = Modifier.fillMaxSize()) {
        BusinessSectionHeader(
            title = stringResource(id = R.string.bottom_nav_qr),
            onLogout = onLogout,
            textColor = Color.White
        )

        // Contenido centrado verticalmente
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Cuadro interactivo para iniciar el escaneo
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(220.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(
                                width = 3.dp,
                                color = RenovaColors.Primary.copy(alpha = 0.4f + 0.4f * glow),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { startScanner() },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Tocar para escanear",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = RenovaColors.Primary,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Código de barras",
                                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = PoppinsFontFamily),
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = RenovaColors.Primary)
                    }
                }

                if (error != null) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = error ?: "",
                            color = Color.Red,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
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
    }
}

