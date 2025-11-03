package com.renova.mobile.ui.screens

import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.ScrollState
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
import com.renova.mobile.network.ApiClient
import com.renova.mobile.network.ClaimBadgeRequest
import com.renova.mobile.network.BadgeCollection
import com.renova.mobile.ui.components.*
import com.renova.mobile.ui.theme.LocalRenovaColors
import com.renova.mobile.ui.theme.PoppinsFontFamily
import com.renova.mobile.ui.theme.RenovaColors
import com.renova.mobile.ui.theme.RenovaColorScheme
import androidx.compose.ui.layout.onGloballyPositioned
import com.renova.mobile.ui.tour.LocalTourState
import androidx.compose.runtime.DisposableEffect
import com.renova.mobile.utils.SessionManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

data class WeeklyChallenge(
    val id: Int,
    val title: String,
    val titleEn: String,
    val description: String,
    val goal: String,
    val rewardText: String,
    val iconEmoji: String,
    val expiresAt: Long,
    val isAccepted: Boolean = false,
    val currentProgress: Int = 0,
    val targetProgress: Int = 100
)

data class MonthlyBadge(
    val id: Int,
    val title: String,
    val titleEn: String,
    val badgeName: String,
    val requiredPoints: Int,
    val bonusPoints: Int,
    val iconRes: Int,
    val color: Color,
    val backgroundColor: Color,
    val isUnlocked: Boolean = false,
    val isClaimed: Boolean = false,
    val currentMonthProgress: Int = 0
)

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

