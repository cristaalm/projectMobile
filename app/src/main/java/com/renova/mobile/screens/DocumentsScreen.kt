package com.renova.mobile.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.renova.mobile.R
import com.renova.mobile.ui.theme.*
import kotlinx.coroutines.delay
import com.renova.mobile.ui.viewmodels.RegisterState
import com.renova.mobile.ui.viewmodels.RegisterViewModel
import com.renova.mobile.ui.viewmodels.UploadState

@Composable
fun DocumentsScreen(
    registerData: RegisterData,
    onBackToRegister: () -> Unit = {},
    onContinueToVerification: (DocumentsData) -> Unit = {},
    viewModel: RegisterViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val colors = MaterialTheme.renovaColors
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val uploadState by viewModel.uploadDocumentsState.collectAsState()

    var ineFrontUri by remember { mutableStateOf<Uri?>(null) }
    var ineBackUri by remember { mutableStateOf<Uri?>(null) }

    var ineFrontValidation by remember { mutableStateOf(ValidationState.IDLE) }
    var ineBackValidation by remember { mutableStateOf(ValidationState.IDLE) }

    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Función para validar tamaño del archivo
    fun validateFileSize(uri: Uri): Boolean {
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val fileSize = inputStream.available()
                val maxSize = 5 * 1024 * 1024 // 5MB en bytes
                return fileSize <= maxSize
            }
        } catch (e: Exception) {
            return false
        }
        return false
    }

    val ineFrontLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            if (validateFileSize(uri)) {
                ineFrontUri = uri
                ineFrontValidation = ValidationState.VALID
            } else {
                errorMessage = context.getString(R.string.error_file_too_large)
                showErrorDialog = true
            }
        }
    }

    val ineBackLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            if (validateFileSize(uri)) {
                ineBackUri = uri
                ineBackValidation = ValidationState.VALID
            } else {
                errorMessage = context.getString(R.string.error_file_too_large)
                showErrorDialog = true
            }
        }
    }

    // Observar el estado de subida
    LaunchedEffect(uploadState) {
        when (uploadState) {
            is UploadState.Success -> {
                val data = DocumentsData(
                    ineFrontUri = ineFrontUri!!,
                    ineBackUri = ineBackUri!!
                )
                onContinueToVerification(data)
            }
            is UploadState.Error -> {
                errorMessage = (uploadState as UploadState.Error).message
                showErrorDialog = true
            }
            else -> {}
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
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.documents_title),
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Poppins,
                textAlign = TextAlign.Center
            )

            Text(
                text = stringResource(R.string.upload_official_id),
                color = Color.White,
                fontSize = 16.sp,
                fontFamily = Poppins,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = stringResource(R.string.step_2_of_3),
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
                        .fillMaxWidth(2f / 3f)
                        .fillMaxHeight()
                        .background(Color.White)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
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
                    DocumentUploadButton(
                        label = stringResource(R.string.upload_ine_front),
                        subtitle = stringResource(R.string.tap_to_select_file),
                        isUploaded = ineFrontUri != null,
                        onClick = { ineFrontLauncher.launch("image/*") },
                        colors = colors
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    DocumentUploadButton(
                        label = stringResource(R.string.upload_ine_back),
                        subtitle = stringResource(R.string.tap_to_select_file),
                        isUploaded = ineBackUri != null,
                        onClick = { ineBackLauncher.launch("image/*") },
                        colors = colors
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedButton(
                        onClick = { onBackToRegister() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = RenovaComponentColors.secondaryButtonColors(),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = RenovaGradients.cardBorderGradient()
                        ),
                        enabled = uploadState !is UploadState.Loading
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
                            val allValid = ineFrontUri != null && ineBackUri != null

                            if (allValid) {
                                val data = DocumentsData(
                                    ineFrontUri = ineFrontUri!!,
                                    ineBackUri = ineBackUri!!
                                )
                                viewModel.uploadDocuments(context, data)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CustomGreenColor
                        ),
                        enabled = uploadState !is UploadState.Loading && ineFrontUri != null && ineBackUri != null
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
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun DocumentUploadButton(
    label: String,
    subtitle: String,
    isUploaded: Boolean,
    onClick: () -> Unit,
    colors: RenovaColorScheme
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isUploaded)
                CustomGreenColor.copy(alpha = 0.2f)
            else
                colors.cardBackground
        ),
        border = if (isUploaded) null else ButtonDefaults.outlinedButtonBorder.copy(
            brush = RenovaGradients.cardBorderGradient()
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(
                    id = if (isUploaded) R.drawable.cheque else R.drawable.subir
                ),
                contentDescription = label,
                tint = if (isUploaded) CustomGreenColor else colors.iconTint,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Poppins,
                color = if (isUploaded) CustomGreenColor else colors.textPrimary
            )

            Text(
                text = if (isUploaded) stringResource(R.string.file_selected) else subtitle,
                fontSize = 12.sp,
                fontFamily = Poppins,
                color = if (isUploaded) CustomGreenColor else colors.textSecondary
            )
        }
    }
}

data class DocumentsData(
    val ineFrontUri: Uri,
    val ineBackUri: Uri
)