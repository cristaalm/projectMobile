package com.renova.mobile.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.renova.mobile.R

@Preview
@Composable
fun LoginScreen(
    onForgotPassword: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var emailEmptyError by remember { mutableStateOf(false) }
    var passwordEmptyError by remember { mutableStateOf(false) }

    // Fondo degradado
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

            // LOGIN CARD
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
                    // CORREO
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            // Solo mostrar error si hay texto y es inválido
                            emailError =
                                it.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(it)
                                    .matches()
                            // Limpiar error de campo vacío si el usuario empieza a escribir
                            if (it.isNotEmpty()) emailEmptyError = false
                        },
                        label = { Text(stringResource(id = R.string.email)) },
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
                            text = if (emailEmptyError) {
                                stringResource(id = R.string.email_required)
                            } else {
                                stringResource(id = R.string.email_invalid)
                            },
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // CONTRASEÑA
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            // Limitar a 14 caracteres
                            if (it.length <= 14) {
                                password = it
                                // Solo mostrar error si hay texto y es inválido
                                passwordError = it.isNotEmpty() && !validatePassword(it)
                                // Limpiar error de campo vacío si el usuario empieza a escribir
                                if (it.isNotEmpty()) passwordEmptyError = false
                            }
                        },
                        label = { Text(text = stringResource(id = R.string.login_password)) },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_lock),
                                contentDescription = "Lock",
                                tint = Color(0xFF00C851)
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    painter = painterResource(
                                        id = if (passwordVisible) R.drawable.ic_visibility_off
                                        else R.drawable.ic_visibility
                                    ),
                                    contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                                    tint = Color(0xFF00C851)
                                )
                            }
                        },
                        isError = passwordError || passwordEmptyError,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
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
                    if ((passwordError && password.isNotEmpty()) || passwordEmptyError) {
                        Text(
                            text = if (passwordEmptyError) {
                                stringResource(id = R.string.required_password)
                            } else {
                                stringResource(id = R.string.password_characters)
                            },
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // BOTÓN INICIAR SESIÓN
                    Button(
                        onClick = {
                            // Validar campos vacíos primero
                            emailEmptyError = email.isEmpty()
                            passwordEmptyError = password.isEmpty()

                            // Si no están vacíos, validar formato
                            if (!emailEmptyError && !passwordEmptyError) {
                                emailError =
                                    !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
                                passwordError = !validatePassword(password)

                                if (!emailError && !passwordError) {
                                    // Validar credenciales de simulación
                                    if (email == "admin@gmail.com" && password == "admin123#") {
                                        showSuccessDialog = true
                                    } else {
                                        showErrorDialog = true
                                    }
                                }
                            }
                            // Si hay campos vacíos, no hacer nada más (solo mostrar errores)
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
                            text = stringResource(id = R.string.login),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // BOTÓN CREAR CUENTA
                    OutlinedButton(
                        onClick = {
                            // Aquí va la lógica para crear cuenta
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFF00C851), Color(0xFF00C851))
                            )
                        )
                    ) {
                        Text(
                            text = stringResource(id = R.string.create_account),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00C851)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // LINK RECUPERAR CONTRASEÑA
                    TextButton(
                        onClick = { onForgotPassword() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(id = R.string.forgot_password),
                            color = Color(0xFF00C851),
                            fontSize = 14.sp,
                            textDecoration = TextDecoration.Underline
                        )
                    }
                }
            }
        }

        // Modal de éxito
        if (showSuccessDialog) {
            AlertDialog(
                onDismissRequest = { showSuccessDialog = false },
                title = {
                    Text(
                        text = stringResource(id = R.string.welcome_message),
                        color = Color(0xFF1B4F5C),
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = stringResource(id = R.string.logged),
                        color = Color.Black
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { showSuccessDialog = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00C851)
                        )
                    ) {
                        Text(text = stringResource(id = R.string.continuar), color = Color.White)
                    }
                },
                containerColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            )
        }

        // Modal de error de credenciales
        if (showErrorDialog) {
            AlertDialog(
                onDismissRequest = { showErrorDialog = false },
                title = {
                    Text(
                        text = stringResource(id = R.string.error_access),
                        color = Color.Red,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = stringResource(id = R.string.incorrect_information),
                        color = Color.Black
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { showErrorDialog = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red
                        )
                    ) {
                        Text(stringResource(id = R.string.retry))
                    }
                },
                containerColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

// Validación de contraseña
fun validatePassword(password: String): Boolean {
    val regex = Regex("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@\$!%*?&#])[A-Za-z\\d@\$!%*?&#]{6,}$")
    return regex.matches(password)
}