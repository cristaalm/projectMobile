package com.renova.mobile.ui.screens

import android.app.Activity
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.renova.mobile.ui.components.ErrorDialog
import com.renova.mobile.ui.components.LoadingState
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.renova.mobile.R
import com.renova.mobile.network.IdentityVerification
import com.renova.mobile.network.UserData
import com.renova.mobile.ui.components.*
import com.renova.mobile.ui.theme.LocalRenovaColors
import com.renova.mobile.ui.theme.RenovaColors
import com.renova.mobile.ui.viewmodels.LanguageViewModel
import com.renova.mobile.ui.viewmodels.ProfileViewModel
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import com.renova.mobile.ui.viewmodels.ProfileUiState
import com.renova.mobile.ui.viewmodels.VerificationStatus
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.*
import com.renova.mobile.ui.components.CustomRefreshIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    languageViewModel: LanguageViewModel,
    profileViewModel: ProfileViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
) {
    val currentLanguage by languageViewModel.currentLanguage.collectAsState()
    val isSpanish = currentLanguage == "es"
    val uiState by profileViewModel.uiState.collectAsState()
    val isRefreshing by profileViewModel.isRefreshing.collectAsState()
    val colors = LocalRenovaColors.current

    // Estado para el pull to refresh
    val pullToRefreshState = rememberPullToRefreshState()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        when (val state = uiState) {
            is ProfileUiState.Loading -> {
                LoadingState(renovaColors = colors)
            }

            is ProfileUiState.Success -> {
                // Agregar PullToRefreshBox
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = {
                        profileViewModel.refreshProfile()
                    },
                    state = pullToRefreshState,
                    indicator = {
                        CustomRefreshIndicator(
                            state = pullToRefreshState,
                            isRefreshing = isRefreshing,
                            renovaColors = colors,
                            modifier = Modifier.align(Alignment.TopCenter)
                        )
                    }
                ) {
                    ProfileContent(
                        user = state.user,
                        identityVerification = state.identityVerification,
                        languageViewModel = languageViewModel,
                        isSpanish = isSpanish,
                        onRefresh = { profileViewModel.refreshProfile() }
                    )
                }
            }

            is ProfileUiState.Error -> {
                // Mostrar contenedor vacío (el ErrorDialog se mostrará encima)
                Box(modifier = Modifier.fillMaxSize())
            }
        }
    }

    // Mostrar ErrorDialog cuando hay un error
    if (uiState is ProfileUiState.Error) {
        ErrorDialog(
            error = (uiState as ProfileUiState.Error).message,
            onRetry = {
                profileViewModel.retry()
            },
            onDismiss = {
                profileViewModel.clearError()
            }
        )
    }
}

