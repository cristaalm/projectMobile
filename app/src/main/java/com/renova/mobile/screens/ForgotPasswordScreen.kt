package com.renova.mobile.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import com.renova.mobile.R
import com.renova.mobile.viewmodel.ForgotPasswordViewModel
import com.renova.mobile.ui.theme.*

@Composable
fun ForgotPasswordScreen(
    onBackToLogin: () -> Unit = {},
    viewModel: ForgotPasswordViewModel = viewModel()
) {
    // Usar los colores del tema
    val colors = MaterialTheme.renovaColors

    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf(false) }
    var emailEmptyError by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }

    val forgotPasswordState by viewModel.forgotPasswordState.collectAsState()

    LaunchedEffect(forgotPasswordState) {
        when {
            forgotPasswordState.isSuccess -> {
                showSuccessDialog = true
                showErrorDialog = false
            }

            forgotPasswordState.error != null -> {
                showErrorDialog = true
                showSuccessDialog = false
            }

            else -> {
                showSuccessDialog = false
                showErrorDialog = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RenovaGradients.backgroundGradient())
            .padding(24.dp)
    ) {
        SubtleLeavesBackground(
            modifier = Modifier.fillMaxSize(),
            leafPositions = listOf(
                LeafPosition(R.drawable.leaf1, Alignment.BottomStart),
                LeafPosition(R.drawable.leaf2, Alignment.TopEnd)
            )
        )

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Logo sin Card
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                        .height(150.dp)
                        .padding(bottom = 24.dp)
                )

                // Forgot Password Form Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Recuperar contraseña",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Ingresa tu correo electrónico y te enviaremos un enlace para restablecer tu contraseña.",
                            fontSize = 14.sp,
                            color = colors.textSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Email Field con label externo
                        Text(
                            text = "Correo electrónico",
                            color = colors.textPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 4.dp)
                        )

                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                emailError = it.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(it).matches()
                                if (it.isNotEmpty()) emailEmptyError = false
                            },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_email),
                                    contentDescription = "Email",
                                    tint = colors.iconTint
                                )
                            },
                            isError = emailError || emailEmptyError,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            textStyle = TextStyle(color = colors.textPrimary),
                            colors = RenovaComponentColors.textFieldColors()
                        )

                        if ((emailError && email.isNotEmpty()) || emailEmptyError) {
                            Text(
                                text = if (emailEmptyError) "El correo es obligatorio" else "Correo inválido",
                                color = RenovaColors.Error,
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Send Button
                        Button(
                            onClick = {
                                emailEmptyError = email.isEmpty()
                                if (!emailEmptyError) {
                                    emailError = !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
                                    if (!emailError) {
                                        viewModel.forgotPassword(email)
                                    }
                                }
                            },
                            enabled = !forgotPasswordState.isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = if (forgotPasswordState.isLoading) {
                                ButtonDefaults.buttonColors(
                                    containerColor = RenovaColors.Error
                                )
                            } else {
                                RenovaComponentColors.primaryButtonColors()
                            }
                        ) {
                            if (forgotPasswordState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = if (androidx.compose.foundation.isSystemInDarkTheme()) RenovaColors.Primary else Color.Black,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "ENVIANDO...",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (androidx.compose.foundation.isSystemInDarkTheme()) RenovaColors.Primary else Color.Black
                                )
                            } else {
                                Text(
                                    "ENVIAR ENLACE",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Back to Login Button
                        TextButton(
                            onClick = { onBackToLogin() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Volver al inicio de sesión",
                                color = RenovaColors.Primary,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // Success Dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                viewModel.clearState()
                onBackToLogin()
            },
            title = {
                Text(
                    "Correo enviado",
                    color = RenovaColors.Success,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    forgotPasswordState.message
                        ?: "Te hemos enviado un enlace de recuperación a tu correo electrónico.",
                    color = colors.textPrimary,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        viewModel.clearState()
                        onBackToLogin()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RenovaColors.Success)
                ) {
                    Text("Entendido", color = Color.White)
                }
            },
            containerColor = colors.cardBackground,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Error Dialog
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = {
                showErrorDialog = false
                viewModel.clearState()
            },
            title = {
                Text(
                    "Error",
                    color = RenovaColors.Error,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    forgotPasswordState.error ?: "Ha ocurrido un error. Intenta de nuevo.",
                    color = colors.textPrimary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showErrorDialog = false
                        viewModel.clearState()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RenovaColors.Error)
                ) {
                    Text("Reintentar", color = Color.White)
                }
            },
            containerColor = colors.cardBackground,
            shape = RoundedCornerShape(16.dp)
        )
    }
}