package com.renova.mobile.ui.screens.business

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.renova.mobile.ui.components.LoadingState
import androidx.compose.ui.unit.dp
import com.renova.mobile.ui.viewmodels.LanguageViewModel
import com.renova.mobile.R
import androidx.compose.ui.text.style.TextAlign
import com.renova.mobile.network.UserData
import com.renova.mobile.ui.components.*
import com.renova.mobile.ui.theme.LocalRenovaColors
import com.renova.mobile.ui.viewmodels.BusinessProfileViewModel
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import com.renova.mobile.ui.viewmodels.BusinessProfileUiState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.*
import androidx.compose.ui.graphics.asImageBitmap
import android.graphics.BitmapFactory
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalContext
import com.renova.mobile.ui.components.SectionHeader
import com.renova.mobile.network.Reward
import androidx.compose.ui.res.stringResource
import com.renova.mobile.ui.screens.RewardDetailSheet
import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
import androidx.compose.ui.window.Dialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessProfile(
    languageViewModel: LanguageViewModel,
    onNavigateBack: () -> Unit = {},
    businessProfileViewModel: BusinessProfileViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
) {
    val currentLanguage by languageViewModel.currentLanguage.collectAsState()
    val isSpanish = currentLanguage == "es"
    val uiState by businessProfileViewModel.uiState.collectAsState()
    val isRefreshing by businessProfileViewModel.isRefreshing.collectAsState()
    val colors = LocalRenovaColors.current

    var showMainErrorModal by remember { mutableStateOf(false) }
    var mainErrorMessage by remember { mutableStateOf("") }
    var canRetryMainError by remember { mutableStateOf(true) }

    var selectedReward: Reward? by remember { mutableStateOf(null) }
    var showRewardDetailSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    val pullToRefreshState = rememberPullToRefreshState()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Box {
            SectionHeader(
                title = stringResource(R.string.business_profile_title),
                hasNavigationIcon = true
            )

            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 4.dp)
                    .zIndex(1f)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    tint = Color.White
                )
            }
        }

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            when (val state = uiState) {
                is BusinessProfileUiState.Loading -> {
                    LoadingState(renovaColors = colors)
                }

                is BusinessProfileUiState.Success -> {
                    PullToRefreshBox(
                        isRefreshing = isRefreshing,
                        onRefresh = {
                            businessProfileViewModel.refreshBusinessProfile()
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
                        BusinessProfileContent(
                            user = state.user,
                            rewards = state.rewards,
                            businessProfileViewModel = businessProfileViewModel,
                            languageViewModel = languageViewModel,
                            onRefresh = { businessProfileViewModel.refreshBusinessProfile() },
                            isSpanish = isSpanish,
                            onRewardClick = { reward ->
                                selectedReward = reward
                                showRewardDetailSheet = true
                            }
                        )
                    }
                }

                is BusinessProfileUiState.Error -> {
                    val errorState = uiState as BusinessProfileUiState.Error
                    val authErrorString = stringResource(R.string.authentication_error)
                    val sessionErrorString = stringResource(R.string.session_error)

                    val isAuthError = errorState.message.contains("token", ignoreCase = true) ||
                            errorState.message.contains(authErrorString, ignoreCase = true) ||
                            errorState.message.contains(sessionErrorString, ignoreCase = true)

                    LaunchedEffect(Unit) {
                        mainErrorMessage = errorState.message
                        canRetryMainError = !isAuthError
                        showMainErrorModal = true
                    }
                    Box(modifier = Modifier.fillMaxSize())
                }
            }

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
                        businessProfileViewModel.retry()
                    }
                } else null
            )

            if (showRewardDetailSheet && selectedReward != null) {
                ModalBottomSheet(
                    onDismissRequest = { showRewardDetailSheet = false },
                    sheetState = sheetState,
                    containerColor = colors.cardBackground,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                ) {
                    RewardDetailSheet(reward = selectedReward!!, isBusiness = true)
                }
            }
        }
    }
}

@Composable
private fun BusinessProfileContent(
    user: UserData,
    rewards: List<Reward>,
    businessProfileViewModel: BusinessProfileViewModel,
    languageViewModel: LanguageViewModel,
    isSpanish: Boolean,
    onRewardClick: (Reward) -> Unit,
    onRefresh: () -> Unit
) {
    val documentImages by businessProfileViewModel.documentImages.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        BusinessHeader(
            user = user,
            languageViewModel = languageViewModel,
            logoBytes = documentImages["logo"]
        )
        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = Modifier.padding(horizontal = 20.dp)
        ) {
            BusinessInfoCard(
                user = user,
                isSpanish = isSpanish,
                viewModel = businessProfileViewModel
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (rewards.isNotEmpty()) {
                RewardsSection(
                    rewards = rewards,
                    onRewardClick = onRewardClick
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun BusinessHeader(
    user: UserData,
    languageViewModel: LanguageViewModel,
    logoBytes: ByteArray? = null
) {
    val currentLanguage by languageViewModel.currentLanguage.collectAsState()
    val isSpanish = currentLanguage == "es"
    val context = LocalContext.current
    val colors = LocalRenovaColors.current

    var showImageZoom by remember { mutableStateOf(false) }

    val logoBitmap = remember(logoBytes) {
        logoBytes?.let { bytes ->
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }
    }

    // Usar las funciones de traducción e iconos del archivo de componentes
    val categoryIcon = getCategoryIcon(user.alliance?.type_shop?.name)
    val translatedCategory = getCategoryTranslation(user.alliance?.type_shop?.name, isSpanish)

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
            modifier = Modifier
                .padding(horizontal = 20.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

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
                            imageVector = Icons.Default.BusinessCenter,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (user.status == 1) {
                                stringResource(R.string.status_active)
                            } else {
                                stringResource(R.string.status_inactive)
                            },
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
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

            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clickable(
                            enabled = logoBitmap != null,
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
                            if (logoBitmap != null) {
                                Image(
                                    bitmap = logoBitmap.asImageBitmap(),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.StoreMallDirectory,
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
                    text = user.alliance?.name ?: user.name,
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Badge de categoría mejorado con icono y traducción
                if (translatedCategory != "-") {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(alpha = 0.3f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = categoryIcon,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = translatedCategory,
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        if (showImageZoom && logoBitmap != null) {
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
                                    bitmap = logoBitmap.asImageBitmap(),
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
}