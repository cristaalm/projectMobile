package com.renova.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.renova.mobile.R
import com.renova.mobile.ui.viewmodels.ProfileViewModel
import com.renova.mobile.ui.viewmodels.ProfileViewModel.*
import com.renova.mobile.network.IdentityVerification
import com.renova.mobile.network.UserData
import com.renova.mobile.ui.theme.LocalRenovaColors
import com.renova.mobile.ui.theme.RenovaColors
import com.renova.mobile.ui.viewmodels.VerificationStatus
import coil.compose.rememberAsyncImagePainter
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.layout.ContentScale
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.renova.mobile.screens.ValidationState
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.rotate


@Composable
fun ProfileField(
    label: String,
    value: String,
    icon: ImageVector,
    verificationStatus: VerificationStatus,
    isMono: Boolean = false
) {
    val colors = LocalRenovaColors.current

    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.textSecondary,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = colors.textSecondary
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = value,
                style = if (isMono)
                    MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                else
                    MaterialTheme.typography.bodyMedium,
                color = colors.textPrimary,
                modifier = Modifier.weight(1f)
            )
            // Mostrar badge de verificado solo si está VERIFIED
            if (verificationStatus == VerificationStatus.VERIFIED) {
                Icon(
                    imageVector = Icons.Default.VerifiedUser,
                    contentDescription = stringResource(R.string.profile_verified_badge),
                    tint = colors.primaryColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun EditableProfileField(
    label: String,
    value: String,
    icon: ImageVector,
    isVerified: Boolean,
    isEditing: Boolean,
    onEditClick: () -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    onValueChange: (String) -> Unit,
    validator: (String) -> Boolean = { true },
    keyboardType: KeyboardType = KeyboardType.Text,
    maxLength: Int? = null,
    errorMessage: String = "",
    isLoading: Boolean = false  // NUEVO PARÁMETRO
) {
    val colors = LocalRenovaColors.current
    var validationState by remember { mutableStateOf(ValidationState.IDLE) }

    // Validación automática con delay
    LaunchedEffect(value) {
        if (isEditing && value.isNotEmpty()) {
            validationState = ValidationState.VALIDATING
            delay(800)
            val isValid = validator(value)
            validationState = if (isValid) ValidationState.VALID else ValidationState.ERROR
        } else if (value.isEmpty()) {
            validationState = ValidationState.IDLE
        }
    }

    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.textSecondary,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = colors.textSecondary
            )
        }
        Spacer(modifier = Modifier.height(4.dp))

        if (!isEditing) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textPrimary,
                    modifier = Modifier.weight(1f)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isVerified) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = stringResource(R.string.profile_verified_badge),
                            tint = colors.primaryColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    if (!isVerified) {
                        IconButton(
                            onClick = onEditClick,
                            modifier = Modifier.size(32.dp),
                            enabled = !isLoading  // DESHABILITAR SI ESTÁ CARGANDO
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = stringResource(R.string.edit_field),
                                tint = colors.primaryColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = value,
                    onValueChange = { newValue ->
                        val filteredValue = if (maxLength != null) {
                            newValue.take(maxLength)
                        } else newValue
                        onValueChange(filteredValue)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium,
                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                    isError = validationState == ValidationState.ERROR,
                    enabled = !isLoading,  // DESHABILITAR SI ESTÁ CARGANDO
                    trailingIcon = {
                        AnimatedVisibility(
                            visible = validationState != ValidationState.IDLE || isLoading,
                            enter = fadeIn() + scaleIn(),
                            exit = fadeOut() + scaleOut()
                        ) {
                            when {
                                isLoading -> {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = colors.primaryColor,
                                        strokeWidth = 2.dp
                                    )
                                }
                                validationState == ValidationState.VALIDATING -> {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = colors.primaryColor,
                                        strokeWidth = 2.dp
                                    )
                                }
                                validationState == ValidationState.VALID -> {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Valid",
                                        tint = colors.primaryColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                validationState == ValidationState.ERROR -> {
                                    Icon(
                                        imageVector = Icons.Default.Error,
                                        contentDescription = "Error",
                                        tint = RenovaColors.Error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.borderFocused,
                        unfocusedBorderColor = colors.border,
                        errorBorderColor = RenovaColors.Error,
                        cursorColor = colors.primaryColor,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary,
                        disabledBorderColor = colors.border.copy(alpha = 0.5f),
                        disabledTextColor = colors.textPrimary.copy(alpha = 0.5f)
                    )
                )

                // Mensaje de error
                AnimatedVisibility(
                    visible = validationState == ValidationState.ERROR,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Text(
                        text = errorMessage,
                        color = RenovaColors.Error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onCancel,
                        enabled = !isLoading,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = colors.textSecondary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.cancel_field),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    // En tu ProfileComponents.kt, reemplaza el Button de guardar:

                    Button(
                        onClick = {
                            android.util.Log.d("EditableProfileField", "🟢 Save button clicked")
                            android.util.Log.d("EditableProfileField", "🟢 Label: $label")
                            android.util.Log.d("EditableProfileField", "🟢 Value: $value")
                            android.util.Log.d("EditableProfileField", "🟢 ValidationState: $validationState")
                            android.util.Log.d("EditableProfileField", "🟢 isLoading: $isLoading")
                            onSave()
                        },
                        enabled = validationState == ValidationState.VALID && !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primaryColor
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.save_field),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DocumentCard(
    label: String,
    isRejected: Boolean,
    documentImageUrl: String? = null,
    modifier: Modifier = Modifier
) {
    val colors = LocalRenovaColors.current

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = if (isRejected)
            RenovaColors.Error.copy(alpha = 0.2f)
        else
            colors.textSecondary.copy(alpha = 0.05f)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (documentImageUrl != null) {
                Image(
                    painter = rememberAsyncImagePainter(documentImageUrl),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .background(
                            colors.textSecondary.copy(alpha = 0.2f),
                            RoundedCornerShape(6.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (label.contains(stringResource(R.string.selfie)))
                            Icons.Default.CameraAlt else Icons.Default.CreditCard,
                        contentDescription = null,
                        tint = if (isRejected)
                            RenovaColors.Error
                        else
                            colors.textSecondary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = colors.textSecondary
            )
        }
    }
}

@Composable
fun PersonalInfoCard(
    user: UserData,
    verificationStatus: VerificationStatus,
    isSpanish: Boolean,
    viewModel: ProfileViewModel
) {
    val colors = LocalRenovaColors.current
    val updateFieldState by viewModel.updateFieldState.collectAsState()
    val isLoading = updateFieldState is UpdateFieldState.Loading

    // ⭐ NUEVO: Rastrear el último error para revertir el campo correcto
    var lastErrorField by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(updateFieldState) {
        when (updateFieldState) {
            is UpdateFieldState.Success -> {
                delay(1000)
                viewModel.resetUpdateState()
                lastErrorField = null
            }
            is UpdateFieldState.Error -> {
                // Guardar el campo que tuvo error
                lastErrorField = (updateFieldState as UpdateFieldState.Error).field
                delay(3000)
                viewModel.resetUpdateState()
            }
            else -> {}
        }
    }

    // Determinar qué campos son editables según el estado
    val canEditBasicInfo = when (verificationStatus) {
        VerificationStatus.VERIFIED -> false
        VerificationStatus.PENDING -> false
        VerificationStatus.REJECTED, VerificationStatus.NO_DOCS -> true
        else -> false
    }

    val canEditContactInfo = when (verificationStatus) {
        VerificationStatus.VERIFIED -> false
        VerificationStatus.PENDING -> true
        VerificationStatus.REJECTED, VerificationStatus.NO_DOCS -> true
        else -> false
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = colors.cardBackground,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.5.dp,
            color = when (verificationStatus){
                VerificationStatus.REJECTED -> RenovaColors.Error
                VerificationStatus.VERIFIED -> colors.primaryColor
                VerificationStatus.PENDING -> RenovaColors.Warning
                else -> colors.textSecondary
            }
        )
    ) {
        Column {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = when (verificationStatus) {
                    VerificationStatus.REJECTED -> RenovaColors.Error.copy(alpha = 0.125f)
                    VerificationStatus.VERIFIED -> colors.primaryColor.copy(alpha = 0.125f)
                    VerificationStatus.PENDING -> RenovaColors.Warning.copy(alpha = 0.125f)
                    else -> colors.textSecondary.copy(alpha = 0.125f)
                }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = when (verificationStatus) {
                            VerificationStatus.REJECTED -> RenovaColors.Error
                            VerificationStatus.VERIFIED -> colors.primaryColor
                            VerificationStatus.PENDING -> RenovaColors.Warning
                            else -> colors.textSecondary
                        },
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = stringResource(R.string.personal_data),
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.textPrimary
                    )
                }
            }

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // NOMBRE
                if (canEditBasicInfo) {
                    var nameValue by remember { mutableStateOf(user.name) }
                    var isEditingName by remember { mutableStateOf(false) }

                    // ⭐ NUEVO: Revertir al valor original cuando hay error
                    LaunchedEffect(user.name) {
                        nameValue = user.name
                    }

                    LaunchedEffect(lastErrorField) {
                        if (lastErrorField == "name") {
                            nameValue = user.name
                            isEditingName = false
                        }
                    }

                    EditableProfileField(
                        label = stringResource(R.string.first_name),
                        value = nameValue,
                        icon = Icons.Default.Person,
                        isVerified = false,
                        isEditing = isEditingName,
                        onEditClick = { isEditingName = true },
                        onSave = {
                            viewModel.updateName(nameValue)
                            isEditingName = false
                        },
                        onCancel = {
                            nameValue = user.name
                            isEditingName = false
                        },
                        onValueChange = { nameValue = it },
                        validator = { validateName(it) },
                        errorMessage = stringResource(R.string.error_name_min_length),
                        isLoading = isLoading
                    )
                } else {
                    ProfileField(
                        label = stringResource(R.string.first_name),
                        value = user.name,
                        icon = Icons.Default.Person,
                        verificationStatus = verificationStatus
                    )
                }

                // APELLIDO
                if (canEditBasicInfo) {
                    var lastNameValue by remember { mutableStateOf(user.last_name) }
                    var isEditingLastName by remember { mutableStateOf(false) }

                    LaunchedEffect(user.last_name) {
                        lastNameValue = user.last_name
                    }

                    // ⭐ NUEVO: Revertir cuando hay error
                    LaunchedEffect(lastErrorField) {
                        if (lastErrorField == "last_name") {
                            lastNameValue = user.last_name
                            isEditingLastName = false
                        }
                    }

                    EditableProfileField(
                        label = stringResource(R.string.last_name),
                        value = lastNameValue,
                        icon = Icons.Default.Person,
                        isVerified = false,
                        isEditing = isEditingLastName,
                        onEditClick = { isEditingLastName = true },
                        onSave = {
                            viewModel.updateLastName(lastNameValue)
                            isEditingLastName = false
                        },
                        onCancel = {
                            lastNameValue = user.last_name
                            isEditingLastName = false
                        },
                        onValueChange = { lastNameValue = it },
                        validator = { validateName(it) },
                        errorMessage = stringResource(R.string.error_name_min_length),
                        isLoading = isLoading
                    )
                } else {
                    ProfileField(
                        label = stringResource(R.string.last_name),
                        value = user.last_name,
                        icon = Icons.Default.Person,
                        verificationStatus = verificationStatus
                    )
                }

                // EMAIL
                if (canEditContactInfo) {
                    var emailValue by remember { mutableStateOf(user.email) }
                    var isEditingEmail by remember { mutableStateOf(false) }

                    LaunchedEffect(user.email) {
                        emailValue = user.email
                    }

                    // ⭐ NUEVO: Revertir cuando hay error
                    LaunchedEffect(lastErrorField) {
                        if (lastErrorField == "email") {
                            emailValue = user.email
                            isEditingEmail = false
                        }
                    }

                    EditableProfileField(
                        label = stringResource(R.string.email),
                        value = emailValue,
                        icon = Icons.Default.Email,
                        isVerified = false,
                        isEditing = isEditingEmail,
                        onEditClick = { isEditingEmail = true },
                        onSave = {
                            viewModel.updateEmail(emailValue)
                            isEditingEmail = false
                        },
                        onCancel = {
                            emailValue = user.email
                            isEditingEmail = false
                        },
                        onValueChange = { emailValue = it },
                        validator = { validateEmail(it) },
                        keyboardType = KeyboardType.Email,
                        errorMessage = stringResource(R.string.email_invalid),
                        isLoading = isLoading
                    )
                } else {
                    ProfileField(
                        label = stringResource(R.string.email),
                        value = user.email,
                        icon = Icons.Default.Email,
                        verificationStatus = verificationStatus
                    )
                }

                // TELÉFONO
                if (canEditContactInfo) {
                    var phoneValue by remember { mutableStateOf(user.phone) }
                    var isEditingPhone by remember { mutableStateOf(false) }

                    LaunchedEffect(user.phone) {
                        phoneValue = user.phone
                    }

                    // ⭐ NUEVO: Revertir cuando hay error
                    LaunchedEffect(lastErrorField) {
                        if (lastErrorField == "phone") {
                            phoneValue = user.phone
                            isEditingPhone = false
                        }
                    }

                    EditableProfileField(
                        label = stringResource(R.string.phone_number),
                        value = phoneValue,
                        icon = Icons.Default.Phone,
                        isVerified = false,
                        isEditing = isEditingPhone,
                        onEditClick = { isEditingPhone = true },
                        onSave = {
                            viewModel.updatePhone(phoneValue)
                            isEditingPhone = false
                        },
                        onCancel = {
                            phoneValue = user.phone
                            isEditingPhone = false
                        },
                        onValueChange = {
                            if (it.all { char -> char.isDigit() }) {
                                phoneValue = it
                            }
                        },
                        validator = { validatePhone(it) },
                        keyboardType = KeyboardType.Phone,
                        maxLength = 10,
                        errorMessage = stringResource(R.string.error_phone_digits),
                        isLoading = isLoading
                    )
                } else {
                    ProfileField(
                        label = stringResource(R.string.phone_number),
                        value = user.phone,
                        icon = Icons.Default.Phone,
                        verificationStatus = verificationStatus
                    )
                }

                // CURP
                if (canEditBasicInfo) {
                    var curpValue by remember { mutableStateOf(user.curp) }
                    var isEditingCurp by remember { mutableStateOf(false) }

                    LaunchedEffect(user.curp) {
                        curpValue = user.curp
                    }

                    // ⭐ NUEVO: Revertir cuando hay error
                    LaunchedEffect(lastErrorField) {
                        if (lastErrorField == "curp") {
                            curpValue = user.curp
                            isEditingCurp = false
                        }
                    }

                    EditableProfileField(
                        label = stringResource(R.string.curp_label),
                        value = curpValue,
                        icon = Icons.Default.CreditCard,
                        isVerified = false,
                        isEditing = isEditingCurp,
                        onEditClick = { isEditingCurp = true },
                        onSave = {
                            viewModel.updateCurp(curpValue)
                            isEditingCurp = false
                        },
                        onCancel = {
                            curpValue = user.curp
                            isEditingCurp = false
                        },
                        onValueChange = {
                            if (it.length <= 18) curpValue = it.uppercase()
                        },
                        validator = { validateCURP(it) },
                        maxLength = 18,
                        errorMessage = "CURP inválido",
                        isLoading = isLoading
                    )
                } else {
                    ProfileField(
                        label = stringResource(R.string.curp_label),
                        value = user.curp,
                        icon = Icons.Default.CreditCard,
                        verificationStatus = verificationStatus,
                        isMono = true
                    )
                }
            }
        }
    }
}

@Composable
fun SecurityCard(
    viewModel: ProfileViewModel,
    verificationStatus: VerificationStatus // ⭐ NUEVO PARÁMETRO
) {
    val colors = LocalRenovaColors.current
    val passwordResetState by viewModel.passwordResetState.collectAsState()

    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var showCurrentPassword by remember { mutableStateOf(false) }
    var showNewPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    var currentPasswordError by remember { mutableStateOf<String?>(null) }
    var newPasswordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    var isExpanded by remember { mutableStateOf(false) }

    val arrowRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(300),
        label = "arrow"
    )

    val isLoading = passwordResetState is ProfileViewModel.PasswordResetState.Loading

    LaunchedEffect(passwordResetState) {
        if (passwordResetState is ProfileViewModel.PasswordResetState.Success) {
            currentPassword = ""
            newPassword = ""
            confirmPassword = ""
            currentPasswordError = null
            newPasswordError = null
            confirmPasswordError = null
            showCurrentPassword = false
            showNewPassword = false
            showConfirmPassword = false
        }
    }

    val errorPasswordTooShort = stringResource(R.string.error_password_too_short)
    val errorPasswordNoNumber = stringResource(R.string.error_password_no_number)
    val errorPasswordNoSpecial = stringResource(R.string.error_password_no_special)
    val errorPasswordMismatch = stringResource(R.string.error_password_mismatch)

    LaunchedEffect(newPassword) {
        if (newPassword.isNotEmpty()) {
            newPasswordError = when {
                newPassword.length < 8 -> errorPasswordTooShort
                !newPassword.any { it.isDigit() } -> errorPasswordNoNumber
                !newPassword.any { it in "!@#$%^&*()_+-=[]{}|;:,.<>?" } -> errorPasswordNoSpecial
                else -> null
            }
        } else {
            newPasswordError = null
        }
    }

    LaunchedEffect(confirmPassword) {
        if (confirmPassword.isNotEmpty()) {
            confirmPasswordError = if (confirmPassword != newPassword) {
                errorPasswordMismatch
            } else null
        } else {
            confirmPasswordError = null
        }
    }

    val isFormValid = currentPassword.isNotEmpty() &&
            newPassword.isNotEmpty() &&
            confirmPassword.isNotEmpty() &&
            newPasswordError == null &&
            confirmPasswordError == null &&
            !isLoading

    // ⭐ NUEVO: Definir colores según el estado de verificación
    val headerBackgroundColor = when (verificationStatus) {
        VerificationStatus.REJECTED -> RenovaColors.Error.copy(alpha = 0.125f)
        VerificationStatus.VERIFIED -> colors.primaryColor.copy(alpha = 0.125f)
        VerificationStatus.PENDING -> RenovaColors.Warning.copy(alpha = 0.125f)
        else -> colors.textSecondary.copy(alpha = 0.125f)
    }

    val iconTint = when (verificationStatus) {
        VerificationStatus.REJECTED -> RenovaColors.Error
        VerificationStatus.VERIFIED -> colors.primaryColor
        VerificationStatus.PENDING -> RenovaColors.Warning
        else -> colors.textSecondary
    }

    val borderColor = when (verificationStatus) {
        VerificationStatus.REJECTED -> RenovaColors.Error
        VerificationStatus.VERIFIED -> colors.primaryColor
        VerificationStatus.PENDING -> RenovaColors.Warning
        else -> colors.textSecondary
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = colors.cardBackground,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.5.dp,
            color = borderColor // ⭐ MODIFICADO: Color dinámico
        )
    ) {
        Column {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                color = headerBackgroundColor // ⭐ MODIFICADO: Color dinámico
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = iconTint, // ⭐ MODIFICADO: Color dinámico
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = stringResource(R.string.security_section),
                            style = MaterialTheme.typography.titleSmall,
                            color = colors.textPrimary
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Toggle",
                        tint = iconTint, // ⭐ MODIFICADO: Color dinámico
                        modifier = Modifier
                            .size(32.dp)
                            .rotate(arrowRotation)
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(tween(300)),
                exit = shrinkVertically(tween(300))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Contraseña Actual
                    Column {
                        Text(
                            text = stringResource(R.string.current_password),
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.textSecondary,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        OutlinedTextField(
                            value = currentPassword,
                            onValueChange = {
                                currentPassword = it
                                currentPasswordError = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = if (showCurrentPassword)
                                VisualTransformation.None
                            else
                                PasswordVisualTransformation(),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = colors.textSecondary
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { showCurrentPassword = !showCurrentPassword }) {
                                    Icon(
                                        imageVector = if (showCurrentPassword)
                                            Icons.Default.Visibility
                                        else
                                            Icons.Default.VisibilityOff,
                                        contentDescription = if (showCurrentPassword)
                                            "Ocultar contraseña"
                                        else
                                            "Mostrar contraseña",
                                        tint = colors.textSecondary
                                    )
                                }
                            },
                            isError = currentPasswordError != null,
                            enabled = !isLoading,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.borderFocused,
                                unfocusedBorderColor = colors.border,
                                errorBorderColor = RenovaColors.Error,
                                cursorColor = colors.primaryColor,
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary
                            )
                        )

                        currentPasswordError?.let { error ->
                            Text(
                                text = error,
                                color = RenovaColors.Error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                            )
                        }
                    }

                    // Nueva Contraseña
                    Column {
                        Text(
                            text = stringResource(R.string.new_password),
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.textSecondary,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = if (showNewPassword)
                                VisualTransformation.None
                            else
                                PasswordVisualTransformation(),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = colors.textSecondary
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { showNewPassword = !showNewPassword }) {
                                    Icon(
                                        imageVector = if (showNewPassword)
                                            Icons.Default.Visibility
                                        else
                                            Icons.Default.VisibilityOff,
                                        contentDescription = if (showNewPassword)
                                            "Ocultar contraseña"
                                        else
                                            "Mostrar contraseña",
                                        tint = colors.textSecondary
                                    )
                                }
                            },
                            isError = newPasswordError != null,
                            enabled = !isLoading,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.borderFocused,
                                unfocusedBorderColor = colors.border,
                                errorBorderColor = RenovaColors.Error,
                                cursorColor = colors.primaryColor,
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary
                            )
                        )

                        newPasswordError?.let { error ->
                            Text(
                                text = error,
                                color = RenovaColors.Error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                            )
                        }
                    }

                    // Confirmar Nueva Contraseña
                    Column {
                        Text(
                            text = stringResource(R.string.confirm_new_password),
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.textSecondary,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = if (showConfirmPassword)
                                VisualTransformation.None
                            else
                                PasswordVisualTransformation(),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = colors.textSecondary
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                                    Icon(
                                        imageVector = if (showConfirmPassword)
                                            Icons.Default.Visibility
                                        else
                                            Icons.Default.VisibilityOff,
                                        contentDescription = if (showConfirmPassword)
                                            "Ocultar contraseña"
                                        else
                                            "Mostrar contraseña",
                                        tint = colors.textSecondary
                                    )
                                }
                            },
                            isError = confirmPasswordError != null,
                            enabled = !isLoading,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.borderFocused,
                                unfocusedBorderColor = colors.border,
                                errorBorderColor = RenovaColors.Error,
                                cursorColor = colors.primaryColor,
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary
                            )
                        )

                        confirmPasswordError?.let { error ->
                            Text(
                                text = error,
                                color = RenovaColors.Error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                            )
                        }
                    }

                    // Botón de Actualizar
                    Button(
                        onClick = {
                            viewModel.resetPassword(
                                currentPassword = currentPassword,
                                newPassword = newPassword,
                                newPasswordConfirmation = confirmPassword
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        enabled = isFormValid,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primaryColor,
                            disabledContainerColor = colors.textSecondary.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.update_password),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

fun validateName(name: String): Boolean =
    name.length >= 2 && name.all { it.isLetter() || it.isWhitespace() }

fun validateEmail(email: String): Boolean =
    android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()

fun validatePhone(phone: String): Boolean =
    phone.length == 10 && phone.all { it.isDigit() }

fun validateCURP(curp: String): Boolean {
    val curpPattern = "^[A-Z]{4}\\d{6}[HM][A-Z]{2}[BCDFGHJKLMNPQRSTVWXYZ]{3}[0-9A-Z]\\d$"
    return curp.matches(curpPattern.toRegex())
}