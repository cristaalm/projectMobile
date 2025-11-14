package com.renova.mobile.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.*
import androidx.compose.runtime.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import java.util.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.renova.mobile.R
import com.renova.mobile.network.ActivityItem
import com.renova.mobile.ui.theme.LocalRenovaColors
import com.renova.mobile.ui.theme.PoppinsFontFamily
import com.renova.mobile.ui.theme.RenovaColorScheme
import com.renova.mobile.ui.theme.RenovaColors
import java.text.SimpleDateFormat

// Componente del indicador de refresco personalizado
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomRefreshIndicator(
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

// Componente de estado de carga
@Composable
fun LoadingState(
    renovaColors: RenovaColorScheme,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = renovaColors.activityPrimary)
    }
}

// Controles de paginación
@Composable
fun PaginationControls(
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
            .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 80.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Button(
            onClick = onPreviousPage,
            enabled = currentPage > 1 && !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (currentPage > 1) RenovaColors.Secondary else renovaColors.buttonDisabled,
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
                containerColor = if (currentPage < totalPages) RenovaColors.Secondary else renovaColors.buttonDisabled,
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

// Función para formatear fechas de manera amigable
@Composable
fun formatFriendlyDate(isoString: String): String {
    val context = LocalContext.current

    fun tryParse(vararg patterns: String): Date? {
        for (p in patterns) {
            try {
                val sdf = SimpleDateFormat(p, Locale.getDefault())
                // Tratar entradas con 'Z' o zona explícita como UTC
                if (p.contains("'Z'") || p.contains("XXX")) {
                    sdf.timeZone = TimeZone.getTimeZone("UTC")
                }
                val d = sdf.parse(isoString)
                if (d != null) return d
            } catch (_: Exception) {
            }
        }
        return null
    }

    val date = tryParse(
        // ISO con microsegundos
        "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX",
        // ISO con milisegundos
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
        // ISO sin fracción
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd'T'HH:mm:ssXXX",
        // Sin 'T' ni zona
        "yyyy-MM-dd HH:mm:ss",
        // ISO sin 'Z'
        "yyyy-MM-dd'T'HH:mm:ss.SSSSSS",
        "yyyy-MM-dd'T'HH:mm:ss.SSS",
        "yyyy-MM-dd'T'HH:mm:ss"
    )

    if (date == null) return isoString

    val calDate = Calendar.getInstance().apply { time = date }
    val today = Calendar.getInstance()
    val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
    val locale = Locale.getDefault()
    val timeFormat = SimpleDateFormat("h:mm a", locale)
    val timeText = timeFormat.format(date).lowercase(locale)

    return when {
        calDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                calDate.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) ->
            "${context.getString(R.string.today)}, ${timeText}"
        calDate.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
                calDate.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR) ->
            "${context.getString(R.string.yesterday)}, ${timeText}"
        else -> {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy, h:mm a", locale)
            dateFormat.format(date).lowercase(locale)
        }
    }
}

