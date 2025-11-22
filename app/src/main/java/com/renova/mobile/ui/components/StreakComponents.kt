package com.renova.mobile.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.renova.mobile.R
import com.renova.mobile.ui.screens.MonthlyBadge
import com.renova.mobile.ui.theme.PoppinsFontFamily
import com.renova.mobile.ui.theme.RenovaColorScheme
import com.renova.mobile.ui.theme.RenovaColors


// WeeklyProgressChart
@Composable
fun WeeklyProgressChart(
    weekData: List<Int>,
    renovaColors: RenovaColorScheme
) {
    val days = listOf("L", "M", "M", "J", "V", "S", "D")
    val maxValue = weekData.maxOrNull() ?: 1

    Column(
    ) {
        Text(
            text = stringResource(R.string.this_week),
            style = MaterialTheme.typography.titleLarge,
            color = renovaColors.textPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 21.sp,
            fontFamily = PoppinsFontFamily
        )

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = stringResource(R.string.recycling_activities_per_day),
            style = MaterialTheme.typography.bodyMedium,
            color = renovaColors.textSecondary,
            fontSize = 14.sp,
            fontFamily = PoppinsFontFamily
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = renovaColors.cardBackground
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                weekData.forEachIndexed { index, value ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "$value",
                            style = MaterialTheme.typography.bodySmall,
                            color = renovaColors.textSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = PoppinsFontFamily
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .height(((value.toFloat() / maxValue) * 80).dp.coerceAtLeast(8.dp))
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            RenovaColors.PrimaryColor,
                                            RenovaColors.PrimaryColor.copy(alpha = 0.7f)
                                        )
                                    ),
                                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                                )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = days[index],
                            style = MaterialTheme.typography.bodySmall,
                            color = renovaColors.textSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = PoppinsFontFamily
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MonthlyBadgesSectionV2(
    badges: List<MonthlyBadge>,
    currentMonthPoints: Int,
    renovaColors: RenovaColorScheme,
    onBadgeClick: (MonthlyBadge) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 18.dp)
    ) {
        Column(
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 0.dp, bottom = 4.dp)
        ) {
            Text(
                text = stringResource(R.string.monthly_badges),
                style = MaterialTheme.typography.titleLarge,
                color = renovaColors.textPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 21.sp,
                fontFamily = PoppinsFontFamily
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = stringResource(R.string.monthly_badges_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = renovaColors.textSecondary,
                fontSize = 13.sp,
                fontFamily = PoppinsFontFamily
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = RenovaColors.PrimaryColor.copy(alpha = 0.1f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.this_month_points),
                        style = MaterialTheme.typography.bodySmall,
                        color = renovaColors.textSecondary,
                        fontSize = 12.sp,
                        fontFamily = PoppinsFontFamily
                    )
                    Text(
                        text = "$currentMonthPoints",
                        style = MaterialTheme.typography.titleLarge,
                        color = RenovaColors.PrimaryColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        fontFamily = PoppinsFontFamily
                    )
                }
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = RenovaColors.PrimaryColor.copy(alpha = 0.5f),
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            itemsIndexed(badges) { index, badge ->
                MonthlyBadgeCardV2(
                    badge = badge,
                    onClick = { onBadgeClick(badge) },
                    modifier = Modifier.width(130.dp),
                    index = index
                )
            }
        }
    }
}

@Composable
fun MonthlyBadgeCardV2(
    badge: MonthlyBadge,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    index: Int = 0
) {
    val progress = (badge.currentMonthProgress.toFloat() / badge.pointsRequired.toFloat()).coerceIn(0f, 1f)

    // Color según estado
    val backgroundColor = when {
        badge.isClaimed -> RenovaColors.PrimaryColor.copy(alpha = 0.7f)
        badge.isUnlocked -> RenovaColors.SecondaryColor
        else -> RenovaColors.SecondaryColor.copy(alpha = 0.5f)
    }

    val textColor = when {
        badge.isClaimed -> Color.White
        badge.isUnlocked -> Color.White
        else -> Color(0xFF9E9E9E)
    }

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
        modifier = modifier
            .height(145.dp)
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
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        LaunchedEffect(isPressed) {
            if (isPressed) {
                kotlinx.coroutines.delay(150)
                isPressed = false
            }
        }

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .background(
                            color = if (badge.isUnlocked || badge.isClaimed) {
                                Color.White.copy(alpha = 0.45f)
                            } else {
                                Color.White.copy(alpha = 0.35f)
                            },
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = badge.iconRes),
                        contentDescription = badge.name,
                        modifier = Modifier.size(28.dp),
                        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color.White)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = badge.name,
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        fontFamily = PoppinsFontFamily,
                        lineHeight = 15.sp,
                        modifier = Modifier.heightIn(min = 30.dp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    when {
                        badge.isClaimed -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = stringResource(R.string.claimed),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp,
                                    fontFamily = PoppinsFontFamily
                                )
                            }
                        }
                        badge.isUnlocked -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Stars,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "+${badge.bonusPoints} pts",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp,
                                    fontFamily = PoppinsFontFamily
                                )
                            }
                        }
                        else -> {
                            Text(
                                text = "${badge.pointsRequired} pts",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontFamily = PoppinsFontFamily
                            )
                        }
                    }
                }
            }

            // Barra de progreso
            if (!badge.isUnlocked && !badge.isClaimed && progress > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .align(Alignment.BottomCenter)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .fillMaxHeight()
                            .background(RenovaColors.PrimaryColor)
                    )
                }
            }
        }
    }
}