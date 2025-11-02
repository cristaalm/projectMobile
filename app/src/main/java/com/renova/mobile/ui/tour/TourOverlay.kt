package com.renova.mobile.ui.tour

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
// --- INICIO MODIFICACIÓN: Imports añadidos ---
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
// --- FIN MODIFICACIÓN ---
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
import com.renova.mobile.ui.theme.PoppinsFontFamily
import com.renova.mobile.ui.theme.LocalRenovaColors

@Composable
fun TourOverlay(
    tourState: TourState,
    currentScreenRoute: String?,
    onNavigate: (String) -> Unit
) {
    val currentStepIndex by tourState.currentStepIndex.collectAsState()
    val targets by tourState.targets.collectAsState()
    val isTourActive by tourState.isTourActive.collectAsState()

    val tourSteps by tourState.tourSteps.collectAsState()

    val currentStep by remember(currentStepIndex, tourSteps) {
        derivedStateOf { tourSteps.getOrNull(currentStepIndex) }
    }

    // --- INICIO MODIFICACIÓN: Obtener TargetInfo completo ---
    val targetInfo by remember(currentStep, targets) {
        derivedStateOf { currentStep?.let { targets[it.targetId] } }
    }
    val targetRect = targetInfo?.rect
    val targetScrollState = targetInfo?.scrollState
    // --- NUEVAS VARIABLES ---
    val targetLazyListState = targetInfo?.lazyListState
    val targetItemIndex = targetInfo?.itemIndex
    // --- FIN MODIFICACIÓN ---

    val isStepOnCorrectScreen = currentStep?.screenRoute == currentScreenRoute

    LaunchedEffect(currentStep, currentScreenRoute, isTourActive) {
        if (!isTourActive) return@LaunchedEffect
        val step = currentStep ?: return@LaunchedEffect

        if (!isStepOnCorrectScreen) {
            onNavigate(step.screenRoute)
        }
    }

    // --- INICIO MODIFICACIÓN: Lógica de Auto-Scroll (para ScrollState y LazyListState) ---
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }

    LaunchedEffect(
        targetRect,
        targetScrollState,
        targetLazyListState,
        targetItemIndex,
        isStepOnCorrectScreen,
        screenHeightPx
    ) {
        if (!isStepOnCorrectScreen || targetRect == null) {
            return@LaunchedEffect
        }

        // Márgenes (TopBar e BottomBar) para definir la "zona visible"
        val topMargin = with(density) { 100.dp.toPx() } // Altura aprox TopBar
        val bottomMargin = with(density) { 180.dp.toPx() } // Altura aprox BottomBar + FAB

        val visibleAreaTop = topMargin
        val visibleAreaBottom = screenHeightPx - bottomMargin

        val isOffScreenTop = targetRect.top < visibleAreaTop // El item está "por encima" de la zona visible
        val isOffScreenBottom = targetRect.bottom > visibleAreaBottom // El item está "por debajo" de la zona visible

        if (isOffScreenBottom || isOffScreenTop) {
            // Comprobar qué tipo de scroll usar
            if (targetScrollState != null) {
                // --- Lógica para ScrollState ---
                // Calcula cuánto scrollear.
                // Queremos que el item aparezca justo debajo del TopBar (topMargin)
                val scrollAmount = (targetRect.top - topMargin).toInt()
                // Asegurarse de no scrollear a un valor negativo
                targetScrollState.animateScrollTo(scrollAmount.coerceAtLeast(0))

            } else if (targetLazyListState != null && targetItemIndex != null) {
                // --- Lógica para LazyListState ---
                // Simplemente scrollea al índice del item.
                // El offset se puede usar si el item es más grande que la pantalla,
                // pero para centrarlo, scrollear al índice es suficiente.
                targetLazyListState.animateScrollToItem(targetItemIndex)
            }
        }
    }
    // --- FIN MODIFICACIÓN ---


    // ... (El resto del Composable (Canvas, TooltipBox, etc) se mantiene igual) ...
    val shouldShowScrim = isTourActive && isStepOnCorrectScreen &&
            (targetRect != null || currentStep?.isWelcomeStep == true)

    val scrimAlpha by animateFloatAsState(
        targetValue = if (shouldShowScrim) 0.7f else 0.0f,
        animationSpec = tween(300),
        label = "scrimAlpha"
    )
    val scrimColor = Color.Black

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                enabled = isTourActive,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { }
            )
            .alpha(if (isTourActive) 1f else 0f)
    ) {
        Canvas(modifier = Modifier
            .fillMaxSize()
            .graphicsLayer(alpha = 0.99f)
        ) {
            drawRect(color = scrimColor.copy(alpha = scrimAlpha))

            val localTargetRect = targetRect
            val localCurrentStep = currentStep
            if (localTargetRect != null &&
                isStepOnCorrectScreen &&
                scrimAlpha > 0.1f &&
                localCurrentStep?.isWelcomeStep != true) {

                val inflatedRect = localTargetRect.inflate(with(density) { 8.dp.toPx() })

                drawRoundRect(
                    color = scrimColor,
                    topLeft = inflatedRect.topLeft,
                    size = inflatedRect.size,
                    cornerRadius = CornerRadius(16.dp.toPx()),
                    blendMode = BlendMode.DstOut
                )
            }
        }

        val localCurrentStep = currentStep
        val localTargetRect = targetRect

        if (localCurrentStep != null &&
            (localTargetRect != null || localCurrentStep.isWelcomeStep) &&
            isStepOnCorrectScreen &&
            isTourActive) {

            TooltipBox(
                step = localCurrentStep,
                targetRect = localTargetRect,
                isFirstStep = tourState.isFirstStepOfTour(),
                isLastStep = tourState.isLastStepOfTour(),
                onNext = { tourState.nextStep() },
                onPrev = { tourState.prevStep() },
                onEnd = { tourState.endTour() }
            )
        }
    }
}

