package com.renova.mobile.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.renova.mobile.R
import com.renova.mobile.network.ActivityItem
import com.renova.mobile.ui.theme.LocalRenovaColors
import com.renova.mobile.ui.theme.RenovaColorScheme
import com.renova.mobile.ui.viewmodels.ActivityViewModel
import java.text.SimpleDateFormat
import java.util.*


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
    val renovaColors = LocalRenovaColors.current
    val state by viewModel.state.collectAsState()
    val pullToRefreshState = rememberPullToRefreshState()

    LaunchedEffect(Unit) {
        viewModel.loadHistory(1)
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
                            onNextPage = { viewModel.nextPage() }
                        )
                    }
                }
            }
        }
    }

    if (state.error != null && state.activities.isEmpty()) {
        ErrorDialog(
            error = state.error ?: "Error desconocido",
            onRetry = {
                viewModel.clearError()
                viewModel.retry()
            },
            onDismiss = {
                viewModel.clearError()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomRefreshIndicator(
    state: PullToRefreshState,
    isRefreshing: Boolean,
    renovaColors: RenovaColorScheme,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.pullToRefreshIndicator(
            state = state,
            isRefreshing = isRefreshing,
            containerColor = renovaColors.activityCardBackground,
            threshold = PullToRefreshDefaults.PositionalThreshold
        ),
        contentAlignment = Alignment.Center
    ) {
        Crossfade(
            targetState = isRefreshing,
            animationSpec = tween(durationMillis = 200),
            modifier = Modifier.align(Alignment.Center)
        ) { refreshing ->
            if (refreshing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = renovaColors.activityPrimary
                )
            } else {
                val distanceFraction = { state.distanceFraction.coerceIn(0f, 1f) }
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "Refresh",
                    tint = renovaColors.activityPrimary,
                    modifier = Modifier
                        .size(24.dp)
                        .graphicsLayer {
                            val progress = distanceFraction()
                            this.alpha = progress
                            this.scaleX = progress
                            this.scaleY = progress
                            this.rotationZ = progress * 180f
                        }
                )
            }
        }
    }
}

@Composable
private fun LoadingState(renovaColors: RenovaColorScheme) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = renovaColors.activityPrimary)
    }
}

@Composable
private fun ErrorDialog(
    error: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "¡Ocurrió un error!",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Text(
                text = "¡Oops! Sucedió un error, por favor reinicia o vuelve a iniciar sesión",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Reintentar", color = MaterialTheme.colorScheme.onPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        },
        shape = RoundedCornerShape(18.dp)
    )
}

