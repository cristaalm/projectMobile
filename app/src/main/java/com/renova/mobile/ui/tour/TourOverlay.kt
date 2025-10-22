package com.renova.mobile.ui.tour

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer // Import necesario
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.renova.mobile.R
import com.renova.mobile.ui.theme.PoppinsFontFamily // Asegúrate que la ruta sea correcta
import com.renova.mobile.ui.theme.LocalRenovaColors // <-- ¡NUEVO IMPORT!
import kotlinx.coroutines.flow.collectLatest

/**
 * El Composable principal del Tour. Se dibuja sobre toda la pantalla.
 */
@Composable
fun TourOverlay(
    tourState: TourState,
    currentScreenRoute: String?,
    onNavigate: (String) -> Unit
) {
    // Lógica de estado (sin cambios)
    val currentStepIndex by tourState.currentStepIndex.collectAsState()
    val targets by tourState.targets.collectAsState()
    val currentStep by remember(currentStepIndex) { derivedStateOf { tourState.tourSteps.getOrNull(currentStepIndex) } }
    val targetRect by remember(currentStep, targets) { derivedStateOf { currentStep?.let { targets[it.targetId] } } }
    val isStepOnCorrectScreen = currentStep?.screenRoute == currentScreenRoute

    // Efecto para navegación automática (sin cambios)
    LaunchedEffect(currentStep, currentScreenRoute) {
        val step = currentStep ?: return@LaunchedEffect
        if (isStepOnCorrectScreen) return@LaunchedEffect
        onNavigate(step.screenRoute)
    }

    val scrimAlpha by animateFloatAsState(
        targetValue = if (isStepOnCorrectScreen && targetRect != null) 0.7f else 0.0f,
        animationSpec = tween(300),
        label = "scrimAlpha"
    )

    // --- COLOR CORREGIDO ---
    // Usar Negro semitransparente para el overlay (común y efectivo)
    // Podrías usar un color de RenovaColors si prefieres, ej: LocalRenovaColors.current.Background.copy(alpha=scrimAlpha)
    val scrimColor = Color.Black
    // --- FIN ---

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { /* Consumir clics */ }
            )
    ) {
        Canvas(modifier = Modifier
            .fillMaxSize()
            .graphicsLayer(alpha = 0.99f) // Para BlendMode.DstOut
        ) {
            // 1. Dibuja el fondo
            drawRect(
                color = scrimColor.copy(alpha = scrimAlpha) // <-- COLOR CORREGIDO
            )

            val localTargetRect = targetRect

            if (localTargetRect != null && isStepOnCorrectScreen) {
                val inflatedRect = localTargetRect.inflate(with(density) { 8.dp.toPx() })

                // 2. "Corta" el agujero
                drawRoundRect(
                    color = scrimColor, // <-- Color consistente (no afecta visualmente con DstOut)
                    topLeft = inflatedRect.topLeft,
                    size = inflatedRect.size,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx()),
                    blendMode = BlendMode.DstOut
                )
            }
        }

        val localCurrentStep = currentStep
        val localTargetRect = targetRect

        // 3. Muestra el Tooltip
        if (localTargetRect != null && localCurrentStep != null && isStepOnCorrectScreen) {
            TooltipBox(
                step = localCurrentStep,
                targetRect = localTargetRect,
                isFirstStep = currentStepIndex == 0,
                isLastStep = currentStepIndex == tourState.tourSteps.size - 1,
                onNext = { tourState.nextStep() },
                onPrev = { tourState.prevStep() },
                onEnd = { tourState.endTour() }
            )
        }
    }
}

/**
 * El cuadro de diálogo (tooltip) que muestra la información del paso.
 */
@Composable
private fun TooltipBox(
    step: TourStep,
    targetRect: Rect,
    isFirstStep: Boolean,
    isLastStep: Boolean,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onEnd: () -> Unit
) {
    // --- NUEVO: Obtener colores de Renova ---
    val renovaColors = LocalRenovaColors.current
    // --- FIN ---

    // Cálculos de tamaño y posición (sin cambios)
    var tooltipSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val (tooltipX, tooltipY) = remember(targetRect, tooltipSize, screenWidthPx, screenHeightPx) {
        calculateTooltipPosition(targetRect, tooltipSize, screenWidthPx, screenHeightPx, density)
    }
    val alpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(delayMillis = 150, durationMillis = 200),
        label = "tooltipAlpha"
    )

    Surface(
        modifier = Modifier
            .offset(x = tooltipX.dp, y = tooltipY.dp)
            .widthIn(max = 300.dp)
            .onSizeChanged { tooltipSize = it }
            .graphicsLayer { this.alpha = alpha },
        shape = RoundedCornerShape(12.dp),
        // --- COLOR CORREGIDO ---
        color = renovaColors.cardBackground, // Usar color de fondo de tarjeta de Renova
        // --- FIN ---
        tonalElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(id = step.titleResId),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                // --- COLOR CORREGIDO ---
                color = renovaColors.textPrimary, // Usar color de texto primario de Renova
                // --- FIN ---
                fontFamily = PoppinsFontFamily // Fuente Poppins
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(id = step.descriptionResId),
                fontSize = 14.sp,
                color = renovaColors.textSecondary, // Usar color de texto secundario de Renova
                fontFamily = PoppinsFontFamily // Fuente Poppins
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onEnd) {
                    Text(
                        stringResource(id = R.string.tour_button_exit),
                        fontFamily = PoppinsFontFamily
                    )
                }
                Row {
                    TextButton(onClick = onPrev, enabled = !isFirstStep) {
                        Text(
                            stringResource(id = R.string.tour_button_prev),
                            fontFamily = PoppinsFontFamily
                        )
                    }
                    Button(
                        onClick = onNext,
                        colors = ButtonDefaults.buttonColors(containerColor = renovaColors.primaryColor)
                    ) {
                        Text(
                            stringResource(id = if (isLastStep) R.string.tour_button_finish else R.string.tour_button_next),
                            fontFamily = PoppinsFontFamily,
                            color = Color.White // Texto blanco sobre botón primario
                        )
                    }
                }
            }
        }
    }
}

/**
 * Calcula dónde posicionar el tooltip (arriba o abajo del objetivo)
 */
private fun calculateTooltipPosition(
    // ... (Sin cambios)
    targetRect: Rect,
    tooltipSize: IntSize,
    screenWidthPx: Float,
    screenHeightPx: Float,
    density: Density
): Pair<Float, Float> {
    val margin = with(density) { 16.dp.toPx() }
    val tooltipHeightPx = tooltipSize.height
    val tooltipWidthPx = tooltipSize.width
    val spaceAbove = targetRect.top
    val spaceBelow = screenHeightPx - targetRect.bottom
    val y = if (spaceBelow > tooltipHeightPx + margin) {
        targetRect.bottom + margin
    } else if (spaceAbove > tooltipHeightPx + margin) {
        targetRect.top - tooltipHeightPx - margin
    } else {
        (screenHeightPx / 2) - (tooltipHeightPx / 2)
    }
    var x = (targetRect.left + targetRect.width / 2) - (tooltipWidthPx / 2)
    if (x < margin) {
        x = margin
    }
    if (x + tooltipWidthPx > screenWidthPx - margin) {
        x = screenWidthPx - tooltipWidthPx - margin
    }
    return Pair(with(density) { x.toDp().value }, with(density) { y.toDp().value })
}