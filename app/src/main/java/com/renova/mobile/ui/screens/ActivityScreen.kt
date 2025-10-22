package com.renova.mobile.ui.screens

import androidx.compose.animation.Crossfade // <-- de v1
import androidx.compose.animation.core.tween // <-- de v1
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable // <-- de v2
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh // <-- de v1
import androidx.compose.material.icons.filled.ShoppingCart // <-- de v2
import androidx.compose.material.icons.filled.Recycling // <-- de v2
import androidx.compose.material.icons.filled.History // <-- de v2
import androidx.compose.material.icons.filled.Person // <-- de v2
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind // <-- de v1
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint // <-- de v1
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas // <-- de v1
import androidx.compose.ui.graphics.graphicsLayer // <-- de v1
import androidx.compose.ui.graphics.toArgb // <-- de v1
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow // <-- de v2
import androidx.compose.ui.unit.Dp // <-- de v1
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.renova.mobile.R
import com.renova.mobile.network.ActivityItem
import com.renova.mobile.ui.theme.LocalRenovaColors
import com.renova.mobile.ui.theme.PoppinsFontFamily // <-- de v2
import com.renova.mobile.ui.theme.RenovaColorScheme
import com.renova.mobile.ui.theme.RenovaColors // <-- de v2
import com.renova.mobile.ui.viewmodels.ActivityViewModel
import java.text.SimpleDateFormat // <-- de v1
import java.util.* // <-- de v1
import com.renova.mobile.ui.components.*

// --- NUEVO: Imports para el Tour (de v1) ---
import androidx.compose.ui.layout.onGloballyPositioned
import com.renova.mobile.ui.tour.LocalTourState
// --- FIN DE IMPORTS ---


// --- Modifier de v1 ---
fun Modifier.greenShadow(
    color: Color = Color(0xFF4CAF50),
    alpha: Float = 0.15f,
    borderRadius: Dp = 16.dp,
    shadowRadius: Dp = 8.dp,
    offsetX: Dp = 0.dp,
    offsetY: Dp = 4.dp
) = this.drawBehind {
    val shadowColor = color.copy(alpha = alpha).toArgb()
    val transparent = color.copy(alpha = 0f).toArgb()

    drawIntoCanvas {
        val paint = Paint()
        val frameworkPaint = paint.asFrameworkPaint()
        frameworkPaint.color = transparent
        frameworkPaint.setShadowLayer(
            shadowRadius.toPx(),
            offsetX.toPx(),
            offsetY.toPx(),
            shadowColor
        )
        it.drawRoundRect(
            0f,
            0f,
            size.width,
            size.height,
            borderRadius.toPx(),
            borderRadius.toPx(),
            paint
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityScreen(
    viewModel: ActivityViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    // --- Lógica de v2 (más avanzada) ---
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

    // Modal de detalle de actividad (de v2)
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

    // Modal de error con opción de reintentar (de v2)
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
    // --- NUEVO: Obtener estado del Tour (de v1) ---
    val tourState = LocalTourState.current
    // --- FIN ---

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
                // --- MODIFICADO: Añadir Box con hook del tour (v1) alrededor de la llamada de v2 ---
                Box(modifier = Modifier.onGloballyPositioned { coords ->
                    tourState.registerTarget("activity_points_card", coords)
                }) {
                    AnimatedPointsCardActivity(
                        totalPoints = state.totalPoints,
                        renovaColors = renovaColors,
                        shouldAnimate = shouldAnimatePoints
                    )
                }
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
                        fontWeight = FontWeight.Bold, // <-- de v2
                        fontSize = 21.sp, // <-- de v2
                        fontFamily = PoppinsFontFamily // <-- de v2
                    )
                    Text(
                        text = stringResource(R.string.earn_points_recycling),
                        style = MaterialTheme.typography.bodyMedium,
                        color = renovaColors.textSecondary,
                        fontSize = 14.sp, // <-- de v2
                        fontFamily = PoppinsFontFamily // <-- de v2
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
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        // --- MODIFICADO: Añadir hook del tour (v1) ---
                        .onGloballyPositioned { coords ->
                            tourState.registerTarget("activity_materials_row", coords)
                        },
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
                    modifier = Modifier
                        .padding(start = 20.dp, top = 12.dp, bottom = 8.dp)
                        // --- MODIFICADO: Añadir hook del tour (v1) ---
                        .onGloballyPositioned { coords ->
                            tourState.registerTarget("activity_history_title", coords)
                        }
                ) {
                    Text(
                        text = stringResource(R.string.history),
                        style = MaterialTheme.typography.titleLarge,
                        color = renovaColors.textPrimary,
                        fontWeight = FontWeight.Bold, // <-- de v2
                        fontSize = 21.sp, // <-- de v2
                        fontFamily = PoppinsFontFamily // <-- de v2
                    )
                    Text(
                        text = stringResource(R.string.activity_record),
                        style = MaterialTheme.typography.bodyMedium,
                        color = renovaColors.textSecondary,
                        fontSize = 14.sp, // <-- de v2
                        fontFamily = PoppinsFontFamily // <-- de v2
                    )
                }
            }
        }

        // --- NUEVO: DisposableEffects agrupados (de v1) ---
        item {
            DisposableEffect("activity_points_card") {
                onDispose { tourState.unregisterTarget("activity_points_card") }
            }
            DisposableEffect("activity_materials_row") {
                onDispose { tourState.unregisterTarget("activity_materials_row") }
            }
            DisposableEffect("activity_history_title") {
                onDispose { tourState.unregisterTarget("activity_history_title") }
            }
        }
        // --- FIN DE BLOQUE NUEVO ---

        // --- Lógica de lista de v2 (con estado vacío) ---
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

// --- Composable de v2 ---
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

// --- Composable (idéntico en ambas versiones) ---
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

// --- Composable de v2 ---
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