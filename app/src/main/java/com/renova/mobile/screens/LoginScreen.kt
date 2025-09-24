package com.renova.mobile.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
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
import androidx.compose.runtime.collectAsState
import com.renova.mobile.R
import com.renova.mobile.viewmodel.LoginViewModel
import androidx.compose.animation.core.*
import androidx.compose.ui.res.stringResource
import com.renova.mobile.network.User

@Composable
fun LoginScreen(
    onForgotPassword: () -> Unit = {},
    onCreateAccount: () -> Unit = {},
    onLoginSuccess: (User?, String, String, String?) -> Unit = { _, _, _, _ -> },
    viewModel: LoginViewModel = viewModel()
) {
    val isDarkTheme = isSystemInDarkTheme()

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
                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                emailError = it.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(it).matches()
                                if (it.isNotEmpty()) emailEmptyError = false
                            },
                            label = { Text(text = stringResource(id = R.string.email), color = textColor) },
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

                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                if (it.length <= 14) {
                                    password = it
                                    passwordError = it.isNotEmpty() && !validatePassword(it)
                                    if (it.isNotEmpty()) passwordEmptyError = false
                                }
                            },
                            label = { Text(text = stringResource(id = R.string.login_password), color = textColor) },
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
                                        contentDescription = if (passwordVisible) {
                                            stringResource(id = R.string.hide_password)
                                        } else {
                                            stringResource(id = R.string.show_password)
                                        },
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

                        // Variable local para controlar el estado de loading visual
                        var isButtonLoading by remember { mutableStateOf(false) }

                        // Sincronizar con el estado del ViewModel
                        LaunchedEffect(loginState.isLoading) {
                            isButtonLoading = loginState.isLoading
                        }

                        Button(
                            onClick = {
                                emailEmptyError = email.isEmpty()
                                passwordEmptyError = password.isEmpty()

                                if (!emailEmptyError && !passwordEmptyError) {
                                    emailError = !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
                                    passwordError = !validatePassword(password)

                                    if (!emailError && !passwordError) {
                                        isButtonLoading = true  // Activar loading inmediatamente
                                        viewModel.login(email, password)
                                    }
                                }
                            },
                            enabled = !isButtonLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isButtonLoading) {
                                    if (isDarkTheme) Color(0xFF00C853) else Color(0xFF212121)
                                } else {
                                    Color(0xFF1B4F5C)
                                }
                            )
                        ) {
                            if (isButtonLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = if (isDarkTheme) Color(0xFF00C853) else Color.Black,
                                    strokeWidth = 3.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(id = R.string.iniciando),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDarkTheme) Color(0xFF00C853) else Color.Black
                                )
                            } else {
                                Text(
                                    text = stringResource(id = R.string.login),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = { onCreateAccount() },
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
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                viewModel.clearState()

                // Pasar los datos de la sesión
                val state = loginState
                onLoginSuccess(
                    state.user,
                    state.token ?: "",
                    state.tokenType ?: "Bearer",
                    state.expiresAt
                )
            },
            text = {
                Text(
                    loginState.message ?: stringResource(id = R.string.logged),
                    color = textColor
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        viewModel.clearState()
                        onLoginSuccess(loginState.user,
                            loginState.token ?: "",
                            loginState.tokenType ?: "Bearer",
                            loginState.expiresAt)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C851))
                ) { Text(text = stringResource(id = R.string.continuar), color = Color.White) }
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
                    text = stringResource(id = R.string.error_access),
                    color = Color.Red,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                val errorMessage = when {
                    loginState.error?.contains("user not found", ignoreCase = true) == true ||
                            loginState.error?.contains("email not found", ignoreCase = true) == true ||
                            loginState.error?.contains("no existe", ignoreCase = true) == true ->
                        stringResource(id = R.string.mail_not_registered)

                    loginState.error?.contains("invalid password", ignoreCase = true) == true ||
                            loginState.error?.contains("wrong password", ignoreCase = true) == true ||
                            loginState.error?.contains("contraseña", ignoreCase = true) == true ->
                        stringResource(id = R.string.incorrect_ppassword)

                    loginState.error?.contains("account blocked", ignoreCase = true) == true ||
                            loginState.error?.contains("blocked", ignoreCase = true) == true ->
                        stringResource(id = R.string.blocked_account)

                    loginState.error?.contains("network", ignoreCase = true) == true ||
                            loginState.error?.contains("connection", ignoreCase = true) == true ->
                        stringResource(id = R.string.connection_internet_filed)

                    loginState.error?.contains("server", ignoreCase = true) == true ->
                        stringResource(id = R.string.error_server)

                    loginState.error?.contains("timeout", ignoreCase = true) == true ->
                        stringResource(id = R.string.connection_timeout)

                    else -> loginState.error ?: stringResource(id = R.string.error_unknown)
                }

                Text(errorMessage, color = textColor)
            },
            confirmButton = {
                Button(
                    onClick = {
                        showErrorDialog = false
                        viewModel.clearState()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text(text = stringResource(id = R.string.retry), color = Color.White) }
            },
            containerColor = insideCardColor,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

fun validatePassword(password: String): Boolean {
    val regex = Regex("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,}$")
    return regex.matches(password)
}

data class LeafPosition(val drawableId: Int, val alignment: Alignment)

@Composable
fun SubtleLeavesBackground(modifier: Modifier = Modifier, leafPositions: List<LeafPosition>) {
    val transition = rememberInfiniteTransition()

    leafPositions.forEach { leaf ->
        val animX by transition.animateFloat(
            initialValue = -10f,
            targetValue = 10f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 3000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        )

        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = leaf.alignment
        ) {
            Image(
                painter = painterResource(id = leaf.drawableId),
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .graphicsLayer {
                        translationX = animX
                    }
            )
        }
    }
}