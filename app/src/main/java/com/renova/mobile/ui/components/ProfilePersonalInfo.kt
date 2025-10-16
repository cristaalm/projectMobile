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
import com.renova.mobile.network.IdentityVerification
import com.renova.mobile.network.UserData
import com.renova.mobile.ui.theme.LocalRenovaColors
import com.renova.mobile.ui.theme.RenovaColors
import com.renova.mobile.ui.viewmodels.VerificationStatus
import coil.compose.rememberAsyncImagePainter
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.layout.ContentScale

@Composable
fun ProfileField(
    label: String,
    value: String,
    icon: ImageVector,
    isVerified: Boolean,
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
            if (isVerified) {
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
    onValueChange: (String) -> Unit
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
                            modifier = Modifier.size(32.dp)
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
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.borderFocused,
                        unfocusedBorderColor = colors.border,
                        cursorColor = colors.primaryColor,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onCancel,
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
                    Button(
                        onClick = onSave,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primaryColor
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
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

@Composable
fun DocumentationSection(
    identityVerification: IdentityVerification?,
    verificationStatus: VerificationStatus,
    isSpanish: Boolean
) {
    val colors = LocalRenovaColors.current

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
                color = when (verificationStatus){
                    VerificationStatus.REJECTED -> RenovaColors.Error.copy(alpha = 0.125f)
                    VerificationStatus.VERIFIED -> colors.primaryColor.copy(alpha = 0.124f)
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
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = when (verificationStatus){
                                VerificationStatus.REJECTED -> RenovaColors.Error
                                VerificationStatus.VERIFIED -> colors.primaryColor
                                VerificationStatus.PENDING -> RenovaColors.Warning
                                else -> colors.textSecondary
                            },
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = stringResource(
                            if (verificationStatus == VerificationStatus.REJECTED)
                                R.string.documentation_rejected
                            else
                                R.string.documentation_submitted
                        ),
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.textPrimary
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DocumentCard(
                    label = stringResource(R.string.selfie),
                    isRejected = verificationStatus == VerificationStatus.REJECTED,
                    modifier = Modifier.weight(1f)
                )
                DocumentCard(
                    label = stringResource(R.string.ine_front),
                    isRejected = verificationStatus == VerificationStatus.REJECTED,
                    modifier = Modifier.weight(1f)
                )
                DocumentCard(
                    label = stringResource(R.string.ine_back),
                    isRejected = verificationStatus == VerificationStatus.REJECTED,
                    modifier = Modifier.weight(1f)
                )
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
    emailValue: String,
    phoneValue: String,
    isEditingEmail: Boolean,
    isEditingPhone: Boolean,
    onEditEmail: () -> Unit,
    onEditPhone: () -> Unit,
    onSaveEmail: () -> Unit,
    onSavePhone: () -> Unit,
    onCancelEmail: () -> Unit,
    onCancelPhone: () -> Unit,
    onEmailChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit
) {
    val colors = LocalRenovaColors.current

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
            // Header de la sección
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = when (verificationStatus) {
                    VerificationStatus.REJECTED -> RenovaColors.Error.copy(alpha = 0.125f)
                    VerificationStatus.VERIFIED -> colors.primaryColor.copy(alpha = 0.125f)
                    VerificationStatus.PENDING -> colors.textSecondary.copy(alpha = 0.125f)
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
                        tint = colors.primaryColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = stringResource(R.string.personal_data),
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.textPrimary
                    )
                }
            }

            // Campos
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (verificationStatus == VerificationStatus.REJECTED || verificationStatus == VerificationStatus.NO_DOCS ) {
                    var nameValue by remember { mutableStateOf(user.name) }
                    var isEditingName by remember { mutableStateOf(false) }

                    EditableProfileField(
                        label = stringResource(R.string.first_name),
                        value = nameValue,
                        icon = Icons.Default.Person,
                        isVerified = false,
                        isEditing = isEditingName,
                        onEditClick = { isEditingName = true },
                        onSave = {
                            // TODO: Implementar actualización
                            isEditingName = false
                        },
                        onCancel = {
                            nameValue = user.name
                            isEditingName = false
                        },
                        onValueChange = { nameValue = it }
                    )
                } else {
                    ProfileField(
                        label = stringResource(R.string.first_name),
                        value = user.name,
                        icon = Icons.Default.Person,
                        isVerified = verificationStatus == VerificationStatus.VERIFIED
                    )
                }

                Divider(color = colors.textSecondary.copy(alpha = 0.1f))

                if (verificationStatus == VerificationStatus.REJECTED || verificationStatus == VerificationStatus.NO_DOCS ) {
                    var lastNameValue by remember { mutableStateOf(user.last_name) }
                    var isEditingLastName by remember { mutableStateOf(false) }

                    EditableProfileField(
                        label = stringResource(R.string.last_name),
                        value = lastNameValue,
                        icon = Icons.Default.Person,
                        isVerified = false,
                        isEditing = isEditingLastName,
                        onEditClick = { isEditingLastName = true },
                        onSave = {
                            // TODO: Implementar actualización
                            isEditingLastName = false
                        },
                        onCancel = {
                            lastNameValue = user.last_name
                            isEditingLastName = false
                        },
                        onValueChange = { lastNameValue = it }
                    )
                } else {
                    ProfileField(
                        label = stringResource(R.string.last_name),
                        value = user.last_name,
                        icon = Icons.Default.Person,
                        isVerified = verificationStatus == VerificationStatus.VERIFIED
                    )
                }

                Divider(color = colors.textSecondary.copy(alpha = 0.1f))

                // CURP - Editable si está rechazado
                if (verificationStatus == VerificationStatus.REJECTED || verificationStatus == VerificationStatus.NO_DOCS) {
                    var curpValue by remember { mutableStateOf(user.curp) }
                    var isEditingCurp by remember { mutableStateOf(false) }

                    EditableProfileField(
                        label = stringResource(R.string.curp_label),
                        value = curpValue,
                        icon = Icons.Default.CreditCard,
                        isVerified = false,
                        isEditing = isEditingCurp,
                        onEditClick = { isEditingCurp = true },
                        onSave = {
                            isEditingCurp = false
                        },
                        onCancel = {
                            curpValue = user.curp
                            isEditingCurp = false
                        },
                        onValueChange = { curpValue = it }
                    )
                } else {
                    ProfileField(
                        label = stringResource(R.string.curp_label),
                        value = user.curp,
                        icon = Icons.Default.CreditCard,
                        isVerified = verificationStatus == VerificationStatus.VERIFIED,
                        isMono = true
                    )
                }
                Divider(color = colors.textSecondary.copy(alpha = 0.1f))

                EditableProfileField(
                    label = stringResource(R.string.email),
                    value = emailValue,
                    icon = Icons.Default.Email,
                    isVerified = verificationStatus == VerificationStatus.VERIFIED,
                    isEditing = isEditingEmail,
                    onEditClick = onEditEmail,
                    onSave = onSaveEmail,
                    onCancel = onCancelEmail,
                    onValueChange = onEmailChange
                )

                Divider(color = colors.textSecondary.copy(alpha = 0.1f))

                EditableProfileField(
                    label = stringResource(R.string.phone_number),
                    value = phoneValue,
                    icon = Icons.Default.Phone,
                    isVerified = verificationStatus == VerificationStatus.VERIFIED,
                    isEditing = isEditingPhone,
                    onEditClick = onEditPhone,
                    onSave = onSavePhone,
                    onCancel = onCancelPhone,
                    onValueChange = onPhoneChange
                )
            }
        }
    }
}