package com.renova.mobile.ui.tour

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
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
import kotlinx.coroutines.delay
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

    val currentStep = tourSteps.getOrNull(currentStepIndex)

    val targetInfo = currentStep?.let { targets[it.targetId] }
    val targetRect = targetInfo?.rect

    val isStepOnCorrectScreen = currentStep?.screenRoute == currentScreenRoute

    LaunchedEffect(currentStep, currentScreenRoute, isTourActive) {
        if (!isTourActive) return@LaunchedEffect
        val step = currentStep ?: return@LaunchedEffect

        if (!isStepOnCorrectScreen) {
            onNavigate(step.screenRoute)
        }
    }

    LaunchedEffect(currentStep, isStepOnCorrectScreen, targets) {
        // Solo ejecutamos si hay un paso activo, estamos en la pantalla correcta
        if (currentStep != null && isStepOnCorrectScreen && !currentStep!!.isWelcomeStep) {
            if (targets.containsKey(currentStep!!.targetId)) return@LaunchedEffect
            delay(3000)

            // Si después del tiempo sigue sin aparecer en la lista de targets
            if (!targets.containsKey(currentStep!!.targetId)) {
                // Terminamos el tour para evitar que la app se quede bloqueada
                tourState.endTour()
                println("TourOverlay: Target ${currentStep!!.targetId} no encontrado. Tour finalizado (Break).")
            }
        }
    }

    // Lógica de Auto-Scroll MEJORADA para scroll hacia arriba
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }

    LaunchedEffect(
        currentStep,
        targets,
        isStepOnCorrectScreen,
        screenHeightPx,
        density
    ) {
        if (!isStepOnCorrectScreen || currentStep == null) {
            return@LaunchedEffect
        }

        // Obtiene manualmente la info del target para el paso actual
        val currentTargetInfo = targets[currentStep.targetId]

        if (currentTargetInfo == null) {
            // El target aún no está en el mapa (probablemente off-screen)
            return@LaunchedEffect
        }

        val rect = currentTargetInfo.rect
        val scrollState = currentTargetInfo.scrollState
        val lazyState = currentTargetInfo.lazyListState
        val itemIndex = currentTargetInfo.itemIndex

        // Márgenes (TopBar e BottomBar) para definir la "zona visible"
        val topMargin = with(density) { 120.dp.toPx() } // Altura aprox TopBar + margen
        val bottomMargin = with(density) { 200.dp.toPx() } // Altura aprox BottomBar + FAB + margen

        val visibleAreaTop = topMargin
        val visibleAreaBottom = screenHeightPx - bottomMargin

        val isOffScreenTop = rect.top < visibleAreaTop // El item está "por encima" de la zona visible
        val isOffScreenBottom = rect.bottom > visibleAreaBottom // El item está "por debajo" de la zona visible

        // Solo hacer scroll si el elemento está fuera de la zona visible
        if (isOffScreenBottom || isOffScreenTop) {
            // Pequeño delay para coordinación
            delay(150)

            // Comprobar qué tipo de scroll usar
            if (scrollState != null) {
                // --- Lógica para ScrollState ---
                if (isOffScreenTop) {
                    // SCROLL HACIA ARRIBA: El target está por encima del área visible
                    val extraOffset = with(density) { 50.dp.toPx() }
                    val scrollAmount = (rect.top - topMargin - extraOffset).toInt()
                    scrollState.animateScrollTo(scrollAmount.coerceAtLeast(0))
                } else if (isOffScreenBottom) {
                    // SCROLL HACIA ABAJO: El target está por debajo del área visible
                    val scrollAmount = (rect.top - topMargin).toInt()
                    scrollState.animateScrollTo(scrollAmount.coerceAtLeast(0))
                }

            } else if (lazyState != null && itemIndex != null) {
                // --- Lógica para LazyListState ---
                try {
                    if (isOffScreenTop) {
                        // SCROLL HACIA ARRIBA: Para elementos que están por encima
                        // Hacemos scroll al item pero con un offset negativo para subir más
                        lazyState.animateScrollToItem(
                            index = itemIndex,
                            scrollOffset = -100 // Offset negativo para subir más arriba
                        )
                    } else {
                        // SCROLL HACIA ABAJO: Comportamiento normal
                        lazyState.animateScrollToItem(itemIndex)
                    }
                } catch (e: Exception) {
                    // Fallback seguro
                    try {
                        lazyState.scrollToItem(itemIndex)
                    } catch (e: Exception) {
                        // Si falla, no hacer nada - evitar bloqueos
                    }
                }
            }
        }
    }

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

            if (targetInfo != null &&
                isStepOnCorrectScreen &&
                scrimAlpha > 0.1f &&
                currentStep?.isWelcomeStep != true
            ) {
                // Recorte más pequeño
                val inflatedRect = targetInfo!!.rect.inflate(with(density) { 0.4.dp.toPx() })

                drawRoundRect(
                    color = scrimColor,
                    topLeft = inflatedRect.topLeft,
                    size = inflatedRect.size,
                    cornerRadius = CornerRadius(16.dp.toPx()),
                    blendMode = BlendMode.DstOut
                )
            }
        }

        if (currentStep != null &&
            isStepOnCorrectScreen &&
            isTourActive
        ) {
            if (targetInfo != null) {
                // Caso normal: El target es válido y está en el mapa
                TooltipBox(
                    step = currentStep!!,
                    targetRect = targetInfo!!.rect,
                    isFirstStep = tourState.isFirstStepOfTour(),
                    isLastStep = tourState.isLastStepOfTour(),
                    onNext = { tourState.nextStep() },
                    onPrev = { tourState.prevStep() },
                    onEnd = { tourState.endTour() }
                )
            } else if (currentStep!!.isWelcomeStep) {
                // Caso especial: Paso de bienvenida (no tiene targetRect)
                TooltipBox(
                    step = currentStep!!,
                    targetRect = null,
                    isFirstStep = tourState.isFirstStepOfTour(),
                    isLastStep = tourState.isLastStepOfTour(),
                    onNext = { tourState.nextStep() },
                    onPrev = { tourState.prevStep() },
                    onEnd = { tourState.endTour() }
                )
            }
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

// Lógica de posicionamiento de Tooltip
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

    val canPlaceAbove = spaceAbove > tooltipHeightPx + margin
    val canPlaceBelow = spaceBelow > tooltipHeightPx + margin

    // Decide la posición Y
    val targetCenterY = targetRect.top + targetRect.height / 2
    val isTargetInTopHalf = targetCenterY < screenHeightPx / 2

    val yPx: Float = if (isTargetInTopHalf) {
        // Target está en la mitad superior, prioriza poner el tooltip DEBAJO
        if (canPlaceBelow) {
            targetRect.bottom + margin
        } else if (canPlaceAbove) {
            // Fallback: ponerlo arriba
            targetRect.top - tooltipHeightPx - margin
        } else {
            // Fallback: centrarlo en la pantalla
            (screenHeightPx / 2) - (tooltipHeightPx / 2)
        }
    } else {
        // Target está en la mitad inferior, prioriza poner el tooltip ARRIBA
        if (canPlaceAbove) {
            targetRect.top - tooltipHeightPx - margin
        } else if (canPlaceBelow) {
            // Fallback: ponerlo abajo
            targetRect.bottom + margin
        } else {
            // Fallback: centrarlo en la pantalla
            (screenHeightPx / 2) - (tooltipHeightPx / 2)
        }
    }

    // Decide la posición X (centrado en el target, con límites de pantalla)
    var xPx: Float = (targetRect.left + targetRect.width / 2) - (tooltipWidthPx / 2)

    if (xPx < margin) {
        xPx = margin
    }
    if (xPx + tooltipWidthPx > screenWidthPx - margin) {
        xPx = screenWidthPx - tooltipWidthPx - margin
    }

    return Pair(with(density) { xPx.toDp().value }, with(density) { yPx.toDp().value })
}