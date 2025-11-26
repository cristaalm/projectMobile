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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.renova.mobile.R
import com.renova.mobile.ui.theme.*
import com.renova.mobile.ui.viewmodels.RegisterViewModel
import com.renova.mobile.ui.viewmodels.UploadState
import com.renova.mobile.utils.ImageCompressionHelper
import java.io.File

@Composable
fun VerificationScreen(
    registerData: RegisterData,
    documentsData: DocumentsData,
    onBackToDocuments: () -> Unit = {},
    onComplete: () -> Unit = {},
    viewModel: RegisterViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val colors = MaterialTheme.renovaColors
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val uploadState by viewModel.uploadSelfieState.collectAsState()

    var selfieUri by remember { mutableStateOf<Uri?>(null) }
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }
    var isCompressing by remember { mutableStateOf(false) }
    var showSuccessModal by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Observar el estado de subida
    LaunchedEffect(uploadState) {
        when (uploadState) {
            is UploadState.Success -> {
                showSuccessModal = true
            }
            is UploadState.Error -> {
                errorMessage = (uploadState as UploadState.Error).message
                showErrorDialog = true
            }
            else -> {}
        }
    }

    LaunchedEffect(showSuccessModal) {
        if (showSuccessModal) {
            kotlinx.coroutines.delay(2500L)

            android.util.Log.d("VerificationScreen", "Navegando al login...")

            // ✅ PRIMERO navegar
            onComplete()

            // ✅ DESPUÉS limpiar (con delay para asegurar navegación)
            kotlinx.coroutines.delay(200L)
            android.util.Log.d("VerificationScreen", "Limpiando después de navegación...")
            viewModel.clearAfterSuccessfulRegistration()
        }
    }

    fun createImageUri(ctx: Context): Uri {
        val imageFile = File(ctx.cacheDir, "selfie_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(
            ctx,
            "${ctx.packageName}.fileprovider",
            imageFile
        )
    }

    // Launcher de cámara con compresión automática
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempImageUri != null) {
            isCompressing = true

            // SIEMPRE comprimir la selfie
            ImageCompressionHelper.compressImage(context, tempImageUri!!, "selfie")
                .onSuccess { compressedUri ->
                    selfieUri = compressedUri
                    isCompressing = false
                    android.util.Log.d("VerificationScreen", " Selfie comprimida exitosamente")
                }
                .onFailure { exception ->
                    isCompressing = false
                    errorMessage = exception.message
                        ?: context.getString(R.string.error_compressing_image)
                    showErrorDialog = true
                    tempImageUri = null
                    android.util.Log.e("VerificationScreen", "Error comprimiendo selfie", exception)
                }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (isGranted) {
            val uri = createImageUri(context)
            tempImageUri = uri
            cameraLauncher.launch(uri)
        }
    }

    // Diálogo de error
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.error_title),
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = errorMessage,
                    fontFamily = Poppins
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showErrorDialog = false
                        viewModel.resetStates()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CustomGreenColor
                    )
                ) {
                    Text(
                        text = stringResource(R.string.accept),
                        fontFamily = Poppins
                    )
                }
            }
        )
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

            Text(
                text = stringResource(R.string.verification_title),
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Poppins,
                textAlign = TextAlign.Center
            )

            Text(
                text = stringResource(R.string.take_selfie_subtitle),
                color = Color.White,
                fontSize = 14.sp,
                fontFamily = Poppins,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = stringResource(R.string.step_3_of_3),
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = Poppins,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

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
                        text = stringResource(R.string.verification_selfie),
                        color = colors.textPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = Poppins,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Button(
                        onClick = {
                            if (hasCameraPermission) {
                                val uri = createImageUri(context)
                                tempImageUri = uri
                                cameraLauncher.launch(uri)
                            } else {
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
                        ),
                        enabled = uploadState !is UploadState.Loading && !isCompressing
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            if (isCompressing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(48.dp),
                                    color = CustomGreenColor,
                                    strokeWidth = 4.dp
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = stringResource(R.string.compressing),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = Poppins,
                                    color = CustomGreenColor
                                )
                            } else {
                                Icon(
                                    painter = painterResource(
                                        id = if (selfieUri != null) R.drawable.cheque else R.drawable.ic_camera
                                    ),
                                    contentDescription = stringResource(R.string.selfie),
                                    tint = if (selfieUri != null) CustomGreenColor else colors.iconTint,
                                    modifier = Modifier.size(48.dp)
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = if (selfieUri != null)
                                        stringResource(R.string.photo_captured)
                                    else
                                        stringResource(R.string.take_selfie),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = Poppins,
                                    color = if (selfieUri != null) CustomGreenColor else colors.textPrimary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            InstructionSection()

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(
                onClick = { onBackToDocuments() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = colors.cardBackground
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = RenovaGradients.cardBorderGradient()
                ),
                enabled = uploadState !is UploadState.Loading && !isCompressing
            ) {
                Text(
                    text = stringResource(R.string.back),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = Poppins,
                    color = CustomGreenColor
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    if (selfieUri != null) {
                        viewModel.uploadSelfie(context, selfieUri!!)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CustomGreenColor
                ),
                enabled = selfieUri != null &&
                        uploadState !is UploadState.Loading &&
                        !isCompressing
            ) {
                if (uploadState is UploadState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = stringResource(R.string.continue_button),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = Poppins,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

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
                    text = stringResource(R.string.instructions),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = Poppins,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = CustomGreenColor,
                )

                InstructionItem(
                    text = stringResource(R.string.instruction_look_camera),
                    icon = R.drawable.ic_camera_fill,
                    colors = colors
                )
                InstructionItem(
                    text = stringResource(R.string.instruction_center_face),
                    icon = R.drawable.ic_info,
                    colors = colors
                )
                InstructionItem(
                    text = stringResource(R.string.instruction_no_accessories),
                    icon = R.drawable.ic_info,
                    colors = colors
                )
                InstructionItem(
                    text = stringResource(R.string.instruction_good_lighting),
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
                    contentDescription = stringResource(R.string.scroll_down),
                    tint = CustomGreenColor,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
fun SuccessModal() {
    val colors = MaterialTheme.renovaColors
    var showProgress by remember { mutableStateOf(true) }
    var showCheck by remember { mutableStateOf(false) }

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

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(1000L)
        showProgress = false
        showCheck = true
        kotlinx.coroutines.delay(500L)
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
                containerColor = colors.cardBackground
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (showProgress) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(80.dp),
                        color = CustomGreenColor,
                        strokeWidth = 6.dp
                    )
                } else {
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
                                contentDescription = stringResource(R.string.success),
                                tint = CustomGreenColor,
                                modifier = Modifier.size(60.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = stringResource(R.string.completed),
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