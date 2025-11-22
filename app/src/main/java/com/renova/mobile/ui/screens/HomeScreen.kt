package com.renova.mobile.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Divider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import com.renova.mobile.R
import com.renova.mobile.network.ActivityItem
import com.renova.mobile.network.ApiClient
import com.renova.mobile.network.ClaimBadgeRequestV2
import com.renova.mobile.ui.components.SectionHeader
import com.renova.mobile.ui.components.formatFriendlyDate
import com.renova.mobile.ui.components.MonthlyBadgesSectionV2
import com.renova.mobile.ui.components.BadgeDialogV2
import com.renova.mobile.ui.components.RetryableErrorModal
import com.renova.mobile.ui.components.DetailSheet
import com.renova.mobile.ui.components.LoadingState
import com.renova.mobile.ui.theme.LocalRenovaColors
import com.renova.mobile.ui.theme.PoppinsFontFamily
import com.renova.mobile.ui.theme.RenovaColorScheme
import com.renova.mobile.ui.theme.RenovaColors
import com.renova.mobile.ui.viewmodels.ActivityViewModel
import com.renova.mobile.utils.SessionManager
import kotlinx.coroutines.launch
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.layout.onGloballyPositioned
import com.renova.mobile.ui.tour.LocalTourState
import androidx.compose.runtime.rememberCoroutineScope

