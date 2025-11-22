package com.renova.mobile.ui.screens

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.renova.mobile.R
import com.renova.mobile.network.ActivityItem
import com.renova.mobile.ui.theme.LocalRenovaColors
import com.renova.mobile.ui.theme.PoppinsFontFamily
import com.renova.mobile.ui.theme.RenovaColors
import com.renova.mobile.ui.viewmodels.ActivityViewModel
import com.renova.mobile.ui.components.*
import androidx.compose.ui.layout.onGloballyPositioned
import com.renova.mobile.ui.tour.LocalTourState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import com.renova.mobile.ui.theme.RenovaColorScheme
import kotlinx.coroutines.delay

@Composable
private fun translateError(errorCode: String): String {
    return when (errorCode) {
        "ERROR_NO_INTERNET" -> stringResource(R.string.no_internet_retry_message)
        "ERROR_SESSION_EXPIRED" -> stringResource(R.string.session_expired)
        "ERROR_UNKNOWN" -> stringResource(R.string.unknown_error)
        else -> errorCode
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityScreen(
    viewModel: ActivityViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val renovaColors = LocalRenovaColors.current
    val state by viewModel.state.collectAsState()
    val pullToRefreshState = rememberPullToRefreshState()
    var selectedActivity by remember { mutableStateOf<ActivityItem?>(null) }
    var isFirstLoad by remember { mutableStateOf(true) }

    // 🛡️ Cargar datos de forma segura
    LaunchedEffect(Unit) {
        try {
            viewModel.loadHistory(1, isManualRefresh = false)
        } catch (e: Exception) {
            // El ViewModel ya maneja el error
        }
    }

    LaunchedEffect(state.activities.isNotEmpty()) {
        if (state.activities.isNotEmpty() && isFirstLoad) {
            isFirstLoad = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            // Caso 1: Primera carga y aún no hay error
            state.isLoading && state.activities.isEmpty() && !state.hasLoadedOnce && state.error == null -> {
                LoadingState(renovaColors)
            }

            // Caso 2: Error crítico (primera carga falló) - PANTALLA COMPLETA
            state.error != null && state.activities.isEmpty() -> {
                ErrorStateFullScreen(
                    errorMessage = translateError(state.error ?: "ERROR_UNKNOWN"),
                    renovaColors = renovaColors,
                    onRetry = {
                        viewModel.clearError()
                        viewModel.retry()
                    }
                )
            }

            // Caso 3: Mostrar contenido con pull-to-refresh
            state.hasLoadedOnce || state.activities.isNotEmpty() -> {
                PullToRefreshBox(
                    isRefreshing = state.isLoading && state.activities.isNotEmpty(),
                    onRefresh = {
                        viewModel.loadHistory(state.currentPage, isManualRefresh = true)
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

                // Snackbar para errores cuando ya hay datos
                if (state.error != null &&
                    state.isManualRefresh &&
                    state.hasLoadedOnce &&
                    state.activities.isNotEmpty()) {

                    val translatedError = translateError(state.error ?: "ERROR_UNKNOWN")

                    LaunchedEffect(state.error) {
                        delay(3000)
                        viewModel.clearError()
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
                                    text = translatedError,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = renovaColors.textPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                TextButton(
                                    onClick = {
                                        viewModel.clearError()
                                        viewModel.retry()
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

            // Caso 4: Estado por defecto (fallback)
            else -> {
                LoadingState(renovaColors)
            }
        }
    }

    // Modal de detalle de actividad
    selectedActivity?.let { activity ->
        ModalBottomSheet(
            onDismissRequest = { selectedActivity = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = renovaColors.cardBackground,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            scrimColor = Color.Black.copy(alpha = 0.32f)
        ) {
            DetailSheet(activity = activity)
        }
    }
}

@Composable
private fun ActivityContent(
    state: com.renova.mobile.ui.viewmodels.ActivityState,
    renovaColors: com.renova.mobile.ui.theme.RenovaColorScheme,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    shouldAnimatePoints: Boolean = false,
    onActivityClick: (ActivityItem) -> Unit
) {
    val tourState = LocalTourState.current
    val lazyListState = rememberLazyListState()

    val isTourActive by tourState.isTourActive.collectAsState()
    val targets by tourState.targets.collectAsState()

    val currentStepTargetId by remember(tourState) {
        derivedStateOf { tourState.currentStep?.targetId }
    }

    // 🆕 Scroll automático durante el tour
    LaunchedEffect(isTourActive, currentStepTargetId) {
        if (isTourActive && currentStepTargetId != null) {
            val indexToScroll = when (currentStepTargetId) {
                "activity_points_card" -> 0
                "activity_materials_row" -> 1
                "activity_history_title" -> 2
                else -> null
            }

            if (indexToScroll != null) {
                var attempts = 0
                while (attempts < 3) {
                    try {
                        delay(100L * (attempts + 1))
                        lazyListState.scrollToItem(indexToScroll)
                        break
                    } catch (e: Exception) {
                        attempts++
                        if (attempts == 3) {
                            try {
                                lazyListState.animateScrollToItem(indexToScroll)
                            } catch (e: Exception) {
                                // Último intento fallido
                            }
                        }
                    }
                }
            }
        }
    }

    Box {
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent),
            contentPadding = PaddingValues(
                top = 14.dp,
                start = 20.dp,
                end = 20.dp,
                bottom = 80.dp
            )
        ) {
            // ITEM 1: Tarjeta de puntos
            item {
                Box(modifier = Modifier.onGloballyPositioned { coords ->
                    tourState.registerTarget(
                        id = "activity_points_card",
                        coordinates = coords,
                        lazyListState = lazyListState,
                        itemIndex = 0
                    )
                }) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = true,
                        enter = if (shouldAnimatePoints && !isTourActive) {
                            androidx.compose.animation.fadeIn(
                                animationSpec = androidx.compose.animation.core.tween(durationMillis = 600)
                            ) + androidx.compose.animation.slideInVertically(
                                initialOffsetY = { -40 },
                                animationSpec = androidx.compose.animation.core.tween(durationMillis = 600)
                            )
                        } else {
                            EnterTransition.None
                        }
                    ) {
                        AnimatedPointsCard(
                            totalPoints = state.totalPoints,
                            renovaColors = renovaColors
                        )
                    }
                }
                DisposableEffect(Unit) {
                    onDispose { tourState.unregisterTarget("activity_points_card") }
                }
            }

            // ITEM 2: Sección de materiales de reciclaje
            item {
                Column(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .onGloballyPositioned { coords ->
                            tourState.registerTarget(
                                id = "activity_materials_row",
                                coordinates = coords,
                                lazyListState = lazyListState,
                                itemIndex = 1
                            )
                        }
                ) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = true,
                        enter = if (shouldAnimatePoints && !isTourActive) {
                            androidx.compose.animation.fadeIn(
                                animationSpec = androidx.compose.animation.core.tween(durationMillis = 600, delayMillis = 100)
                            ) + androidx.compose.animation.slideInVertically(
                                initialOffsetY = { -30 },
                                animationSpec = androidx.compose.animation.core.tween(durationMillis = 600, delayMillis = 100)
                            )
                        } else {
                            EnterTransition.None
                        }
                    ) {
                        Column {
                            // Título y subtítulo
                            Text(
                                text = stringResource(R.string.recycling_materials),
                                style = MaterialTheme.typography.titleLarge,
                                color = renovaColors.textPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 21.sp,
                                fontFamily = PoppinsFontFamily
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = stringResource(R.string.earn_points_recycling),
                                style = MaterialTheme.typography.bodyMedium,
                                color = renovaColors.textSecondary,
                                fontSize = 14.sp,
                                fontFamily = PoppinsFontFamily
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Tarjetas de estadísticas
                            Row(
                                modifier = Modifier.fillMaxWidth(),
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
                                    title = "Total",
                                    count = state.totalPlastic + state.totalAluminum,
                                    icon = R.drawable.bottle,
                                    backgroundRes = R.drawable.fondo_comercio,
                                    showIcon = false,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
                DisposableEffect(Unit) {
                    onDispose { tourState.unregisterTarget("activity_materials_row") }
                }
            }

            // ITEM 3: Sección de historial
            item {
                Column(
                    modifier = Modifier
                        .padding(top = 24.dp)
                        .onGloballyPositioned { coords ->
                            tourState.registerTarget(
                                id = "activity_history_title",
                                coordinates = coords,
                                lazyListState = lazyListState,
                                itemIndex = 2
                            )
                        }
                ) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = true,
                        enter = if (shouldAnimatePoints && !isTourActive) {
                            androidx.compose.animation.fadeIn(
                                animationSpec = androidx.compose.animation.core.tween(durationMillis = 600, delayMillis = 300)
                            ) + androidx.compose.animation.slideInVertically(
                                initialOffsetY = { -20 },
                                animationSpec = androidx.compose.animation.core.tween(durationMillis = 600, delayMillis = 300)
                            )
                        } else {
                            EnterTransition.None
                        }
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.history),
                                style = MaterialTheme.typography.titleLarge,
                                color = renovaColors.textPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 21.sp,
                                fontFamily = PoppinsFontFamily
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = stringResource(R.string.activity_record),
                                style = MaterialTheme.typography.bodyMedium,
                                color = renovaColors.textSecondary,
                                fontSize = 14.sp,
                                fontFamily = PoppinsFontFamily
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Lista de actividades o estado vacío
                    Column(modifier = Modifier.fillMaxWidth()) {
                        if (state.activities.isEmpty()) {
                            EmptyStateInline(renovaColors = renovaColors)
                        } else {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
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
                    }
                }

                DisposableEffect(Unit) {
                    onDispose {
                        tourState.unregisterTarget("activity_history_title")
                    }
                }
            }

            // ITEM 4: Controles de paginación
            if (state.activities.isNotEmpty()) {
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

        // 🛡️ Targets invisibles para el tour (aseguran registro)
        if (isTourActive) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .onGloballyPositioned { coords ->
                            if (!targets.containsKey("activity_points_card")) {
                                tourState.registerTarget(
                                    id = "activity_points_card",
                                    coordinates = coords,
                                    lazyListState = lazyListState,
                                    itemIndex = 0
                                )
                            }
                        }
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .onGloballyPositioned { coords ->
                            if (!targets.containsKey("activity_materials_row")) {
                                tourState.registerTarget(
                                    id = "activity_materials_row",
                                    coordinates = coords,
                                    lazyListState = lazyListState,
                                    itemIndex = 1
                                )
                            }
                        }
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .onGloballyPositioned { coords ->
                            if (!targets.containsKey("activity_history_title")) {
                                tourState.registerTarget(
                                    id = "activity_history_title",
                                    coordinates = coords,
                                    lazyListState = lazyListState,
                                    itemIndex = 2
                                )
                            }
                        }
                )
            }
        }
    }
}

@Composable
private fun ErrorStateFullScreen(
    errorMessage: String,
    renovaColors: com.renova.mobile.ui.theme.RenovaColorScheme,
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
                    MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(50.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.WifiOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
                modifier = Modifier.size(50.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.error_loading_activity),
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.error,
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

@Composable
fun ActivityHistoryCard(
    activity: ActivityItem,
    colors: com.renova.mobile.ui.theme.RenovaColorScheme,
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

        Column(horizontalAlignment = Alignment.End) {
            val displayPoints = activity.points

            val pointsColor = when (activity.type_history) {
                1 -> colors.negativePoints
                2 -> if (activity.points == 0) colors.textPrimary else colors.primaryColor
                3 -> if (activity.points < 0) colors.negativePoints else colors.primaryColor
                else -> if (activity.points == 0) colors.textPrimary else colors.primaryColor
            }
            Text(
                text = "$displayPoints",
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
    val fontSize = when {
        count >= 1000 -> 34.sp
        count >= 100 -> 38.sp
        count >= 10 -> 44.sp
        else -> 44.sp
    }

    val isTotal = title.lowercase() == "total"
    val textColor = if (isTotal) RenovaColors.Secondary else Color.White

    Card(
        modifier = modifier.height(120.dp),
        shape = RoundedCornerShape(20.dp),
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
                    .clip(RoundedCornerShape(20.dp))
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = count.toString(),
                    fontSize = fontSize,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = PoppinsFontFamily,
                    color = textColor,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(0.dp))

                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = PoppinsFontFamily,
                    color = textColor,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun EmptyStateInline(
    renovaColors: com.renova.mobile.ui.theme.RenovaColorScheme,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 32.dp, top = 48.dp, end = 32.dp, bottom = 80.dp),
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
                imageVector = Icons.Default.History,
                contentDescription = null,
                tint = renovaColors.activityPrimary.copy(alpha = 0.5f),
                modifier = Modifier.size(50.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = stringResource(R.string.no_activity_yet),
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = renovaColors.textPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

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