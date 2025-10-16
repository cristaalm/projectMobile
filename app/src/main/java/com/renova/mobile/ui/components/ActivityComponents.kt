package com.renova.mobile.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.renova.mobile.R
import com.renova.mobile.ui.theme.RenovaColorScheme
import java.text.SimpleDateFormat
import java.util.*

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

// Componente de diálogo de error
@Composable
fun ErrorDialog(
    error: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    title: String = "¡Ocurrió un error!",
    message: String = "¡Oops! Sucedió un error, por favor reinicia o vuelve a iniciar sesión"
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Text(
                text = message,
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

// Función para formatear fechas de manera amigable
@Composable
fun formatFriendlyDate(isoString: String): String {
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