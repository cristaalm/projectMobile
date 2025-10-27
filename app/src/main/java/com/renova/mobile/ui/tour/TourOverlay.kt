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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.CornerRadius // Import específico para CornerRadius
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
import kotlinx.coroutines.flow.collectLatest // No estrictamente necesario aquí, pero buena práctica

/**
 * El Composable principal del Tour. Se dibuja sobre toda la pantalla.
 */
@Composable
fun TourOverlay(
    tourState: TourState,
    currentScreenRoute: String?,
    onNavigate: (String) -> Unit // Lambda para solicitar navegación
) {
    // --- Observar estado relevante del TourState ---
    val currentStepIndex by tourState.currentStepIndex.collectAsState() // Necesario para que recomponga al cambiar paso
    val targets by tourState.targets.collectAsState() // Mapa de targets registrados
    val isTourActive by tourState.isTourActive.collectAsState() // Para saber si mostrar algo

    // --- Calcular paso y rectángulo actuales (derivados de los estados) ---
    // Usamos 'derivedStateOf' para recalcular solo si currentStepIndex o targets cambian
    val currentStep by remember(currentStepIndex, tourState.tourSteps) {
        derivedStateOf { tourState.tourSteps.getOrNull(currentStepIndex) }
    }
    val targetRect by remember(currentStep, targets) {
        derivedStateOf { currentStep?.let { targets[it.targetId] } }
    }

    // Comprobar si el paso actual corresponde a la pantalla visible
    val isStepOnCorrectScreen = currentStep?.screenRoute == currentScreenRoute

    // --- Efecto para manejar la navegación automática entre pantallas ---
    LaunchedEffect(currentStep, currentScreenRoute, isTourActive) {
        if (!isTourActive) return@LaunchedEffect // No hacer nada si el tour terminó
        val step = currentStep ?: return@LaunchedEffect // Necesitamos un paso actual

        // Si el paso actual NO es para la pantalla actual, pide navegar
        if (!isStepOnCorrectScreen) {
            // Asegúrate de que onNavigate maneje la lógica de no navegar si ya está en la ruta
            onNavigate(step.screenRoute)
        }
    }

    // --- Animación del fondo oscurecido (scrim) ---
    val scrimAlpha by animateFloatAsState(
        // Mostrar scrim solo si el tour está activo, el paso es para esta pantalla y tenemos un objetivo
        targetValue = if (isTourActive && isStepOnCorrectScreen && targetRect != null) 0.7f else 0.0f,
        animationSpec = tween(300),
        label = "scrimAlpha"
    )
    val scrimColor = Color.Black // Color base del scrim

    // --- Contenedor principal del Overlay ---
    // Ocupa toda la pantalla y consume clics para evitar interacciones debajo
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                enabled = isTourActive, // Solo consume clics si el tour está activo
                interactionSource = remember { MutableInteractionSource() },
                indication = null, // Sin efecto visual al clickear el scrim
                onClick = { /* No hacer nada al clickear el scrim */ }
            )
            .alpha(if (isTourActive) 1f else 0f) // Oculta todo si el tour no está activo
    ) {
        // --- Canvas para dibujar el Scrim y el Agujero ---
        Canvas(modifier = Modifier
            .fillMaxSize()
            // graphicsLayer necesario para que BlendMode.DstOut funcione correctamente
            .graphicsLayer(alpha = 0.99f)
        ) {
            // 1. Dibuja el scrim (fondo semitransparente)
            drawRect(
                color = scrimColor.copy(alpha = scrimAlpha) // Aplica la opacidad animada
            )

            // Obtiene el rectángulo del objetivo actual (si existe y es para esta pantalla)
            val localTargetRect = targetRect
            if (localTargetRect != null && isStepOnCorrectScreen && scrimAlpha > 0.1f) { // Solo dibuja agujero si scrim es visible
                // Infla ligeramente el rectángulo para crear un borde alrededor del objetivo
                val inflatedRect = localTargetRect.inflate(with(density) { 8.dp.toPx() })

                // 2. "Corta" el agujero usando BlendMode.DstOut
                // Dibuja un rectángulo redondeado con el color base del scrim (no importa el color exacto con DstOut)
                // donde la forma se superpone con el scrim ya dibujado, se vuelve transparente.
                drawRoundRect(
                    color = scrimColor, // Color base, no afecta visualmente con DstOut
                    topLeft = inflatedRect.topLeft,
                    size = inflatedRect.size,
                    cornerRadius = CornerRadius(16.dp.toPx()), // Esquinas redondeadas para el agujero
                    blendMode = BlendMode.DstOut // Modo de mezcla que "resta" o "corta"
                )
            }
        }

        // --- Mostrar el Tooltip ---
        // Obtiene las variables locales para asegurar estabilidad en la composición
        val localCurrentStep = currentStep
        val localTargetRect = targetRect

        // Muestra el Tooltip solo si:
        // - Hay un paso actual
        // - Hay un rectángulo de objetivo para ese paso
        // - El paso corresponde a la pantalla actual
        if (localCurrentStep != null && localTargetRect != null && isStepOnCorrectScreen && isTourActive) {
            TooltipBox(
                step = localCurrentStep,
                targetRect = localTargetRect,
                isFirstStep = tourState.isFirstStepOfTour(), // Usa la función auxiliar
                isLastStep = tourState.isLastStepOfTour(),   // Usa la función auxiliar
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
    targetRect: Rect, // Rectángulo del elemento resaltado
    isFirstStep: Boolean, // Indica si es el primer paso (para habilitar/deshabilitar botón 'Ant')
    isLastStep: Boolean, // Indica si es el último paso (para cambiar texto de 'Sig' a 'Fin')
    onNext: () -> Unit, // Acción al presionar 'Siguiente' o 'Finalizar'
    onPrev: () -> Unit, // Acción al presionar 'Anterior'
    onEnd: () -> Unit // Acción al presionar 'Salir'
) {
    // --- Obtener colores del tema ---
    val renovaColors = LocalRenovaColors.current

    // --- Estado y cálculo de posición del Tooltip ---
    var tooltipSize by remember { mutableStateOf(IntSize.Zero) } // Tamaño del tooltip (se mide con onSizeChanged)
    val density = LocalDensity.current // Para convertir Dp a Px
    val configuration = LocalConfiguration.current // Para obtener dimensiones de pantalla
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }

    // Calcula la posición (X, Y) donde debe aparecer el tooltip usando la función auxiliar
    val (tooltipX, tooltipY) = remember(targetRect, tooltipSize, screenWidthPx, screenHeightPx) {
        calculateTooltipPosition(targetRect, tooltipSize, screenWidthPx, screenHeightPx, density)
    }

    // --- Animación de aparición del Tooltip ---
    val alpha by animateFloatAsState(
        targetValue = 1f, // Siempre visible cuando se compone
        animationSpec = tween(delayMillis = 150, durationMillis = 200), // Pequeño retraso y fade-in
        label = "tooltipAlpha"
    )

    // --- Composable del Tooltip ---
    Surface(
        modifier = Modifier
            // Aplica el offset calculado para posicionar el tooltip
            .offset(x = tooltipX.dp, y = tooltipY.dp)
            // Limita el ancho máximo del tooltip
            .widthIn(max = 300.dp)
            // Mide el tamaño real del tooltip una vez compuesto
            .onSizeChanged { tooltipSize = it }
            // Aplica la animación de alpha
            .graphicsLayer { this.alpha = alpha },
        shape = RoundedCornerShape(12.dp), // Esquinas redondeadas
        color = renovaColors.cardBackground, // Color de fondo del tooltip
        tonalElevation = 4.dp // Sombra sutil
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // --- Título del paso ---
            Text(
                text = stringResource(id = step.titleResId),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = renovaColors.textPrimary, // Color de texto primario
                fontFamily = PoppinsFontFamily // Fuente personalizada
            )
            Spacer(modifier = Modifier.height(8.dp))
            // --- Descripción del paso ---
            Text(
                text = stringResource(id = step.descriptionResId),
                fontSize = 14.sp,
                color = renovaColors.textSecondary, // Color de texto secundario
                fontFamily = PoppinsFontFamily // Fuente personalizada
            )
            Spacer(modifier = Modifier.height(16.dp))
            // --- Botones de navegación del tour ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween, // Espacio entre Salir y (Ant, Sig)
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón Salir
                TextButton(onClick = onEnd) {
                    Text(
                        stringResource(id = R.string.tour_button_exit),
                        fontFamily = PoppinsFontFamily
                    )
                }
                // Botones Anterior y Siguiente/Finalizar
                Row {
                    // Botón Anterior (deshabilitado si es el primer paso)
                    TextButton(onClick = onPrev, enabled = !isFirstStep) {
                        Text(
                            stringResource(id = R.string.tour_button_prev),
                            fontFamily = PoppinsFontFamily
                        )
                    }
                    // Botón Siguiente o Finalizar
                    Button(
                        onClick = onNext, // La lógica de finalizar está en TourState.nextStep()
                        colors = ButtonDefaults.buttonColors(containerColor = renovaColors.primaryColor) // Color primario
                    ) {
                        Text(
                            // Cambia el texto a "Fin" si es el último paso
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
 * Calcula la posición (X, Y) en Dp para el Tooltip.
 * Intenta colocarlo debajo del objetivo, si no cabe, lo pone arriba.
 * Si no cabe en ningún lado, lo centra verticalmente.
 * Ajusta horizontalmente para que no se salga de la pantalla.
 */
private fun calculateTooltipPosition(
    targetRect: Rect,       // Rectángulo del elemento resaltado (en Px)
    tooltipSize: IntSize,   // Tamaño del tooltip medido (en Px)
    screenWidthPx: Float,   // Ancho total de la pantalla (en Px)
    screenHeightPx: Float,  // Alto total de la pantalla (en Px)
    density: Density        // Densidad de pantalla para conversiones Px/Dp
): Pair<Float, Float> {     // Devuelve Pair(X, Y) en Dp

    val margin = with(density) { 16.dp.toPx() } // Margen alrededor del objetivo y bordes (en Px)
    val tooltipHeightPx = tooltipSize.height
    val tooltipWidthPx = tooltipSize.width

    // Espacio disponible arriba y abajo del objetivo
    val spaceAbove = targetRect.top
    val spaceBelow = screenHeightPx - targetRect.bottom

    // Calcular coordenada Y:
    val yPx = if (spaceBelow > tooltipHeightPx + margin) {
        // Cabe debajo: Posición Y = borde inferior del objetivo + margen
        targetRect.bottom + margin
    } else if (spaceAbove > tooltipHeightPx + margin) {
        // No cabe debajo, pero sí arriba: Posición Y = borde superior del objetivo - altura tooltip - margen
        targetRect.top - tooltipHeightPx - margin
    } else {
        // No cabe ni arriba ni abajo (raro, pero posible): Centrar verticalmente en pantalla
        (screenHeightPx / 2) - (tooltipHeightPx / 2)
    }

    // Calcular coordenada X: Intentar centrar horizontalmente con el objetivo
    var xPx = (targetRect.left + targetRect.width / 2) - (tooltipWidthPx / 2)

    // Ajustar X para que no se salga por la izquierda
    if (xPx < margin) {
        xPx = margin
    }
    // Ajustar X para que no se salga por la derecha
    if (xPx + tooltipWidthPx > screenWidthPx - margin) {
        xPx = screenWidthPx - tooltipWidthPx - margin
    }

    // Convertir coordenadas calculadas (Px) a Dp para usarlas en Modifier.offset
    return Pair(with(density) { xPx.toDp().value }, with(density) { yPx.toDp().value })
}