@Composable
private fun ErrorStateFullScreen(
    errorMessage: String,
    renovaColors: RenovaColorScheme,
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
                    renovaColors.activityPrimary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(50.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.WifiOff,
                contentDescription = null,
                tint = renovaColors.activityPrimary.copy(alpha = 0.5f),
                modifier = Modifier.size(50.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.error_loading_activity),
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = renovaColors.textPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = errorMessage,
            fontFamily = PoppinsFontFamily,
            fontSize = 14.sp,
            color = renovaColors.textSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = renovaColors.primaryColor
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
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: ActivityViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val renovaColors = LocalRenovaColors.current
    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val scope = rememberCoroutineScope()
    val tourState = LocalTourState.current

    // Estados para badges (versión V2 desde el backend)
    var monthlyBadges by remember { mutableStateOf<List<MonthlyBadge>>(emptyList()) }
    var selectedBadge by remember { mutableStateOf<MonthlyBadge?>(null) }
    var isClaimingBadge by remember { mutableStateOf(false) }
    var showErrorModal by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var hasLoadedOnce by remember { mutableStateOf(false) }

    // Estado para el modal de actividad
    var selectedActivity by remember { mutableStateOf<ActivityItem?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Estados para datos del usuario
    var currentMonthPoints by remember { mutableStateOf(0) }
    var userId by remember { mutableStateOf(0) }
    var userBadges by remember { mutableStateOf(com.renova.mobile.network.BadgeCollection()) }

    // 🔄 Función para cargar datos
    fun loadHomeData() {
        scope.launch {
            try {
                viewModel.loadHistory(1)

                val token = sessionManager.getAccessToken()
                if (token != null) {
                    // 1. Obtener datos del usuario
                    try {
                        val identityResponse = ApiClient.apiService.identifyUser(
                            com.renova.mobile.network.IdentifyUserRequest(
                                token = token,
                                with_identity = false
                            )
                        )

                        if (identityResponse.isSuccessful && identityResponse.body()?.success == true) {
                            identityResponse.body()?.data?.user?.let { userData ->
                                userId = userData.id
                                currentMonthPoints = userData.points_month
                                userBadges = userData.badge
                                sessionManager.saveUser(userData)
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        // Continuar aunque falle identityUser
                    }

                    // 2. Obtener badges desde el backend
                    try {
                        val badgesResponse = ApiClient.apiService.getAllBadges(
                            perPage = 100,
                            status = 1
                        )

                        if (badgesResponse.isSuccessful && badgesResponse.body()?.success == true) {
                            badgesResponse.body()?.data?.data?.let { badges ->
                                monthlyBadges = badges.map { badge ->
                                    val (iconRes, color, bgColor) = getBadgeVisualConfig(badge.name)
                                    val isClaimed = userBadges.isClaimed(badge.name)

                                    MonthlyBadge(
                                        id = badge.id,
                                        name = badge.name,
                                        pointsRequired = badge.pointsRequired,
                                        bonusPoints = badge.pointsAwarded,
                                        iconRes = iconRes,
                                        isActive = badge.status,
                                        isUnlocked = currentMonthPoints >= badge.pointsRequired,
                                        isClaimed = isClaimed,
                                        currentMonthProgress = currentMonthPoints.coerceAtMost(badge.pointsRequired)
                                    )
                                }.sortedBy { it.pointsRequired }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        // No crítico si fallan los badges
                    }

                    hasLoadedOnce = true
                } else {
                    errorMessage = context.getString(R.string.session_not_found)
                }
            } catch (e: Exception) {
                errorMessage = when {
                    e.message?.contains("Unable to resolve host", ignoreCase = true) == true ||
                            e.message?.contains("timeout", ignoreCase = true) == true ||
                            e.message?.contains("Failed to connect", ignoreCase = true) == true ||
                            e.message?.contains("No address associated with hostname", ignoreCase = true) == true ->
                        context.getString(R.string.no_internet_connection)
                    e.message?.contains("401", ignoreCase = true) == true ||
                            e.message?.contains("Unauthorized", ignoreCase = true) == true ->
                        "Sesión expirada. Por favor, inicia sesión nuevamente"
                    else -> e.message ?: context.getString(R.string.unknown_error)
                }
                e.printStackTrace()
            }
        }
    }

    // Carga inicial
    LaunchedEffect(Unit) {
        loadHomeData()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SectionHeader(
            title = stringResource(id = R.string.bottom_nav_home),
            textColor = Color.White
        )

        // 🎯 Lógica de renderizado basada en estados
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                // Caso 1: Cargando por primera vez (sin error todavía)
                state.isLoading && state.activities.isEmpty() && !state.hasLoadedOnce && state.error == null && errorMessage.isEmpty() -> {
                    LoadingState(renovaColors = renovaColors)
                }

                // Caso 2: Error crítico sin datos previos - PANTALLA COMPLETA
                (state.error != null || errorMessage.isNotEmpty()) && !state.hasLoadedOnce && !hasLoadedOnce -> {
                    ErrorStateFullScreen(
                        errorMessage = state.error ?: errorMessage,
                        renovaColors = renovaColors,
                        onRetry = {
                            viewModel.clearError()
                            errorMessage = ""
                            loadHomeData()
                        }
                    )
                }

                // Caso 3: Contenido exitoso
                state.hasLoadedOnce || hasLoadedOnce -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(bottom = 80.dp)
                    ) {
                        // Card de puntos
                        Box(modifier = Modifier.onGloballyPositioned { coords ->
                            tourState.registerTarget(
                                id = "home_points_card",
                                coordinates = coords,
                                scrollState = scrollState
                            )
                        }) {
                            AnimatedPointsCard(
                                totalPoints = state.totalPoints,
                                renovaColors = renovaColors
                            )
                        }
                        DisposableEffect("home_points_card") {
                            onDispose { tourState.unregisterTarget("home_points_card") }
                        }

                        // Actividad reciente
                        Column(
                            modifier = Modifier.onGloballyPositioned { coords ->
                                tourState.registerTarget(
                                    id = "home_recent_activity",
                                    coordinates = coords,
                                    scrollState = scrollState
                                )
                            }
                        ) {
                            Column(
                                modifier = Modifier.padding(
                                    start = 24.dp,
                                    end = 24.dp,
                                    top = 6.dp,
                                    bottom = 10.dp
                                )
                            ) {
                                Text(
                                    text = stringResource(R.string.recent_activity),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = renovaColors.textPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 21.sp,
                                    fontFamily = PoppinsFontFamily
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = stringResource(R.string.last_movements),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = renovaColors.textSecondary,
                                    fontSize = 14.sp,
                                    fontFamily = PoppinsFontFamily
                                )
                            }

                            val recentActivities = state.activities.take(3)

                            if (recentActivities.isEmpty() && !state.isLoading) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 24.dp, vertical = 32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.History,
                                            contentDescription = null,
                                            tint = renovaColors.textSecondary.copy(alpha = 0.3f),
                                            modifier = Modifier.size(64.dp)
                                        )
                                        Text(
                                            text = stringResource(R.string.no_activity_yet),
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = renovaColors.textSecondary,
                                            textAlign = TextAlign.Center,
                                            fontFamily = PoppinsFontFamily
                                        )
                                    }
                                }
                            } else {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 24.dp),
                                    verticalArrangement = Arrangement.spacedBy(0.dp)
                                ) {
                                    recentActivities.forEachIndexed { index, activity ->
                                        HistoryActivityCard(
                                            activity = activity,
                                            colors = renovaColors,
                                            onClick = {
                                                selectedActivity = activity
                                            }
                                        )
                                        if (index < recentActivities.size - 1) {
                                            Divider(
                                                color = RenovaColors.PrimaryColor,
                                                thickness = 1.dp,
                                                modifier = Modifier.padding(vertical = 3.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        DisposableEffect("home_recent_activity") {
                            onDispose { tourState.unregisterTarget("home_recent_activity") }
                        }

                        // Sección de logros mensuales
                        Box(modifier = Modifier.onGloballyPositioned { coords ->
                            tourState.registerTarget(
                                id = "home_achievements_section",
                                coordinates = coords,
                                scrollState = scrollState
                            )
                        }) {
                            MonthlyBadgesSectionV2(
                                badges = monthlyBadges,
                                currentMonthPoints = currentMonthPoints,
                                renovaColors = renovaColors,
                                onBadgeClick = { badge ->
                                    selectedBadge = badge
                                }
                            )
                        }
                        DisposableEffect("home_achievements_section") {
                            onDispose { tourState.unregisterTarget("home_achievements_section") }
                        }
                    }
                }

                // Caso 4: Fallback
                else -> {
                    LoadingState(renovaColors = renovaColors)
                }
            }
        }
    }

    // Modal de detalle de actividad
    selectedActivity?.let { activity ->
        ModalBottomSheet(
            onDismissRequest = { selectedActivity = null },
            sheetState = sheetState,
            containerColor = renovaColors.cardBackground,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            scrimColor = Color.Black.copy(alpha = 0.32f)
        ) {
            DetailSheet(activity = activity)
        }
    }

    // Diálogo de badge V2
    selectedBadge?.let { badge ->
        BadgeDialogV2(
            badge = badge,
            currentMonthPoints = currentMonthPoints,
            isClaimingBadge = isClaimingBadge,
            onDismiss = { selectedBadge = null },
            onClaim = {
                scope.launch {
                    isClaimingBadge = true
                    try {
                        val response = ApiClient.apiService.claimBadgeV2(
                            ClaimBadgeRequestV2(
                                userId = userId,
                                badgeId = badge.id
                            )
                        )

                        if (response.isSuccessful && response.body()?.success == true) {
                            response.body()?.data?.user?.let { updatedUserData ->
                                sessionManager.saveUser(updatedUserData)

                                val currentUser = sessionManager.getUser()
                                currentUser?.let { user ->
                                    val updatedUser = user.copy(
                                        points_month = updatedUserData.points_month,
                                        badge = updatedUserData.badge,
                                        total_points = updatedUserData.total_points
                                    )
                                    sessionManager.saveSession(
                                        sessionManager.getAccessToken() ?: "",
                                        sessionManager.getTokenType(),
                                        sessionManager.getExpiresAt(),
                                        updatedUser,
                                        sessionManager.hasRememberMe()
                                    )
                                }

                                currentMonthPoints = updatedUserData.points_month
                                userBadges = updatedUserData.badge

                                monthlyBadges = monthlyBadges.map { b ->
                                    if (b.id == badge.id) {
                                        b.copy(isClaimed = true)
                                    } else b
                                }

                                viewModel.loadHistory(1)
                            }
                            selectedBadge = null
                        } else {
                            errorMessage = response.body()?.message
                                ?: context.getString(R.string.unknown_error)
                            showErrorModal = true
                        }
                    } catch (e: Exception) {
                        errorMessage = when {
                            e.message?.contains("Unable to resolve host", ignoreCase = true) == true ||
                                    e.message?.contains("timeout", ignoreCase = true) == true ||
                                    e.message?.contains("Failed to connect", ignoreCase = true) == true ||
                                    e.message?.contains("No address associated with hostname", ignoreCase = true) == true ->
                                context.getString(R.string.no_internet_connection)
                            else -> e.message ?: context.getString(R.string.unknown_error)
                        }
                        showErrorModal = true
                    } finally {
                        isClaimingBadge = false
                    }
                }
            }
        )
    }

    // Modal de error con retry (solo para errores secundarios)
    if (showErrorModal) {
        RetryableErrorModal(
            isVisible = showErrorModal,
            errorMessage = errorMessage,
            onDismiss = {
                showErrorModal = false
                errorMessage = ""
            },
            onRetry = {
                showErrorModal = false
                selectedBadge?.let { badge ->
                    scope.launch {
                        isClaimingBadge = true
                        try {
                            val response = ApiClient.apiService.claimBadgeV2(
                                ClaimBadgeRequestV2(
                                    userId = userId,
                                    badgeId = badge.id
                                )
                            )

                            if (response.isSuccessful && response.body()?.success == true) {
                                response.body()?.data?.user?.let { updatedUserData ->
                                    sessionManager.saveUser(updatedUserData)

                                    val currentUser = sessionManager.getUser()
                                    currentUser?.let { user ->
                                        val updatedUser = user.copy(
                                            points_month = updatedUserData.points_month,
                                            badge = updatedUserData.badge,
                                            total_points = updatedUserData.total_points
                                        )
                                        sessionManager.saveSession(
                                            sessionManager.getAccessToken() ?: "",
                                            sessionManager.getTokenType(),
                                            sessionManager.getExpiresAt(),
                                            updatedUser,
                                            sessionManager.hasRememberMe()
                                        )
                                    }

                                    currentMonthPoints = updatedUserData.points_month
                                    userBadges = updatedUserData.badge

                                    monthlyBadges = monthlyBadges.map { b ->
                                        if (b.id == badge.id) b.copy(isClaimed = true) else b
                                    }

                                    viewModel.loadHistory(1)
                                }
                                selectedBadge = null
                            } else {
                                errorMessage = response.body()?.message
                                    ?: context.getString(R.string.unknown_error)
                                showErrorModal = true
                            }
                        } catch (e: Exception) {
                            errorMessage = when {
                                e.message?.contains("Unable to resolve host", ignoreCase = true) == true ||
                                        e.message?.contains("timeout", ignoreCase = true) == true ||
                                        e.message?.contains("Failed to connect", ignoreCase = true) == true ||
                                        e.message?.contains("No address associated with hostname", ignoreCase = true) == true ->
                                    context.getString(R.string.no_internet_connection)
                                else -> e.message ?: context.getString(R.string.unknown_error)
                            }
                            showErrorModal = true
                        } finally {
                            isClaimingBadge = false
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun HistoryActivityCard(
    activity: ActivityItem,
    colors: RenovaColorScheme,
    onClick: () -> Unit = {}
) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val iconData = when (activity.type_history) {
            1 -> Pair(Icons.Default.ShoppingCart, RenovaColors.PrimaryColor)
            2 -> Pair(Icons.Default.Recycling, RenovaColors.PrimaryColor)
            3 -> Pair(Icons.Default.Person, RenovaColors.PrimaryColor)
            else -> Pair(Icons.Default.History, RenovaColors.PrimaryColor)
        }
        val icon = iconData.first
        val iconColor = iconData.second

        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(32.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = when (activity.type_history) {
                    1 -> stringResource(R.string.reward_exchange)
                    2 -> stringResource(R.string.recycling)
                    3 -> stringResource(R.string.points_adjustment)
                    else -> stringResource(R.string.activity)
                },
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.SemiBold
                ),
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            val subtitleText = when (activity.type_history) {
                1 -> activity.alliance?.name ?: ""
                2 -> {
                    val materialName = activity.material_type?.name ?: ""
                    val baseName = when {
                        materialName.contains("Plástico", ignoreCase = true)  ->
                            context.getString(R.string.plastic)
                        materialName.contains("Aluminio", ignoreCase = true) ->
                            context.getString(R.string.aluminum)
                        else -> materialName
                    }
                    if (activity.scan?.is_crushed == true) "$baseName - ${context.getString(R.string.crushed)}" else baseName
                }
                3 -> context.getString(R.string.manual_adjustment)
                else -> activity.alliance?.name ?: ""
            }

            if (subtitleText.isNotBlank()) {
                Text(
                    text = subtitleText,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = PoppinsFontFamily
                    ),
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = formatFriendlyDate(activity.created_at),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = PoppinsFontFamily
                ),
                color = colors.textSecondary
            )
        }

        Column(
            horizontalAlignment = Alignment.End
        ) {
            val pointsColor = when (activity.type_history) {
                1 -> colors.negativePoints
                2 -> if (activity.points == 0) colors.textPrimary else colors.primaryColor
                3 -> if (activity.points < 0) colors.negativePoints else colors.primaryColor
                else -> if (activity.points == 0) colors.textPrimary else colors.primaryColor
            }
            val pointsText = when (activity.type_history) {
                1 -> if ("${activity.points}".startsWith("-")) "${activity.points}" else "-${activity.points}"
                2 -> if (activity.points == 0) "-${activity.points}-" else "+${activity.points}"
                3 -> if (activity.points < 0) "${activity.points}" else "+${activity.points}"
                else -> if (activity.points == 0) "${activity.points}" else "+${activity.points}"
            }
            Text(
                text = pointsText,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold
                ),
                color = pointsColor
            )
        }
    }
}

@Composable
private fun AnimatedPointsCard(
    totalPoints: Int,
    renovaColors: RenovaColorScheme
) {
    var animatedPoints by remember { mutableStateOf(0f) }

    LaunchedEffect(totalPoints) {
        animate(
            initialValue = 0f,
            targetValue = totalPoints.toFloat(),
            animationSpec = tween(
                durationMillis = 1500,
                easing = FastOutSlowInEasing
            )
        ) { value, _ ->
            animatedPoints = value
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 14.dp, start = 18.dp, end = 20.dp, bottom = 8.dp)
            .height(135.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.fondo),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(
                    Color(0xFF05D16E),
                    blendMode = androidx.compose.ui.graphics.BlendMode.Modulate
                ),
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(24.dp))
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(R.string.total_points),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(0.dp))
                    Row {
                        Text(
                            text = java.text.NumberFormat.getIntegerInstance(
                                java.util.Locale.forLanguageTag("es-MX")
                            ).format(animatedPoints.toInt()),
                            style = MaterialTheme.typography.displayLarge,
                            color = Color.White,
                            fontSize = 52.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 52.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .background(
                            Color.White.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(20.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.leaf),
                        contentDescription = null,
                        modifier = Modifier.size(45.dp),
                        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(
                            Color.White.copy(alpha = 0.9f)
                        )
                    )
                }
            }
        }
    }
}