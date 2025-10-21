package com.renova.mobile.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.*
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
import com.renova.mobile.R
import com.renova.mobile.network.ActivityItem
import com.renova.mobile.ui.theme.LocalRenovaColors
import com.renova.mobile.ui.theme.PoppinsFontFamily
import com.renova.mobile.ui.theme.RenovaColorScheme
import com.renova.mobile.ui.theme.RenovaColors
import com.renova.mobile.ui.viewmodels.ActivityViewModel
import com.renova.mobile.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityScreen(
    viewModel: ActivityViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val renovaColors = LocalRenovaColors.current
    val state by viewModel.state.collectAsState()
    val pullToRefreshState = rememberPullToRefreshState()
    val sheetState = rememberModalBottomSheetState()
    var selectedActivity by remember { mutableStateOf<ActivityItem?>(null) }
    var showErrorModal by remember { mutableStateOf(false) }

    var isFirstLoad by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        viewModel.loadHistory(1)
    }

    LaunchedEffect(state.activities.isNotEmpty()) {
        if (state.activities.isNotEmpty() && isFirstLoad){
            isFirstLoad = false
        }
    }

    LaunchedEffect(state.error) {
        if (state.error != null) {
            showErrorModal = true
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        when {
            state.isLoading && state.activities.isEmpty() -> {
                LoadingState(renovaColors)
            }

            state.error != null && state.activities.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize())
            }

            else -> {
                PullToRefreshBox(
                    isRefreshing = state.isLoading && state.activities.isNotEmpty(),
                    onRefresh = {
                        viewModel.loadHistory(state.currentPage)
                    },
                    state = pullToRefreshState,
                    indicator = {
                        CustomRefreshIndicator(
                            state = pullToRefreshState,
                            isRefreshing = state.isLoading && state.activities.isNotEmpty(),
                            renovaColors = renovaColors,
                            modifier = Modifier.align(Alignment.TopCenter)
                        )
                    }
                ) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = !state.isLoading || state.activities.isNotEmpty(),
                        enter = androidx.compose.animation.fadeIn(
                            animationSpec = androidx.compose.animation.core.tween(durationMillis = 500)
                        ),
                        exit = androidx.compose.animation.fadeOut(
                            animationSpec = androidx.compose.animation.core.tween(durationMillis = 300)
                        )
                    ) {
                        ActivityContent(
                            state = state,
                            renovaColors = renovaColors,
                            onPreviousPage = { viewModel.previousPage() },
                            onNextPage = { viewModel.nextPage() },
                            shouldAnimatePoints = isFirstLoad,
                            onActivityClick = { activity ->
                                selectedActivity = activity
                            }
                        )
                    }
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

    // Modal de error con opción de reintentar
    RetryableErrorModal(
        isVisible = showErrorModal,
        errorMessage = state.error ?: stringResource(R.string.unknown_error),
        onDismiss = {
            showErrorModal = false
            viewModel.clearError()
        },
        onRetry = {
            showErrorModal = false
            viewModel.clearError()
            viewModel.retry()
        }
    )
}

