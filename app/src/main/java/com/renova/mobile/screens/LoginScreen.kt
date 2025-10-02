package com.renova.mobile.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.blur
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import com.renova.mobile.R
import com.renova.mobile.viewmodel.LoginViewModel
import androidx.compose.animation.core.*
import androidx.compose.ui.res.stringResource
import com.renova.mobile.network.User
import com.renova.mobile.ui.theme.*
import com.renova.mobile.ui.screens.TermsAndConditionsDialog

@Composable
fun LoginScreen(
    onForgotPassword: () -> Unit = {},
    onCreateAccount: () -> Unit = {},
    onLoginSuccess: (User?, String, String, String?) -> Unit = { _, _, _, _ -> },
    viewModel: LoginViewModel = viewModel()
) {
    // Usar los colores del tema
    val colors = MaterialTheme.renovaColors

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var emailEmptyError by remember { mutableStateOf(false) }
    var passwordEmptyError by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RenovaGradients.backgroundGradient())
            .padding(24.dp)
            .then(
                if (showTermsDialog) Modifier.blur(8.dp) else Modifier
            )
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

                // Login Form Card
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
                        // Email Field con label externo
                        Text(
                            text = stringResource(id = R.string.email),
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
                                text = if (emailEmptyError) {
                                    stringResource(id = R.string.email_required)
                                } else {
                                    stringResource(id = R.string.email_invalid)
                                },
                                color = RenovaColors.Error,
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Password Field con label externo
                        Text(
                            text = stringResource(id = R.string.login_password),
                            color = colors.textPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 4.dp)
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                if (it.length <= 14) {
                                    password = it
                                    passwordError = it.isNotEmpty() && !validatePassword(it)
                                    if (it.isNotEmpty()) passwordEmptyError = false
                                }
                            },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_lock),
                                    contentDescription = "Lock",
                                    tint = colors.iconTint
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
                                        tint = colors.iconTint
                                    )
                                }
                            },
                            isError = passwordError || passwordEmptyError,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            textStyle = TextStyle(color = colors.textPrimary),
                            colors = RenovaComponentColors.textFieldColors()
                        )

                        if ((passwordError && password.isNotEmpty()) || passwordEmptyError) {
                            Text(
                                text = if (passwordEmptyError) {
                                    stringResource(id = R.string.required_password)
                                } else {
                                    stringResource(id = R.string.password_characters)
                                },
                                color = RenovaColors.Error,
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Loading state
                        var isButtonLoading by remember { mutableStateOf(false) }

                        LaunchedEffect(loginState.isLoading) {
                            isButtonLoading = loginState.isLoading
                        }

                        // Login Button
                        Button(
                            onClick = {
                                emailEmptyError = email.isEmpty()
                                passwordEmptyError = password.isEmpty()

                                if (!emailEmptyError && !passwordEmptyError) {
                                    emailError = !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
                                    passwordError = !validatePassword(password)

                                    if (!emailError && !passwordError) {
                                        isButtonLoading = true
                                        viewModel.login(email, password)
                                    }
                                }
                            },
                            enabled = !isButtonLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = if (isButtonLoading) {
                                RenovaComponentColors.loadingButtonColors(androidx.compose.foundation.isSystemInDarkTheme())
                            } else {
                                RenovaComponentColors.primaryButtonColors()
                            }
                        ) {
                            if (isButtonLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = if (androidx.compose.foundation.isSystemInDarkTheme()) RenovaColors.Primary else Color.Black,
                                    strokeWidth = 3.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(id = R.string.iniciando),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (androidx.compose.foundation.isSystemInDarkTheme()) RenovaColors.Primary else Color.Black
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

                        // Create Account Button
                        OutlinedButton(
                            onClick = { onCreateAccount() },
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
                                text = stringResource(id = R.string.create_account),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = RenovaColors.Primary
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Forgot Password
                        TextButton(
                            onClick = { onForgotPassword() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(id = R.string.forgot_password),
                                color = RenovaColors.Primary,
                                fontSize = 14.sp,
                                textDecoration = TextDecoration.Underline
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Términos y Condiciones
                val annotatedText = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = RenovaColors.Primary, fontSize = 13.sp)) {
                        append("Al continuar, aceptas nuestros ")
                    }
                    withStyle(
                        style = SpanStyle(
                            color = RenovaColors.Primary,
                            fontSize = 13.sp,
                            textDecoration = TextDecoration.Underline,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append("Términos de Servicio")
                    }
                    withStyle(style = SpanStyle(color = RenovaColors.Primary, fontSize = 13.sp)) {
                        append(" y ")
                    }
                    withStyle(
                        style = SpanStyle(
                            color = RenovaColors.Primary,
                            fontSize = 13.sp,
                            textDecoration = TextDecoration.Underline,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append("Política de Privacidad")
                    }
                }

                Text(
                    text = annotatedText,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                        .clickable { showTermsDialog = true }
                )
            }
        }
    }

    // Terms and Conditions Dialog
    TermsAndConditionsDialog(
        showDialog = showTermsDialog,
        onDismiss = { showTermsDialog = false }
    )

    // Success Dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                viewModel.clearState()
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
                    color = colors.textPrimary
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
                    colors = ButtonDefaults.buttonColors(containerColor = RenovaColors.Primary)
                ) {
                    Text(
                        text = stringResource(id = R.string.continuar),
                        color = Color.White
                    )
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
                    text = stringResource(id = R.string.error_access),
                    color = RenovaColors.Error,
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

                Text(errorMessage, color = colors.textPrimary)
            },
            confirmButton = {
                Button(
                    onClick = {
                        showErrorDialog = false
                        viewModel.clearState()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RenovaColors.Error)
                ) {
                    Text(
                        text = stringResource(id = R.string.retry),
                        color = Color.White
                    )
                }
            },
            containerColor = colors.cardBackground,
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