@Composable
private fun ProfileContent(
    user: UserData,
    identityVerification: IdentityVerification?,
    languageViewModel: LanguageViewModel,
    isSpanish: Boolean,
    onRefresh: () -> Unit
) {
    val context = LocalContext.current
    val verificationStatus = VerificationStatus.fromCode(user.verification_status)
    val colors = LocalRenovaColors.current
// Estados de edición
    var isEditingEmail by remember { mutableStateOf(false) }
    var isEditingPhone by remember { mutableStateOf(false) }
    var emailValue by remember(user.email) { mutableStateOf(user.email) }
    var phoneValue by remember(user.phone) { mutableStateOf(user.phone) }

    // Documentos editables para usuarios rechazados
    val editDocuments = remember(identityVerification) {
        mutableStateOf(
            listOf(
                DocumentCardData(
                    type = DocumentType.SELFIE,
                    imageUrl = identityVerification?.selfie_url
                ),
                DocumentCardData(
                    type = DocumentType.INE_FRONT,
                    imageUrl = identityVerification?.ine_front_url
                ),
                DocumentCardData(
                    type = DocumentType.INE_BACK,
                    imageUrl = identityVerification?.ine_back_url
                )
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        ProfileHeader(
            user = user,
            verificationStatus = verificationStatus,
            languageViewModel = languageViewModel
        )

        // Contenido
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Banner de estado
            VerificationBanner(
                verificationStatus = verificationStatus,
                rejectionReason = identityVerification?.rejection_reason,
                isSpanish = isSpanish
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Card de información personal
            PersonalInfoCard(
                user = user,
                verificationStatus = verificationStatus,
                isSpanish = isSpanish,
                emailValue = emailValue,
                phoneValue = phoneValue,
                isEditingEmail = isEditingEmail,
                isEditingPhone = isEditingPhone,
                onEditEmail = { isEditingEmail = true },
                onEditPhone = { isEditingPhone = true },
                onSaveEmail = {
                    // TODO: Implementar actualización de email
                    isEditingEmail = false
                },
                onSavePhone = {
                    // TODO: Implementar actualización de teléfono
                    isEditingPhone = false
                },
                onCancelEmail = {
                    emailValue = user.email
                    isEditingEmail = false
                },
                onCancelPhone = {
                    phoneValue = user.phone
                    isEditingPhone = false
                },
                onEmailChange = { emailValue = it },
                onPhoneChange = { phoneValue = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Sección de documentos
            // Sección de documentos
            if (identityVerification != null) {
                // USAR SIEMPRE DocumentsUploadSection para todos los estados
                DocumentsUploadSection(
                    documents = editDocuments.value,
                    verificationStatus = verificationStatus,
                    onImageSelected = { type: DocumentType, uri: Uri ->
                        // Solo permitir cambios si está rechazado
                        if (verificationStatus == VerificationStatus.REJECTED) {
                            editDocuments.value = editDocuments.value.map { doc ->
                                if (doc.type == type) {
                                    doc.copy(imageUri = uri)
                                } else {
                                    doc
                                }
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Botón de reenviar SOLO si está rechazado
                if (verificationStatus == VerificationStatus.REJECTED) {
                    val allDocumentsReady = editDocuments.value.all { doc ->
                        doc.imageUri != null || doc.imageUrl != null
                    }

                    Button(
                        onClick = {
                            // TODO: Implementar reenvío de documentación
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .then(
                                if (allDocumentsReady) {
                                    Modifier.border(
                                        width = 1.5.dp,
                                        color = RenovaColors.Warning,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                } else Modifier
                            ),
                        enabled = allDocumentsReady,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RenovaColors.Warning,
                            disabledContainerColor = when (verificationStatus) {
                                VerificationStatus.REJECTED -> RenovaColors.Error
                                VerificationStatus.VERIFIED -> colors.primaryColor
                                VerificationStatus.PENDING -> RenovaColors.Warning
                                else -> colors.textSecondary
                            },
                            contentColor = Color.White,
                            disabledContentColor = when (verificationStatus) {
                                VerificationStatus.REJECTED -> RenovaColors.Error
                                VerificationStatus.VERIFIED -> colors.primaryColor
                                VerificationStatus.PENDING -> RenovaColors.Warning
                                else -> colors.textSecondary
                            },
                        ),
                        shape = RoundedCornerShape(12.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 0.dp,
                            pressedElevation = 0.dp,
                            disabledElevation = 0.dp
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.resubmit_documentation),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun ProfileHeader(
    user: UserData,
    verificationStatus: VerificationStatus,
    languageViewModel: LanguageViewModel,
) {
    val currentLanguage by languageViewModel.currentLanguage.collectAsState()
    val isSpanish = currentLanguage == "es"
    val context = LocalContext.current
    val colors = LocalRenovaColors.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(345.dp)
            .background(MaterialTheme.colorScheme.primary)
            .paint(
                painter = painterResource(id = R.drawable.fondo_chico),
                contentScale = ContentScale.FillBounds,
                alignment = Alignment.Center
            )
            .padding(horizontal = 16.dp, vertical = 6.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Column {
            Row(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (verificationStatus != null) {
                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color =  colors.surface.copy(alpha = 0.3f),
                        modifier = Modifier
                            .width(130.dp)
                            .height(44.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = when (verificationStatus) {
                                    VerificationStatus.PENDING -> Icons.Default.Schedule
                                    VerificationStatus.REJECTED -> Icons.Default.Cancel
                                    VerificationStatus.VERIFIED -> Icons.Default.VerifiedUser
                                    else -> Icons.Default.QuestionMark
                                },
                                contentDescription = null,
                                tint = colors.surface,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when (verificationStatus) {
                                    VerificationStatus.VERIFIED -> stringResource(R.string.profile_verified_badge)
                                    VerificationStatus.PENDING -> stringResource(R.string.profile_pending_badge)
                                    VerificationStatus.REJECTED -> stringResource(R.string.profile_rejected_badge)
                                    else -> stringResource(R.string.profile_no_badge)
                                },
                                color = colors.surface,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.width(90.dp))
                }
                LanguageToggle(
                    isSpanish = isSpanish,
                    onLanguageChange = { newLang ->
                        languageViewModel.changeLanguage(newLang) {
                            (context as? Activity)?.recreate()
                        }
                    }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            // Foto de perfil y puntos
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(modifier = Modifier.size(96.dp)) {
                    Surface(
                        modifier = Modifier.size(96.dp),
                        shape = CircleShape,
                        color = colors.surface,
                        shadowElevation = 8.dp
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = colors.primaryColor,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }

                    if (verificationStatus == VerificationStatus.VERIFIED) {
                        Surface(
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.BottomEnd),
                            shape = CircleShape,
                            color = colors.primaryColor
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = colors.surface,
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${user.name} ${user.last_name}",
                    color = colors.surface,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.W600
                )

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = colors.surface.copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier
                            .wrapContentWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = colors.surface,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = java.text.NumberFormat.getIntegerInstance(java.util.Locale.forLanguageTag("es-MX")).format(user.total_points),
                            color = colors.surface,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.W700
                        )
                        Text(
                            text = stringResource(R.string.points_unit),
                            color = colors.surface.copy(alpha = 0.9f),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.W600
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VerificationBanner(
    verificationStatus: VerificationStatus,
    rejectionReason: String?,
    isSpanish: Boolean
) {
    val colors = LocalRenovaColors.current
    val protectedTitle = stringResource(R.string.protected_profile_title)
    val protectedDesc = stringResource(R.string.protected_profile_description)
    val pendingTitle = stringResource(R.string.verification_pending_title)
    val pendingDesc = stringResource(R.string.verification_pending_description)
    val rejectedTitle = stringResource(R.string.verification_rejected_title)
    val rejectedDesc = stringResource(R.string.verification_rejected_description)
    val emptyTitle = stringResource(R.string.verification_empty_title)
    val emptyDesc = stringResource(R.string.verification_empty_description)

    val backgroundColor: Color
    val icon: ImageVector
    val title: String
    val description: String
    val border: androidx.compose.foundation.BorderStroke

    when (verificationStatus) {
        VerificationStatus.VERIFIED -> {
            backgroundColor = colors.primaryColor.copy(alpha = 0.125f)
            icon = Icons.Default.VerifiedUser
            title = protectedTitle
            description = protectedDesc
            border = androidx.compose.foundation.BorderStroke(
                width = 1.5.dp,
                color = colors.primaryColor
            )
        }
        VerificationStatus.PENDING -> {
            backgroundColor = RenovaColors.Warning.copy(alpha = 0.125f)
            icon = Icons.Default.Schedule
            title = pendingTitle
            description = pendingDesc
            border = androidx.compose.foundation.BorderStroke(
                width = 1.5.dp,
                color = RenovaColors.Warning
            )
        }
        VerificationStatus.REJECTED -> {
            backgroundColor = RenovaColors.Error.copy(alpha = 0.125f)
            icon = Icons.Default.Cancel
            title = rejectedTitle
            description = rejectionReason ?: rejectedDesc
            border = androidx.compose.foundation.BorderStroke(
                width = 1.5.dp,
                color = RenovaColors.Error
            )
        }
        VerificationStatus.NO_DOCS -> {
            backgroundColor = colors.textSecondary.copy(alpha = 0.125f)
            icon = Icons.Default.QuestionMark
            title = emptyTitle
            description = emptyDesc
            border = androidx.compose.foundation.BorderStroke(
                width = 1.5.dp,
                color = colors.textSecondary
            )
        }
        else -> return
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent,
        border = border
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = when (verificationStatus) {
                        VerificationStatus.REJECTED -> RenovaColors.Error
                        VerificationStatus.VERIFIED -> colors.primaryColor
                        VerificationStatus.PENDING -> RenovaColors.Warning
                        else -> colors.textSecondary
                    },
                    modifier = Modifier.size(24.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = colors.textPrimary,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = description,
                        color = colors.textSecondary.copy(alpha = 0.95f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(16.dp))
}