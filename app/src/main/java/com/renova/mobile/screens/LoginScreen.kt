package com.renova.mobile.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
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
import androidx.compose.ui.unit.em
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import com.renova.mobile.R
import com.renova.mobile.viewmodel.LoginViewModel
import androidx.compose.animation.core.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.res.stringResource
import com.renova.mobile.network.User
import com.renova.mobile.ui.theme.*
import com.renova.mobile.ui.screens.TermsAndConditionsDialog
import com.renova.mobile.ui.theme.PoppinsFontFamily

// Color personalizado #50bd67
val CustomGreenColor = Color(0xFF50bd67)

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
                    contentDescription = stringResource(id = R.string.logo_description),
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
                            fontFamily = PoppinsFontFamily,
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
                                    contentDescription = stringResource(id = R.string.email),
                                    tint = colors.iconTint
                                )
                            },
                            isError = emailError || emailEmptyError,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            textStyle = TextStyle(
                                color = colors.textPrimary,
                                fontFamily = PoppinsFontFamily
                            ),
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
                                fontFamily = PoppinsFontFamily,
                                lineHeight = 14.sp,
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
                            fontFamily = PoppinsFontFamily,
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
                                    contentDescription = stringResource(id = R.string.login_password),
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
                            textStyle = TextStyle(
                                color = colors.textPrimary,
                                fontFamily = PoppinsFontFamily
                            ),
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
                                fontFamily = PoppinsFontFamily,
                                lineHeight = 14.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Remember Me Checkbox - Diseño circular mejorado
                        var rememberMe by remember { mutableStateOf(false) }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) { rememberMe = !rememberMe },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            // Checkbox circular personalizado
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(
                                        color = if (rememberMe) CustomGreenColor else Color.Transparent,
                                        shape = RoundedCornerShape(50)
                                    )
                                    .border(
                                        width = 2.dp,
                                        color = if (rememberMe) CustomGreenColor else colors.textSecondary.copy(alpha = 0.4f),
                                        shape = RoundedCornerShape(50)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (rememberMe) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_check),
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = stringResource(id = R.string.remember_me),
                                color = colors.textPrimary,
                                fontSize = 14.sp,
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Loading state
                        var isButtonLoading by remember { mutableStateOf(false) }

                        LaunchedEffect(loginState.isLoading) {
                            isButtonLoading = loginState.isLoading
                        }

                        // Login Button con color #50bd67
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
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CustomGreenColor,
                                disabledContainerColor = CustomGreenColor.copy(alpha = 0.6f)
                            )
                        ) {
                            if (isButtonLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.White,
                                    strokeWidth = 3.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(id = R.string.iniciando),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = PoppinsFontFamily,
                                    color = Color.White
                                )
                            } else {
                                Text(
                                    text = stringResource(id = R.string.login),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = PoppinsFontFamily,
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
                                fontFamily = PoppinsFontFamily,
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
                                fontFamily = PoppinsFontFamily,
                                textDecoration = TextDecoration.Underline
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Términos y Condiciones
                TermsAndPrivacyText(
                    onClick = { showTermsDialog = true }
                )
            }
        }
    }

    // Terms and Conditions Dialog
    TermsAndConditionsDialog(
        showDialog = showTermsDialog,
        onDismiss = { showTermsDialog = false }
    )

    // Success Dialog con auto-redirect
    if (showSuccessDialog) {
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(1000)
            showSuccessDialog = false
            viewModel.clearState()
            onLoginSuccess(
                loginState.user,
                loginState.token ?: "",
                loginState.tokenType ?: "Bearer",
                loginState.expiresAt
            )
        }

        AlertDialog(
            onDismissRequest = { },
            title = {
                Text(
                    text = stringResource(id = R.string.welcome_message),
                    color = CustomGreenColor,
                    fontWeight = FontWeight.Bold,
                    fontFamily = PoppinsFontFamily,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = CustomGreenColor,
                        strokeWidth = 4.dp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(id = R.string.logged),
                        color = colors.textPrimary,
                        fontFamily = PoppinsFontFamily,
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp
                    )
                }
            },
            confirmButton = { },
            containerColor = colors.cardBackground,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Error Dialog - MOSTRAR MENSAJE EXACTO DE LA API
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
                    fontWeight = FontWeight.Bold,
                    fontFamily = PoppinsFontFamily
                )
            },
            text = {
                // Mostrar el mensaje exacto que devuelve la API
                Text(
                    text = loginState.error ?: "Error inesperado",
                    color = colors.textPrimary,
                    fontFamily = PoppinsFontFamily
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
                    Text(
                        text = stringResource(id = R.string.retry),
                        color = Color.White,
                        fontFamily = PoppinsFontFamily
                    )
                }
            },
            containerColor = colors.cardBackground,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun TermsAndPrivacyText(onClick: () -> Unit) {
    val byContinuing = stringResource(id = R.string.terms_by_continuing)
    val termsOfService = stringResource(id = R.string.terms_of_service)
    val and = stringResource(id = R.string.terms_and)
    val privacyPolicy = stringResource(id = R.string.privacy_policy)

    val baseColor = Color.White
    val strokeColor = Color.White

    val annotatedText = buildAnnotatedString {
        withStyle(
            style = SpanStyle(
                color = baseColor,
                fontSize = 13.sp,
                fontFamily = PoppinsFontFamily
            )
        ) { append(byContinuing + " ") }

        withStyle(
            style = SpanStyle(
                color = baseColor,
                fontSize = 13.sp,
                fontFamily = PoppinsFontFamily,
                textDecoration = TextDecoration.Underline,
                fontWeight = FontWeight.Bold
            )
        ) { append(termsOfService) }

        withStyle(
            style = SpanStyle(
                color = baseColor,
                fontSize = 13.sp,
                fontFamily = PoppinsFontFamily
            )
        ) { append(" $and ") }

        withStyle(
            style = SpanStyle(
                color = baseColor,
                fontSize = 13.sp,
                fontFamily = PoppinsFontFamily,
                textDecoration = TextDecoration.Underline,
                fontWeight = FontWeight.Bold
            )
        ) { append(privacyPolicy) }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
            .border(
                width = 1.dp,
                color = Color.White,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = annotatedText,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawContext.canvas.nativeCanvas.apply {
                        val strokeWidth = 2f
                        val paint = android.graphics.Paint().apply {
                            style = android.graphics.Paint.Style.STROKE
                            this.strokeWidth = strokeWidth
                            color = android.graphics.Color.WHITE
                            textSize = 40f
                            textAlign = android.graphics.Paint.Align.LEFT
                            isAntiAlias = true
                        }
                    }
                },
            style = TextStyle(
                color = Color.White,
                fontSize = 13.sp,
                fontFamily = PoppinsFontFamily
            )
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
    val transition = rememberInfiniteTransition(label = "leafAnimation")

    leafPositions.forEach { leaf ->
        val animX by transition.animateFloat(
            initialValue = -20f,
            targetValue = 20f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 3500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "leafX"
        )

        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = leaf.alignment
        ) {
            Image(
                painter = painterResource(id = leaf.drawableId),
                contentDescription = null,
                modifier = Modifier
                    .size(250.dp)
                    .offset(
                        x = when (leaf.alignment) {
                            Alignment.TopEnd, Alignment.CenterEnd, Alignment.BottomEnd -> 60.dp
                            Alignment.TopStart, Alignment.CenterStart, Alignment.BottomStart -> (-60).dp
                            else -> 0.dp
                        },
                        y = when (leaf.alignment) {
                            Alignment.TopStart, Alignment.TopCenter, Alignment.TopEnd -> (-60).dp
                            Alignment.BottomStart, Alignment.BottomCenter, Alignment.BottomEnd -> 60.dp
                            else -> 0.dp
                        }
                    )
                    .graphicsLayer {
                        translationX = animX
                    }
            )
        }
    }
}