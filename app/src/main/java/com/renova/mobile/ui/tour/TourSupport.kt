package com.renova.mobile.ui.tour

import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.toSize
import com.renova.mobile.R
import com.renova.mobile.navigation.NavigationItem
import com.renova.mobile.navigation.TopNavigationItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Define las rutas de los grafos de navegación que usará el tour.
 */
object TourRoutes {
    val HOME = NavigationItem.Home.route
    val STORE_GRAPH = "store_graph" // Ruta del grafo de navegación de tienda
    val STORE_LIST = "store_list" // Ruta específica dentro del grafo de tienda
    val REWARDS = "reward_screen/{allianceId}" // Ruta con argumento para recompensas
    val QR = NavigationItem.QR.route
    val PROFILE = NavigationItem.Profile.route
    val ACTIVITY = TopNavigationItem.Activity.route // Ruta de la barra superior
}

/**
 * Define la estructura de un solo paso en el tour.
 * @param id Identificador único del paso (ej. "step_home_points").
 * @param titleResId ID del recurso de string para el título del tooltip (ej. R.string.tour_title_home_points).
 * @param descriptionResId ID del recurso de string para la descripción del tooltip (ej. R.string.tour_desc_home_points).
 * @param screenRoute La ruta de NavHost donde este paso debe mostrarse (ej. TourRoutes.HOME). Debe coincidir con las rutas de tu NavController.
 * @param targetId El ID único que usaremos para vincular este paso con un Composable mediante `Modifier.onGloballyPositioned`.
 */
data class TourStep(
    val id: String,
    @StringRes val titleResId: Int,
    @StringRes val descriptionResId: Int,
    val screenRoute: String,
    val targetId: String
)

/**
 * Un Composable auxiliar que observa un estado de error proporcionado
 * (generalmente desde un ViewModel) y cierra automáticamente el tour
 * si detecta un error relacionado con la autenticación (como 401)
 * mientras el tour está activo.
 *
 * Se debe colocar dentro de cada pantalla principal que pueda experimentar
 * errores de red y que participe en el tour.
 *
 * @param error El mensaje de error actual (String? - puede ser nulo si no hay error).
 * @param isTourActiveFlow El StateFlow<Boolean> del TourState que indica si el tour está activo.
 * @param endTour La lambda (función) a llamar para finalizar el tour (normalmente `tourState::endTour`).
 */
@Composable
fun HandleTourOnError(
    error: String?,
    isTourActiveFlow: StateFlow<Boolean>, // Recibe el StateFlow del TourState
    endTour: () -> Unit                   // Recibe la función TourState::endTour
) {
    // Observa si el tour está activo
    val isTourActive by isTourActiveFlow.collectAsState()
    // Obtiene el contexto actual para poder mostrar un Toast
    val context = LocalContext.current

    // LaunchedEffect se (re)ejecuta si 'error' o 'isTourActive' cambian.
    LaunchedEffect(error, isTourActive) {
        // Solo actúa si el tour está activo y SI hay un mensaje de error
        if (isTourActive && error != null) {
            // Comprueba si el mensaje de error contiene palabras clave
            // asociadas a errores de autenticación o sesión expirada.
            val isAuthError = error.contains("401", ignoreCase = true) ||
                    error.contains("Unauthorized", ignoreCase = true) ||
                    error.contains("autenticación", ignoreCase = true) ||
                    error.contains("sesión", ignoreCase = true) ||
                    error.contains("token", ignoreCase = true)

            // Si se detecta un error de autenticación...
            if (isAuthError) {
                // ...llama a la función para terminar el tour.
                endTour()
                // ...y opcionalmente, muestra un mensaje al usuario.
                Toast.makeText(context, "Tour finalizado por error de sesión", Toast.LENGTH_SHORT).show()
            }
        }
    }
}


/**
 * Clase principal que gestiona el estado y la lógica del tour guiado.
 * Mantiene el paso actual, si está activo, las posiciones de los elementos
 * objetivo, y maneja la navegación entre pasos en modo completo o
 * modo específico de pantalla.
 */
class TourState {
    // --- Estados Internos Observables ---

