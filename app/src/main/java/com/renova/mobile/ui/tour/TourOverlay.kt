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
import androidx.compose.ui.graphics.graphicsLayer // <-- 1. IMPORTAR ESTO
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.collectLatest

/**
 * El Composable principal del Tour. Se dibuja sobre toda la pantalla.
 */
@Composable
fun TourOverlay(
    tourState: TourState,
    currentScreenRoute: String?,
    onNavigate: (String) -> Unit // Para el tour completo (aún no se usa)
) {
    // Observamos los 'StateFlow' directamente para que Compose reaccione a los cambios.

    // 1.A. Observa el índice actual
    val currentStepIndex by tourState.currentStepIndex.collectAsState()

    // 1.B. Observa el mapa de 'targets' (posiciones)
    val targets by tourState.targets.collectAsState()

    // 1.C. Deriva el 'currentStep' basado en el 'currentStepIndex'
    val currentStep by remember(currentStepIndex) {
        derivedStateOf { tourState.tourSteps.getOrNull(currentStepIndex) }
    }

    // 1.D. Deriva el 'targetRect' basado en el 'currentStep' y los 'targets'
    val targetRect by remember(currentStep, targets) {
        derivedStateOf { currentStep?.let { targets[it.targetId] } }
    }
    // --- FIN DE CORRECCIÓN 1 ---

    val isStepOnCorrectScreen = currentStep?.screenRoute == currentScreenRoute

    LaunchedEffect(currentStep, currentScreenRoute) {
        val step = currentStep ?: return@LaunchedEffect

        // Si ya estamos en la pantalla correcta, no hacemos nada.
        if (isStepOnCorrectScreen) return@LaunchedEffect

        // Si NO estamos en la pantalla correcta, le decimos a NavController que navegue.
        onNavigate(step.screenRoute)
    }

    val scrimAlpha by animateFloatAsState(
        targetValue = if (isStepOnCorrectScreen && targetRect != null) 0.7f else 0.0f,
        animationSpec = tween(300),
        label = "scrimAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { /* Consumir clics */ }
            )
    ) {
        // Esto es un truco común para que los BlendMode (modos de fusión) funcionen
        // de manera fiable en todas las versiones de Android y GPUs.
        Canvas(modifier = Modifier
            .fillMaxSize()
            .graphicsLayer(alpha = 0.99f) // <-- 2. AÑADIR ESTA LÍNEA
        ) {
            // 1. Dibuja el fondo
            drawRect(
                color = Color.Black.copy(alpha = scrimAlpha)
            )

            val localTargetRect = targetRect

            if (localTargetRect != null && isStepOnCorrectScreen) {
                val inflatedRect = localTargetRect.inflate(with(density) { 8.dp.toPx() })

                // Usamos DstOut (Destination Out) para "cortar" el agujero
                // en el destino (el fondo negro que acabamos de dibujar).
                drawRoundRect(
                    color = Color.Black, // El color no importa, solo la forma
                    topLeft = inflatedRect.topLeft,
                    size = inflatedRect.size,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx()),
                    blendMode = BlendMode.DstOut // <-- Esto corta el agujero
                )
            }
        }

        val localCurrentStep = currentStep
        val localTargetRect = targetRect

        if (localTargetRect != null && localCurrentStep != null && isStepOnCorrectScreen) {
            TooltipBox(
                step = localCurrentStep,
                targetRect = localTargetRect,
                // Usamos el 'currentStepIndex' que SÍ reacciona a los cambios
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
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = step.title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = step.description,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // El botón "Salir" ahora está habilitado (antes era gris)
                TextButton(onClick = onEnd) {
                    Text("Salir")
                }

                Row {
                    // El botón "Ant." se habilitará en el paso 2
                    TextButton(onClick = onPrev, enabled = !isFirstStep) {
                        Text("Ant.")
                    }
                    Button(onClick = onNext) {
                        Text(if (isLastStep) "Fin" else "Sig.")
                    }
                }
            }
        }
    }
}

/**
 * Calcula dónde posicionar el tooltip (arriba o abajo del objetivo)
 * y lo centra horizontalmente, asegurando que no se salga de la pantalla.
 */
private fun calculateTooltipPosition(
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

    // --- Posición Y (Arriba o Abajo) ---
    val y = if (spaceBelow > tooltipHeightPx + margin) {
        // Colocar DEBAJO del objetivo
        targetRect.bottom + margin
    } else if (spaceAbove > tooltipHeightPx + margin) {
        // Colocar ARRIBA del objetivo
        targetRect.top - tooltipHeightPx - margin
    } else {
        // Fallback: centrar verticalmente (si no hay espacio)
        (screenHeightPx / 2) - (tooltipHeightPx / 2)
    }

    // --- Posición X (Centrado) ---
    var x = (targetRect.left + targetRect.width / 2) - (tooltipWidthPx / 2)

    // Ajustar X para que no se salga de los bordes de la pantalla
    if (x < margin) {
        x = margin
    }
    if (x + tooltipWidthPx > screenWidthPx - margin) {
        x = screenWidthPx - tooltipWidthPx - margin
    }

    // Convertir de Px a Dp para el Modifier.offset
    return Pair(with(density) { x.toDp().value }, with(density) { y.toDp().value })
}