@Composable
private fun ActivityContent(
    state: com.renova.mobile.ui.viewmodels.ActivityState,
    renovaColors: RenovaColorScheme,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    shouldAnimatePoints: Boolean = false,
    onActivityClick: (ActivityItem) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            androidx.compose.animation.AnimatedVisibility(
                visible = true,
                enter = androidx.compose.animation.fadeIn(
                    animationSpec = androidx.compose.animation.core.tween(durationMillis = 600)
                ) + androidx.compose.animation.slideInVertically(
                    initialOffsetY = { -40 },
                    animationSpec = androidx.compose.animation.core.tween(durationMillis = 600)
                )
            ) {
                AnimatedPointsCardActivity(
                    totalPoints = state.totalPoints,
                    renovaColors = renovaColors,
                    shouldAnimate = shouldAnimatePoints
                )
            }
        }

        item {
            androidx.compose.animation.AnimatedVisibility(
                visible = true,
                enter = androidx.compose.animation.fadeIn(
                    animationSpec = androidx.compose.animation.core.tween(durationMillis = 600, delayMillis = 100)
                ) + androidx.compose.animation.slideInVertically(
                    initialOffsetY = { -30 },
                    animationSpec = androidx.compose.animation.core.tween(durationMillis = 600, delayMillis = 100)
                )
            ) {
                Column(
                    modifier = Modifier.padding(top = 0.dp, start = 20.dp, end = 20.dp, bottom = 8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.recycling_materials),
                        style = MaterialTheme.typography.titleLarge,
                        color = renovaColors.textPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 21.sp,
                        fontFamily = PoppinsFontFamily
                    )
                    Text(
                        text = stringResource(R.string.earn_points_recycling),
                        style = MaterialTheme.typography.bodyMedium,
                        color = renovaColors.textSecondary,
                        fontSize = 14.sp,
                        fontFamily = PoppinsFontFamily
                    )
                }
            }
        }

        item {
            androidx.compose.animation.AnimatedVisibility(
                visible = true,
                enter = androidx.compose.animation.fadeIn(
                    animationSpec = androidx.compose.animation.core.tween(durationMillis = 600, delayMillis = 200)
                ) + androidx.compose.animation.slideInVertically(
                    initialOffsetY = { -20 },
                    animationSpec = androidx.compose.animation.core.tween(durationMillis = 600, delayMillis = 200)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MaterialStatCard(
                        title = stringResource(R.string.plastic),
                        count = state.totalPlastic,
                        icon = R.drawable.bottle,
                        backgroundRes = R.drawable.fondo_chico,
                        modifier = Modifier.weight(1f)
                    )
                    MaterialStatCard(
                        title = stringResource(R.string.aluminum),
                        count = state.totalAluminum,
                        icon = R.drawable.can,
                        backgroundRes = R.drawable.fondo_botella,
                        modifier = Modifier.weight(1f)
                    )
                    MaterialStatCard(
                        title = stringResource(R.string.total_materials),
                        count = state.totalPlastic + state.totalAluminum,
                        icon = R.drawable.bottle,
                        backgroundRes = R.drawable.fondo_comercio,
                        showIcon = false,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        item {
            androidx.compose.animation.AnimatedVisibility(
                visible = true,
                enter = androidx.compose.animation.fadeIn(
                    animationSpec = androidx.compose.animation.core.tween(durationMillis = 600, delayMillis = 300)
                ) + androidx.compose.animation.slideInVertically(
                    initialOffsetY = { -20 },
                    animationSpec = androidx.compose.animation.core.tween(durationMillis = 600, delayMillis = 300)
                )
            ) {
                Column(
                    modifier = Modifier.padding(start = 20.dp, top = 12.dp, bottom = 8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.history),
                        style = MaterialTheme.typography.titleLarge,
                        color = renovaColors.textPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 21.sp,
                        fontFamily = PoppinsFontFamily
                    )
                    Text(
                        text = stringResource(R.string.activity_record),
                        style = MaterialTheme.typography.bodyMedium,
                        color = renovaColors.textSecondary,
                        fontSize = 14.sp,
                        fontFamily = PoppinsFontFamily
                    )
                }
            }
        }

        // Aquí va el contenido de registros o el estado vacío
        if (state.activities.isEmpty()) {
            item {
                EmptyStateInline(renovaColors = renovaColors)
            }
        } else {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    state.activities.forEachIndexed { index, activity ->
                        ActivityHistoryCard(
                            activity = activity,
                            colors = renovaColors,
                            onClick = {
                                onActivityClick(activity)
                            }
                        )
                        if (index < state.activities.size - 1) {
                            Divider(
                                color = RenovaColors.PrimaryColor,
                                thickness = 1.dp,
                                modifier = Modifier.padding(vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            item {
                androidx.compose.animation.AnimatedVisibility(
                    visible = true,
                    enter = androidx.compose.animation.fadeIn(
                        animationSpec = androidx.compose.animation.core.tween(
                            durationMillis = 600,
                            delayMillis = 500 + (state.activities.size * 50)
                        )
                    )
                ) {
                    PaginationControls(
                        currentPage = state.currentPage,
                        totalPages = state.totalPages,
                        isLoading = state.isLoading,
                        renovaColors = renovaColors,
                        onPreviousPage = onPreviousPage,
                        onNextPage = onNextPage
                    )
                }
            }
        }
    }
}

@Composable
fun ActivityHistoryCard(
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
                    // Agregar "- Aplastada" si es plástico y está aplastada
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
private fun MaterialStatCard(
    title: String,
    count: Int,
    icon: Int,
    backgroundRes: Int,
    showIcon: Boolean = true,
    modifier: Modifier = Modifier
) {
    val renovaColors = LocalRenovaColors.current

    Card(
        modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = backgroundRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(18.dp))
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 16.dp, horizontal = 13.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (showIcon) {
                        Image(
                            painter = painterResource(id = icon),
                            contentDescription = title,
                            modifier = Modifier.size(34.dp),
                            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color.White)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                    }
                    Text(
                        text = count.toString(),
                        style = MaterialTheme.typography.displayMedium,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(0.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun EmptyStateInline(
    renovaColors: RenovaColorScheme,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icono decorativo
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
                imageVector = Icons.Default.History,
                contentDescription = null,
                tint = renovaColors.activityPrimary.copy(alpha = 0.5f),
                modifier = Modifier.size(50.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Título
        Text(
            text = stringResource(R.string.no_activity_yet),
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = renovaColors.textPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Descripción
        Text(
            text = stringResource(R.string.no_activity_description),
            fontFamily = PoppinsFontFamily,
            fontSize = 14.sp,
            color = renovaColors.textSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
    }
}