    // Indica si el tour está actualmente activo (mostrando overlay y tooltip)
    private val _isTourActive = MutableStateFlow(false)
    val isTourActive = _isTourActive.asStateFlow() // Exponer como StateFlow inmutable

    // Índice del paso actual en la lista `tourSteps`
    private val _currentStepIndex = MutableStateFlow(0)
    val currentStepIndex = _currentStepIndex.asStateFlow() // Exponer como StateFlow

    // Mapa que almacena los rectángulos (posición y tamaño) de los elementos objetivo (targets)
    // La clave es el `targetId` del TourStep, el valor es el Rect calculado.
    private val _targets = MutableStateFlow<Map<String, Rect>>(emptyMap())
    val targets = _targets.asStateFlow() // Exponer como StateFlow

    // --- Estados para el Modo Específico de Pantalla ---

    // Indica si el tour se inició manualmente para una pantalla específica
    private val _isScreenSpecificMode = MutableStateFlow(false)
    val isScreenSpecificMode = _isScreenSpecificMode.asStateFlow() // Exponer como StateFlow

    // Almacena la ruta de la pantalla para la cual se inició el tour en modo específico
    private var screenSpecificRoute: String? = null

    // Almacena el índice (en `tourSteps`) del ÚLTIMO paso que se debe mostrar
    // cuando se está en modo específico de pantalla (para omitir pasos de navegación).
    private var screenSpecificLastIndex: Int = -1

    // --- Conjunto de IDs de Targets considerados "Navegación" ---
    // Pasos que apuntan a estos targets serán omitidos si son el último paso
    // en el modo específico de pantalla.
    private val navigationTargetIds = setOf(
        "bottom_bar_store",
        "bottom_bar_qr",
        "bottom_bar_profile",
        "top_bar_activity_button" // Incluimos también el de la top bar
        // Añade aquí otros IDs si tienes más botones en barras que use el tour y quieras omitir
    )

