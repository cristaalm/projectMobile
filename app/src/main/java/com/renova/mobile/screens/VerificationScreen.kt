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
            Spacer(modifier = Modifier.height(32.dp))

            // Header
            Text(
                text = "VERIFICACIÓN",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Poppins,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Toma una selfie para\nconfirmar tu identidad",
                color = Color.White,
                fontSize = 16.sp,
                fontFamily = Poppins,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Barra de progreso (Paso 3 de 3)
            Row(
                modifier = Modifier.fillMaxWidth(0.8f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Paso 3 de 3 - Verificación facial",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontFamily = Poppins,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            LinearProgressIndicator(
                progress = 3f / 3f,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(10.dp)
                    .clip(RoundedCornerShape(50.dp)),
                color = Color.White,
                trackColor = Color.LightGray.copy(alpha = 0.4f)
            )

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
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "INSTRUCCIONES:",
                        color = colors.textPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = Poppins,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    InstructionItem("Mira directamente a la cámara", colors)
                    InstructionItem("Mantén el rostro centrado", colors)
                    InstructionItem("Evita usar accesorios que cubran tu cara", colors)
                    InstructionItem("Asegúrate de tener buena iluminación", colors)
                }
            }

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
fun InstructionItem(text: String, colors: RenovaColorScheme) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "• ",
            color = colors.textPrimary,
            fontSize = 14.sp,
            fontFamily = Poppins
        )
        Text(
            text = text,
            color = colors.textPrimary,
            fontSize = 14.sp,
            fontFamily = Poppins,
            modifier = Modifier.weight(1f)
        )
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