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
import com.renova.mobile.ui.components.LoadingState
import androidx.compose.ui.unit.dp
import com.renova.mobile.R
import androidx.compose.ui.text.style.TextAlign
import com.renova.mobile.network.IdentityVerification
import com.renova.mobile.network.UserData
import com.renova.mobile.ui.components.*
import com.renova.mobile.ui.theme.LocalRenovaColors
import com.renova.mobile.ui.theme.RenovaColors
import com.renova.mobile.ui.viewmodels.LanguageViewModel
import com.renova.mobile.ui.viewmodels.ProfileViewModel
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.ui.window.Dialog
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import com.renova.mobile.ui.viewmodels.ProfileUiState
import com.renova.mobile.ui.viewmodels.VerificationStatus
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.*
import androidx.compose.ui.graphics.asImageBitmap
import android.graphics.BitmapFactory
import coil.compose.rememberAsyncImagePainter

// --- IMPORTS AÑADIDOS DE LA RAMA 'tour' ---
import androidx.compose.ui.layout.onGloballyPositioned
import com.renova.mobile.ui.tour.LocalTourState
import androidx.compose.runtime.DisposableEffect
// --- FIN DE IMPORTS ---


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

    // Lógica de ErrorModal de 'develop'
    var showMainErrorModal by remember { mutableStateOf(false) }
    var mainErrorMessage by remember { mutableStateOf("") }
    var canRetryMainError by remember { mutableStateOf(true) }

    val pullToRefreshState = rememberPullToRefreshState()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        when (val state = uiState) {
            is ProfileUiState.Loading -> {
                LoadingState(renovaColors = colors)
            }

            is ProfileUiState.Success -> {
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
                        profileViewModel = profileViewModel,
                        onRefresh = { profileViewModel.refreshProfile() }
                    )
                }
            }

            // Lógica de Error avanzada de 'develop'
            is ProfileUiState.Error -> {
                val errorState = uiState as ProfileUiState.Error
                val isAuthError = errorState.message.contains("token", ignoreCase = true) ||
                        errorState.message.contains("autenticación", ignoreCase = true) ||
                        errorState.message.contains("sesión", ignoreCase = true)

                LaunchedEffect(Unit) {
                    mainErrorMessage = errorState.message
                    canRetryMainError = !isAuthError
                    showMainErrorModal = true
                }
                Box(modifier = Modifier.fillMaxSize())
            }
        }
    }

    // Error modal principal (de 'develop')
    ErrorModal(
        isVisible = showMainErrorModal,
        errorMessage = mainErrorMessage,
        onDismiss = {
            showMainErrorModal = false
            mainErrorMessage = ""
        },
        onRetry = if (canRetryMainError) {
            {
                showMainErrorModal = false
                profileViewModel.retry()
            }
        } else null
    )
}

