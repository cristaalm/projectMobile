package com.renova.mobile.ui.screens

import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.CardDefaults.cardElevation
import androidx.compose.material3.pulltorefresh.*
import androidx.compose.runtime.*
import coil.decode.GifDecoder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.style.TextAlign
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
import com.renova.mobile.ui.tour.LocalTourState
import com.renova.mobile.ui.tour.TourState
import com.renova.mobile.ui.viewmodels.StreakViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

data class MonthlyBadge(
    val id: Int,
    val name: String,
    val pointsRequired: Int,
    val bonusPoints: Int,
    val iconRes: Int,
    val isActive: Boolean,
    val isUnlocked: Boolean = false,
    val isClaimed: Boolean = false,
    val currentMonthProgress: Int = 0
)

fun getBadgeVisualConfig(badgeId: Int): Triple<Int, Color, Color> {
    // ✅ Tus 4 iconos actuales
    val availableIcons = listOf(
        R.drawable.ic_goal_1,
        R.drawable.ic_goal_2,
        R.drawable.ic_goal_3,
        R.drawable.ic_goal_4
    )

    // ✅ Colores variados para hacer más interesante
    val availableColors = listOf(
        Pair(Color.White, Color(0xFF024653)),  // Verde azulado oscuro
        Pair(Color.White, Color(0xFF01C851)),  // Verde brillante
        Pair(Color.White, Color(0xFF1976D2)),  // Azul
        Pair(Color.White, Color(0xFF388E3C)),  // Verde medio
        Pair(Color.White, Color(0xFF00897B)),  // Turquesa
        Pair(Color.White, Color(0xFF5E35B1))   // Morado
    )

    // ✅ Usa el ID del badge para elegir (siempre será el mismo para cada badge)
    val iconIndex = badgeId % availableIcons.size
    val colorIndex = badgeId % availableColors.size

    val selectedIcon = availableIcons[iconIndex]
    val (textColor, bgColor) = availableColors[colorIndex]

    return Triple(selectedIcon, textColor, bgColor)
}

@Composable
private fun translateError(errorCode: String): String {
    return when (errorCode) {
        "ERROR_NO_INTERNET" -> stringResource(R.string.no_internet_retry_message)
        "ERROR_SESSION_EXPIRED" -> stringResource(R.string.session_expired)
        "ERROR_UNKNOWN" -> stringResource(R.string.unknown_error)
        else -> errorCode
    }
}

