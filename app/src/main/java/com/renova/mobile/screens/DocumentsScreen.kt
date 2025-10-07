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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.renova.mobile.R
import com.renova.mobile.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun DocumentsScreen(
    registerData: RegisterData,
    onBackToRegister: () -> Unit = {},
    onContinueToVerification: (DocumentsData) -> Unit = {}
) {
    val colors = MaterialTheme.renovaColors
    val scrollState = rememberScrollState()

    var ineFrontUri by remember { mutableStateOf<Uri?>(null) }
    var ineBackUri by remember { mutableStateOf<Uri?>(null) }
    var curp by remember { mutableStateOf("") }

    var ineFrontValidation by remember { mutableStateOf(ValidationState.IDLE) }
    var ineBackValidation by remember { mutableStateOf(ValidationState.IDLE) }
    var curpValidation by remember { mutableStateOf(ValidationState.IDLE) }

    val ineFrontLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        ineFrontUri = uri
        ineFrontValidation = if (uri != null) ValidationState.VALID else ValidationState.IDLE
    }

    val ineBackLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        ineBackUri = uri
        ineBackValidation = if (uri != null) ValidationState.VALID else ValidationState.IDLE
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
                text = "DOCUMENTOS",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Poppins,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Suba su identificación oficial",
                color = Color.White,
                fontSize = 16.sp,
                fontFamily = Poppins,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Paso 2 de 3",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = Poppins,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            //Barra de progreso (Paso 2 lleno)
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

            // Card con formulario
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
                    // Botón Subir INE (Frontal)
                    DocumentUploadButton(
                        label = "SUBIR INE (Frontal)",
                        subtitle = "Toca para seleccionar archivo",
                        isUploaded = ineFrontUri != null,
                        onClick = { ineFrontLauncher.launch("image/*") },
                        colors = colors
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Botón Subir INE (Reverso)
                    DocumentUploadButton(
                        label = "SUBIR INE (Reverso)",
                        subtitle = "Toca para seleccionar archivo",
                        isUploaded = ineBackUri != null,
                        onClick = { ineBackLauncher.launch("image/*") },
                        colors = colors
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Campo CURP
                    ValidatedTextField(
                        value = curp,
                        onValueChange = {
                            if (it.length <= 18) curp = it.uppercase()
                        },
                        label = "Número de documento/CURP",
                        leadingIcon = R.drawable.document,
                        validationState = curpValidation,
                        onValidationChange = { curpValidation = it },
                        validator = { validateCURP(it) },
                        keyboardType = KeyboardType.Text,
                        colors = colors
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Botón Regresar
                    OutlinedButton(
                        onClick = { onBackToRegister() },
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
                            val allValid = ineFrontUri != null &&
                                    ineBackUri != null &&
                                    curpValidation == ValidationState.VALID

                            if (allValid) {
                                onContinueToVerification(
                                    DocumentsData(
                                        ineFrontUri = ineFrontUri!!,
                                        ineBackUri = ineBackUri!!,
                                        curp = curp
                                    )
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CustomGreenColor
                        )
                    ) {
                        Text(
                            text = "CONTINUAR",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = Poppins,
                            color = Color.White
                        )
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
                text = if (isUploaded) "Archivo seleccionado" else subtitle,
                fontSize = 12.sp,
                fontFamily = Poppins,
                color = if (isUploaded) CustomGreenColor else colors.textSecondary
            )
        }
    }
}

data class DocumentsData(
    val ineFrontUri: Uri,
    val ineBackUri: Uri,
    val curp: String
)

fun validateCURP(curp: String): Boolean {
    val curpPattern = "^[A-Z]{4}[0-9]{6}[HM][A-Z]{5}[0-9]{2}$"
    return curp.matches(curpPattern.toRegex())
}