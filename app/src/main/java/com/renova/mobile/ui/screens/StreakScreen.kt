package com.renova.mobile.ui.screens

import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
// --- INICIO MODIFICACIÓN: Imports añadidos ---
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.ScrollState
// --- FIN MODIFICACIÓN ---
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.CardDefaults.cardElevation
import androidx.compose.runtime.*
import coil.decode.GifDecoder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.renova.mobile.R
import com.renova.mobile.ui.components.*
import com.renova.mobile.ui.theme.LocalRenovaColors
import com.renova.mobile.ui.theme.PoppinsFontFamily
import com.renova.mobile.ui.theme.RenovaColors
import com.renova.mobile.ui.theme.RenovaColorScheme

import androidx.compose.ui.layout.onGloballyPositioned
import com.renova.mobile.ui.tour.LocalTourState
import androidx.compose.runtime.DisposableEffect

data class WeeklyChallenge(
    // ... (Data class no cambia) ...
    val id: Int,
    val title: String,
    val titleEn: String,
    val description: String,
    val goal: String,
    val rewardText: String,
    val iconEmoji: ImageVector,
    val expiresAt: Long,
    val isAccepted: Boolean = false,
    val currentProgress: Int = 0,
    val targetProgress: Int = 100
)

data class MonthlyBadge(
    // ... (Data class no cambia) ...
    val id: Int,
    val title: String,
    val titleEn: String,
    val requiredPoints: Int,
    val bonusPoints: Int,
    val iconRes: Int,
    val color: Color,
    val backgroundColor: Color,
    val isUnlocked: Boolean = false,
    val currentMonthProgress: Int = 0
)
// StreakScreen.kt
@Composable
fun StreakScreen() {
    val renovaColors = LocalRenovaColors.current
    val scrollState = rememberScrollState() // <-- ScrollState definido aquí
    val tourState = LocalTourState.current

    // ... (Lógica de variables (currentStreak, weeklyChallenge, etc) no cambia) ...
    val currentStreak = 7
    val longestStreak = 15
    val totalRecyclingDays = 42
    val currentMonthPoints = 850

    val weeklyChallenge = WeeklyChallenge(
        id = 1,
        title = "Maratón de Plástico",
        titleEn = "Plastic Marathon",
        description = "Escanea y recicla 50 botellas de plástico (PET) en la semana",
        goal = "50 botellas de plástico",
        rewardText = "Entrada gratis al cine",
        iconEmoji = Icons.Default.Theaters,
        expiresAt = 3,
        isAccepted = false,
        currentProgress = 32,
        targetProgress = 50
    )

    var isChallengeAccepted by remember { mutableStateOf(weeklyChallenge.isAccepted) }
    var selectedBadge by remember { mutableStateOf<MonthlyBadge?>(null) }
    var showChallengeDialog by remember { mutableStateOf(false) }

    val monthlyBadges = listOf(
        MonthlyBadge(
            id = 1,
            title = "Guerrero Ecológico",
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
            title = "Reciclador Pro",
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
            title = "Héroe Verde",
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
            title = "Salvador del Planeta",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState) // <-- Aplicar scrollState
                .padding(bottom = 16.dp)
        ) {
            Box(modifier = Modifier.onGloballyPositioned { coords ->
                // --- MODIFICADO: Pasar scrollState ---
                tourState.registerTarget(
                    id = "streak_card_main",
                    coordinates = coords,
                    scrollState = scrollState
                )
            }) {
                StreakCard(
                    currentStreak = currentStreak,
                    longestStreak = longestStreak,
                    totalDays = totalRecyclingDays,
                    renovaColors = renovaColors
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.onGloballyPositioned { coords ->
                // --- MODIFICADO: Pasar scrollState ---
                tourState.registerTarget(
                    id = "streak_weekly_challenge",
                    coordinates = coords,
                    scrollState = scrollState
                )
            }) {
                WeeklyChallengeCard(
                    challenge = weeklyChallenge,
                    isAccepted = isChallengeAccepted,
                    onAccept = { isChallengeAccepted = true },
                    onClick = { showChallengeDialog = true },
                    renovaColors = renovaColors
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.onGloballyPositioned { coords ->
                // --- MODIFICADO: Pasar scrollState ---
                tourState.registerTarget(
                    id = "streak_monthly_badges",
                    coordinates = coords,
                    scrollState = scrollState
                )
            }) {
                MonthlyBadgesSection(
                    badges = monthlyBadges,
                    currentMonthPoints = currentMonthPoints,
                    renovaColors = renovaColors,
                    onBadgeClick = { selectedBadge = it }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.onGloballyPositioned { coords ->
                // --- MODIFICADO: Pasar scrollState ---
                tourState.registerTarget(
                    id = "streak_weekly_progress",
                    coordinates = coords,
                    scrollState = scrollState
                )
            }) {
                WeeklyProgressChart(
                    weekData = listOf(2, 3, 1, 4, 3, 2, 1),
                    renovaColors = renovaColors
                )
            }
        }
    }

    DisposableEffect("streak_card_main") {
        onDispose { tourState.unregisterTarget("streak_card_main") }
    }
    DisposableEffect("streak_weekly_challenge") {
        onDispose { tourState.unregisterTarget("streak_weekly_challenge") }
    }
    DisposableEffect("streak_monthly_badges") {
        onDispose { tourState.unregisterTarget("streak_monthly_badges") }
    }
    DisposableEffect("streak_weekly_progress") {
        onDispose { tourState.unregisterTarget("streak_weekly_progress") }
    }

    // ... (Diálogos (Badge, Challenge) no cambian) ...
    selectedBadge?.let { badge ->
        BadgeDialog(
            badge = badge,
            currentMonthPoints = currentMonthPoints,
            onDismiss = { selectedBadge = null }
        )
    }

    if (showChallengeDialog) {
        WeeklyChallengeDialog(
            challenge = weeklyChallenge,
            isAccepted = isChallengeAccepted,
            onAccept = {
                isChallengeAccepted = true
                showChallengeDialog = false
            },
            onDismiss = { showChallengeDialog = false },
            renovaColors = renovaColors
        )
    }
}

@Composable
private fun WeeklyChallengeCard(
    // ... (Este Composable no cambia) ...
    challenge: WeeklyChallenge,
    isAccepted: Boolean,
    onAccept: () -> Unit,
    onClick: () -> Unit,
    renovaColors: RenovaColorScheme
) {
    val progress = if (challenge.targetProgress > 0) {
        (challenge.currentProgress.toFloat() / challenge.targetProgress.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val daysRemaining = 4

    Column(
        modifier = Modifier.padding(horizontal = 20.dp)
    ) {
        Text(
            text = stringResource(R.string.weekly_challenge),
            style = MaterialTheme.typography.titleLarge,
            color = renovaColors.textPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 21.sp,
            fontFamily = PoppinsFontFamily
        )

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = stringResource(R.string.weekly_challenge_reset),
            style = MaterialTheme.typography.bodyMedium,
            color = renovaColors.textSecondary,
            fontSize = 13.sp,
            fontFamily = PoppinsFontFamily
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onClick() },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = RenovaColors.SecondaryColor
            ),
            elevation = cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = challenge.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp,
                            fontFamily = PoppinsFontFamily
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Flag,
                                contentDescription = null,
                                tint = RenovaColors.TertiaryColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = challenge.goal,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                fontFamily = PoppinsFontFamily
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(
                                Color.White.copy(alpha = 0.15f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Theaters,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }

                if (isAccepted) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyMedium,
                            color = RenovaColors.TertiaryColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            fontFamily = PoppinsFontFamily
                        )
                        Text(
                            text = "${challenge.currentProgress} / ${challenge.targetProgress}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            fontFamily = PoppinsFontFamily
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .background(
                                Color.White.copy(alpha = 0.2f),
                                RoundedCornerShape(5.dp)
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress)
                                .height(10.dp)
                                .background(
                                    RenovaColors.TertiaryColor,
                                    RoundedCornerShape(5.dp)
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StreakCard(
    // ... (Este Composable no cambia) ...
    currentStreak: Int,
    longestStreak: Int,
    totalDays: Int,
    renovaColors: RenovaColorScheme
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp, 0.dp, 10.dp, 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = cardElevation(defaultElevation = 0.dp)
    ){
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp, 18.dp, 8.dp, 0.dp)
        ){
            Image(
                painter = painterResource(id = R.drawable.fondo_perfil),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alignment = Alignment.Center,
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(24.dp))
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ){
                Column(
                    modifier = Modifier.weight(1f)
                ){
                    Text(
                        text = stringResource(R.string.current_streak),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        fontFamily = PoppinsFontFamily,
                        color = Color.White.copy(alpha = 1.5f)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.Bottom
                    ){
                        Text(
                            text = "$currentStreak",
                            style = MaterialTheme.typography.displayLarge,
                            color = Color.White,
                            fontSize = 56.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 56.sp,
                            fontFamily = PoppinsFontFamily
                        )
                        Text(
                            text = " ${stringResource(R.string.days)}",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 20.sp,
                            modifier = Modifier.padding(bottom = 8.dp),
                            fontFamily = PoppinsFontFamily
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ){
                        Column {
                            Text(
                                text = stringResource(R.string.best_streak),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = PoppinsFontFamily
                            )
                            Text(
                                text = "$longestStreak ${stringResource(R.string.days)}",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                fontFamily = PoppinsFontFamily
                            )
                        }

                        Column {
                            Text(
                                text = stringResource(R.string.total_days),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = PoppinsFontFamily
                            )
                            Text(
                                text = "$totalDays ${stringResource(R.string.days)}",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                fontFamily = PoppinsFontFamily
                            )
                        }
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
                    GifPlayer(modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}

@Composable
private fun GifPlayer(modifier: Modifier = Modifier) {
    // ... (Este Composable no cambia) ...
    val context = LocalContext.current
    val imageLoader = ImageLoader.Builder(context)
        .components {
            if (Build.VERSION.SDK_INT >= 28) {
                add(ImageDecoderDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
        }
        .build()
    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(R.raw.flame)
            .build(),
        imageLoader = imageLoader,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier
    )
}