    // --- Lista Completa de Pasos del Tour ---
    // Define la secuencia completa del tour de bienvenida.
    // Cada TourStep vincula un texto (título, descripción) a un elemento visual (targetId)
    // en una pantalla específica (screenRoute).
    val tourSteps = listOf(
        TourStep(
            id = "step_home_points",
            titleResId = R.string.tour_title_home_points,
            descriptionResId = R.string.tour_desc_home_points,
            screenRoute = TourRoutes.HOME,
            targetId = "home_points_card"
        ),
        TourStep(
            id = "step_home_activity",
            titleResId = R.string.tour_title_home_activity,
            descriptionResId = R.string.tour_desc_home_activity,
            screenRoute = TourRoutes.HOME,
            targetId = "home_recent_activity"
        ),
        TourStep(
            id = "step_home_achievements",
            titleResId = R.string.tour_title_home_achievements,
            descriptionResId = R.string.tour_desc_home_achievements,
            screenRoute = TourRoutes.HOME,
            targetId = "home_achievements_section"
        ),
        TourStep(
            id = "step1_home_to_store",
            titleResId = R.string.tour_title_nav_main,
            descriptionResId = R.string.tour_desc_nav_main,
            screenRoute = TourRoutes.HOME,
            targetId = "bottom_bar_store" // Target de navegación
        ),
        TourStep(
            id = "step2_store_categories",
            titleResId = R.string.tour_title_store_categories,
            descriptionResId = R.string.tour_desc_store_categories,
            screenRoute = TourRoutes.STORE_LIST,
            targetId = "store_categories"
        ),
        TourStep(
            id = "step3_store_title",
            titleResId = R.string.tour_title_store_alliances,
            descriptionResId = R.string.tour_desc_store_alliances,
            screenRoute = TourRoutes.STORE_LIST,
            targetId = "store_alliances_title"
        ),
        TourStep(
            id = "step4_store_alliance",
            titleResId = R.string.tour_title_store_rewards,
            descriptionResId = R.string.tour_desc_store_rewards,
            screenRoute = TourRoutes.STORE_LIST,
            targetId = "store_first_alliance"
        ),
        TourStep(
            id = "step5_store_to_qr",
            titleResId = R.string.tour_title_qr_button,
            descriptionResId = R.string.tour_desc_qr_button,
            screenRoute = TourRoutes.STORE_LIST,
            targetId = "bottom_bar_qr" // Target de navegación
        ),
        TourStep(
            id = "step6_qr_info",
            titleResId = R.string.tour_title_qr_container,
            descriptionResId = R.string.tour_desc_qr_container,
            screenRoute = TourRoutes.QR,
            targetId = "qr_card"
        ),
        TourStep(
            id = "step7_qr_switch",
            titleResId = R.string.tour_title_qr_barcode,
            descriptionResId = R.string.tour_desc_qr_barcode,
            screenRoute = TourRoutes.QR,
            targetId = "qr_switch_button"
        ),
        TourStep(
            id = "step8_qr_to_profile",
            titleResId = R.string.tour_title_profile_button,
            descriptionResId = R.string.tour_desc_profile_button,
            screenRoute = TourRoutes.QR,
            targetId = "bottom_bar_profile" // Target de navegación
        ),
        TourStep(
            id = "step9_profile_language",
            titleResId = R.string.tour_title_profile_language,
            descriptionResId = R.string.tour_desc_profile_language,
            screenRoute = TourRoutes.PROFILE,
            targetId = "profile_language_toggle"
        ),
        TourStep(
            id = "step10_profile_info",
            titleResId = R.string.tour_title_profile_info,
            descriptionResId = R.string.tour_desc_profile_info,
            screenRoute = TourRoutes.PROFILE,
            targetId = "profile_info_card"
        ),
        TourStep(
            id = "step11_profile_to_topbar",
            titleResId = R.string.tour_title_activity_button,
            descriptionResId = R.string.tour_desc_activity_button,
            screenRoute = TourRoutes.PROFILE,
            targetId = "top_bar_activity_button" // Target de navegación
        ),
        TourStep(
            id = "step13_activity_points",
            titleResId = R.string.tour_title_activity_points,
            descriptionResId = R.string.tour_desc_activity_points,
            screenRoute = TourRoutes.ACTIVITY,
            targetId = "activity_points_card"
        ),
        TourStep(
            id = "step14_activity_materials",
            titleResId = R.string.tour_title_activity_materials,
            descriptionResId = R.string.tour_desc_activity_materials,
            screenRoute = TourRoutes.ACTIVITY,
            targetId = "activity_materials_row"
        ),
        TourStep(
            id = "step15_activity_history",
            titleResId = R.string.tour_title_activity_history,
            descriptionResId = R.string.tour_desc_activity_history,
            screenRoute = TourRoutes.ACTIVITY,
            targetId = "activity_history_title"
        )
    )

    // --- Propiedades Calculadas ---

    /** El objeto TourStep actual basado en `_currentStepIndex`, o null si el índice es inválido. */
    val currentStep: TourStep?
        get() = tourSteps.getOrNull(_currentStepIndex.value)

    /** El Rect (posición y tamaño) del target para el paso actual, o null si no se ha registrado. */
    val currentTargetRect: Rect?
        get() = currentStep?.let { _targets.value[it.targetId] }

    // --- Funciones Públicas para Controlar el Tour ---

    /**
     * Inicia el tour completo desde el primer paso.
     * Usado típicamente después del inicio de sesión.
     * Resetea el modo a "completo".
     */
    fun startTour() {
        _isScreenSpecificMode.value = false // Desactiva modo específico
        screenSpecificRoute = null        // Borra ruta guardada
        screenSpecificLastIndex = -1      // Resetea índice final
        _currentStepIndex.value = 0       // Empieza desde el principio
        _isTourActive.value = true        // Activa el tour
    }

