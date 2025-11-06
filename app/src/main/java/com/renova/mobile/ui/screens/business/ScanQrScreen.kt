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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap


@Composable
fun BusinessQRScreen(onLogout: () -> Unit, vm: BusinessSaleViewModel = viewModel(), navController: NavController) {
    val context = LocalContext.current

    val scannedUser by vm.scannedUser.collectAsState()
    val isLoading by vm.isLoading.collectAsState()
    val error by vm.error.collectAsState()

    // Historial de escaneos (solo UI, respeta la lógica de identificación del usuario)
    data class ScanRecord(val code: String, val timestamp: Long)
    val recentScans = remember { mutableStateListOf<ScanRecord>() }

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
                // Guardar en historial local
                recentScans.add(0, ScanRecord(code = result.contents, timestamp = System.currentTimeMillis()))
                if (recentScans.size > 10) {
                    recentScans.removeLast()
                }
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
            title = stringResource(id = R.string.tab_qr),
            onLogout = onLogout,
            textColor = Color.White
        )

        // Contenido centrado verticalmente
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                // Botón principal circular con gradiente y anillos pulsantes
                val interactionSource = remember { MutableInteractionSource() }
                val pressed by interactionSource.collectIsPressedAsState()
                val scale by animateFloatAsState(if (pressed) 0.97f else 1f, animationSpec = tween(180))
                var isScanAnim by remember { mutableStateOf(false) }
                val scanProgress by animateFloatAsState(
                    targetValue = if (isScanAnim) 1f else 0f,
                    animationSpec = tween(900, easing = LinearEasing),
                    finishedListener = {
                        if (isScanAnim) {
                            isScanAnim = false
                            startScanner()
                        }
                    }
                )

                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(240.dp)
                            .scale(scale)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        RenovaColors.Primary.copy(alpha = 0.85f),
                                        RenovaColors.Primary.copy(alpha = 0.35f)
                                    )
                                )
                            )
                            .clickable(interactionSource = interactionSource, indication = null) {
                                isScanAnim = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        // Anillos pulsantes
                        val pulse by rememberInfiniteTransition().animateFloat(
                            initialValue = 0f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1400, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            )
                        )
                        Canvas(modifier = Modifier.matchParentSize()) {
                            val radius = size.minDimension / 2
                            val ringColor = RenovaColors.Primary.copy(alpha = 0.18f)
                            // Dibujar tres anillos con fase desplazada
                            for (i in 0..2) {
                                val factor = (pulse + i * 0.33f).let { if (it > 1f) it - 1f else it }
                                val ringRadius = radius * (0.76f + factor * 0.22f)
                                drawCircle(
                                    color = ringColor,
                                    radius = ringRadius,
                                    style = Stroke(width = 6f)
                                )
                            }
                        }

                        // Ícono/retícula de escaneo en el centro (sin texto)
                        Canvas(modifier = Modifier.size(96.dp)) {
                            val w = size.width
                            val h = size.height
                            val len = w * 0.22f
                            val margin = w * 0.08f
                            val strokeWidth = 8f
                            val c = Color.White
                            // Esquinas tipo retícula
                            // Superior izquierda
                            drawLine(color = c, start = Offset(margin, margin), end = Offset(margin + len, margin), strokeWidth = strokeWidth, cap = StrokeCap.Round)
                            drawLine(color = c, start = Offset(margin, margin), end = Offset(margin, margin + len), strokeWidth = strokeWidth, cap = StrokeCap.Round)
                            // Superior derecha
                            drawLine(color = c, start = Offset(w - margin, margin), end = Offset(w - margin - len, margin), strokeWidth = strokeWidth, cap = StrokeCap.Round)
                            drawLine(color = c, start = Offset(w - margin, margin), end = Offset(w - margin, margin + len), strokeWidth = strokeWidth, cap = StrokeCap.Round)
                            // Inferior izquierda
                            drawLine(color = c, start = Offset(margin, h - margin), end = Offset(margin + len, h - margin), strokeWidth = strokeWidth, cap = StrokeCap.Round)
                            drawLine(color = c, start = Offset(margin, h - margin), end = Offset(margin, h - margin - len), strokeWidth = strokeWidth, cap = StrokeCap.Round)
                            // Inferior derecha
                            drawLine(color = c, start = Offset(w - margin, h - margin), end = Offset(w - margin - len, h - margin), strokeWidth = strokeWidth, cap = StrokeCap.Round)
                            drawLine(color = c, start = Offset(w - margin, h - margin), end = Offset(w - margin, h - margin - len), strokeWidth = strokeWidth, cap = StrokeCap.Round)
                        }

                        // Línea de escaneo animada
                        val buttonSize = 240.dp
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color.White.copy(alpha = 0.0f),
                                            Color.White.copy(alpha = 0.85f),
                                            Color.White.copy(alpha = 0.0f)
                                        )
                                    )
                                )
                                .offset(y = ((scanProgress * buttonSize.value).dp) - 1.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Título debajo del botón
                Text(
                    text = context.getString(R.string.scan_barcode_prompt),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold
                    ),
                    color = RenovaColors.Primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

                // Descripción
                Text(
                    text = stringResource(id = R.string.scan_barcode_description),
                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = PoppinsFontFamily),
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 20.dp),
                    textAlign = TextAlign.Center
                )

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

                AnimatedVisibility(
                    visible = scannedUser != null,
                    enter = fadeIn(tween(250)) + slideInVertically(tween(250)) { it / 6 },
                    exit = fadeOut(tween(200)) + slideOutVertically(tween(200)) { it / 6 }
                ) {
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
                        ) {
                            Text(
                                text = context.getString(R.string.consumer_label),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = RenovaColors.Primary,
                            )

                            Text(
                                text = context.getString(R.string.name_label) + " ${scannedUser!!.name} ${scannedUser!!.last_name ?: ""}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = PoppinsFontFamily)
                            )
                            Text(
                                text = context.getString(R.string.available_points_label) + " ${scannedUser!!.total_points}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = PoppinsFontFamily)
                            )
                        }
                    }
                }

                // Historial de escaneos recientes
                Spacer(modifier = Modifier.height(24.dp))
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    if (recentScans.isNotEmpty()) {
                        Text(
                            text = "Escaneos Recientes",
                            // Si deseas crear un string, reemplazar por R.string.recent_scans
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold
                            ),
                            color = RenovaColors.Primary,
                            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                        )
                        recentScans.take(5).forEach { rec ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (androidx.compose.foundation.isSystemInDarkTheme()) Color.Black else RenovaColors.Light.Surface
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Código: ${rec.code}",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = PoppinsFontFamily),
                                            color = Color.Black
                                        )
                                        Text(
                                            text = formatRelativeTime(rec.timestamp),
                                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = PoppinsFontFamily),
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatRelativeTime(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val minutes = (diff / 60000).toInt()
    return when {
        minutes < 1 -> "Hace unos segundos"
        minutes == 1 -> "Hace 1 min"
        minutes < 60 -> "Hace $minutes min"
        else -> {
            val hours = minutes / 60
            if (hours == 1) "Hace 1 h" else "Hace ${hours} h"
        }
    }
}

