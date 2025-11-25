package com.renova.mobile.ui.screens

import android.app.Activity
import kotlinx.coroutines.delay
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.ScrollState
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
import androidx.compose.ui.layout.onGloballyPositioned
import com.renova.mobile.ui.tour.LocalTourState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.unit.sp

@Composable
private fun translateError(errorCode: String): String {
    return when (errorCode) {
        "ERROR_NO_INTERNET" -> stringResource(R.string.no_internet_retry_message)
        "ERROR_SESSION_EXPIRED" -> stringResource(R.string.session_expired)
        "ERROR_FORBIDDEN" -> stringResource(R.string.error_forbidden)
        "ERROR_NOT_FOUND" -> stringResource(R.string.error_not_found)
        "ERROR_SERVER" -> stringResource(R.string.error_server)
        "ERROR_PROFILE_NOT_FOUND" -> stringResource(R.string.error_profile_not_found)
        "ERROR_USER_INFO" -> stringResource(R.string.error_user_info)
        "ERROR_PROCESS_IMAGE" -> stringResource(R.string.error_process_image)
        "ERROR_UPDATE_NAME" -> stringResource(R.string.error_update_name)
        "ERROR_UPDATE_LASTNAME" -> stringResource(R.string.error_update_lastname)
        "ERROR_UPDATE_EMAIL" -> stringResource(R.string.error_update_email)
        "ERROR_UPDATE_PHONE" -> stringResource(R.string.error_update_phone)
        "ERROR_UPDATE_CURP" -> stringResource(R.string.error_update_curp)
        "ERROR_REQUEST_VERIFICATION" -> stringResource(R.string.error_request_verification)
        "ERROR_UNKNOWN" -> stringResource(R.string.unknown_error)
        "ERROR_EMAIL_ALREADY_EXISTS" -> stringResource(R.string.error_email_already_exists)
        "ERROR_PHONE_ALREADY_EXISTS" -> stringResource(R.string.error_phone_already_exists)
        "ERROR_CURP_ALREADY_EXISTS" -> stringResource(R.string.error_curp_already_exists)
        "ERROR_DUPLICATE_DATA" -> stringResource(R.string.error_duplicate_data)
        "ERROR_VALIDATION" -> stringResource(R.string.error_validation)
        "ERROR_CURRENT_PASSWORD_INCORRECT" -> stringResource(R.string.error_current_password_incorrect)
        "ERROR_PASSWORD_TOO_SHORT" -> stringResource(R.string.error_password_too_short)
        "ERROR_PASSWORD_NO_NUMBER" -> stringResource(R.string.error_password_no_number)
        "ERROR_PASSWORD_NO_SPECIAL" -> stringResource(R.string.error_password_no_special)
        "ERROR_PASSWORD_MISMATCH" -> stringResource(R.string.error_password_mismatch)
        "ERROR_PASSWORD_SAME_AS_OLD" -> stringResource(R.string.error_password_same_as_old)
        "ERROR_RESET_PASSWORD" -> stringResource(R.string.error_reset_password)
        "ERROR_PASSWORD_VALIDATION_FAILED" -> stringResource(R.string.error_password_validation_failed)
        else -> errorCode
    }
}


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

                if (state.isManualRefresh) {
                    val currentError = remember { mutableStateOf<String?>(null) }

                    LaunchedEffect(state.isManualRefresh) {
                        delay(50)
                        val errorState = profileViewModel.uiState.value
                        if (errorState is ProfileUiState.Error && errorState.isManualRefresh) {
                            currentError.value = errorState.message
                        }
                    }

                    currentError.value?.let { errorMsg ->
                        // ✅ FIX: Translate error HERE (in @Composable context)
                        val translatedError = translateError(errorMsg)

                        // ✅ FIX: LaunchedEffect WITHOUT translateError call inside
                        LaunchedEffect(errorMsg) {
                            delay(3000)
                            profileViewModel.clearError()
                            currentError.value = null
                        }

                        // Snackbar at top of screen
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = colors.cardBackground,
                                shadowElevation = 4.dp
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.WifiOff,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text(
                                        text = translatedError,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = colors.textPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    TextButton(
                                        onClick = {
                                            currentError.value = null
                                            profileViewModel.retry()
                                        }
                                    ) {
                                        Text(
                                            text = stringResource(R.string.retry),
                                            color = colors.primaryColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            is ProfileUiState.Error -> {
                val errorState = uiState as ProfileUiState.Error

                // ✅ Solo mostrar pantalla completa si NO es refresh manual
                if (!errorState.isManualRefresh) {
                    val isAuthError = errorState.message.contains("SESSION_EXPIRED", ignoreCase = true)

                    ErrorStateFullScreen(
                        errorMessage = translateError(errorState.message),
                        colors = colors,
                        title = stringResource(R.string.error_loading_profile),
                        canRetry = !isAuthError,
                        onRetry = {
                            profileViewModel.retry()
                        }
                    )
                } else {
                    // Si es manual refresh, mostrar lo que había antes (Success) con el Snackbar
                    LoadingState(renovaColors = colors)
                }
            }
        }
    }
}

@Composable
private fun ErrorStateFullScreen(
    errorMessage: String,
    colors: com.renova.mobile.ui.theme.RenovaColorScheme,
    title: String,
    canRetry: Boolean,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(
                    MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(50.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (canRetry) Icons.Default.WifiOff else Icons.Default.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
                modifier = Modifier.size(50.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = if (canRetry)
                title
            else
                stringResource(R.string.session_expired),
            fontFamily = com.renova.mobile.ui.theme.PoppinsFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = errorMessage,
            fontFamily = com.renova.mobile.ui.theme.PoppinsFontFamily,
            fontSize = 14.sp,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (canRetry) {
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primaryColor
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.retry),
                    fontFamily = com.renova.mobile.ui.theme.PoppinsFontFamily,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
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
    val tourState = LocalTourState.current

    // Estados del ViewModel
    val documentImages by profileViewModel.documentImages.collectAsState()
    val verificationRequestState by profileViewModel.verificationRequestState.collectAsState()
    val documentUploadState by profileViewModel.documentUploadState.collectAsState()
    val passwordResetState by profileViewModel.passwordResetState.collectAsState()

    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showUploadSuccessDialog by remember { mutableStateOf(false) }
    var uploadedDocumentName by remember { mutableStateOf("") }
    var showPasswordSuccessDialog by remember { mutableStateOf(false) }

    // ✅ FIX: Observar passwordResetState y traducir ANTES del LaunchedEffect
    val passwordErrorTranslated = remember(passwordResetState) {
        when (passwordResetState) {
            is ProfileViewModel.PasswordResetState.Error ->
                (passwordResetState as ProfileViewModel.PasswordResetState.Error).message
            else -> null
        }
    }

    // ✅ FIX: Traducir el error en el contexto @Composable
    passwordErrorTranslated?.let { errorCode ->
        val translated = translateError(errorCode)

        LaunchedEffect(errorCode) {
            errorMessage = translated
            showErrorDialog = true
            profileViewModel.resetPasswordResetState()
        }
    }

    // ✅ FIX: Observar el estado Success por separado
    LaunchedEffect(passwordResetState) {
        if (passwordResetState is ProfileViewModel.PasswordResetState.Success) {
            showPasswordSuccessDialog = true
            profileViewModel.resetPasswordResetState()
        }
    }

    // Cargar imágenes de manera defensiva
    LaunchedEffect(Unit) {
        try {
            profileViewModel.loadDocumentImages(user.id, identityVerification)
        } catch (e: Exception) {
            e.printStackTrace()
        }
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

    // ⭐ Observar cambios en updateFieldState
    val updateFieldState by profileViewModel.updateFieldState.collectAsState()

    // ✅ FIX: Traducir error de updateField
    val updateFieldErrorTranslated = remember(updateFieldState) {
        when (updateFieldState) {
            is ProfileViewModel.UpdateFieldState.Error ->
                (updateFieldState as ProfileViewModel.UpdateFieldState.Error).message
            else -> null
        }
    }

    updateFieldErrorTranslated?.let { errorCode ->
        val translated = translateError(errorCode)

        LaunchedEffect(errorCode) {
            errorMessage = translated
            showErrorDialog = true
            profileViewModel.resetUpdateState()
        }
    }

    LaunchedEffect(updateFieldState) {
        if (updateFieldState is ProfileViewModel.UpdateFieldState.Success) {
            delay(1000)
            profileViewModel.resetUpdateState()
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

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 80.dp)
    ) {
        ProfileHeader(
            user = user,
            verificationStatus = verificationStatus,
            languageViewModel = languageViewModel,
            selfieBytes = documentImages["selfie"],
            scrollState = scrollState
        )
        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = Modifier.padding(horizontal = 20.dp)
        ) {
            Box(modifier = Modifier.onGloballyPositioned { coords ->
                tourState.registerTarget(
                    id = "profile_verification_banner",
                    coordinates = coords,
                    scrollState = scrollState
                )
            }) {
                VerificationBanner(
                    verificationStatus = verificationStatus,
                    rejectionReason = identityVerification?.rejection_reason,
                    isSpanish = isSpanish
                )
            }
            DisposableEffect("profile_verification_banner") {
                onDispose { tourState.unregisterTarget("profile_verification_banner") }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(modifier = Modifier.onGloballyPositioned { coords ->
                tourState.registerTarget(
                    id = "profile_info_card",
                    coordinates = coords,
                    scrollState = scrollState
                )
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

            Spacer(modifier = Modifier.height(12.dp))

            SecurityCard(
                viewModel = profileViewModel,
                verificationStatus = verificationStatus
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (identityVerification != null) {
                Box(modifier = Modifier.onGloballyPositioned { coords ->
                    tourState.registerTarget(
                        id = "profile_documents_section",
                        coordinates = coords,
                        scrollState = scrollState
                    )
                }) {
                    DocumentsUploadSection(
                        documents = editDocuments,
                        verificationStatus = verificationStatus,
                        documentImages = documentImages,
                        documentUploadState = documentUploadState,
                        onImageSelected = { type: DocumentType, uri: Uri ->
                            profileViewModel.uploadDocument(type, uri, context)
                        }
                    )
                }
                DisposableEffect("profile_documents_section") {
                    onDispose { tourState.unregisterTarget("profile_documents_section") }
                }

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
        }
    }

    // ⭐ Diálogo de éxito de cambio de contraseña
    if (showPasswordSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showPasswordSuccessDialog = false },
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
                    text = stringResource(R.string.password_updated_title),
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.password_updated_message),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = colors.textSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { showPasswordSuccessDialog = false }
                ) {
                    Text(stringResource(R.string.understood))
                }
            },
            containerColor = colors.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Diálogos existentes
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
    selfieBytes: ByteArray? = null,
    scrollState: ScrollState
) {
    val currentLanguage by languageViewModel.currentLanguage.collectAsState()
    val isSpanish = currentLanguage == "es"
    val context = LocalContext.current
    val colors = LocalRenovaColors.current

    val tourState = LocalTourState.current

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
            modifier = Modifier.padding(horizontal = 20.dp)
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
                        color = Color.White.copy(alpha = 0.3f),
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
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (verificationStatus) {
                                    VerificationStatus.VERIFIED -> stringResource(R.string.profile_verified_badge)
                                    VerificationStatus.PENDING -> stringResource(R.string.profile_pending_badge)
                                    VerificationStatus.REJECTED -> stringResource(R.string.profile_rejected_badge)
                                    else -> stringResource(R.string.profile_no_badge)
                                },
                                color = Color.White,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                // --- MODIFICADO: Añadir wrapper del Tour (de 'tour') ---
                Box(modifier = Modifier.onGloballyPositioned { coords ->
                    // --- MODIFICADO: Pasar scrollState ---
                    tourState.registerTarget(
                        id = "profile_language_toggle",
                        coordinates = coords,
                        scrollState = scrollState // <--- MODIFICACIÓN: Usar el scrollState recibido
                    )
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

            Spacer(modifier = Modifier.height(8.dp))

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
                        color = Color.White,
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

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${user.name} ${user.last_name}",
                    color = Color.White,
                    style = MaterialTheme. typography.titleLarge,
                    fontWeight = FontWeight.W600,
                    textAlign = TextAlign.Center,
                    maxLines = 3,
                    modifier = Modifier.padding(horizontal = 20.dp),
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))

                // --- AÑADIDO: Sección de Puntos (de 'tour') ---
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier
                            .wrapContentWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = java.text.NumberFormat.getIntegerInstance(
                                java.util.Locale.forLanguageTag("es-MX")
                            ).format(user.total_points),
                            color = Color.White,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.W700
                        )
                        Text(
                            text = stringResource(R.string.points_unit),
                            color = Color.White.copy(alpha = 0.9f),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.W600
                        )
                    }
                }
                // --- Fin de Sección de Puntos ---

                Spacer(modifier = Modifier.height(12.dp)) // <-- Spacer extra para dar aire
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
                    color = Color.White
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
                                    tint = colors.textPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f),
                            shape = RoundedCornerShape(12.dp),
                            shadowElevation = 0.dp
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