    /**
     * Inicia el tour mostrando solo los pasos correspondientes a la `currentScreenRoute`.
     * Omite el último paso si este apunta a un target de navegación (definido en `navigationTargetIds`).
     * Usado para iniciar el tour manualmente desde un botón de ayuda en una pantalla.
     *
     * @param currentScreenRoute La ruta de NavHost de la pantalla actual.
     */
    fun startTourForScreen(currentScreenRoute: String) {
        // 1. Encuentra los índices de TODOS los pasos que pertenecen a la pantalla actual.
        val screenStepIndices = tourSteps.indices.filter { tourSteps[it].screenRoute == currentScreenRoute }

        // 2. Si se encontraron pasos para esta pantalla...
        if (screenStepIndices.isNotEmpty()) {
            val firstIndex = screenStepIndices.first() // Índice del primer paso de esta pantalla
            var lastIndexToShow = screenStepIndices.last() // Índice del último paso (potencialmente)

            // 3. Comprueba si el ÚLTIMO paso de esta pantalla es un paso de navegación.
            val lastStepTargetId = tourSteps[lastIndexToShow].targetId
            if (lastStepTargetId in navigationTargetIds) {
                // Si es de navegación:
                // a. Si SÓLO había UN paso en esta pantalla y era de navegación, no inicies el tour.
                if (screenStepIndices.size == 1) {
                    println("TourState: Único paso para $currentScreenRoute es de navegación. No se inicia el tour.")
                    endTour() // Asegura que esté inactivo
                    return // Sal de la función
                }
                // b. Si había MÁS de un paso, ajusta el último índice a mostrar al PENÚLTIMO paso.
                lastIndexToShow = screenStepIndices[screenStepIndices.size - 2]
            }

            // 4. Configura el estado para el modo específico.
            _isScreenSpecificMode.value = true          // Activa el modo específico
            screenSpecificRoute = currentScreenRoute    // Guarda la ruta
            screenSpecificLastIndex = lastIndexToShow   // Guarda el último índice VÁLIDO a mostrar
            _currentStepIndex.value = firstIndex        // Empieza en el primer paso de la pantalla
            _isTourActive.value = true                  // Activa el tour
        } else {
            // 5. Si no se encontraron pasos para esta pantalla.
            println("TourState: No hay pasos definidos para la ruta $currentScreenRoute")
            endTour() // Asegura inactividad
        }
    }

    /**
     * Avanza al siguiente paso del tour.
     * En modo completo, avanza al siguiente en la lista `tourSteps`.
     * En modo específico, avanza al siguiente paso DENTRO de la misma pantalla,
     * respetando el `screenSpecificLastIndex`.
     * Si es el último paso (en cualquier modo), finaliza el tour.
     */
    fun nextStep() {
        if (!_isTourActive.value) return // Sal si no está activo

        if (_isScreenSpecificMode.value) {
            // --- MODO ESPECÍFICO ---
            // 1. ¿Ya estamos en el último paso permitido para esta pantalla?
            if (_currentStepIndex.value >= screenSpecificLastIndex) {
                endTour() // Sí -> Termina el tour
                return
            }

            // 2. Busca el índice del SIGUIENTE paso en la misma pantalla.
            val currentRoute = screenSpecificRoute ?: return endTour() // Seguridad
            val nextIndexInScreen = tourSteps.indexOfFirst {
                it.screenRoute == currentRoute && tourSteps.indexOf(it) > _currentStepIndex.value
            }

            // 3. Si se encontró un siguiente paso Y está dentro del límite permitido...
            if (nextIndexInScreen != -1 && nextIndexInScreen <= screenSpecificLastIndex) {
                _currentStepIndex.value = nextIndexInScreen // ...avanza a ese paso.
            } else {
                // Si no se encontró o excede el límite (no debería pasar por el check inicial, pero por si acaso)...
                endTour() // ...termina el tour.
            }
        } else {
            // --- MODO COMPLETO ---
            // Avanza si no es el último paso general.
            if (_currentStepIndex.value < tourSteps.size - 1) {
                _currentStepIndex.value++
            } else {
                endTour() // Termina si era el último.
            }
        }
    }

