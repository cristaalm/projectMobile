package com.renova.mobile.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.renova.mobile.R
import com.renova.mobile.ui.theme.*
import java.io.File

@Composable
fun VerificationScreen(
    registerData: RegisterData,
    documentsData: DocumentsData,
    onBackToDocuments: () -> Unit = {},
    onComplete: () -> Unit = {}
) {
    val colors = MaterialTheme.renovaColors
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    var selfieUri by remember { mutableStateOf<Uri?>(null) }
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }
    var showSuccessModal by remember { mutableStateOf(false) }
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Efecto para redirigir después de mostrar el modal
    LaunchedEffect(showSuccessModal) {
        if (showSuccessModal) {
            kotlinx.coroutines.delay(1500L)
            onComplete()
        }
    }

    // Crear URI temporal para la foto
    fun createImageUri(ctx: Context): Uri {
        val imageFile = File(ctx.cacheDir, "selfie_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(
            ctx,
            "${ctx.packageName}.fileprovider",
            imageFile
        )
    }

    // Launcher de cámara
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempImageUri != null) {
            // La foto se guardó exitosamente
            selfieUri = tempImageUri
        }
    }

    // Launcher para solicitar permiso de cámara
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (isGranted) {
            // Si se otorgó el permiso, abrir la cámara
            val uri = createImageUri(context)
            tempImageUri = uri
            cameraLauncher.launch(uri)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RenovaGradients.backgroundGradient())
    ) {
        SubtleLeavesBackground(
            modifier = Modifier.fillMaxSize(),
            leafPositions = listOf(
                LeafPosition(R.drawable.leaf1, Alignment.BottomStart),
                LeafPosition(R.drawable.leaf2, Alignment.TopEnd)
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Header
            Text(
                text = "VERIFICACIÓN",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Poppins,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Tome una selfie para\nconfirmar su identidad",
                color = Color.White,
                fontSize = 14.sp,
                fontFamily = Poppins,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            //Barra de progreso (Paso 3 lleno)
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(10.dp)
                    .clip(RoundedCornerShape(50.dp))
                    .background(Color.LightGray.copy(alpha = 0.4f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(3f / 3f)
                        .fillMaxHeight()
                        .background(Color.White)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Card con botón de selfie
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "SELFIE DE VERIFICACIÓN",
                        color = colors.textPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = Poppins,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Botón tomar selfie
                    Button(
                        onClick = {
                            // Verificar si tiene permiso de cámara
                            if (hasCameraPermission) {
                                // Crear URI temporal y abrir cámara
                                val uri = createImageUri(context)
                                tempImageUri = uri
                                cameraLauncher.launch(uri)
                            } else {
                                // Solicitar permiso
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selfieUri != null)
                                CustomGreenColor.copy(alpha = 0.2f)
                            else
                                colors.cardBackground
                        ),
                        border = if (selfieUri != null) null else ButtonDefaults.outlinedButtonBorder.copy(
                            brush = RenovaGradients.cardBorderGradient()
                        )
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                painter = painterResource(
                                    id = if (selfieUri != null) R.drawable.cheque else R.drawable.ic_camera
                                ),
                                contentDescription = "Selfie",
                                tint = if (selfieUri != null) CustomGreenColor else colors.iconTint,
                                modifier = Modifier.size(48.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = if (selfieUri != null) "Foto capturada" else "TOMAR SELFIE",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = Poppins,
                                color = if (selfieUri != null) CustomGreenColor else colors.textPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Card de instrucciones
            InstructionSection()

            Spacer(modifier = Modifier.height(24.dp))

            // Botón Regresar
            OutlinedButton(
                onClick = { onBackToDocuments() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = RenovaComponentColors.secondaryButtonColors(),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = RenovaGradients.cardBorderGradient()
                )
            ) {
                Text(
                    text = "REGRESAR",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = Poppins,
                    color = CustomGreenColor
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Botón Continuar
            Button(
                onClick = {
                    if (selfieUri != null) {
                        showSuccessModal = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CustomGreenColor
                ),
                enabled = selfieUri != null
            ) {
                Text(
                    text = "CONTINUAR",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = Poppins,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        // Modal de éxito
        if (showSuccessModal) {
            SuccessModal()
        }
    }
}

@Composable
fun InstructionItem(
    text: String,
    icon: Int,
    colors: RenovaColorScheme
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            tint = CustomGreenColor,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = Poppins,
                fontWeight = FontWeight.Medium
            ),
            color = colors.textPrimary
        )
    }
}

@Composable
fun InstructionSection() {
    val colors = MaterialTheme.renovaColors
    val scrollState = rememberScrollState()

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
            .heightIn(max = 300.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.cardBackground
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "INSTRUCCIONES:",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = Poppins,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = CustomGreenColor,
                )

                InstructionItem(
                    text = "Mire directamente a la cámara",
                    icon = R.drawable.ic_camera,
                    colors = colors
                )
                InstructionItem(
                    text = "Mantenga el rostro centrado",
                    icon = R.drawable.ic_info,
                    colors = colors
                )
                InstructionItem(
                    text = "Evite usar accesorios que cubran su cara",
                    icon = R.drawable.ic_info,
                    colors = colors
                )
                InstructionItem(
                    text = "Asegúrese de tener buena iluminación",
                    icon = R.drawable.ic_info,
                    colors = colors
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .graphicsLayer { alpha = bottomAlpha }
                    .padding(bottom = 8.dp, end = 16.dp)
                    .offset(y = (arrowOffset * 5).dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_close_2),
                    contentDescription = "Scroll hacia abajo",
                    tint = CustomGreenColor,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
fun SuccessModal() {
    var showProgress by remember { mutableStateOf(true) }
    var showCheck by remember { mutableStateOf(false) }

    // Animación de entrada del modal
    val alpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(300),
        label = "alpha"
    )

    val scale by animateFloatAsState(
        targetValue = if (showCheck) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    // Controlar el flujo: progress -> check -> redirect
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(1000L) // Mostrar progress por 1s
        showProgress = false
        showCheck = true
        kotlinx.coroutines.delay(500L) // Mostrar check por 0.5s
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f * alpha)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .size(240.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    this.alpha = alpha
                },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (showProgress) {
                    // Circular Progress
                    CircularProgressIndicator(
                        modifier = Modifier.size(80.dp),
                        color = CustomGreenColor,
                        strokeWidth = 6.dp
                    )
                } else {
                    // Check animado
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(50.dp))
                                .background(CustomGreenColor.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.cheque),
                                contentDescription = "Success",
                                tint = CustomGreenColor,
                                modifier = Modifier.size(60.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "¡Completado!",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = Poppins,
                            color = CustomGreenColor
                        )
                    }
                }
            }
        }
    }
}