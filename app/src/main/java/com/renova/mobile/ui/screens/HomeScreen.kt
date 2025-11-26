package com.renova.mobile.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import com.renova.mobile.ui.viewmodels.StreakViewModel
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.layout.onGloballyPositioned
import com.renova.mobile.R
import com.renova.mobile.network.ActivityItem
import com.renova.mobile.ui.components.*
import com.renova.mobile.ui.theme.LocalRenovaColors
import com.renova.mobile.ui.theme.PoppinsFontFamily
import com.renova.mobile.ui.theme.RenovaColors
import com.renova.mobile.ui.theme.RenovaColorScheme
import com.renova.mobile.ui.viewmodels.ActivityViewModel
import com.renova.mobile.ui.tour.LocalTourState
import kotlinx.coroutines.delay

@Composable
private fun translateError(errorCode: String, context: android.content.Context): String {
    return when (errorCode) {
        "ERROR_NO_INTERNET" -> context.getString(R.string.no_internet_retry_message)
        "ERROR_SESSION_EXPIRED" -> context.getString(R.string.session_expired)
        "ERROR_UNKNOWN" -> context.getString(R.string.unknown_error)
        else -> errorCode
    }
}

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
    activityViewModel: ActivityViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    streakViewModel: StreakViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val renovaColors = LocalRenovaColors.current
    val activityState by activityViewModel.state.collectAsState()
    val streakState by streakViewModel.state.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val tourState = LocalTourState.current

    // Estados locales solo para UI
    var selectedActivity by remember { mutableStateOf<ActivityItem?>(null) }
    var selectedBadge by remember { mutableStateOf<MonthlyBadge?>(null) }
    var showClaimErrorModal by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Carga inicial
    LaunchedEffect(Unit) {
        activityViewModel.loadHistory(1)
        streakViewModel.loadStreakData(isManualRefresh = false)
    }

    // Mostrar modal de error al reclamar badge
    LaunchedEffect(streakState.claimError) {
        if (streakState.claimError != null) {
            showClaimErrorModal = true
        }
    }

    // ... resto del código ...

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SectionHeader(
            title = stringResource(id = R.string.bottom_nav_home),
            textColor = Color.White
        )

        Box(modifier = Modifier.fillMaxSize()) {
            when {
                // Caso 1: Cargando por primera vez
                activityState.isLoading && !activityState.hasLoadedOnce && activityState.error == null -> {
                    LoadingState(renovaColors = renovaColors)
                }

                // Caso 2: Error crítico sin datos previos - PANTALLA COMPLETA
                activityState.error != null && !activityState.hasLoadedOnce -> {
                    ErrorStateFullScreen(
                        errorMessage = translateError(
                            activityState.error ?: "ERROR_UNKNOWN",
                            context
                        ),
                        renovaColors = renovaColors,
                        onRetry = {
                            activityViewModel.clearError()
                            activityViewModel.loadHistory(1)
                            streakViewModel.loadStreakData(isManualRefresh = false)
                        }
                    )
                }

                // Caso 3: Contenido exitoso
                activityState.hasLoadedOnce -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState)
                                .padding(bottom = 80.dp)
                        ) {
                            // Card de puntos
                            Box(
                                modifier = Modifier.onGloballyPositioned { coords ->
                                    tourState.registerTarget(
                                        id = "home_points_card",
                                        coordinates = coords,
                                        scrollState = scrollState
                                    )
                                }
                            ) {
                                AnimatedPointsCard(
                                    totalPoints = activityState.totalPoints,
                                    renovaColors = renovaColors
                                )
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

                                val recentActivities = activityState.activities.take(3)

                                if (recentActivities.isEmpty() && !activityState.isLoading) {
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
                                                onClick = { selectedActivity = activity }
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

                            // Sección de logros mensuales - USAR streakState ⬅️
                            Box(
                                modifier = Modifier.onGloballyPositioned { coords ->
                                    tourState.registerTarget(
                                        id = "home_achievements_section",
                                        coordinates = coords,
                                        scrollState = scrollState
                                    )
                                }
                            ) {
                                MonthlyBadgesSectionV2(
                                    badges = streakState.monthlyBadges,
                                    currentMonthPoints = streakState.currentMonthPoints,
                                    renovaColors = renovaColors,
                                    onBadgeClick = { badge -> selectedBadge = badge }
                                )
                            }
                        }

                        // Snackbar de error en refresh/operaciones no críticas
                        if (activityState.error != null && activityState.hasLoadedOnce) {
                            LaunchedEffect(activityState.error) {
                                delay(3000)
                                activityViewModel.clearError()
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                contentAlignment = Alignment.TopCenter
                            ) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    color = renovaColors.cardBackground,
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
                                            text = activityState.error ?: "",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = renovaColors.textPrimary,
                                            modifier = Modifier.weight(1f)
                                        )
                                        TextButton(
                                            onClick = {
                                                activityViewModel.clearError()
                                                activityViewModel.loadHistory(1)
                                                streakViewModel.loadStreakData(isManualRefresh = false)
                                            }
                                        ) {
                                            Text(
                                                text = stringResource(R.string.retry),
                                                color = renovaColors.primaryColor
                                            )
                                        }
                                    }
                                }
                            }
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

    // Diálogo de badge V2 - USAR streakViewModel
    selectedBadge?.let { badge ->
        BadgeDialogV2(
            badge = badge,
            currentMonthPoints = streakState.currentMonthPoints,
            isClaimingBadge = streakState.isClaimingBadge,
            onDismiss = { selectedBadge = null },
            onClaim = {
                streakViewModel.claimBadge(badge.id)
                selectedBadge = null
            }
        )
    }

    // Modal de error al reclamar badge - USAR streakState
    if (showClaimErrorModal && streakState.claimError != null) {
        RetryableErrorModal(
            isVisible = true,
            errorMessage = translateError(
                streakState.claimError ?: "ERROR_UNKNOWN",
                context
            ),
            onDismiss = {
                showClaimErrorModal = false
                streakViewModel.clearClaimError()
            },
            onRetry = {
                showClaimErrorModal = false
                streakViewModel.clearClaimError()
                selectedBadge?.let { badge ->
                    streakViewModel.claimBadge(badge.id)
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

        Column(modifier = Modifier.weight(1f)) {
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
                        materialName.contains("Plástico", ignoreCase = true) ->
                            context.getString(R.string.plastic)
                        materialName.contains("Aluminio", ignoreCase = true) ->
                            context.getString(R.string.aluminum)
                        else -> materialName
                    }
                    if (activity.scan?.is_crushed == true)
                        "$baseName - ${context.getString(R.string.crushed)}"
                    else baseName
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

        Column(horizontalAlignment = Alignment.End) {
            val pointsColor = when (activity.type_history) {
                1 -> colors.negativePoints
                2 -> if (activity.points == 0) colors.textPrimary else colors.primaryColor
                3 -> if (activity.points < 0) colors.negativePoints else colors.primaryColor
                else -> if (activity.points == 0) colors.textPrimary else colors.primaryColor
            }
            val pointsText = when (activity.type_history) {
                1 -> if ("${activity.points}".startsWith("-")) "${activity.points}"
                else "-${activity.points}"
                2 -> if (activity.points == 0) "-${activity.points}-"
                else "+${activity.points}"
                3 -> if (activity.points < 0) "${activity.points}"
                else "+${activity.points}"
                else -> if (activity.points == 0) "${activity.points}"
                else "+${activity.points}"
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