@Composable
fun DetailSheet(activity: ActivityItem) {
    val colors = LocalRenovaColors.current
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Determinar el color según si suma o resta puntos
    val pointsColor = when (activity.type_history) {
        1 -> colors.negativePoints // Canjeo - siempre rojo
        2 -> if (activity.points == 0) colors.textPrimary else colors.primaryColor
        3 -> if (activity.points < 0) colors.negativePoints else colors.primaryColor
        else -> if (activity.points < 0) colors.negativePoints else colors.primaryColor
    }

    // Determinar el texto de los puntos
    val pointsText = when (activity.type_history) {
        1 -> if ("${activity.points}".startsWith("-")) "${activity.points}" else "-${activity.points}"
        2 -> if (activity.points == 0) "-${activity.points}-" else "+${activity.points}"
        3 -> if (activity.points < 0) "${activity.points}" else "+${activity.points}"
        else -> if (activity.points < 0) "${activity.points}" else "+${activity.points * -1}"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Icono según el tipo de actividad
        val (icon, iconColor) = when (activity.type_history) {
            1 -> Icons.Default.ShoppingCart to colors.primaryColor
            2 -> Icons.Default.Recycling to colors.primaryColor
            3 -> Icons.Default.Person to colors.primaryColor
            else -> Icons.Default.History to colors.primaryColor
        }

        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(48.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Título del tipo de actividad
        Text(
            text = when (activity.type_history) {
                1 -> stringResource(R.string.reward_exchange)
                2 -> stringResource(R.string.recycling)
                3 -> stringResource(R.string.points_adjustment)
                else -> stringResource(R.string.activity)
            },
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = colors.textPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Subtítulo según el tipo
        when (activity.type_history) {
            1 -> {
                // Canjeo: Mostrar nombre del comercio y recompensa
                activity.alliance?.let { alliance ->
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = alliance.name,
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = colors.textPrimary,
                        textAlign = TextAlign.Center
                    )
                }
                activity.reward?.let { reward ->
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = reward.name,
                        fontFamily = PoppinsFontFamily,
                        fontSize = 16.sp,
                        color = colors.textSecondary,
                        textAlign = TextAlign.Center
                    )
                    reward.description?.let { desc ->
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = desc,
                            fontFamily = PoppinsFontFamily,
                            fontSize = 14.sp,
                            color = colors.textSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            2 -> {
                // Reciclaje: Mostrar tipo de material
                activity.material_type?.let { material ->
                    val materialName = when {
                        material.name.contains("Plástico", ignoreCase = true) ->
                            context.getString(R.string.plastic)
                        material.name.contains("Aluminio", ignoreCase = true) ->
                            context.getString(R.string.aluminum)
                        else -> material.name
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = materialName,
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = colors.textPrimary,
                        textAlign = TextAlign.Center
                    )
                    material.description?.let { desc ->
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = desc,
                            fontFamily = PoppinsFontFamily,
                            fontSize = 14.sp,
                            color = colors.textSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                // Información del escaneo
                activity.scan?.let { scan ->
                    Spacer(modifier = Modifier.height(10.dp))
                    // Descripción del escaneo
                    scan.description?.let { desc ->
                        Text(
                            text = desc,
                            fontFamily = PoppinsFontFamily,
                            fontSize = 14.sp,
                            color = colors.textSecondary,
                            textAlign = TextAlign.Center
                        )
                    }

                }
            }
            3 -> {

                // Ajuste manual: Mostrar descripción si existe
                Text(
                    text = context.getString(R.string.manual_adjustment),
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = colors.textPrimary,
                    textAlign = TextAlign.Center
                )

                activity.description?.let { desc ->
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = desc,
                        fontFamily = PoppinsFontFamily,
                        fontSize = 14.sp,
                        color = colors.textSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Cantidad si existe (para canjeos)
        if (activity.quantity != null && activity.type_history == 1) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "${context.getString(R.string.quantity)}: ${activity.quantity}",
                fontFamily = PoppinsFontFamily,
                fontSize = 14.sp,
                color = colors.textSecondary,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Puntos con color dinámico
        Text(
            text = pointsText,
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 56.sp,
            color = pointsColor,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Equivalente en MXN
        Text(
            text = "$${"%.2f".format(kotlin.math.abs(activity.points) * 0.01)} MXN",
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp,
            color = colors.textSecondary
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Fecha
        Text(
            text = formatFriendlyDate(activity.created_at),
            fontFamily = PoppinsFontFamily,
            fontSize = 14.sp,
            color = colors.textSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun AnimatedPointsCardActivity(
    totalPoints: Float,
    renovaColors: RenovaColorScheme,
    shouldAnimate: Boolean = true
) {
    // Animación del contador de puntos
    var animatedPoints by remember { mutableStateOf(if (shouldAnimate) 0f else totalPoints.toFloat()) }

    LaunchedEffect(totalPoints, shouldAnimate) {
        if (shouldAnimate) {
            animate(
                initialValue = 0f,
                targetValue = totalPoints.toFloat(),
                animationSpec = tween(
                    durationMillis = 2500,
                    easing = FastOutSlowInEasing
                )
            ) { value, _ ->
                animatedPoints = value
            }
        } else {
            animatedPoints = totalPoints.toFloat()
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 14.dp, start = 18.dp, end = 20.dp, bottom = 8.dp)
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
                    Spacer(modifier = Modifier.height(4.dp))
                    Row {
                        Text(
                            text = java.text.NumberFormat.getIntegerInstance(
                                java.util.Locale.forLanguageTag("es-MX")
                            ).format(animatedPoints.toFloat()),
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
                    contentAlignment = Alignment.BottomStart
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.leaf),
                        contentDescription = null,
                        modifier = Modifier
                            .size(80.dp)
                            .padding(start = 8.dp, bottom = 8.dp),
                        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(
                            Color.White.copy(alpha = 0.9f)
                        )
                    )
                }
            }
        }
    }
}