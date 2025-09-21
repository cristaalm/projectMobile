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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.renova.mobile.R
import com.renova.mobile.viewmodel.LoginViewModel
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onForgotPassword: () -> Unit = {},
    onLoginSuccess: () -> Unit = {},
    viewModel: LoginViewModel = viewModel()
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

    val loginState by viewModel.loginState.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(loginState) {
        when {
            loginState.isSuccess -> {
                showSuccessDialog = true
                showErrorDialog = false
            }
            loginState.error != null -> {
                showErrorDialog = true
                showSuccessDialog = false
            }
            else -> {
                showSuccessDialog = false
                showErrorDialog = false
            }
        }
    }

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
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            emailError = it.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(it).matches()
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

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            if (it.length <= 14) {
                                password = it
                                passwordError = it.isNotEmpty() && !validatePassword(it)
                                if (it.isNotEmpty()) passwordEmptyError = false
                            }
                        },
                        label = { Text("Contraseña") },
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

                    if ((passwordError && password.isNotEmpty()) || passwordEmptyError) {
                        Text(
                            text = if (passwordEmptyError) "La contraseña es obligatoria" else "Debe tener al menos 6 caracteres, letras, números y un símbolo",
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            emailEmptyError = email.isEmpty()
                            passwordEmptyError = password.isEmpty()

                            if (!emailEmptyError && !passwordEmptyError) {
                                emailError = !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
                                passwordError = !validatePassword(password)

                                if (!emailError && !passwordError) {
                                    viewModel.login(email, password)
                                }
                            }
                        },
                        enabled = !loginState.isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1B4F5C)
                        )
                    ) {
                        if (loginState.isLoading) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )

                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "INICIANDO...",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        } else {
                            Text(
                                "INICIAR SESIÓN",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = {
                            // Lógica para crear cuenta
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
                            "CREAR CUENTA",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00C851)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(
                        onClick = { onForgotPassword() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "¿Olvidaste tu contraseña?",
                            color = Color(0xFF00C851),
                            fontSize = 14.sp,
                            textDecoration = TextDecoration.Underline
                        )
                    }
                }
            }
        }

        if (showSuccessDialog) {
            AlertDialog(
                onDismissRequest = {
                    showSuccessDialog = false
                    viewModel.clearState()
                    onLoginSuccess()
                },
                title = {
                    Text(
                        text = "¡Bienvenido!",
                        color = Color(0xFF1B4F5C),
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = loginState.message ?: "Has iniciado sesión correctamente.",
                        color = Color.Black
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showSuccessDialog = false
                            viewModel.clearState()
                            onLoginSuccess()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00C851)
                        )
                    ) {
                        Text("Continuar", color = Color.White)
                    }
                },
                containerColor = Color.White,
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
                        text = "Error de acceso",
                        color = Color.Red,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = loginState.error ?: "Correo o contraseña incorrectos. Por favor verifica tus datos.",
                        color = Color.Black
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showErrorDialog = false
                            viewModel.clearState()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red
                        )
                    ) {
                        Text("Reintentar", color = Color.White)
                    }
                },
                containerColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

fun validatePassword(password: String): Boolean {
    val regex = Regex("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,}$")
    return regex.matches(password)
}