@Composable
private fun TooltipBox(
    step: TourStep,
    targetRect: Rect?,
    isFirstStep: Boolean,
    isLastStep: Boolean,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onEnd: () -> Unit
) {
    // ... (El Composable TooltipBox se mantiene igual) ...
    val renovaColors = LocalRenovaColors.current

    var tooltipSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }

    val (tooltipX, tooltipY) = remember(targetRect, tooltipSize, screenWidthPx, screenHeightPx, step.isWelcomeStep) {
        if (step.isWelcomeStep || targetRect == null) {
            val x = (screenWidthPx / 2) - (tooltipSize.width / 2)
            val y = (screenHeightPx / 2) - (tooltipSize.height / 2)
            Pair(with(density) { x.toDp().value }, with(density) { y.toDp().value })
        } else {
            calculateTooltipPosition(targetRect, tooltipSize, screenWidthPx, screenHeightPx, density)
        }
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
        color = renovaColors.cardBackground,
        tonalElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(id = step.titleResId),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = renovaColors.textPrimary,
                fontFamily = PoppinsFontFamily
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(id = step.descriptionResId),
                fontSize = 14.sp,
                color = renovaColors.textSecondary,
                fontFamily = PoppinsFontFamily
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
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

private fun calculateTooltipPosition(
    targetRect: Rect,
    tooltipSize: IntSize,
    screenWidthPx: Float,
    screenHeightPx: Float,
    density: Density
): Pair<Float, Float> {
    // ... (Esta función no cambia) ...
    val margin = with(density) { 16.dp.toPx() }
    val tooltipHeightPx = tooltipSize.height
    val tooltipWidthPx = tooltipSize.width

    val safeAreaTop = margin
    val safeAreaBottom = screenHeightPx - tooltipHeightPx - margin

    val targetIsOffBottom = targetRect.top > safeAreaBottom
    val targetIsOffTop = targetRect.bottom < safeAreaTop

    val yPx: Float
    if (targetIsOffBottom) {
        yPx = screenHeightPx - tooltipHeightPx - margin
    } else if (targetIsOffTop) {
        yPx = margin
    } else {
        val spaceAbove = targetRect.top
        val spaceBelow = screenHeightPx - targetRect.bottom

        yPx = if (spaceBelow > tooltipHeightPx + margin) {
            targetRect.bottom + margin
        } else if (spaceAbove > tooltipHeightPx + margin) {
            targetRect.top - tooltipHeightPx - margin
        } else {
            (screenHeightPx / 2) - (tooltipHeightPx / 2)
        }
    }

    var xPx: Float
    if (targetIsOffBottom || targetIsOffTop) {
        xPx = (screenWidthPx / 2) - (tooltipWidthPx / 2)
    } else {
        xPx = (targetRect.left + targetRect.width / 2) - (tooltipWidthPx / 2)
    }

    if (xPx < margin) {
        xPx = margin
    }
    if (xPx + tooltipWidthPx > screenWidthPx - margin) {
        xPx = screenWidthPx - tooltipWidthPx - margin
    }

    return Pair(with(density) { xPx.toDp().value }, with(density) { yPx.toDp().value })
}