@Composable
private fun ProfileContent(
    user: UserData,
    identityVerification: IdentityVerification?,
    languageViewModel: LanguageViewModel,
    isSpanish: Boolean,
    profileViewModel: ProfileViewModel,
    onRefresh: () -> Unit
) {
    val context = LocalContext.current
    val verificationStatus = VerificationStatus.fromCode(user.verification_status)
    val colors = LocalRenovaColors.current

    // --- AÑADIDO: Obtener estado del Tour (de 'tour') ---
    val tourState = LocalTourState.current
    // --- FIN ---

    // Toda la lógica de ViewModel de 'develop'
    val documentImages by profileViewModel.documentImages.collectAsState()
    val verificationRequestState by profileViewModel.verificationRequestState.collectAsState()
    val documentUploadState by profileViewModel.documentUploadState.collectAsState()

    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showUploadSuccessDialog by remember { mutableStateOf(false) }
    var uploadedDocumentName by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        profileViewModel.loadDocumentImages(user.id, identityVerification)
    }

    LaunchedEffect(verificationRequestState) {
        when (verificationRequestState) {
            is ProfileViewModel.VerificationRequestState.Success -> {
                showSuccessDialog = true
                profileViewModel.resetVerificationRequestState()
            }
            is ProfileViewModel.VerificationRequestState.Error -> {
                errorMessage = (verificationRequestState as ProfileViewModel.VerificationRequestState.Error).message
                showErrorDialog = true
                profileViewModel.resetVerificationRequestState()
            }
            else -> {}
        }
    }

    LaunchedEffect(documentUploadState) {
        when (documentUploadState) {
            is ProfileViewModel.DocumentUploadState.Success -> {
                val docType = (documentUploadState as ProfileViewModel.DocumentUploadState.Success).documentType
                uploadedDocumentName = when (docType) {
                    DocumentType.SELFIE -> context.getString(R.string.selfie)
                    DocumentType.INE_FRONT -> context.getString(R.string.ine_front)
                    DocumentType.INE_BACK -> context.getString(R.string.ine_back)
                }
                showUploadSuccessDialog = true
                profileViewModel.resetDocumentUploadState()
            }
            is ProfileViewModel.DocumentUploadState.Error -> {
                val state = documentUploadState as ProfileViewModel.DocumentUploadState.Error
                errorMessage = state.message
                showErrorDialog = true
                profileViewModel.resetDocumentUploadState()
            }
            else -> {}
        }
    }

    val editDocuments = remember(documentImages) {
        listOf(
            DocumentCardData(
                type = DocumentType.SELFIE,
                imageUrl = if (documentImages.containsKey("selfie"))
                    "memory://selfie" else null
            ),
            DocumentCardData(
                type = DocumentType.INE_FRONT,
                imageUrl = if (documentImages.containsKey("ine_front"))
                    "memory://ine_front" else null
            ),
            DocumentCardData(
                type = DocumentType.INE_BACK,
                imageUrl = if (documentImages.containsKey("ine_back"))
                    "memory://ine_back" else null
            )
        )
    }
    // Fin de la lógica de 'develop'

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        ProfileHeader(
            user = user,
            verificationStatus = verificationStatus,
            languageViewModel = languageViewModel,
            selfieBytes = documentImages["selfie"] // <-- Se pasa la selfie de 'develop'
        )
        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = Modifier.padding(horizontal = 10.dp)
        ) {
            Spacer(modifier = Modifier.height(1.dp))
            VerificationBanner(
                verificationStatus = verificationStatus,
                rejectionReason = identityVerification?.rejection_reason,
                isSpanish = isSpanish
            )
            Spacer(modifier = Modifier.height(12.dp))

            // --- MODIFICADO: Añadir wrapper del Tour (de 'tour') ---
            Box(modifier = Modifier.onGloballyPositioned { coords ->
                tourState.registerTarget("profile_info_card", coords)
            }) {
                PersonalInfoCard(
                    user = user,
                    verificationStatus = verificationStatus,
                    isSpanish = isSpanish,
                    viewModel = profileViewModel
                )
            }
            DisposableEffect("profile_info_card") {
                onDispose { tourState.unregisterTarget("profile_info_card") }
            }
            // --- FIN DE MODIFICACIÓN ---

            Spacer(modifier = Modifier.height(12.dp))

            // --- Toda la sección de documentos de 'develop' ---
            if (identityVerification != null) {
                DocumentsUploadSection(
                    documents = editDocuments,
                    verificationStatus = verificationStatus,
                    documentImages = documentImages,
                    documentUploadState = documentUploadState,
                    onImageSelected = { type: DocumentType, uri: Uri ->
                        profileViewModel.uploadDocument(type, uri, context)
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (verificationStatus == VerificationStatus.REJECTED) {
                    val allDocumentsReady = editDocuments.all { doc ->
                        doc.imageUrl != null
                    }

                    val isLoading = verificationRequestState is ProfileViewModel.VerificationRequestState.Loading

                    Button(
                        onClick = {
                            profileViewModel.requestVerification()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .then(
                                if (allDocumentsReady && !isLoading) {
                                    Modifier.border(
                                        width = 1.5.dp,
                                        color = RenovaColors.Warning,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                } else Modifier
                            ),
                        enabled = allDocumentsReady && !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RenovaColors.Warning,
                            disabledContainerColor = colors.textSecondary,
                            contentColor = Color.White,
                            disabledContentColor = Color.White.copy(alpha = 0.6f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 0.dp,
                            pressedElevation = 0.dp,
                            disabledElevation = 0.dp
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isLoading)
                                stringResource(R.string.requesting_verification)
                            else
                                stringResource(R.string.request_verification),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
            // --- Fin de la sección de documentos ---
        }
    }

    // --- Diálogos de 'develop' ---
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = colors.primaryColor,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    text = stringResource(R.string.verification_requested_title),
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.verification_requested_message),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = colors.textSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { showSuccessDialog = false }
                ) {
                    Text(stringResource(R.string.understood))
                }
            },
            containerColor = colors.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showUploadSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showUploadSuccessDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.CloudDone,
                    contentDescription = null,
                    tint = colors.primaryColor,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    text = stringResource(R.string.document_uploaded_title),
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.document_uploaded_message, uploadedDocumentName),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = colors.textSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { showUploadSuccessDialog = false }
                ) {
                    Text(stringResource(R.string.accept))
                }
            },
            containerColor = colors.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    ErrorModal(
        isVisible = showErrorDialog,
        errorMessage = errorMessage,
        onDismiss = {
            showErrorDialog = false
            errorMessage = ""
        },
        onRetry = null
    )
}