@Composable
private fun ErrorStateFullScreen(
    errorMessage: String,
    renovaColors: RenovaColorScheme,
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
            text = stringResource(R.string.error_loading_streak),
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
            Text(
                text = stringResource(R.string.retry),
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun getBadgeStateColor(isUnlocked: Boolean, isClaimed: Boolean): Color {
    return when {
        isClaimed -> RenovaColors.PrimaryColor.copy(alpha = 0.7f)
        isUnlocked -> RenovaColors.SecondaryColor
        else -> if(isSystemInDarkTheme() == true){
            RenovaColors.SecondaryColor.copy(alpha = 0.4f)
        } else {
            RenovaColors.SecondaryColor.copy(alpha = 0.7f)
        }
    }
}

fun calculateDaysSinceRegistration(createdAt: String): Int {
    return try {
        val cleanedDate = createdAt.replace(Regex("\\.\\d+Z"), "Z")

        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        val registrationDate = format.parse(cleanedDate)

        if (registrationDate == null) {
            return 0
        }

        val currentDate = Date()
        val diffInMillis = currentDate.time - registrationDate.time
        val days = (diffInMillis / (1000L * 60 * 60 * 24)).toInt()

        days.coerceAtLeast(0)

    } catch (e: Exception) {
        e.printStackTrace()
        0
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreakScreen(
    viewModel: StreakViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val renovaColors = LocalRenovaColors.current
    val scrollState = rememberScrollState()
    val tourState = LocalTourState.current
    val state by viewModel.state.collectAsState()
    val pullToRefreshState = rememberPullToRefreshState()

    var selectedBadge by remember { mutableStateOf<MonthlyBadge?>(null) }
    var showClaimErrorModal by remember { mutableStateOf(false) }

    // Registrar y desregistrar targets del tour
    DisposableEffect(Unit) {
        onDispose {
            tourState.unregisterTarget("streak_card_main")
            tourState.unregisterTarget("streak_monthly_badges")
            tourState.unregisterTarget("streak_weekly_progress")
        }
    }

    // Mostrar modal de error al reclamar badge
    LaunchedEffect(state.claimError) {
        if (state.claimError != null) {
            showClaimErrorModal = true
        }
    }

    LaunchedEffect(state.monthlyBadges, state.isClaimingBadge) {
        selectedBadge?.let { currentBadge ->
            android.util.Log.d("StreakScreen", "📦 Estado actualizado - isClaimingBadge: ${state.isClaimingBadge}")

            // Buscar el badge actualizado en el nuevo estado
            val updatedBadge = state.monthlyBadges.find { it.id == currentBadge.id }
            if (updatedBadge != null) {
                android.util.Log.d("StreakScreen", "🔄 Badge actualizado - isClaimed: ${updatedBadge.isClaimed}, isUnlocked: ${updatedBadge.isUnlocked}")
                selectedBadge = updatedBadge // ✅ Actualizar con el nuevo estado
            }
        }
    }


    Box(modifier = Modifier.fillMaxSize()) {
        when {
            // Caso 1: Primera carga y aún no hay error
            state.isLoading && !state.hasLoadedOnce && state.error == null -> {
                LoadingState(renovaColors = renovaColors)
            }

            // Caso 2: Error crítico (primera carga falló) - PANTALLA COMPLETA
            state.error != null && !state.hasLoadedOnce -> {
                ErrorStateFullScreen(
                    errorMessage = translateError(state.error ?: "ERROR_UNKNOWN"),
                    renovaColors = renovaColors,
                    onRetry = {
                        viewModel.clearError()
                        viewModel.retry()
                    }
                )
            }

            // Caso 3: Contenido exitoso con pull-to-refresh
            state.hasLoadedOnce -> {
                PullToRefreshBox(
                    isRefreshing = state.isLoading && state.hasLoadedOnce,
                    onRefresh = {
                        viewModel.loadStreakData(isManualRefresh = true)
                    },
                    state = pullToRefreshState,
                    indicator = {
                        CustomRefreshIndicator(
                            state = pullToRefreshState,
                            isRefreshing = state.isLoading && state.hasLoadedOnce,
                            renovaColors = renovaColors,
                            modifier = Modifier.align(Alignment.TopCenter)
                        )
                    }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState)
                                .padding(top = 20.dp,bottom = 80.dp)
                        ) {
                            Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                                StreakCard(
                                    currentStreak = state.currentStreak,
                                    isStreakActive = state.isStreakActive,
                                    renovaColors = renovaColors,
                                    scrollState = scrollState,
                                    tourState = tourState
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Box(
                                modifier = Modifier.onGloballyPositioned {
                                    tourState.registerTarget(
                                        id = "streak_monthly_badges",
                                        coordinates = it,
                                        scrollState = scrollState
                                    )
                                }
                            ) {
                                if (state.badgesLoadError) {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 20.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = renovaColors.cardBackground
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(24.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_goal_1),
                                                contentDescription = null,
                                                tint = renovaColors.textSecondary.copy(alpha = 0.5f),
                                                modifier = Modifier.size(48.dp)
                                            )
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Text(
                                                text = stringResource(R.string.badges_load_error),
                                                style = MaterialTheme.typography.titleMedium,
                                                color = renovaColors.textPrimary,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = PoppinsFontFamily
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = stringResource(R.string.check_connection_try_again),
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = renovaColors.textSecondary,
                                                fontFamily = PoppinsFontFamily,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                } else {
                                    MonthlyBadgesSectionV2(
                                        badges = state.monthlyBadges,
                                        currentMonthPoints = state.currentMonthPoints,
                                        renovaColors = renovaColors,
                                        onBadgeClick = { selectedBadge = it }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                                if (state.weekData.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier.onGloballyPositioned {
                                            tourState.registerTarget(
                                                id = "streak_weekly_progress",
                                                coordinates = it,
                                                scrollState = scrollState
                                            )
                                        }
                                    ) {
                                        WeeklyProgressChart(
                                            weekData = state.weekData,
                                            renovaColors = renovaColors
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Snackbar de error en refresh manual (igual que ActivityScreen)
                if (state.error != null &&
                    state.isManualRefresh &&
                    state.hasLoadedOnce) {

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

            // Caso 4: Fallback
            else -> {
                LoadingState(renovaColors = renovaColors)
            }
        }
    }

    // Dialog del badge seleccionado
    selectedBadge?.let { badge ->
        BadgeDialogV2(
            badge = badge, // Este badge se actualizará automáticamente
            currentMonthPoints = state.currentMonthPoints,
            isClaimingBadge = state.isClaimingBadge,
            onDismiss = {
                selectedBadge = null
            },
            onClaim = {
                viewModel.claimBadge(badge.id)
            }
        )
    }

    // Modal de error al reclamar badge
    if (showClaimErrorModal && state.claimError != null) {
        RetryableErrorModal(
            isVisible = true,
            errorMessage = translateError(state.claimError ?: "ERROR_UNKNOWN"),
            onDismiss = {
                showClaimErrorModal = false
                viewModel.clearClaimError()
            },
            onRetry = {
                showClaimErrorModal = false
                viewModel.clearClaimError()
                selectedBadge?.let { badge ->
                    viewModel.claimBadge(badge.id)
                }
            }
        )
    }
}

@Composable
private fun StreakCard(
    currentStreak: Int,
    isStreakActive: Boolean,
    renovaColors: RenovaColorScheme,
    scrollState: ScrollState,
    tourState: TourState
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned {
                tourState.registerTarget(
                    id = "streak_card_main",
                    coordinates = it,
                    scrollState = scrollState
                )
            },
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = cardElevation(defaultElevation = 0.dp)
    ){
        Box(
            modifier = Modifier
                .fillMaxWidth()
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.current_streak),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            fontFamily = PoppinsFontFamily,
                            color = Color.White.copy(alpha = 1.5f)
                        )

                        if (isStreakActive) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        Color(0xFF01C851),
                                        shape = CircleShape
                                    )
                            )
                        }
                    }

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
                            lineHeight = 56.sp,
                            modifier = Modifier.padding(bottom = 13.dp),
                            fontFamily = PoppinsFontFamily
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .background(
                            if (isStreakActive)
                                Color.White.copy(alpha = 0.15f)
                            else
                                Color(0xFF6B7280).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(20.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isStreakActive) {
                        GifPlayer(modifier = Modifier.fillMaxSize())
                    } else {
                        GifPlayer(
                            modifier = Modifier.fillMaxSize(),
                            colorFilter = ColorFilter.colorMatrix(
                                ColorMatrix().apply {
                                    setToSaturation(0f)
                                }
                            ),
                            alpha = 0.4f
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GifPlayer(
    modifier: Modifier = Modifier,
    colorFilter: ColorFilter? = null,
    alpha: Float = 1f
) {
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
        modifier = modifier,
        colorFilter = colorFilter,
        alpha = alpha
    )
}