@Composable
fun StreakScreen() {
    val renovaColors = LocalRenovaColors.current
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val scope = rememberCoroutineScope()

    var currentStreak by remember { mutableStateOf(0) }
    var isStreakActive by remember { mutableStateOf(false) }
    var longestStreak by remember { mutableStateOf(0) }
    var totalRecyclingDays by remember { mutableStateOf(0) }
    var currentMonthPoints by remember { mutableStateOf(0) }
    var userId by remember { mutableStateOf(0) }
    var userBadges by remember { mutableStateOf(BadgeCollection()) }
    var weekData by remember { mutableStateOf(listOf<Int>()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }

    var selectedBadge by remember { mutableStateOf<MonthlyBadge?>(null) }
    var isClaimingBadge by remember { mutableStateOf(false) }
    var showErrorModal by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isLoading = true

        try {
            // Obtener token y llamar a identityUser para datos actualizados
            val token = sessionManager.getAccessToken()

            if (token != null) {
                val identityResponse = ApiClient.apiService.identifyUser(
                    com.renova.mobile.network.IdentifyUserRequest(
                        token = token,
                        with_identity = false
                    )
                )

                if (identityResponse.isSuccessful && identityResponse.body()?.success == true) {
                    identityResponse.body()?.data?.user?.let { userData ->
                        userId = userData.id
                        currentMonthPoints = userData.points_month
                        userBadges = userData.badge

                        totalRecyclingDays = calculateDaysSinceRegistration(userData.created_at)

                        // Actualizar UserData en SessionManager
                        sessionManager.saveUser(userData)
                    }
                } else {
                    errorMessage = context.getString(R.string.unknown_error)
                }
            } else {
                errorMessage = "No se encontró sesión activa"
            }

            val streakResponse = ApiClient.apiService.getStreak()

            if (streakResponse.isSuccessful && streakResponse.body()?.success == true) {
                streakResponse.body()?.data?.let { streakData ->
                    currentStreak = streakData.streak
                    isStreakActive = streakData.is_active
                    if (currentStreak > longestStreak) {
                        longestStreak = currentStreak
                    }
                }
            }

            val scansResponse = ApiClient.apiService.getScansByDayOfWeek()

            if (scansResponse.isSuccessful && scansResponse.body()?.success == true) {
                scansResponse.body()?.data?.let { scansList ->
                    weekData = scansList.map { it.scans_count }
                }
            } else {
                android.util.Log.e("StreakScreen", "❌ Error en scans: ${scansResponse.message()}")
            }
        } catch (e: Exception) {
            errorMessage = e.message ?: context.getString(R.string.unknown_error)
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    val monthlyBadges = remember(currentMonthPoints, userBadges) {
        listOf(
            MonthlyBadge(
                id = 1,
                title = "Guerrero Ecológico",
                titleEn = "Eco Warrior",
                badgeName = "Eco Warrior",
                requiredPoints = 100,
                bonusPoints = 50,
                iconRes = R.drawable.ic_goal_1,
                color = Color.White,
                backgroundColor = Color(0xFF024653),
                isUnlocked = currentMonthPoints >= 100,
                isClaimed = userBadges.ecoWarrior,
                currentMonthProgress = currentMonthPoints.coerceAtMost(100)
            ),
            MonthlyBadge(
                id = 2,
                title = "Reciclador Pro",
                titleEn = "Recycler Pro",
                badgeName = "Recycler Pro",
                requiredPoints = 500,
                bonusPoints = 300,
                iconRes = R.drawable.ic_goal_2,
                color = Color.White,
                backgroundColor = Color(0xFF01C851),
                isUnlocked = currentMonthPoints >= 500,
                isClaimed = userBadges.recyclerPro,
                currentMonthProgress = currentMonthPoints.coerceAtMost(500)
            ),
            MonthlyBadge(
                id = 3,
                title = "Héroe Verde",
                titleEn = "Green Hero",
                badgeName = "Green Hero",
                requiredPoints = 1000,
                bonusPoints = 600,
                iconRes = R.drawable.ic_goal_3,
                color = Color.White,
                backgroundColor = Color(0xFF024653),
                isUnlocked = currentMonthPoints >= 1000,
                isClaimed = userBadges.greenHero,
                currentMonthProgress = currentMonthPoints.coerceAtMost(1000)
            ),
            MonthlyBadge(
                id = 4,
                title = "Salvador del Planeta",
                titleEn = "Planet Saver",
                badgeName = "Planet Saver",
                requiredPoints = 2500,
                bonusPoints = 1000,
                iconRes = R.drawable.ic_goal_4,
                color = Color.White,
                backgroundColor = Color(0xFF01C851),
                isUnlocked = currentMonthPoints >= 2500,
                isClaimed = userBadges.planetSaver,
                currentMonthProgress = currentMonthPoints.coerceAtMost(2500)
            )
        )
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(bottom = 16.dp)
            ) {
                StreakCard(
                    currentStreak = currentStreak,
                    isStreakActive = isStreakActive,
                    longestStreak = longestStreak,
                    totalDays = totalRecyclingDays,
                    renovaColors = renovaColors
                )

                Spacer(modifier = Modifier.height(16.dp))

                MonthlyBadgesSection(
                    badges = monthlyBadges,
                    currentMonthPoints = currentMonthPoints,
                    renovaColors = renovaColors,
                    onBadgeClick = { selectedBadge = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (weekData.isNotEmpty()) {
                    WeeklyProgressChart(
                        weekData = weekData,
                        renovaColors = renovaColors
                    )
                }
            }
        }
    }

    selectedBadge?.let { badge ->
        BadgeDialog(
            badge = badge,
            currentMonthPoints = currentMonthPoints,
            isClaimingBadge = isClaimingBadge,
            onDismiss = { selectedBadge = null },
            onClaim = {
                scope.launch {
                    isClaimingBadge = true
                    try {
                        val response = ApiClient.apiService.claimBadge(
                            ClaimBadgeRequest(
                                userId = userId,
                                badgeName = badge.badgeName
                            )
                        )

                        if (response.isSuccessful && response.body()?.success == true) {
                            response.body()?.data?.user?.let { updatedUserData ->
                                // Actualizar UserData si existe
                                sessionManager.saveUser(updatedUserData)

                                // Actualizar también User para mantener sincronía
                                val currentUser = sessionManager.getUser()
                                currentUser?.let { user ->
                                    val updatedUser = user.copy(
                                        points_month = updatedUserData.points_month,
                                        badge = updatedUserData.badge,
                                        total_points = updatedUserData.total_points
                                    )
                                    sessionManager.saveSession(
                                        sessionManager.getAccessToken() ?: "",
                                        sessionManager.getTokenType(),
                                        sessionManager.getExpiresAt(),
                                        updatedUser,
                                        sessionManager.hasRememberMe()
                                    )
                                }

                                currentMonthPoints = updatedUserData.points_month
                                userBadges = updatedUserData.badge
                            }
                            selectedBadge = null
                        } else {
                            errorMessage = response.body()?.message
                                ?: context.getString(R.string.unknown_error)
                            showErrorModal = true
                        }
                    } catch (e: Exception) {
                        errorMessage = e.message ?: context.getString(R.string.unknown_error)
                        showErrorModal = true
                    } finally {
                        isClaimingBadge = false
                    }
                }
            }
        )
    }

    RetryableErrorModal(
        isVisible = showErrorModal,
        errorMessage = errorMessage,
        onDismiss = {
            showErrorModal = false
            errorMessage = ""
        },
        onRetry = {
            showErrorModal = false
            selectedBadge?.let { badge ->
                scope.launch {
                    isClaimingBadge = true
                    try {
                        val response = ApiClient.apiService.claimBadge(
                            ClaimBadgeRequest(
                                userId = userId,
                                badgeName = badge.badgeName
                            )
                        )

                        if (response.isSuccessful && response.body()?.success == true) {
                            response.body()?.data?.user?.let { updatedUserData ->
                                // Actualizar UserData si existe
                                sessionManager.saveUser(updatedUserData)

                                // Actualizar también User para mantener sincronía
                                val currentUser = sessionManager.getUser()
                                currentUser?.let { user ->
                                    val updatedUser = user.copy(
                                        points_month = updatedUserData.points_month,
                                        badge = updatedUserData.badge,
                                        total_points = updatedUserData.total_points
                                    )
                                    sessionManager.saveSession(
                                        sessionManager.getAccessToken() ?: "",
                                        sessionManager.getTokenType(),
                                        sessionManager.getExpiresAt(),
                                        updatedUser,
                                        sessionManager.hasRememberMe()
                                    )
                                }

                                currentMonthPoints = updatedUserData.points_month
                                userBadges = updatedUserData.badge
                            }
                            selectedBadge = null
                        } else {
                            errorMessage = response.body()?.message
                                ?: context.getString(R.string.unknown_error)
                            showErrorModal = true
                        }
                    } catch (e: Exception) {
                        errorMessage = e.message ?: context.getString(R.string.unknown_error)
                        showErrorModal = true
                    } finally {
                        isClaimingBadge = false
                    }
                }
            }
        }
    )
}

@Composable
private fun StreakCard(
    currentStreak: Int,
    isStreakActive: Boolean,
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
                            colorFilter = androidx.compose.ui.graphics.ColorFilter.colorMatrix(
                                androidx.compose.ui.graphics.ColorMatrix().apply {
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
    colorFilter: androidx.compose.ui.graphics.ColorFilter? = null,
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