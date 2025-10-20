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
import com.renova.mobile.ui.theme.LocalRenovaColors
import com.renova.mobile.ui.theme.PoppinsFontFamily
import com.renova.mobile.ui.theme.RenovaColorScheme
import com.renova.mobile.ui.theme.RenovaColors
import com.renova.mobile.ui.viewmodels.ActivityViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

data class Achievement(
    val id: Int,
    val title: String,
    val description: String,
    val iconRes: Int,
    val requiredPoints: Int,
    val color: Color,
    val backgroundColor: Color
)

@Composable
fun HomeScreen(
    viewModel: ActivityViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val renovaColors = LocalRenovaColors.current
    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        viewModel.loadHistory(1)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
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
            // Card de puntos totales con contador animado
            AnimatedPointsCard(
                totalPoints = state.totalPoints,
                renovaColors = renovaColors
            )

            // Título de actividad reciente
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

            // Lista de actividades (solo 3) - EXACTAMENTE como BusinessHomeScreen
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
                        Image(
                            painter = painterResource(id = R.drawable.leaf),
                            contentDescription = null,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape),
                            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(
                                renovaColors.textSecondary.copy(alpha = 0.3f)
                            )
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
                // Tabla de actividades EXACTA como BusinessHomeScreen
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
                            onClick = { /* Opcional: agregar acción de click */ }
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

            // Sección de logros desbloqueados
            AchievementsSection(
                totalPoints = state.totalPoints,
                renovaColors = renovaColors
            )
        }
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
        // Icono según el tipo de actividad
        val (icon, iconColor) = when (activity.type_history) {
            2 -> Icons.Default.Recycling to RenovaColors.Success // Reciclaje
            1 -> Icons.Default.ShoppingCart to RenovaColors.Primary // Compra/Canjeo
            else -> Icons.Default.History to RenovaColors.Warning // Actividad
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
            // Título de la actividad
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

            // Fecha (formato amigable: Hoy/Ayer/dd/MM/yyyy h:mm a)
            Text(
                text = formatFriendlyDate(activity.created_at),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = PoppinsFontFamily
                ),
                color = colors.textSecondary
            )
        }

        // Puntos
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
private fun AchievementsSection(
    totalPoints: Int,
    renovaColors: RenovaColorScheme
) {
    val context = LocalContext.current

    val achievements = remember {
        listOf(
            Achievement(
                id = 1,
                title = context.getString(R.string.achievement_eco_warrior),
                description = context.getString(R.string.achievement_eco_warrior_desc),
                iconRes = R.drawable.ic_goal_1,
                requiredPoints = 100,
                color = Color(0xFFFFFFFF),
                backgroundColor = Color(0xFF024653)
            ),
            Achievement(
                id = 2,
                title = context.getString(R.string.achievement_recycler_pro),
                description = context.getString(R.string.achievement_recycler_pro_desc),
                iconRes = R.drawable.ic_goal_2,
                requiredPoints = 500,
                color = Color(0xFF000000),
                backgroundColor = Color(0xFFCCFF00)
            ),
            Achievement(
                id = 3,
                title = context.getString(R.string.achievement_green_hero),
                description = context.getString(R.string.achievement_green_hero_desc),
                iconRes = R.drawable.ic_goal_3,
                requiredPoints = 1000,
                color = Color(0xFFFFFFFF),
                backgroundColor = Color(0xFF01C851)
            ),
            Achievement(
                id = 4,
                title = context.getString(R.string.achievement_planet_saver),
                description = context.getString(R.string.achievement_planet_saver_desc),
                iconRes = R.drawable.ic_goal_4,
                requiredPoints = 2500,
                color = Color(0xFFFFFFFF),
                backgroundColor = Color(0xFF024653)
            )
        )
    }

    var selectedAchievement by remember { mutableStateOf<Achievement?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 18.dp)
    ) {
        // Título de la sección
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            Text(
                text = stringResource(R.string.unlocked_achievements),
                style = MaterialTheme.typography.titleLarge,
                color = renovaColors.textPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 21.sp,
                fontFamily = PoppinsFontFamily
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = stringResource(R.string.your_achievements_and_ranks),
                style = MaterialTheme.typography.bodyMedium,
                color = renovaColors.textSecondary,
                fontSize = 14.sp,
                fontFamily = PoppinsFontFamily
            )
        }

        // Lista horizontal de logros con scroll
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            itemsIndexed(achievements) { index, achievement ->
                AchievementCard(
                    achievement = achievement,
                    isUnlocked = totalPoints >= achievement.requiredPoints,
                    onClick = { selectedAchievement = achievement },
                    index = index
                )
            }
        }
    }

    // Modal de información
    selectedAchievement?.let { achievement ->
        AchievementDialog(
            achievement = achievement,
            isUnlocked = totalPoints >= achievement.requiredPoints,
            currentPoints = totalPoints,
            onDismiss = { selectedAchievement = null }
        )
    }
}

