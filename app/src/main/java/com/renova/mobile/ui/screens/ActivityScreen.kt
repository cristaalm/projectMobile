package com.renova.mobile.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.renova.mobile.R
import com.renova.mobile.network.ActivityItem
import com.renova.mobile.ui.theme.LocalRenovaColors
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

@Composable
fun ActivityScreen(
    viewModel: ActivityViewModel = viewModel()
) {
    val renovaColors = LocalRenovaColors.current
    val state by viewModel.state.collectAsState()

    // Cargar datos al iniciar la pantalla
    LaunchedEffect(Unit) {
        viewModel.loadHistory(1)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(renovaColors.activityBackground)
    ) {
        when {
            state.isLoading && state.activities.isEmpty() -> {
                LoadingState(renovaColors)
            }
            state.error != null && state.activities.isEmpty() -> {
                ErrorState(
                    error = state.error ?: "Error desconocido",
                    renovaColors = renovaColors,
                    onRetry = { viewModel.retry() }
                )
            }
            else -> {
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

@Composable
private fun LoadingState(renovaColors: com.renova.mobile.ui.theme.RenovaColorScheme) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = renovaColors.activityPrimary)
    }
}

@Composable
private fun ErrorState(
    error: String,
    renovaColors: com.renova.mobile.ui.theme.RenovaColorScheme,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = error,
                color = renovaColors.negativePoints
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text("Reintentar")
            }
        }
    }
}

@Composable
private fun ActivityContent(
    state: com.renova.mobile.ui.viewmodels.ActivityState,
    renovaColors: com.renova.mobile.ui.theme.RenovaColorScheme,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Encabezado
        Text(
            text = stringResource(R.string.recycling_materials),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = renovaColors.activityPrimary,
            modifier = Modifier.padding(top = 20.dp, start = 20.dp, end = 20.dp, bottom = 8.dp)
        )

        // Cuadros de materiales
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MaterialStatCard(
                title = stringResource(R.string.plastic),
                count = state.totalPlastic,
                icon = R.drawable.bottle,
                cardColor = renovaColors.plasticCardBackground,
                iconTint = renovaColors.activityPrimary,
                cardWidth = 120.dp,
                cardHeight = 150.dp
            )
            Spacer(modifier = Modifier.width(24.dp))
            MaterialStatCard(
                title = stringResource(R.string.aluminum),
                count = state.totalAluminum,
                icon = R.drawable.can,
                cardColor = renovaColors.aluminumCardBackground,
                iconTint = renovaColors.aluminumIconTint,
                cardWidth = 120.dp,
                cardHeight = 150.dp
            )
        }

        // Total de materiales
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.total_materials),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = renovaColors.activityPrimary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${state.totalPlastic + state.totalAluminum}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = renovaColors.activityCardBackground,
                modifier = Modifier
                    .background(renovaColors.activityPrimary, shape = RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp, vertical = 2.dp)
            )
        }

        // Historial
        Text(
            text = stringResource(R.string.history),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = renovaColors.activityPrimary,
            modifier = Modifier.padding(start = 20.dp, top = 8.dp, bottom = 8.dp)
        )

        // Lista
        Box(modifier = Modifier.weight(1f)) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.activities.size) { index ->
                    ActivityCard(item = state.activities[index])
                }
            }

            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = renovaColors.activityPrimary)
                }
            }
        }

        // Paginación
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