@Composable
fun ProfileHeader(
    user: UserData,
    verificationStatus: VerificationStatus? = null,
    languageViewModel: LanguageViewModel,
    selfieBytes: ByteArray? = null
) {
    val currentLanguage by languageViewModel.currentLanguage.collectAsState()
    val isSpanish = currentLanguage == "es"
    val context = LocalContext.current
    val colors = LocalRenovaColors.current

    // --- AÑADIDO: Obtener estado del Tour (de 'tour') ---
    val tourState = LocalTourState.current
    // --- FIN ---

    var showImageZoom by remember { mutableStateOf(false) }

    val selfieBitmap = remember(selfieBytes) {
        selfieBytes?.let { bytes ->
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }
    }

    // Se usa el layout de 'develop'
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        contentAlignment = Alignment.CenterStart
    ) {
        Image(
            painter = painterResource(id = R.drawable.fondo_perfil),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alignment = Alignment.Center,
            modifier = Modifier.matchParentSize()
        )
        Column(
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
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
                        color = colors.surface.copy(alpha = 0.3f),
                        modifier = Modifier
                            .wrapContentWidth()
                            .height(44.dp)
                    ) {
                        Row(
                            modifier = Modifier
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
                    Spacer(modifier = Modifier.width(1.dp))
                }

                // --- MODIFICADO: Añadir wrapper del Tour (de 'tour') ---
                Box(modifier = Modifier.onGloballyPositioned { coords ->
                    tourState.registerTarget("profile_language_toggle", coords)
                }) {
                    LanguageToggle(
                        isSpanish = isSpanish,
                        onLanguageChange = { newLang ->
                            languageViewModel.changeLanguage(newLang) {
                                (context as? Activity)?.recreate()
                            }
                        }
                    )
                }
                DisposableEffect("profile_language_toggle") {
                    onDispose { tourState.unregisterTarget("profile_language_toggle") }
                }
                // --- FIN DE MODIFICACIÓN ---
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Lógica de selfie y zoom de 'develop'
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clickable(
                            enabled = selfieBitmap != null,
                            onClick = { showImageZoom = true }
                        )
                ) {
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
                            if (selfieBitmap != null) {
                                Image(
                                    bitmap = selfieBitmap.asImageBitmap(),
                                    contentDescription = stringResource(R.string.profile_picture),
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = colors.primaryColor,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "${user.name} ${user.last_name}",
                    color = colors.surface,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.W600,
                    textAlign = TextAlign.Center,
                    maxLines = 3,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))

                // --- AÑADIDO: Sección de Puntos (de 'tour') ---
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
                            text = "${user.total_points}",
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
                // --- Fin de Sección de Puntos ---

                Spacer(modifier = Modifier.height(6.dp)) // <-- Spacer extra para dar aire
            }
        }
    }

    // Diálogo de zoom de imagen (de 'develop')
    if (showImageZoom && selfieBitmap != null) {
        Dialog(
            onDismissRequest = { showImageZoom = false }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clickable { showImageZoom = false }
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = colors.surface
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.profile_picture),
                                style = MaterialTheme.typography.titleLarge,
                                color = colors.textPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            IconButton(
                                onClick = { showImageZoom = false }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = stringResource(R.string.close),
                                    tint = colors.textSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f),
                            shape = RoundedCornerShape(12.dp),
                            shadowElevation = 4.dp
                        ) {
                            Image(
                                bitmap = selfieBitmap.asImageBitmap(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
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
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = description,
                        color = colors.textSecondary.copy(alpha = 0.95f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}