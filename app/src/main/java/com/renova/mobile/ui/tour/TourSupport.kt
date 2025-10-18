package com.renova.mobile.ui.tour

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.toSize
import com.renova.mobile.navigation.NavigationItem // Importar
import com.renova.mobile.navigation.TopNavigationItem // Importar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Define las rutas de los grafos de navegación que usará el tour.
 */
object TourRoutes {
    val HOME = NavigationItem.Home.route
    val STORE_GRAPH = "store_graph"
    val STORE_LIST = "store_list"
    val REWARDS = "reward_screen/{allianceId}"
    val QR = NavigationItem.QR.route
    val PROFILE = NavigationItem.Profile.route
    val ACTIVITY = TopNavigationItem.Activity.route
}

/**
 * Define la estructura de un solo paso en el tour.
 * @param id Identificador único del paso.
 * @param title Título a mostrar en el tooltip.
 * @param description Descripción del paso.
 * @param screenRoute La ruta de NavHost donde este paso debe mostrarse.
 * @param targetId El ID que usaremos para vincular este paso con un Composable en la UI.
 */
data class TourStep(
    val id: String,
    val title: String,
    val description: String,
    val screenRoute: String,
    val targetId: String
)

/**
 * Clase que gestiona el estado del tour (activo, paso actual, posiciones de los elementos).
 */
class TourState {
    private val _isTourActive = MutableStateFlow(false)
    val isTourActive = _isTourActive.asStateFlow()

    private val _currentStepIndex = MutableStateFlow(0)
    val currentStepIndex = _currentStepIndex.asStateFlow()

    private val _targets = MutableStateFlow<Map<String, Rect>>(emptyMap())
    val targets = _targets.asStateFlow()