    /**
     * Retrocede al paso anterior del tour.
     * En modo completo, retrocede al anterior en la lista `tourSteps`.
     * En modo específico, retrocede al paso anterior DENTRO de la misma pantalla.
     * No hace nada si es el primer paso (en cualquier modo).
     */
    fun prevStep() {
        if (!_isTourActive.value) return // Sal si no está activo

        if (_isScreenSpecificMode.value) {
            // --- MODO ESPECÍFICO ---
            val currentRoute = screenSpecificRoute ?: return // Seguridad
            // Busca el índice del ANTERIOR paso en la misma pantalla.
            val prevIndexInScreen = tourSteps.indexOfLast {
                it.screenRoute == currentRoute && tourSteps.indexOf(it) < _currentStepIndex.value
            }
            // Si se encontró (no es -1), retrocede.
            if (prevIndexInScreen != -1) {
                _currentStepIndex.value = prevIndexInScreen
            }
            // Si es -1, ya estaba en el primero de la pantalla, no hace nada.
        } else {
            // --- MODO COMPLETO ---
            // Retrocede si no es el primer paso general.
            if (_currentStepIndex.value > 0) {
                _currentStepIndex.value--
            }
        }
    }

    /**
     * Finaliza el tour, reseteando todos los estados relacionados.
     */
    fun endTour() {
        _isTourActive.value = false         // Desactiva el tour
        _currentStepIndex.value = 0         // Resetea índice
        _isScreenSpecificMode.value = false // Desactiva modo específico
        screenSpecificRoute = null        // Borra ruta guardada
        screenSpecificLastIndex = -1      // Resetea índice final
        _targets.update { emptyMap() }     // Limpia targets
    }

    // --- Funciones Auxiliares para el Tooltip ---

    /**
     * Verifica si el paso actual es el PRIMERO a mostrar en el tour actual
     * (ya sea el inicio del tour completo o el inicio del tour de pantalla específica).
     * Usado para habilitar/deshabilitar el botón "Anterior".
     */
    fun isFirstStepOfTour(): Boolean {
        if (!_isTourActive.value) return true // Considera "primero" si está inactivo

        return if (_isScreenSpecificMode.value) {
            // MODO ESPECÍFICO: Es el primero si no hay paso ANTERIOR en la MISMA pantalla.
            val currentRoute = screenSpecificRoute ?: return true
            tourSteps.indexOfLast { it.screenRoute == currentRoute && tourSteps.indexOf(it) < _currentStepIndex.value } == -1
        } else {
            // MODO COMPLETO: Es el primero si el índice es 0.
            _currentStepIndex.value == 0
        }
    }

    /**
     * Verifica si el paso actual es el ÚLTIMO a mostrar en el tour actual
     * (ya sea el final del tour completo o el `screenSpecificLastIndex` calculado).
     * Usado para cambiar el texto del botón "Siguiente" a "Finalizar".
     */
    fun isLastStepOfTour(): Boolean {
        if (!_isTourActive.value) return true // Considera "último" si está inactivo

        return if (_isScreenSpecificMode.value) {
            // MODO ESPECÍFICO: Compara con el índice final calculado y guardado.
            _currentStepIndex.value == screenSpecificLastIndex
        } else {
            // MODO COMPLETO: Compara con el último índice de la lista completa.
            _currentStepIndex.value == tourSteps.size - 1
        }
    }


    fun registerTarget(id: String, coordinates: LayoutCoordinates?) {
        // Si las coordenadas son nulas o el elemento ya no está adjunto a la ventana, elimina el target.
        if (coordinates == null || !coordinates.isAttached) {
            _targets.update { it - id }
            return
        }
        // Calcula la posición relativa a la ventana y el tamaño.
        val position = coordinates.positionInWindow()
        val size = coordinates.size.toSize()
        // Obtiene el rectángulo anterior si existía.
        val existingRect = _targets.value[id]

        // Actualiza el mapa SOLO si es un target nuevo o si su posición/tamaño cambió.
        // Esto optimiza y evita recomposiciones innecesarias del TourOverlay.
        if (existingRect == null || existingRect.topLeft != position || existingRect.size != size) {
            _targets.update { currentTargets ->
                currentTargets + (id to Rect(position, size))
            }
        }
    }

    fun unregisterTarget(id: String) {
        _targets.update { currentTargets ->
            currentTargets - id
        }
    }
}

val LocalTourState = compositionLocalOf<TourState> {
    // Se lanza un error si se intenta acceder a `LocalTourState.current`
    // sin haber provisto una instancia de `TourState` previamente.
    error("No TourState provided")
}