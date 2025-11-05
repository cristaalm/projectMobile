package com.renova.mobile.ui.screens.business.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.shadow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.zIndex
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.renova.mobile.R
import com.renova.mobile.ui.components.BusinessSectionHeader
import com.renova.mobile.ui.theme.LocalRenovaColors
import com.renova.mobile.ui.theme.PoppinsFontFamily
import com.renova.mobile.ui.theme.RenovaColors
import com.renova.mobile.viewmodel.BusinessStatisticsViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun BusinessStatisticsScreen(
    allianceId: Int,
    onLogout: () -> Unit,
    onNavigateBack: () -> Unit = {},
    viewModel: BusinessStatisticsViewModel = viewModel()
) {
    val colors = LocalRenovaColors.current
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Cargar datos al iniciar
    LaunchedEffect(allianceId) {
        viewModel.loadStatistics(allianceId)
    }

    // Mostrar errores
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            // Header
            Box {
                BusinessSectionHeader(
                    title = "     ${stringResource(R.string.statistics)}",
                    onLogout = onLogout,
                    textColor = Color.White
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

            // Loading indicator
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = RenovaColors.Primary)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    // Tarjetas de métricas principales
                    uiState.stats?.let { stats ->
                        item {
                            StatCard(
                                title = stringResource(R.string.statistics_total_revenue),
                                value = formatCurrency(stats.totalIncome),
                                icon = Icons.Filled.AttachMoney,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        item {
                            StatCard(
                                title = stringResource(R.string.statistics_total_points),
                                value = "${formatNumber(stats.totalPoints)} pts",
                                icon = Icons.Filled.Star,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        item {
                            StatCard(
                                title = stringResource(R.string.statistics_average_ticket),
                                value = formatCurrency(stats.averageTicket),
                                icon = Icons.Filled.Leaderboard,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        item {
                            StatCard(
                                title = stringResource(R.string.statistics_customers_served),
                                value = formatNumber(stats.totalCustomersServed),
                                icon = Icons.Filled.Groups,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // Sección "Últimos 7 días"
                    uiState.activityData?.let { activityData ->
                        item {
                            SectionTitle(text = stringResource(R.string.statistics_last_7_days))
                        }

                        item {
                            WeeklyBarChart(
                                data = activityData.statsToWeek.map { it.totalActivity },
                                labels = activityData.statsToWeek.map {
                                    parseDayLabel(it.day)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(
                                        elevation = 3.dp,
                                        shape = RoundedCornerShape(12.dp),
                                        spotColor = RenovaColors.Light.ActivityShadowColor
                                    )
                            )
                        }

                        item {
                            Last7DaysSummaryCard(
                                totalSales = activityData.totalSales,
                                totalPoints = activityData.totalPoints,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(
                                        elevation = 3.dp,
                                        shape = RoundedCornerShape(12.dp),
                                        spotColor = RenovaColors.Light.ActivityShadowColor
                                    )
                            )
                        }
                    }

                    // Productos más canjeados
                    if (uiState.topRewards.isNotEmpty()) {
                        item {
                            SectionTitle(text = stringResource(R.string.statistics_top_products))
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(
                                        elevation = 3.dp,
                                        shape = RoundedCornerShape(12.dp),
                                        spotColor = RenovaColors.Light.ActivityShadowColor
                                    ),
                                colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                                shape = RoundedCornerShape(12.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    uiState.topRewards.take(3).forEach { reward ->
                                        TopProductRow(
                                            name = reward.rewardName,
                                            count = reward.totalClaimed
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Snackbar para errores
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    val colors = LocalRenovaColors.current
    Card(
        modifier = modifier.shadow(
            elevation = 3.dp,
            shape = RoundedCornerShape(16.dp),
            spotColor = RenovaColors.Light.ActivityShadowColor
        ),
        colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFF05D16E).copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color(0xFF05D16E))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = PoppinsFontFamily,
                        color = RenovaColors.Primary
                    )
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = colors.textPrimary
                    ),
                    maxLines = Int.MAX_VALUE
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    val colors = LocalRenovaColors.current
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge.copy(
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.Bold
        ),
        color = RenovaColors.Primary,
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )
}

@Composable
private fun TopProductRow(name: String, count: Int) {
    val colors = LocalRenovaColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = PoppinsFontFamily
            ),
            color = colors.textPrimary,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = formatNumber(count),
            style = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.End
            ),
            color = colors.textSecondary
        )
    }
}

@Composable
private fun WeeklyBarChart(
    data: List<Int>,
    labels: List<String>,
    modifier: Modifier = Modifier
) {
    val colors = LocalRenovaColors.current
    val maxValue = (data.maxOrNull() ?: 1).toFloat()

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            val barMaxHeight = 120.dp
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                data.forEachIndexed { index, value ->
                    val ratio = if (maxValue == 0f) 0f else (value / maxValue)
                    val barHeight = barMaxHeight * ratio
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 22.dp, height = barMaxHeight)
                                .background(RenovaColors.Primary.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .size(width = 22.dp, height = barHeight)
                                    .background(RenovaColors.Primary, RoundedCornerShape(6.dp))
                            )
                        }
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = labels.getOrNull(index) ?: "",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = PoppinsFontFamily
                            ),
                            color = colors.textSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Last7DaysSummaryCard(
    totalSales: Int,
    totalPoints: Int,
    modifier: Modifier = Modifier
) {
    val colors = LocalRenovaColors.current
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(RenovaColors.Primary.copy(alpha = 0.12f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.History,
                    contentDescription = null,
                    tint = RenovaColors.Primary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "${stringResource(R.string.statistics_sales_last_week)}: ${formatNumber(totalSales)}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = colors.textPrimary
                )
                Text(
                    text = "${stringResource(R.string.statistics_points_last_week)}: ${formatNumber(totalPoints)} pts",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = colors.textPrimary
                )
            }
        }
    }
}

// Helper functions
private fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
    return formatter.format(amount)
}

private fun formatNumber(number: Int): String {
    return NumberFormat.getNumberInstance(Locale("es", "MX")).format(number)
}

private fun parseDayLabel(day: String): String {
    return when (day.uppercase()) {
        "MONDAY" -> "L"
        "TUESDAY" -> "M"
        "WEDNESDAY" -> "X"
        "THURSDAY" -> "J"
        "FRIDAY" -> "V"
        "SATURDAY" -> "S"
        "SUNDAY" -> "D"
        else -> day.first().toString()
    }
}