    /**
     * Lista COMPLETA de todos los pasos del tour, en orden.
     */
    val tourSteps = listOf(
        // --- Paso 1: Home -> Bottom Bar (Tienda) ---
        TourStep(
            id = "step1_home_to_store",
            title = "Navegación Principal",
            description = "Bienvenido a Renova. Empecemos por la Tienda. Toca 'Sig.' para continuar.",
            screenRoute = TourRoutes.HOME,
            targetId = "bottom_bar_store"
        ),
        // --- Pasos 2-4: Store (Ya implementados) ---
        TourStep(
            id = "step2_store_categories",
            title = "Categorías",
            description = "Aquí puedes filtrar las tiendas por categoría. Toca 'Todas' para ver todas de nuevo.",
            screenRoute = TourRoutes.STORE_LIST,
            targetId = "store_categories"
        ),
        TourStep(
            id = "step3_store_title",
            title = "Tiendas Aliadas",
            description = "Esta es la lista de nuestras tiendas aliadas donde puedes ganar puntos.",
            screenRoute = TourRoutes.STORE_LIST,
            targetId = "store_alliances_title"
        ),
        TourStep(
            id = "step4_store_alliance",
            title = "Ver Recompensas",
            description = "Toca una tienda (o 'Sig.') para ver las recompensas exclusivas que ofrece por tus puntos.",
            screenRoute = TourRoutes.STORE_LIST,
            targetId = "store_first_alliance"
        ),
        // --- Paso 5: Store -> Bottom Bar (QR) ---
        TourStep(
            id = "step5_store_to_qr",
            title = "Tu Código QR",
            description = "¡Genial! Ahora veamos tu código QR. Este es el botón principal.",
            screenRoute = TourRoutes.STORE_LIST, // Aún estamos en Store
            targetId = "bottom_bar_qr" // Pero apuntamos al QR
        ),
        // --- Pasos 6-7: QR Screen ---
        TourStep(
            id = "step6_qr_info",
            title = "QR para Contenedor",
            description = "Usa este código QR para identificarte en los contenedores inteligentes y sumar puntos.",
            screenRoute = TourRoutes.QR,
            targetId = "qr_card" // Apunta a la tarjeta del QR
        ),
        TourStep(
            id = "step7_qr_switch",
            title = "Código de Barras",
            description = "También puedes tocar este botón para cambiar a tu código de barras. ¡Úsalo para canjear recompensas en tiendas!",
            screenRoute = TourRoutes.QR,
            targetId = "qr_switch_button" // Apunta al botón de cambiar
        ),
        // --- Paso 8: QR -> Bottom Bar (Profile) ---
        TourStep(
            id = "step8_qr_to_profile",
            title = "Tu Perfil",
            description = "Casi terminamos. Por último, echemos un vistazo a tu perfil.",
            screenRoute = TourRoutes.QR, // Aún estamos en QR
            targetId = "bottom_bar_profile" // Apuntamos a Perfil
        ),
        // --- Pasos 9-10: Profile Screen ---
        TourStep(
            id = "step9_profile_language",
            title = "Cambiar Idioma",
            description = "Aquí puedes cambiar el idioma de la aplicación.",
            screenRoute = TourRoutes.PROFILE,
            targetId = "profile_language_toggle"
        ),
        TourStep(
            id = "step10_profile_info",
            title = "Información Personal",
            description = "Y aquí puedes ver y editar tu información personal y el estado de tu verificación.",
            screenRoute = TourRoutes.PROFILE,
            targetId = "profile_info_card"
        ),
        // --- Paso 11: Profile -> Activity Screen (Navegación) ---
        TourStep(
            id = "step11_profile_to_activity",
            title = "Tu Actividad",
            description = "Finalmente, veamos tu historial de actividad. Toca 'Sig.' para ir.",
            screenRoute = TourRoutes.PROFILE, // Aún en Perfil
            targetId = "profile_info_card" // Apuntamos a lo último
            // El próximo paso navegará a 'ACTIVITY'
        ),
        // --- Pasos 12-14: Activity Screen ---
        TourStep(
            id = "step12_activity_points",
            title = "Puntos Totales",
            description = "Esta tarjeta muestra el total de puntos que has acumulado.",
            screenRoute = TourRoutes.ACTIVITY,
            targetId = "activity_points_card"
        ),
        TourStep(
            id = "step13_activity_materials",
            title = "Materiales Reciclados",
            description = "Aquí ves un resumen de cuánto plástico y aluminio has reciclado.",
            screenRoute = TourRoutes.ACTIVITY,
            targetId = "activity_materials_row"
        ),
        TourStep(
            id = "step14_activity_history",
            title = "Historial",
            description = "Y debajo encontrarás el historial detallado de todos tus movimientos. ¡Eso es todo!",
            screenRoute = TourRoutes.ACTIVITY,
            targetId = "activity_history_title"
        )
    )

    val currentStep: TourStep?
        get() = tourSteps.getOrNull(_currentStepIndex.value)

    val currentTargetRect: Rect?
        get() = currentStep?.let { _targets.value[it.targetId] }

    fun startTour() {
        _currentStepIndex.value = 0
        _isTourActive.value = true
    }

    fun nextStep() {
        if (_currentStepIndex.value < tourSteps.size - 1) {
            _currentStepIndex.value++
        } else {
            endTour()
        }
    }

    fun prevStep() {
        if (_currentStepIndex.value > 0) {
            _currentStepIndex.value--
        }
    }

    fun endTour() {
        _isTourActive.value = false
        _currentStepIndex.value = 0
    }

    fun registerTarget(id: String, coordinates: LayoutCoordinates?) {
        if (coordinates == null || !coordinates.isAttached) {
            _targets.update { it - id } // Eliminar si se desvincula
            return
        }
        val position = coordinates.positionInWindow() // Posición relativa a la ventana
        val size = coordinates.size.toSize()
        _targets.update { it + (id to Rect(position, size)) }
    }

    fun unregisterTarget(id: String) {
        _targets.update { it - id }
    }
}

/**
 * Proporciona el TourState a todo el árbol de Composables.
 */
val LocalTourState = compositionLocalOf<TourState> { error("No TourState provided") }