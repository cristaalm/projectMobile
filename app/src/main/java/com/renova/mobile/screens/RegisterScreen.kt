package com.renova.mobile.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.renova.mobile.R
import com.renova.mobile.ui.theme.*
import kotlinx.coroutines.delay

val Poppins = FontFamily(
    Font(R.font.poppins_light, FontWeight.Light),
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_bold, FontWeight.Bold)
)

@Composable
fun RegisterScreen(
    onBackToLogin: () -> Unit = {},
    onContinueToDocuments: (RegisterData) -> Unit = {}
) {
    val colors = MaterialTheme.renovaColors
    val scrollState = rememberScrollState()

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var firstNameValidation by remember { mutableStateOf(ValidationState.IDLE) }
    var lastNameValidation by remember { mutableStateOf(ValidationState.IDLE) }
    var emailValidation by remember { mutableStateOf(ValidationState.IDLE) }
    var phoneValidation by remember { mutableStateOf(ValidationState.IDLE) }
    var passwordValidation by remember { mutableStateOf(ValidationState.IDLE) }
    var confirmPasswordValidation by remember { mutableStateOf(ValidationState.IDLE) }

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
                text = stringResource(R.string.register_title),
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Poppins,
                textAlign = TextAlign.Center
            )

            Text(
                text = stringResource(R.string.complete_information),
                color = Color.White,
                fontSize = 16.sp,
                fontFamily = Poppins,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = stringResource(R.string.step_1_of_3),
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
                        .fillMaxWidth(1f / 3f)
                        .fillMaxHeight()
                        .background(Color.White)
                )
            }

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
                    Text(
                        text = stringResource(R.string.personal_data),
                        color = colors.textPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = Poppins,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    ValidatedTextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = stringResource(R.string.first_name),
                        leadingIcon = R.drawable.usuario_relleno,
                        validationState = firstNameValidation,
                        onValidationChange = { firstNameValidation = it },
                        validator = { validateName(it) },
                        colors = colors
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    ValidatedTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = stringResource(R.string.last_name),
                        leadingIcon = R.drawable.usuario_relleno,
                        validationState = lastNameValidation,
                        onValidationChange = { lastNameValidation = it },
                        validator = { validateName(it) },
                        colors = colors
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    ValidatedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = stringResource(R.string.email),
                        leadingIcon = R.drawable.ic_email,
                        validationState = emailValidation,
                        onValidationChange = { emailValidation = it },
                        validator = { validateEmail(it) },
                        keyboardType = KeyboardType.Email,
                        colors = colors
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    ValidatedTextField(
                        value = phone,
                        onValueChange = {
                            if (it.length <= 10 && it.all { char -> char.isDigit() }) {
                                phone = it
                            }
                        },
                        label = stringResource(R.string.phone_number),
                        leadingIcon = R.drawable.phone,
                        validationState = phoneValidation,
                        onValidationChange = { phoneValidation = it },
                        validator = { validatePhone(it) },
                        keyboardType = KeyboardType.Phone,
                        colors = colors
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    ValidatedTextField(
                        value = password,
                        onValueChange = {
                            if (it.length <= 14) password = it
                        },
                        label = stringResource(R.string.password),
                        leadingIcon = R.drawable.ic_lock,
                        validationState = passwordValidation,
                        onValidationChange = { passwordValidation = it },
                        validator = { validatePassword(it) },
                        keyboardType = KeyboardType.Password,
                        isPassword = true,
                        passwordVisible = passwordVisible,
                        onPasswordVisibilityChange = { passwordVisible = it },
                        colors = colors
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    ValidatedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            if (it.length <= 14) confirmPassword = it
                        },
                        label = stringResource(R.string.confirm_password),
                        leadingIcon = R.drawable.ic_lock,
                        validationState = confirmPasswordValidation,
                        onValidationChange = { confirmPasswordValidation = it },
                        validator = { validateConfirmPassword(password, it) },
                        keyboardType = KeyboardType.Password,
                        isPassword = true,
                        passwordVisible = confirmPasswordVisible,
                        onPasswordVisibilityChange = { confirmPasswordVisible = it },
                        colors = colors
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedButton(
                        onClick = { onBackToLogin() },
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
                            val allValid = firstNameValidation == ValidationState.VALID &&
                                    lastNameValidation == ValidationState.VALID &&
                                    emailValidation == ValidationState.VALID &&
                                    phoneValidation == ValidationState.VALID &&
                                    passwordValidation == ValidationState.VALID &&
                                    confirmPasswordValidation == ValidationState.VALID

                            if (allValid) {
                                onContinueToDocuments(
                                    RegisterData(
                                        firstName = firstName,
                                        lastName = lastName,
                                        email = email,
                                        phone = phone,
                                        password = password
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
                            text = stringResource(R.string.continue_button),
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
fun ValidatedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: Int,
    validationState: ValidationState,
    onValidationChange: (ValidationState) -> Unit,
    validator: (String) -> Boolean,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordVisibilityChange: (Boolean) -> Unit = {},
    colors: RenovaColorScheme
) {
    var isValidating by remember { mutableStateOf(false) }

    LaunchedEffect(value) {
        if (value.isEmpty()) {
            onValidationChange(ValidationState.IDLE)
            return@LaunchedEffect
        }

        onValidationChange(ValidationState.VALIDATING)
        isValidating = true
        delay(800)

        val isValid = validator(value)
        onValidationChange(if (isValid) ValidationState.VALID else ValidationState.ERROR)
        isValidating = false
    }

    Column {
        Text(
            text = label,
            color = colors.textPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = Poppins,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp)
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            leadingIcon = {
                Icon(
                    painter = painterResource(id = leadingIcon),
                    contentDescription = label,
                    tint = colors.iconTint
                )
            },
            trailingIcon = {
                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AnimatedVisibility(
                        visible = validationState != ValidationState.IDLE,
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut()
                    ) {
                        when (validationState) {
                            ValidationState.VALIDATING -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = CustomGreenColor,
                                    strokeWidth = 2.dp
                                )
                            }

                            ValidationState.VALID -> {
                                Icon(
                                    painter = painterResource(id = R.drawable.cheque),
                                    contentDescription = stringResource(R.string.valid),
                                    tint = CustomGreenColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            ValidationState.ERROR -> {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_close_2),
                                    contentDescription = stringResource(R.string.error),
                                    tint = RenovaColors.Error,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            else -> {}
                        }
                    }

                    if (isPassword) {
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = { onPasswordVisibilityChange(!passwordVisible) }) {
                            Icon(
                                painter = painterResource(
                                    id = if (passwordVisible) R.drawable.ic_visibility_off
                                    else R.drawable.ic_visibility
                                ),
                                contentDescription = if (passwordVisible)
                                    stringResource(R.string.hide)
                                else
                                    stringResource(R.string.show),
                                tint = colors.iconTint
                            )
                        }
                    }
                }
            },
            visualTransformation = if (isPassword && !passwordVisible)
                PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            textStyle = TextStyle(
                color = colors.textPrimary,
                fontFamily = Poppins
            ),
            colors = RenovaComponentColors.textFieldColors(),
            isError = validationState == ValidationState.ERROR
        )

        AnimatedVisibility(
            visible = validationState == ValidationState.ERROR,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Text(
                text = getErrorMessage(label),
                color = RenovaColors.Error,
                fontSize = 12.sp,
                fontFamily = Poppins,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

enum class ValidationState {
    IDLE, VALIDATING, VALID, ERROR
}

data class RegisterData(
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val password: String
)

fun validateName(name: String): Boolean =
    name.length >= 2 && name.all { it.isLetter() || it.isWhitespace() }

fun validateEmail(email: String): Boolean =
    android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()

fun validatePhone(phone: String): Boolean =
    phone.length == 10 && phone.all { it.isDigit() }

fun validateConfirmPassword(password: String, confirmPassword: String): Boolean =
    password == confirmPassword && password.isNotEmpty()

@Composable
fun getErrorMessage(label: String): String {
    return when {
        label.contains(stringResource(R.string.first_name), ignoreCase = true) ||
                label.contains(stringResource(R.string.last_name), ignoreCase = true) ->
            stringResource(R.string.error_name_min_length)

        label.contains(stringResource(R.string.email), ignoreCase = true) ->
            stringResource(R.string.email_invalid)

        label.contains(stringResource(R.string.phone_number), ignoreCase = true) ->
            stringResource(R.string.error_phone_digits)

        label.contains(stringResource(R.string.password), ignoreCase = true) &&
                !label.contains(stringResource(R.string.confirm_password), ignoreCase = true) ->
            stringResource(R.string.error_password_requirements)

        label.contains(stringResource(R.string.confirm_password), ignoreCase = true) ->
            stringResource(R.string.error_passwords_not_match)

        else -> stringResource(R.string.error_invalid_field)
    }
}