@Composable
private fun AchievementCard(
    achievement: Achievement,
    isUnlocked: Boolean,
    onClick: () -> Unit,
    index: Int
) {
    // Animación de entrada
    var isVisible by remember { mutableStateOf(false) }
    val offsetX by animateFloatAsState(
        targetValue = if (isVisible) 0f else 100f,
        animationSpec = tween(
            durationMillis = 500,
            delayMillis = index * 100,
            easing = FastOutSlowInEasing
        ),
        label = "offsetX"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(
            durationMillis = 500,
            delayMillis = index * 100
        ),
        label = "alpha"
    )

    LaunchedEffect(Unit) {
        isVisible = true
    }

    // Animación de click
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scale"
    )

    Card(
        modifier = Modifier
            .width(120.dp)
            .height(115.dp)
            .offset(x = offsetX.dp)
            .alpha(alpha)
            .scale(scale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isPressed = true
                onClick()
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) achievement.backgroundColor else Color(0xFF2C2C2E)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isUnlocked) 4.dp else 2.dp
        )
    ) {
        LaunchedEffect(isPressed) {
            if (isPressed) {
                kotlinx.coroutines.delay(150)
                isPressed = false
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(9.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icono del logro
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        color = if (isUnlocked) {
                            Color.White.copy(alpha = 0.5f)
                        } else {
                            Color(0xFF3C3C3E)
                        },
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = achievement.iconRes),
                    contentDescription = achievement.title,
                    modifier = Modifier.size(24.dp),
                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(
                        if (isUnlocked) Color(0xFF2E7D32) else Color(0xFF6C6C70)
                    ),
                    alpha = if (isUnlocked) 1f else 0.4f
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Título del logro
            Text(
                text = achievement.title,
                style = MaterialTheme.typography.titleSmall,
                color = if (isUnlocked) achievement.color else Color(0xFFAEAEB2),
                fontWeight = FontWeight.Bold,
                fontSize = 9.5.sp,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontFamily = PoppinsFontFamily,
                lineHeight = 10.5.sp
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Puntos requeridos
            Text(
                text = "${achievement.requiredPoints} pts",
                style = MaterialTheme.typography.bodySmall,
                color = if (isUnlocked) achievement.color.copy(alpha = 0.7f) else Color(0xFF8E8E93),
                fontSize = 8.5.sp,
                fontFamily = PoppinsFontFamily
            )
        }
    }
}

@Composable
private fun AchievementDialog(
    achievement: Achievement,
    isUnlocked: Boolean,
    currentPoints: Int,
    onDismiss: () -> Unit
) {
    // Animación de entrada del diálogo
    var showDialog by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (showDialog) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "dialogScale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (showDialog) 1f else 0f,
        animationSpec = tween(300),
        label = "dialogAlpha"
    )

    LaunchedEffect(Unit) {
        showDialog = true
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    this.alpha = alpha
                },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isUnlocked) achievement.backgroundColor else Color(0xFF2C2C2E)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icono grande del logro
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(
                            color = if (isUnlocked) {
                                achievement.color.copy(alpha = 0.2f)
                            } else {
                                Color(0xFF3C3C3E)
                            },
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = achievement.iconRes),
                        contentDescription = achievement.title,
                        modifier = Modifier.size(55.dp),
                        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(
                            if (isUnlocked) achievement.color else Color(0xFF6C6C70)
                        ),
                        alpha = if (isUnlocked) 1f else 0.4f
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Título
                Text(
                    text = achievement.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = if (isUnlocked) achievement.color else Color(0xFF8E8E93),
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center,
                    fontFamily = PoppinsFontFamily
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Estado del logro
                Text(
                    text = if (isUnlocked) stringResource(R.string.unlocked) else stringResource(R.string.locked),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isUnlocked) achievement.color else Color(0xFF6C6C70),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    fontFamily = PoppinsFontFamily
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Descripción
                Text(
                    text = achievement.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isUnlocked) achievement.color.copy(alpha = 0.8f) else Color(0xFFAEAEB2),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    fontFamily = PoppinsFontFamily,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Progreso
                if (!isUnlocked) {
                    val progress = (currentPoints.toFloat() / achievement.requiredPoints.toFloat()).coerceIn(0f, 1f)

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.progress_format, currentPoints, achievement.requiredPoints),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFAEAEB2),
                            fontSize = 12.sp,
                            fontFamily = PoppinsFontFamily
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .background(Color(0xFF3C3C3E), RoundedCornerShape(4.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(progress)
                                    .height(8.dp)
                                    .background(achievement.color, RoundedCornerShape(4.dp))
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }

                // Botón de cerrar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isUnlocked) {
                                achievement.color.copy(alpha = 0.2f)
                            } else {
                                Color(0xFF48484A)
                            }
                        )
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.close),
                        color = if (isUnlocked) achievement.color else Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        fontFamily = PoppinsFontFamily
                    )
                }
            }
        }
    }
}

@Composable
private fun AnimatedPointsCard(
    totalPoints: Int,
    renovaColors: RenovaColorScheme
) {
    // Animación del contador de puntos
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