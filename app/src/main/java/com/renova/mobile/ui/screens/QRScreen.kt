package com.renova.mobile.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.renova.mobile.R
import com.renova.mobile.repository.LoginRepository
import com.renova.mobile.ui.components.SectionHeader
import com.renova.mobile.ui.theme.RenovaColors
import com.renova.mobile.utils.SessionManager
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidx.compose.animation.core.*
import androidx.compose.animation.Crossfade
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.EncodeHintType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import com.renova.mobile.ui.viewmodels.QRViewModel
import com.renova.mobile.ui.viewmodels.QRState
import com.renova.mobile.ui.components.CustomRefreshIndicator
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material.icons.filled.Star
import androidx.compose.animation.core.animateIntAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRScreen(
    viewModel: QRViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    val uiState by viewModel.state.collectAsState()
    val pullToRefreshState = rememberPullToRefreshState()

    val user = sessionManager.getUser()
    val accessToken = sessionManager.getAccessToken()

    val qrCode = remember(accessToken) {
        if (!accessToken.isNullOrBlank()) accessToken else "0"
    }

    val nameUser = user?.name ?: "Usuario no identificado"
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()

    var showQr by remember { mutableStateOf(true) }

    PullToRefreshBox(
        isRefreshing = uiState.isLoading,
        onRefresh = { viewModel.loadPoints() },
        state = pullToRefreshState,
        indicator = {
            CustomRefreshIndicator(
                state = pullToRefreshState,
                isRefreshing = uiState.isLoading,
                renovaColors = com.renova.mobile.ui.theme.LocalRenovaColors.current,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
        ) {
            SectionHeader(title = stringResource(id = if (showQr) R.string.mycode else R.string.points_code_title), textColor = Color.White)
            Spacer(modifier = Modifier.height(20.dp))

            // ===== CARD PRINCIPAL =====
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(5f / 5f)
                    .padding(horizontal = 16.dp)
                    .shadow(
                        elevation = 3.dp,
                        shape = RoundedCornerShape(16.dp),
                        spotColor = RenovaColors.Light.ActivityShadowColor
                    ),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                // Animación simplificada: Crossfade entre vistas, sin rotación

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Transparent)
                        .paint(
                            painter = painterResource(id = R.drawable.fondo_chico),
                            contentScale = ContentScale.Crop
                        )
                        .padding(15.dp)
                ) {
                    // === Vista principal: Crossfade entre código de barras y QR ===
                    Crossfade(targetState = showQr, animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)) { showingQr ->
                        if (!showingQr) {
                            Column(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // ==== Fila 1: usuario ====
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = nameUser,
                                            color = Color.White,
                                            style = MaterialTheme.typography.headlineSmall.copy(
                                                fontFamily = com.renova.mobile.ui.theme.PoppinsFontFamily,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                        // Puntos: estilo diferente y animado
                                        val animatedPoints by animateIntAsState(
                                            targetValue = uiState.points,
                                            animationSpec = tween(durationMillis = 500),
                                            label = "animatedPoints"
                                        )
                                        val pointsFormatted = java.text.NumberFormat.getIntegerInstance(java.util.Locale.forLanguageTag("es-MX")).format(animatedPoints)
                                        Text(
                                            text = "$pointsFormatted ${stringResource(id = R.string.points_unit)}",
                                            color = Color.White,
                                            style = MaterialTheme.typography.headlineSmall.copy(
                                                fontFamily = com.renova.mobile.ui.theme.PoppinsFontFamily,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        )
                                        Text(
                                            text = stringResource(id = R.string.current_points),
                                            color = Color.White,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontFamily = com.renova.mobile.ui.theme.PoppinsFontFamily,
                                                fontWeight = FontWeight.Medium
                                            )
                                        )
                                    }

                                    IconButton(
                                        onClick = { showQr = !showQr },
                                        modifier = Modifier
                                            .size(56.dp)
                                            .background(
                                                color = Color.White.copy(alpha = 0.3f),
                                                shape = RoundedCornerShape(16.dp)
                                            )
                                    ) {
                                        Icon(
                                            imageVector = if (showQr) Icons.Default.CreditCard else Icons.Default.QrCode,
                                            contentDescription = if (showQr) "Mostrar código" else "Mostrar QR",
                                            tint = if (isDark) Color.Black else Color.White,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }

                                Divider(
                                    color = if (isDark) Color.Black.copy(alpha = 0.5f)
                                    else Color.White.copy(alpha = 0.5f),
                                    thickness = 1.dp,
                                    modifier = Modifier.padding(vertical = 10.dp)
                                )

                                // ==== Contenido (Código de barras) ====
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp)
                                        .height(200.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White), // fondo blanco mejora lectura
                                    contentAlignment = Alignment.Center
                                ) {
                                    // Datos del código de barras: validar que el código tenga exactamente 13 dígitos
                                    val codeDigits = user?.code_identity ?: ""
                                    val barcodeData: String? = if (codeDigits.length == 13) codeDigits else null

                                    // Ajustar tamaño objetivo según ancho de pantalla
                                    val configuration = LocalConfiguration.current
                                    val density = LocalDensity.current
                                    val screenWidthDp = configuration.screenWidthDp
                                    val targetWidth = with(density) { (screenWidthDp * 0.9f).dp.toPx().toInt() }
                                    val targetHeight = (targetWidth * 0.25f).toInt()

                                    // Hints para mejorar la calidad del código de barras
                                    val hints = mapOf(
                                        EncodeHintType.MARGIN to 10, // zona tranquila alrededor
                                        EncodeHintType.CHARACTER_SET to "UTF-8" // codificación estándar de texto
                                    )

                                    // Generar código EAN-13 sólo si el código es válido (13 dígitos)
                                    val bitMatrix: BitMatrix? = if (barcodeData != null) {
                                        try {
                                            MultiFormatWriter().encode(
                                                barcodeData,
                                                BarcodeFormat.EAN_13,
                                                targetWidth,
                                                targetHeight,
                                                hints
                                            )
                                        } catch (e: Exception) {
                                            null
                                        }
                                    } else null

                                    // Colores estándar: barras negras sobre fondo blanco
                                    val backgroundAndroid = android.graphics.Color.WHITE
                                    val barColorCompose = RenovaColors.Primary
                                    val barColorInt = barColorCompose.toArgb()

                                    if (bitMatrix != null) {
                                        val width = bitMatrix.width
                                        val height = bitMatrix.height
                                        val barcodeBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                                        for (x in 0 until width) {
                                            for (y in 0 until height) {
                                                barcodeBitmap.setPixel(
                                                    x,
                                                    y,
                                                    if (bitMatrix[x, y]) barColorInt else backgroundAndroid
                                                )
                                            }
                                        }
                                        val ratio = width.toFloat() / height.toFloat()
                                        Image(
                                            bitmap = barcodeBitmap.asImageBitmap(),
                                            contentDescription = "Código de barras",
                                            modifier = Modifier
                                                .fillMaxWidth(0.9f)
                                                .aspectRatio(ratio)
                                        )
                                    } else {
                                        Text(
                                            text = "Código no encontrado",
                                            style = MaterialTheme.typography.headlineMedium.copy(
                                                fontFamily = com.renova.mobile.ui.theme.PoppinsFontFamily,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = barColorCompose,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        } else {
                            // === Vista alternativa: Mostrar QR ===
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                            ) {
                                // ==== Fila 1: usuario ====
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = nameUser,
                                            color = if (isDark) Color.Black else Color.White,
                                            style = MaterialTheme.typography.headlineSmall.copy(
                                                fontFamily = com.renova.mobile.ui.theme.PoppinsFontFamily,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                        val animatedPoints by animateIntAsState(
                                            targetValue = uiState.points,
                                            animationSpec = tween(durationMillis = 500),
                                            label = "animatedPoints2"
                                        )
                                        val pointsFormatted2 = java.text.NumberFormat.getIntegerInstance(java.util.Locale.forLanguageTag("es-MX")).format(animatedPoints)
                                        Text(
                                            text = "$pointsFormatted2 ${stringResource(id = R.string.points_unit)}",
                                            color = if (isDark) Color.Black else Color.White,
                                            style = MaterialTheme.typography.headlineSmall.copy(
                                                fontFamily = com.renova.mobile.ui.theme.PoppinsFontFamily,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        )
                                        Text(
                                            text = stringResource(id = R.string.current_points),
                                            color = if (isDark) Color.Black else Color.White,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontFamily = com.renova.mobile.ui.theme.PoppinsFontFamily,
                                                fontWeight = FontWeight.Medium
                                            )
                                        )
                                    }

                                    IconButton(
                                        onClick = { showQr = !showQr },
                                        modifier = Modifier
                                            .size(56.dp)
                                            .background(
                                                color = Color.White.copy(alpha = 0.3f),
                                                shape = RoundedCornerShape(16.dp)
                                            )
                                    ) {
                                        Icon(
                                            imageVector = if (showQr) Icons.Default.CreditCard else Icons.Default.QrCode,
                                            contentDescription = if (showQr) "Mostrar código" else "Mostrar QR",
                                            tint = if (isDark) Color.Black else Color.White,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }

                                Divider(
                                    color = if (isDark) Color.Black.copy(alpha = 0.5f)
                                    else Color.White.copy(alpha = 0.5f),
                                    thickness = 1.dp,
                                    modifier = Modifier.padding(vertical = 10.dp)
                                )

                                // ==== Contenido (QR) ====
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val qrEncoder = QRGEncoder(
                                        qrCode, null, QRGContents.Type.TEXT, 500
                                    ).apply {
                                        val primaryAndroidQR = android.graphics.Color.rgb(
                                            (RenovaColors.Primary.red * 255).toInt(),
                                            (RenovaColors.Primary.green * 255).toInt(),
                                            (RenovaColors.Primary.blue * 255).toInt()
                                        )
                                        colorBlack = android.graphics.Color.WHITE
                                        colorWhite = primaryAndroidQR
                                    }

                                    val qrBitmap: Bitmap = qrEncoder.bitmap
                                    Image(
                                        bitmap = qrBitmap.asImageBitmap(),
                                        contentDescription = "Código QR",
                                        modifier = Modifier
                                            .fillMaxWidth(0.7f)
                                            .aspectRatio(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ===== SECCIÓN DE INSTRUCCIONES =====
            InstructionSection(showQr)
        }
    }
}

@Composable
private fun InstructionSection(showQr: Boolean) {
    val scrollState = rememberScrollState()

    val isScrolledToStart by remember { derivedStateOf { scrollState.value == 0 } }
    val isScrolledToEnd by remember {
        derivedStateOf { scrollState.maxValue == 0 || scrollState.value >= scrollState.maxValue }
    }

    val bottomAlpha by animateFloatAsState(
        targetValue = if (isScrolledToEnd) 0f else 1f,
        label = "bottomAlpha"
    )

    val arrowOffset by animateFloatAsState(
        targetValue = if (isScrolledToEnd) 0f else 1f,
        animationSpec = if (isScrolledToEnd) tween(300) else infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "arrowOffset"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .heightIn(max = 300.dp)
            .shadow(
                elevation = 3.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = RenovaColors.Light.ActivityShadowColor
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (androidx.compose.foundation.isSystemInDarkTheme())
                Color.Black else RenovaColors.Light.Surface
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.instructions_title),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = com.renova.mobile.ui.theme.PoppinsFontFamily,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = RenovaColors.Primary,
                )

                if (showQr) {
                    InstructionItem(text = stringResource(id = R.string.instruction_1), icon = Icons.Default.CameraAlt)
                    InstructionItem(text = stringResource(id = R.string.instruction_2), icon = Icons.Default.Info)
                    InstructionItem(text = stringResource(id = R.string.instruction_3), icon = Icons.Default.Info)
                } else {
                    InstructionItem(text = stringResource(id = R.string.code_instruction_1), icon = Icons.Default.Info)
                    InstructionItem(text = stringResource(id = R.string.code_instruction_2), icon = Icons.Default.Info)
                    InstructionItem(text = stringResource(id = R.string.code_instruction_3), icon = Icons.Default.Info)
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .alpha(bottomAlpha)
                    .padding(bottom = 8.dp, end = 16.dp)
                    .offset(y = (arrowOffset * 5).dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = "Scroll hacia abajo",
                    tint = RenovaColors.Primary,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
fun InstructionItem(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = RenovaColors.Primary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = com.renova.mobile.ui.theme.PoppinsFontFamily,
                fontWeight = FontWeight.Medium
            ),
            color = if (androidx.compose.foundation.isSystemInDarkTheme()) Color.White else Color.Black
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewQRScreen() {
    QRScreen()
}
