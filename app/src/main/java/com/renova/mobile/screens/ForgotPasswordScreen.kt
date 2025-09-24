package com.renova.mobile.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
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

@Composable
fun ForgotPasswordScreen(
    onBackToLogin: () -> Unit = {},
    viewModel: ForgotPasswordViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf(false) }
    var emailEmptyError by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }

    val isDarkTheme = isSystemInDarkTheme()
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

    val gradientBrush = if (isDarkTheme) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF004D40),
                Color(0xFF00695C),
                Color(0xFF00796B)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF00E676),
                Color(0xFF00C853),
                Color(0xFF00A843),
                Color(0xFF1B5E20)
            )
        )
    }

    val insideCardColor = if (isDarkTheme) Color(0xFF1E1E1E) else Color.White
    val textColor = if (isDarkTheme) Color(0xFFE0E0E0) else Color.Black

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush)
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
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = insideCardColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "Logo",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = insideCardColor),
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
                            color = textColor,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Ingresa tu correo electrónico y te enviaremos un enlace para restablecer tu contraseña.",
                            fontSize = 14.sp,
                            color = textColor.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                emailError =
                                    it.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(
                                        it
                                    ).matches()
                                if (it.isNotEmpty()) emailEmptyError = false
                            },
                            label = { Text("Correo electrónico", color = textColor) },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_email),
                                    contentDescription = "Email",
                                    tint = Color(0xFF00C851)
                                )
                            },
                            isError = emailError || emailEmptyError,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            textStyle = TextStyle(color = textColor),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF00C851),
                                unfocusedBorderColor = Color(0xFF00C851).copy(alpha = 0.5f),
                                errorBorderColor = Color.Red,
                                cursorColor = Color(0xFF00C851),
                                focusedLabelColor = Color(0xFF00C851),
                                unfocusedLabelColor = textColor.copy(alpha = 0.5f),
                                errorLabelColor = Color.Red
                            )
                        )

                        if ((emailError && email.isNotEmpty()) || emailEmptyError) {
                            Text(
                                text = if (emailEmptyError) "El correo es obligatorio" else "Correo inválido",
                                color = Color.Red,
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                emailEmptyError = email.isEmpty()
                                if (!emailEmptyError) {
                                    emailError = !android.util.Patterns.EMAIL_ADDRESS.matcher(email)
                                        .matches()
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
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (forgotPasswordState.isLoading) {
                                    Color(0xFFFF1744)
                                } else {
                                    Color(0xFF1B4F5C)
                                }
                            )
                        ) {
                            if (forgotPasswordState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = if (isDarkTheme) Color(0xFF00C853) else Color.Black,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "ENVIANDO...",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDarkTheme) Color(0xFF00C853) else Color.Black
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

                        TextButton(
                            onClick = { onBackToLogin() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Volver al inicio de sesión",
                                color = Color(0xFF00C851),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }

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
                    color = Color(0xFF00C851),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    forgotPasswordState.message
                        ?: "Te hemos enviado un enlace de recuperación a tu correo electrónico.",
                    color = textColor,
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
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C851))
                ) {
                    Text("Entendido", color = Color.White)
                }
            },
            containerColor = insideCardColor,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = {
                showErrorDialog = false
                viewModel.clearState()
            },
            title = {
                Text(
                    "Error",
                    color = Color.Red,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    forgotPasswordState.error ?: "Ha ocurrido un error. Intenta de nuevo.",
                    color = textColor
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showErrorDialog = false
                        viewModel.clearState()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Reintentar", color = Color.White)
                }
            },
            containerColor = insideCardColor,
            shape = RoundedCornerShape(16.dp)
        )
    }
}