@Composable
private fun ActivityContent(
    state: com.renova.mobile.ui.viewmodels.ActivityState,
    renovaColors: RenovaColorScheme,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit
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
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, start = 18.dp, end = 20.dp, bottom = 10.dp)
                        .height(140.dp),
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
                                        text = "${state.totalPoints}",
                                        style = MaterialTheme.typography.displayLarge,
                                        color = Color.White,
                                        fontSize = 52.sp,
                                        fontWeight = FontWeight.Bold,
                                        lineHeight = 52.sp
                                    )

                                    Spacer(modifier = Modifier.width(2.dp))

                                    Text(
                                        modifier = Modifier.padding(top = 22.dp),
                                        text = stringResource(R.string.points_unit),
                                        style = MaterialTheme.typography.displayLarge,
                                        color = Color.White,
                                        fontSize = 30.sp,
                                        fontWeight = FontWeight.Bold,
                                        lineHeight = 30.sp
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
                                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color.White.copy(alpha = 0.9f))
                                )
                            }
                        }
                    }
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
                        color = renovaColors.textPrimary
                    )
                    Text(
                        text = stringResource(R.string.earn_points_recycling),
                        style = MaterialTheme.typography.bodyMedium,
                        color = renovaColors.textSecondary
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
                        color = renovaColors.textPrimary
                    )
                    Text(
                        text = stringResource(R.string.activity_record),
                        style = MaterialTheme.typography.bodyMedium,
                        color = renovaColors.textSecondary
                    )
                }
            }
        }

        items(state.activities.size) { index ->
            androidx.compose.animation.AnimatedVisibility(
                visible = true,
                enter = androidx.compose.animation.fadeIn(
                    animationSpec = androidx.compose.animation.core.tween(
                        durationMillis = 400,
                        delayMillis = 400 + (index * 50)
                    )
                ) + androidx.compose.animation.slideInVertically(
                    initialOffsetY = { 20 },
                    animationSpec = androidx.compose.animation.core.tween(
                        durationMillis = 400,
                        delayMillis = 400 + (index * 50)
                    )
                )
            ) {
                Box(modifier = Modifier.padding(horizontal = 18.dp)) {
                    ActivityCard(
                        item = state.activities[index],
                        renovaColors = renovaColors
                    )
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

@Composable
private fun PaginationControls(
    currentPage: Int,
    totalPages: Int,
    isLoading: Boolean,
    renovaColors: RenovaColorScheme,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Button(
            onClick = onPreviousPage,
            enabled = currentPage > 1 && !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (currentPage > 1) renovaColors.buttonEnabled else renovaColors.buttonDisabled,
                contentColor = renovaColors.activityCardBackground
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .weight(1f)
                .height(40.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.back),
                contentDescription = stringResource(R.string.previous),
                tint = renovaColors.activityCardBackground,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(stringResource(R.string.previous), color = renovaColors.activityCardBackground)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "$currentPage ${stringResource(R.string.of)} $totalPages",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.align(Alignment.CenterVertically),
            color = renovaColors.textPrimary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Button(
            onClick = onNextPage,
            enabled = currentPage < totalPages && !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (currentPage < totalPages) renovaColors.buttonEnabled else renovaColors.buttonDisabled,
                contentColor = renovaColors.activityCardBackground
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .weight(1f)
                .height(40.dp)
        ) {
            Text(stringResource(R.string.next), color = renovaColors.activityCardBackground)
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                painter = painterResource(id = R.drawable.next),
                contentDescription = stringResource(R.string.next),
                tint = renovaColors.activityCardBackground,
                modifier = Modifier.size(20.dp)
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
private fun ActivityCard(item: ActivityItem, renovaColors: RenovaColorScheme) {
    val context = LocalContext.current
    val isPointRedemption = item.type_history == 1

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 0.dp, vertical = 6.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                if (isPointRedemption) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = stringResource(R.string.points_exchange),
                            style = MaterialTheme.typography.titleMedium,
                            color = renovaColors.textPrimary,
                            maxLines = 2,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "${item.points}",
                            style = MaterialTheme.typography.titleMedium,
                            color = renovaColors.negativePoints,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                    if (item.alliance != null) {
                        Text(
                            text = item.alliance.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = renovaColors.textSecondary,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                    if (item.reward != null) {
                        Text(
                            text = item.reward.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = renovaColors.textSecondary,
                            maxLines = 3,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = stringResource(R.string.product_entry),
                            style = MaterialTheme.typography.titleMedium,
                            color = renovaColors.textPrimary,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "+${item.points}",
                            style = MaterialTheme.typography.titleMedium,
                            color = renovaColors.positivePoints,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                    val materialName = item.material_type?.name ?: stringResource(R.string.unknown_material)
                    val displayText = if (item.scan?.is_crushed == false) {
                        "$materialName - ${stringResource(R.string.crushed)}"
                    } else {
                        materialName
                    }
                    Text(
                        text = displayText,
                        style = MaterialTheme.typography.bodySmall,
                        color = renovaColors.textSecondary,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = formatFriendlyDate(item.created_at),
                    style = MaterialTheme.typography.bodySmall,
                    color = renovaColors.textSecondary
                )
            }
        }

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(renovaColors.activityPrimary)
        )
    }
}

@Composable
private fun formatFriendlyDate(isoString: String): String {
    val context = LocalContext.current
    val sdfInput = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
    sdfInput.timeZone = TimeZone.getTimeZone("UTC")
    val date = try { sdfInput.parse(isoString) } catch (_: Exception) { null }
    if (date == null) return isoString

    val calDate = Calendar.getInstance().apply { time = date }
    val today = Calendar.getInstance()
    val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
    val locale = Locale.getDefault()
    val timeFormat = SimpleDateFormat("h:mm a", locale)

    return when {
        calDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                calDate.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) ->
            "${context.getString(R.string.today)}, ${timeFormat.format(date).lowercase()}"
        calDate.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
                calDate.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR) ->
            "${context.getString(R.string.yesterday)}, ${timeFormat.format(date).lowercase()}"
        else -> {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy, h:mm a", locale)
            dateFormat.format(date).lowercase()
        }
    }
}