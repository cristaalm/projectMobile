package com.renova.mobile.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.renova.mobile.R
import com.renova.mobile.network.ActivityItem
import com.renova.mobile.ui.theme.LocalRenovaColors
import com.renova.mobile.ui.theme.PoppinsFontFamily
import com.renova.mobile.ui.theme.RenovaColorScheme
import com.renova.mobile.ui.theme.RenovaColors
import com.renova.mobile.ui.viewmodels.ActivityViewModel
import java.text.SimpleDateFormat
import java.util.*
import com.renova.mobile.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityScreen(
    viewModel: ActivityViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val renovaColors = LocalRenovaColors.current
    val state by viewModel.state.collectAsState()
    val pullToRefreshState = rememberPullToRefreshState()
    var showErrorModal by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadHistory(1)
    }

    // Mostrar modal de error cuando haya un error
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
                // Mostrar pantalla vacía cuando hay error
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

    // Modal de error con opción de reintentar
    RetryableErrorModal(
        isVisible = showErrorModal,
        errorMessage = state.error ?: stringResource(R.string.unknown_error),
        onDismiss = {
            showErrorModal = false
            viewModel.clearError()
            // NO se recarga automáticamente, solo cierra el modal
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
                AnimatedPointsCardActivity(
                    totalPoints = state.totalPoints,
                    renovaColors = renovaColors
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

        item {
            // Tabla de actividades con el mismo estilo que HomeScreen
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
                        onClick = { /* Opcional: agregar acción de click */ }
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
        val (icon, iconColor) = when (activity.type_history) {
            1 -> Icons.Default.ShoppingCart to RenovaColors.PrimaryColor // Canjeo - Verde
            2 -> Icons.Default.Recycling to RenovaColors.PrimaryColor // Reciclaje - Verde
            3 -> Icons.Default.Refresh to RenovaColors.PrimaryColor // Ajuste manual - Verde
            else -> Icons.Default.History to RenovaColors.PrimaryColor // Actividad - Verde
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
                    1 -> stringResource(R.string.reward_exchange)
                    2 -> stringResource(R.string.recycling)
                    3 -> stringResource(R.string.points_adjustment) // Nuevo tipo
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

            // Subtítulo según el tipo
            val subtitleText = when (activity.type_history) {
                1 -> {
                    // Para canje, mostrar nombre del comercio
                    activity.alliance?.name ?: ""
                }
                2 -> {
                    // Para reciclaje, mostrar tipo de material (Plástico o Aluminio)
                    val materialName = activity.material_type?.name ?: ""
                    when {
                        materialName.contains("PET", ignoreCase = true) ||
                                materialName.contains("HDPE", ignoreCase = true) ->
                            context.getString(R.string.plastic)
                        materialName.contains("Aluminio", ignoreCase = true) ||
                                materialName.contains("Aluminum", ignoreCase = true) ->
                            context.getString(R.string.aluminum)
                        else -> materialName
                    }
                }
                3 -> {
                    // Para ajuste manual, mostrar "Ajuste manual de puntos"
                    context.getString(R.string.manual_adjustment)
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

            // Para canje, agregar línea adicional con "1 x Nombre de la recompensa"
            if (activity.type_history == 1 && activity.reward != null) {
                Text(
                    text = "1 x ${activity.reward.name}",
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
            val pointsColor = when (activity.type_history) {
                1 -> RenovaColors.Error // Rojo para canjeo (siempre negativo)
                2 -> RenovaColors.PrimaryColor // Verde para reciclaje (siempre positivo)
                3 -> if (activity.points < 0) RenovaColors.Error  else RenovaColors.PrimaryColor
                else -> if (activity.points < 0) colors.negativePoints else colors.primaryColor
            }
            val pointsText = when (activity.type_history) {
                1 -> if ("${activity.points}".startsWith("-")) "${activity.points} pts" else "-${activity.points} pts"
                2 -> "+${activity.points} pts" // Reciclaje (siempre positivo)
                3 -> if (activity.points < 0) "${activity.points} pts" else "+${activity.points} pts"
                else -> if (activity.points < 0) "${activity.points} pts" else "+${activity.points} pts"
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