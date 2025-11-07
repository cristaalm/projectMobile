package com.renova.mobile.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Divider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import com.renova.mobile.R
import com.renova.mobile.network.ActivityItem
import com.renova.mobile.network.ApiClient
import com.renova.mobile.network.ClaimBadgeRequest
import com.renova.mobile.ui.components.SectionHeader
import com.renova.mobile.ui.components.formatFriendlyDate
import com.renova.mobile.ui.components.MonthlyBadgesSection
import com.renova.mobile.ui.components.BadgeDialog
import com.renova.mobile.ui.components.RetryableErrorModal
import com.renova.mobile.ui.components.DetailSheet
import com.renova.mobile.ui.theme.LocalRenovaColors
import com.renova.mobile.ui.theme.PoppinsFontFamily
import com.renova.mobile.ui.theme.RenovaColorScheme
import com.renova.mobile.ui.theme.RenovaColors
import com.renova.mobile.ui.viewmodels.ActivityViewModel
import com.renova.mobile.utils.SessionManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlinx.coroutines.launch

// --- INICIO: IMPORTS DEL TOUR ---
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.layout.onGloballyPositioned
import com.renova.mobile.ui.tour.LocalTourState
import androidx.compose.runtime.rememberCoroutineScope
// --- FIN: IMPORTS DEL TOUR ---

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

    // Estados para badges
    var selectedBadge by remember { mutableStateOf<MonthlyBadge?>(null) }
    var isClaimingBadge by remember { mutableStateOf(false) }
    var showErrorModal by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Estado para el modal de actividad
    var selectedActivity by remember { mutableStateOf<ActivityItem?>(null) }
    val sheetState = rememberModalBottomSheetState()

    // Estados para datos del usuario
    var currentMonthPoints by remember { mutableStateOf(0) }
    var userId by remember { mutableStateOf(0) }
    var userBadges by remember { mutableStateOf(com.renova.mobile.network.BadgeCollection()) }

    LaunchedEffect(Unit) {
        viewModel.loadHistory(1)

        // Cargar datos del usuario desde la sesión o API
        try {
            val token = sessionManager.getAccessToken()
            if (token != null) {
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

                        // Actualizar UserData en SessionManager
                        sessionManager.saveUser(userData)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val monthlyBadges = listOf(
        MonthlyBadge(
            id = 1,
            title = "Guerrero Ecológico",
            titleEn = "Eco Warrior",
            badgeName = "Eco Warrior",
            requiredPoints = 100,
            bonusPoints = 50,
            iconRes = R.drawable.ic_goal_1,
            color = Color.White,
            backgroundColor = Color(0xFF024653),
            isUnlocked = currentMonthPoints >= 100,
            isClaimed = userBadges.ecoWarrior,
            currentMonthProgress = currentMonthPoints.coerceAtMost(100)
        ),
        MonthlyBadge(
            id = 2,
            title = "Reciclador Pro",
            titleEn = "Recycler Pro",
            badgeName = "Recycler Pro",
            requiredPoints = 500,
            bonusPoints = 300,
            iconRes = R.drawable.ic_goal_2,
            color = Color.White,
            backgroundColor = Color(0xFF01C851),
            isUnlocked = currentMonthPoints >= 500,
            isClaimed = userBadges.recyclerPro,
            currentMonthProgress = currentMonthPoints.coerceAtMost(500)
        ),
        MonthlyBadge(
            id = 3,
            title = "Héroe Verde",
            titleEn = "Green Hero",
            badgeName = "Green Hero",
            requiredPoints = 1000,
            bonusPoints = 600,
            iconRes = R.drawable.ic_goal_3,
            color = Color.White,
            backgroundColor = Color(0xFF024653),
            isUnlocked = currentMonthPoints >= 1000,
            isClaimed = userBadges.greenHero,
            currentMonthProgress = currentMonthPoints.coerceAtMost(1000)
        ),
        MonthlyBadge(
            id = 4,
            title = "Salvador del Planeta",
            titleEn = "Planet Saver",
            badgeName = "Planet Saver",
            requiredPoints = 2500,
            bonusPoints = 1000,
            iconRes = R.drawable.ic_goal_4,
            color = Color.White,
            backgroundColor = Color(0xFF01C851),
            isUnlocked = currentMonthPoints >= 2500,
            isClaimed = userBadges.planetSaver,
            currentMonthProgress = currentMonthPoints.coerceAtMost(2500)
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SectionHeader(
            title = stringResource(id = R.string.bottom_nav_home),
            textColor = Color.White
        )

        if (state.isLoading && state.activities.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = renovaColors.activityPrimary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(bottom = 80.dp) // Padding para la barra de navegación
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
                    MonthlyBadgesSection(
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
    }

    // Modal de detalle de actividad
    selectedActivity?.let { activity ->
        ModalBottomSheet(
            onDismissRequest = { selectedActivity = null },
            sheetState = sheetState,
            containerColor = renovaColors.cardBackground,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            DetailSheet(activity = activity)
        }
    }

    // DIÁLOGO DE BADGE con lógica de claim
    selectedBadge?.let { badge ->
        BadgeDialog(
            badge = badge,
            currentMonthPoints = currentMonthPoints,
            isClaimingBadge = isClaimingBadge,
            onDismiss = { selectedBadge = null },
            onClaim = {
                scope.launch {
                    isClaimingBadge = true
                    try {
                        val response = ApiClient.apiService.claimBadge(
                            ClaimBadgeRequest(
                                userId = userId,
                                badgeName = badge.badgeName
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
                                viewModel.loadHistory(1)
                            }
                            selectedBadge = null
                        } else {
                            errorMessage = response.body()?.message
                                ?: context.getString(R.string.unknown_error)
                            showErrorModal = true
                        }
                    } catch (e: Exception) {
                        errorMessage = e.message ?: context.getString(R.string.unknown_error)
                        showErrorModal = true
                    } finally {
                        isClaimingBadge = false
                    }
                }
            }
        )
    }

    // Modal de error con retry
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
                        val response = ApiClient.apiService.claimBadge(
                            ClaimBadgeRequest(
                                userId = userId,
                                badgeName = badge.badgeName
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
                                viewModel.loadHistory(1)
                            }
                            selectedBadge = null
                        } else {
                            errorMessage = response.body()?.message
                                ?: context.getString(R.string.unknown_error)
                            showErrorModal = true
                        }
                    } catch (e: Exception) {
                        errorMessage = e.message ?: context.getString(R.string.unknown_error)
                        showErrorModal = true
                    } finally {
                        isClaimingBadge = false
                    }
                }
            }
        }
    )
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
        // Icono según el tipo de actividad
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