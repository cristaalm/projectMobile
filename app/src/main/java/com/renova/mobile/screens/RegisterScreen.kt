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
import androidx.compose.ui.platform.LocalContext
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
import com.renova.mobile.ui.viewmodels.RegisterState
import com.renova.mobile.ui.viewmodels.RegisterViewModel
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
    onContinueToDocuments: (RegisterData) -> Unit = {},
    viewModel: RegisterViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val colors = MaterialTheme.renovaColors
    val scrollState = rememberScrollState()
    val registerState by viewModel.registerState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.setSessionManager(context)
    }

    // ✅ DESPUÉS: Obtener datos del ViewModel
    val savedFormData by viewModel.formData.collectAsState()

    var firstName by remember(savedFormData) { mutableStateOf(savedFormData.firstName) }
    var lastName by remember(savedFormData) { mutableStateOf(savedFormData.lastName) }
    var email by remember(savedFormData) { mutableStateOf(savedFormData.email) }
    var phone by remember(savedFormData) { mutableStateOf(savedFormData.phone) }
    var curp by remember(savedFormData) { mutableStateOf(savedFormData.curp) }
    var password by remember(savedFormData) { mutableStateOf(savedFormData.password) }
    var confirmPassword by remember(savedFormData) { mutableStateOf(savedFormData.password) }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var firstNameValidation by remember { mutableStateOf(ValidationState.IDLE) }
    var lastNameValidation by remember { mutableStateOf(ValidationState.IDLE) }
    var emailValidation by remember { mutableStateOf(ValidationState.IDLE) }
    var phoneValidation by remember { mutableStateOf(ValidationState.IDLE) }
    var curpValidation by remember { mutableStateOf(ValidationState.IDLE) }
    var passwordValidation by remember { mutableStateOf(ValidationState.IDLE) }
    var confirmPasswordValidation by remember { mutableStateOf(ValidationState.IDLE) }

    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // ✅ CORREGIDO: Observar el estado del registro SIN volver a llamar registerUser
    LaunchedEffect(registerState) {
        when (registerState) {
            is RegisterState.Success -> {
                // Solo navegar a la siguiente pantalla
                val data = RegisterData(
                    firstName = firstName,
                    lastName = lastName,
                    email = email,
                    phone = phone,
                    curp = curp,
                    password = password
                )
                onContinueToDocuments(data)
            }

            is RegisterState.Error -> {
                errorMessage = (registerState as RegisterState.Error).message
                showErrorDialog = true
            }

            else -> {}
        }
    }

    // Diálogo de error
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = {
                Text(
                    text = "Error",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = errorMessage,
                    fontFamily = Poppins
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showErrorDialog = false
                        viewModel.resetStates()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CustomGreenColor
                    )
                ) {
                    Text("Aceptar", fontFamily = Poppins)
                }
            }
        )
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
                .imePadding()
                .navigationBarsPadding()
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
                        value = curp,
                        onValueChange = {
                            if (it.length <= 18) curp = it.uppercase()
                        },
                        label = stringResource(R.string.document_curp_number),
                        leadingIcon = R.drawable.document,
                        validationState = curpValidation,
                        onValidationChange = { curpValidation = it },
                        validator = { validateCURP(it) },
                        keyboardType = KeyboardType.Text,
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
                        ),
                        enabled = registerState !is RegisterState.Loading
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
                                    curpValidation == ValidationState.VALID &&
                                    passwordValidation == ValidationState.VALID &&
                                    confirmPasswordValidation == ValidationState.VALID

                            if (allValid && registerState !is RegisterState.Loading) {
                                val data = RegisterData(
                                    firstName = firstName,
                                    lastName = lastName,
                                    email = email,
                                    phone = phone,
                                    curp = curp,
                                    password = password
                                )
                                // ✅ Guardar en el ViewModel ANTES de registrar
                                viewModel.updateFormData(data)
                                viewModel.registerUser(data)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CustomGreenColor
                        ),
                        enabled = registerState !is RegisterState.Loading
                    ) {
                        if (registerState is RegisterState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
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
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ==================== CAMPOS Y VALIDACIONES ====================

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
            val errorText = when {
                label.contains(stringResource(R.string.password), ignoreCase = true) &&
                        !label.contains(stringResource(R.string.confirm_password), ignoreCase = true) ->
                    getPasswordError(value)
                label.contains(stringResource(R.string.first_name), ignoreCase = true) ||
                        label.contains(stringResource(R.string.last_name), ignoreCase = true) ->
                    getNameError(value)
                else -> getErrorMessage(label)
            }

            Text(
                text = errorText,
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
    val curp: String,
    val password: String
)

// ==================== VALIDADORES ====================

fun validateName(name: String): Boolean =
    name.length >= 2 &&
            name.all { it.isLetter() || it.isWhitespace() } &&
            !name.any { it.isDigit() }

fun validateEmail(email: String): Boolean =
    android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()

fun validatePhone(phone: String): Boolean =
    phone.length == 10 && phone.all { it.isDigit() }

fun validateCURP(curp: String): Boolean {
    val curpPattern = "^[A-Z]{4}\\d{6}[HM][A-Z]{2}[BCDFGHJKLMNPQRSTVWXYZ]{3}[0-9A-Z]\\d$"
    return curp.matches(curpPattern.toRegex())
}


fun validateConfirmPassword(password: String, confirmPassword: String): Boolean =
    password == confirmPassword && password.isNotEmpty()

// ==================== MENSAJES DE ERROR DINÁMICOS ====================
@Composable
fun getPasswordError(password: String): String {
    return when {
        password.length < 8 -> stringResource(R.string.validation_err_pwd_min_length)
        !password.any { it.isDigit() } -> stringResource(R.string.validation_err_pwd_need_number)
        !password.any { it in "!@#$%^&*()_+-=[]{};':\"\\|,.<>/?`~" } ->
            stringResource(R.string.validation_err_pwd_need_special)
        else -> ""
    }
}

@Composable
fun getNameError(name: String): String {
    return when {
        name.any { it.isDigit() } -> stringResource(R.string.validation_err_name_no_digits)
        name.length < 2 -> stringResource(R.string.validation_err_name_min_chars)
        else -> stringResource(R.string.validation_err_name_invalid)
    }
}

@Composable
fun getErrorMessage(label: String): String {
    return when {
        label.contains(stringResource(R.string.email), ignoreCase = true) ->
            stringResource(R.string.validation_err_email_invalid)
        label.contains(stringResource(R.string.phone_number), ignoreCase = true) ->
            stringResource(R.string.validation_err_phone_invalid)
        label.contains(stringResource(R.string.document_curp_number), ignoreCase = true) ->
            stringResource(R.string.validation_err_curp_invalid)
        label.contains(stringResource(R.string.confirm_password), ignoreCase = true) ->
            stringResource(R.string.validation_err_pwd_no_match)
        else -> stringResource(R.string.validation_err_field_invalid)
    }
}