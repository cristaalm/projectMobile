package com.renova.mobile.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import com.renova.mobile.R

@Composable
fun ForgotPasswordScreen(
    onBackToLogin: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf(false) }
    var emailEmptyError by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    // Fondo degradado igual al login
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF00E676),
            Color(0xFF00C853),
            Color(0xFF00A843),
            Color(0xFF1B5E20)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // LOGO CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
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

            // FORGOT PASSWORD CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // TÍTULO
                    Text(
                        text = "Recuperar Contraseña",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B4F5C),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Ingresa tu correo electrónico y te enviaremos un enlace para restablecer tu contraseña.",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // CORREO
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            // Solo mostrar error si hay texto y es inválido
                            emailError = it.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(it).matches()
                            // Limpiar error de campo vacío si el usuario empieza a escribir
                            if (it.isNotEmpty()) emailEmptyError = false
                        },
                        label = { Text("Correo electrónico") },
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
                        textStyle = TextStyle(color = Color.Black),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00C851),
                            unfocusedBorderColor = Color(0xFF00C851).copy(alpha = 0.5f),
                            errorBorderColor = Color.Red,
                            cursorColor = Color(0xFF00C851),
                            focusedLabelColor = Color(0xFF00C851),
                            unfocusedLabelColor = Color.Gray
                        )
                    )
                    // Solo mostrar error si hay texto y es inválido O si está vacío y se intentó enviar
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

                    // BOTÓN ENVIAR
                    Button(
                        onClick = {
                            // Validar campo vacío primero
                            emailEmptyError = email.isEmpty()

                            // Si no está vacío, validar formato
                            if (!emailEmptyError) {
                                emailError = !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()

                                if (!emailError) {
                                    showSuccessDialog = true
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1B4F5C)
                        )
                    ) {
                        Text(
                            "ENVIAR ENLACE",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // BOTÓN VOLVER AL LOGIN
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

        // Modal de éxito
        if (showSuccessDialog) {
            AlertDialog(
                onDismissRequest = {
                    showSuccessDialog = false
                    onBackToLogin()
                },
                title = {
                    Text(
                        text = "¡Correo enviado!",
                        color = Color(0xFF00C851),
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = "Se ha enviado un enlace de recuperación a tu correo electrónico. Revisa tu bandeja de entrada.",
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showSuccessDialog = false
                            onBackToLogin()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00C851)
                        )
                    ) {
                        Text("Entendido", color = Color.White)
                    }
                },
                containerColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}