@Composable
private fun PaginationControls(
    currentPage: Int,
    totalPages: Int,
    isLoading: Boolean,
    renovaColors: com.renova.mobile.ui.theme.RenovaColorScheme,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Button(
            onClick = onPreviousPage,
            enabled = currentPage > 1 && !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (currentPage > 1) renovaColors.buttonEnabled else renovaColors.buttonDisabled,
                contentColor = renovaColors.activityCardBackground
            ),
            shape = RoundedCornerShape(50),
            modifier = Modifier.weight(1f)
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
            modifier = Modifier.align(Alignment.CenterVertically),
            color = renovaColors.activityPrimary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.width(12.dp))
        Button(
            onClick = onNextPage,
            enabled = currentPage < totalPages && !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (currentPage < totalPages) renovaColors.buttonEnabled else renovaColors.buttonDisabled,
                contentColor = renovaColors.activityCardBackground
            ),
            shape = RoundedCornerShape(50),
            modifier = Modifier.weight(1f)
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
    cardColor: Color,
    iconTint: Color,
    cardWidth: Dp = 100.dp,
    cardHeight: Dp = 140.dp
) {
    val renovaColors = LocalRenovaColors.current
    val backgroundRes = when (icon) {
        R.drawable.bottle -> R.drawable.fondo_botella
        R.drawable.can -> R.drawable.fondo_lata
        else -> 0
    }
    val textColor = iconTint

    Card(
        modifier = Modifier
            .width(cardWidth)
            .height(cardHeight)
            .greenShadow(
                color = renovaColors.shadowColor,
                alpha = 0.2f,
                shadowRadius = 6.dp,
                offsetY = 3.dp
            ),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            if (backgroundRes != 0) {
                Image(
                    painter = painterResource(id = backgroundRes),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize().clip(RoundedCornerShape(18.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(cardColor, shape = RoundedCornerShape(18.dp))
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 18.dp, bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Image(
                    painter = painterResource(id = icon),
                    contentDescription = title,
                    modifier = Modifier.size(48.dp),
                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(iconTint)
                )
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    text = count.toString(),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun ActivityCard(item: ActivityItem) {
    val renovaColors = LocalRenovaColors.current
    val context = LocalContext.current
    val isPointRedemption = item.type_history == 1  // 1 = Canjeo, 2 = Reciclaje

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .greenShadow(
                color = renovaColors.shadowColor,
                alpha = 0.15f,
                shadowRadius = 8.dp,
                offsetY = 4.dp
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = renovaColors.activityCardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 4.dp, top = 14.dp, bottom = 14.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Círculo con fondo personalizado
            val (backgroundRes, logoRes) = when {
                isPointRedemption -> Pair(R.drawable.fondo_comercio, null)
                item.material_type?.slug?.contains("plastico") == true ||
                        item.material_type?.slug?.contains("plastic") == true ->
                    Pair(R.drawable.fondo_botella, R.drawable.bottle)
                item.material_type?.slug?.contains("aluminio") == true ||
                        item.material_type?.slug?.contains("aluminum") == true ->
                    Pair(R.drawable.fondo_lata, R.drawable.can)
                else -> Pair(R.drawable.fondo_botella, R.drawable.bottle)
            }
            Box(
                modifier = Modifier.size(45.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = backgroundRes),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(50.dp).clip(RoundedCornerShape(50))
                )
                if (isPointRedemption && item.alliance != null) {
                    val logoUrl = if (item.alliance.logo) {
                        "https://renova-3q4h.onrender.com/storage/alliances/${item.alliance.id}.${item.alliance.ext}"
                    } else {
                        "https://renova-3q4h.onrender.com/build/assets/shop-8Hm9KsxR.jpg"
                    }
                    // Logo más pequeño y circular
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color.White.copy(alpha = 0.7f)),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(logoUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = item.alliance.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(24.dp).clip(RoundedCornerShape(50))
                        )
                    }
                } else if (logoRes != null) {
                    // Icono del material (botella o lata) como logo principal
                    val iconTint = when (logoRes) {
                        R.drawable.bottle -> renovaColors.activityPrimary
                        R.drawable.can -> renovaColors.aluminumIconTint
                        else -> renovaColors.activityPrimary
                    }
                    Image(
                        painter = painterResource(id = logoRes),
                        contentDescription = item.material_type?.name ?: "Material",
                        modifier = Modifier.size(25.dp),
                        contentScale = ContentScale.Fit,
                        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(iconTint)
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (isPointRedemption) {
                    Text(
                        text = "Canjeo de Puntos",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = renovaColors.activityPrimary,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    item.alliance?.let {
                        Text(
                            text = it.name,
                            fontSize = 16.sp,
                            color = renovaColors.activitySecondary,
                            lineHeight = 18.sp,
                            maxLines = 2,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                    item.reward?.let {
                        Text(
                            text = it.name,
                            fontSize = 14.sp,
                            color = renovaColors.activitySecondary,
                            lineHeight = 18.sp,
                            maxLines = 2,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                } else {
                    Text(
                        text = item.material_type?.name ?: "Material Desconocido",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = renovaColors.activityPrimary,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = formatFriendlyDate(item.created_at),
                    fontSize = 13.sp,
                    color = renovaColors.activitySecondary
                )
            }
            Text(
                text = "${if (item.points >= 0) "+" else ""}${item.points} pts",
                fontWeight = FontWeight.Bold,
                color = if (item.points >= 0) renovaColors.positivePoints else renovaColors.negativePoints,
                fontSize = 18.sp,
                modifier = Modifier
                    .padding(start = 6.dp)
                    .align(Alignment.Top)
            )
        }
    }
}

private fun formatFriendlyDate(isoString: String): String {
    val sdfInput = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
    sdfInput.timeZone = TimeZone.getTimeZone("UTC")
    val date = try { sdfInput.parse(isoString) } catch (_: Exception) { null }
    if (date == null) return isoString

    val calDate = Calendar.getInstance().apply { time = date }
    val today = Calendar.getInstance()
    val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
    val locale = Locale.forLanguageTag("es-MX")
    val timeFormat = SimpleDateFormat("h:mm a", locale)

    return when {
        calDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                calDate.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) ->
            "Hoy, ${timeFormat.format(date).lowercase()}"
        calDate.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
                calDate.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR) ->
            "Ayer, ${timeFormat.format(date).lowercase()}"
        else -> {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy, h:mm a", locale)
            dateFormat.format(date).lowercase()
        }
    }
}