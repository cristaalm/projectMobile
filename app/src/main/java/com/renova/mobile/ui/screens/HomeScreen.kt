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
import androidx.compose.material3.Icon
import com.renova.mobile.R
import com.renova.mobile.network.ActivityItem
import com.renova.mobile.ui.components.SectionHeader
import com.renova.mobile.ui.components.formatFriendlyDate
import com.renova.mobile.ui.components.MonthlyBadgesSection
import com.renova.mobile.ui.components.BadgeDialog
import com.renova.mobile.ui.theme.LocalRenovaColors
import com.renova.mobile.ui.theme.PoppinsFontFamily
import com.renova.mobile.ui.theme.RenovaColorScheme
import com.renova.mobile.ui.theme.RenovaColors
import com.renova.mobile.ui.viewmodels.ActivityViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

// --- INICIO: IMPORTS DEL TOUR ---
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.layout.onGloballyPositioned
import com.renova.mobile.ui.tour.LocalTourState
// --- FIN: IMPORTS DEL TOUR ---
@Composable
fun HomeScreen(
    viewModel: ActivityViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val renovaColors = LocalRenovaColors.current
    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()

    val tourState = LocalTourState.current

    // Estado para el badge seleccionado
    var selectedBadge by remember { mutableStateOf<MonthlyBadge?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadHistory(1)
    }

    // LÓGICA DE PUNTOS MENSUALES - Calcular puntos del mes actual
    val currentMonthPoints = remember(state.activities) {
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)

        state.activities.filter { activity ->
            try {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                dateFormat.timeZone = TimeZone.getTimeZone("UTC")
                val date = dateFormat.parse(activity.created_at)

                val activityCalendar = Calendar.getInstance().apply {
                    time = date ?: return@filter false
                }
                activityCalendar.get(Calendar.MONTH) == currentMonth &&
                        activityCalendar.get(Calendar.YEAR) == currentYear
            } catch (e: Exception) {
                false
            }
        }.sumOf { it.points }
    }

    val monthlyBadges = listOf(
        MonthlyBadge(
            id = 1,
            title = stringResource(R.string.achievement_eco_warrior),
            titleEn = "Eco Warrior",
            requiredPoints = 100,
            bonusPoints = 50,
            iconRes = R.drawable.ic_goal_1,
            color = Color.White,
            backgroundColor = Color(0xFF024653),
            isUnlocked = currentMonthPoints >= 100,
            currentMonthProgress = currentMonthPoints.coerceAtMost(100)
        ),
        MonthlyBadge(
            id = 2,
            title = stringResource(R.string.achievement_recycler_pro),
            titleEn = "Recycler Pro",
            requiredPoints = 500,
            bonusPoints = 300,
            iconRes = R.drawable.ic_goal_2,
            color = Color.White,
            backgroundColor = Color(0xFF01C851),
            isUnlocked = currentMonthPoints >= 500,
            currentMonthProgress = currentMonthPoints.coerceAtMost(500)
        ),
        MonthlyBadge(
            id = 3,
            title = stringResource(R.string.achievement_green_hero),
            titleEn = "Green Hero",
            requiredPoints = 1000,
            bonusPoints = 600,
            iconRes = R.drawable.ic_goal_3,
            color = Color.White,
            backgroundColor = Color(0xFF024653),
            isUnlocked = currentMonthPoints >= 1000,
            currentMonthProgress = currentMonthPoints.coerceAtMost(1000)
        ),
        MonthlyBadge(
            id = 4,
            title = stringResource(R.string.achievement_planet_saver),
            titleEn = "Planet Saver",
            requiredPoints = 2500,
            bonusPoints = 1000,
            iconRes = R.drawable.ic_goal_4,
            color = Color.White,
            backgroundColor = Color(0xFF01C851),
            isUnlocked = currentMonthPoints >= 2500,
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
            ) {
                // Card de puntos
                Box(modifier = Modifier.onGloballyPositioned { coords ->
                    tourState.registerTarget("home_points_card", coords)
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
                        tourState.registerTarget("home_recent_activity", coords)
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
                                    onClick = { }
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

                // Sección de logros mensuales - CON DISEÑO DE STREAKSCREEN
                Box(modifier = Modifier.onGloballyPositioned { coords ->
                    tourState.registerTarget("home_achievements_section", coords)
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

    // DIÁLOGO DE BADGE - Del archivo de componentes más reciente
    selectedBadge?.let { badge ->
        BadgeDialog(
            badge = badge,
            currentMonthPoints = currentMonthPoints,
            onDismiss = { selectedBadge = null }
        )
    }
}


@Composable
fun HistoryActivityCard(
    activity: ActivityItem,
    colors: RenovaColorScheme,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val (icon, iconColor) = when (activity.type_history) {
            2 -> Icons.Default.Recycling to RenovaColors.Success
            1 -> Icons.Default.ShoppingCart to RenovaColors.Primary
            else -> Icons.Default.History to RenovaColors.Warning
        }

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
                    2 -> stringResource(R.string.recycling)
                    1 -> stringResource(R.string.reward_exchange)
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
                2 -> activity.material_type?.name ?: ""
                1 -> {
                    val name = activity.reward?.name ?: "Recompensa"
                    "1 x $name"
                }
                else -> activity.alliance?.name ?: ""
            }
            if (subtitleText.isNotBlank()) {
                Text(
                    text = subtitleText,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = PoppinsFontFamily
                    ),
                    color = colors.textSecondary,
                    maxLines = 2,
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
            val pointsColor = if (activity.points < 0) colors.negativePoints else colors.primaryColor
            val pointsText = if (activity.type_history == 1) "${activity.points} pts" else "+${activity.points} pts"
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