package com.renova.mobile.ui.tour

import androidx.annotation.StringRes
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.toSize
import com.renova.mobile.R
import com.renova.mobile.navigation.NavigationItem
import com.renova.mobile.navigation.TopNavigationItem
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
 * @param titleResId ID del recurso de string para el título.
 * @param descriptionResId ID del recurso de string para la descripción.
 * @param screenRoute La ruta de NavHost donde este paso debe mostrarse.
 * @param targetId El ID que usaremos para vincular este paso con un Composable en la UI.
 */
data class TourStep(
    val id: String,
    @StringRes val titleResId: Int, // <-- Correcto: Int
    @StringRes val descriptionResId: Int, // <-- Correcto: Int
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
     * Lista COMPLETA de todos los pasos del tour, usando R.string.
     */
    // --- CORREGIDO: Usar R.string ---
    val tourSteps = listOf(
        TourStep(
            id = "step1_home_to_store",
            titleResId = R.string.tour_title_nav_main, // <-- Usar R.string
            descriptionResId = R.string.tour_desc_nav_main, // <-- Usar R.string
            screenRoute = TourRoutes.HOME,
            targetId = "bottom_bar_store"
        ),
        TourStep(
            id = "step2_store_categories",
            titleResId = R.string.tour_title_store_categories, // <-- Usar R.string
            descriptionResId = R.string.tour_desc_store_categories, // <-- Usar R.string
            screenRoute = TourRoutes.STORE_LIST,
            targetId = "store_categories"
        ),
        TourStep(
            id = "step3_store_title",
            titleResId = R.string.tour_title_store_alliances, // <-- Usar R.string
            descriptionResId = R.string.tour_desc_store_alliances, // <-- Usar R.string
            screenRoute = TourRoutes.STORE_LIST,
            targetId = "store_alliances_title"
        ),
        TourStep(
            id = "step4_store_alliance",
            titleResId = R.string.tour_title_store_rewards, // <-- Usar R.string
            descriptionResId = R.string.tour_desc_store_rewards, // <-- Usar R.string
            screenRoute = TourRoutes.STORE_LIST,
            targetId = "store_first_alliance"
        ),
        TourStep(
            id = "step5_store_to_qr",
            titleResId = R.string.tour_title_qr_button, // <-- Usar R.string
            descriptionResId = R.string.tour_desc_qr_button, // <-- Usar R.string
            screenRoute = TourRoutes.STORE_LIST,
            targetId = "bottom_bar_qr"
        ),
        TourStep(
            id = "step6_qr_info",
            titleResId = R.string.tour_title_qr_container, // <-- Usar R.string
            descriptionResId = R.string.tour_desc_qr_container, // <-- Usar R.string
            screenRoute = TourRoutes.QR,
            targetId = "qr_card"
        ),
        TourStep(
            id = "step7_qr_switch",
            titleResId = R.string.tour_title_qr_barcode, // <-- Usar R.string
            descriptionResId = R.string.tour_desc_qr_barcode, // <-- Usar R.string
            screenRoute = TourRoutes.QR,
            targetId = "qr_switch_button"
        ),
        TourStep(
            id = "step8_qr_to_profile",
            titleResId = R.string.tour_title_profile_button, // <-- Usar R.string
            descriptionResId = R.string.tour_desc_profile_button, // <-- Usar R.string
            screenRoute = TourRoutes.QR,
            targetId = "bottom_bar_profile"
        ),
        TourStep(
            id = "step9_profile_language",
            titleResId = R.string.tour_title_profile_language, // <-- Usar R.string
            descriptionResId = R.string.tour_desc_profile_language, // <-- Usar R.string
            screenRoute = TourRoutes.PROFILE,
            targetId = "profile_language_toggle"
        ),
        TourStep(
            id = "step10_profile_info",
            titleResId = R.string.tour_title_profile_info, // <-- Usar R.string
            descriptionResId = R.string.tour_desc_profile_info, // <-- Usar R.string
            screenRoute = TourRoutes.PROFILE,
            targetId = "profile_info_card"
        ),
        TourStep(
            id = "step11_profile_to_topbar",
            titleResId = R.string.tour_title_activity_button, // <-- Usar R.string
            descriptionResId = R.string.tour_desc_activity_button, // <-- Usar R.string
            screenRoute = TourRoutes.PROFILE,
            targetId = "top_bar_activity_button"
        ),
        TourStep(
            id = "step13_activity_points",
            titleResId = R.string.tour_title_activity_points, // <-- Usar R.string
            descriptionResId = R.string.tour_desc_activity_points, // <-- Usar R.string
            screenRoute = TourRoutes.ACTIVITY,
            targetId = "activity_points_card"
        ),
        TourStep(
            id = "step14_activity_materials",
            titleResId = R.string.tour_title_activity_materials, // <-- Usar R.string
            descriptionResId = R.string.tour_desc_activity_materials, // <-- Usar R.string
            screenRoute = TourRoutes.ACTIVITY,
            targetId = "activity_materials_row"
        ),
        TourStep(
            id = "step15_activity_history",
            titleResId = R.string.tour_title_activity_history, // <-- Usar R.string
            descriptionResId = R.string.tour_desc_activity_history, // <-- Usar R.string
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
        // Limpiar targets al finalizar para evitar problemas si se reinicia rápido
        _targets.update { emptyMap() }
    }

    fun registerTarget(id: String, coordinates: LayoutCoordinates?) {
        if (coordinates == null || !coordinates.isAttached) {
            _targets.update { it - id } // Eliminar si se desvincula
            return
        }
        val position = coordinates.positionInWindow() // Posición relativa a la ventana
        val size = coordinates.size.toSize()
        // Solo actualizar si la posición o tamaño cambiaron significativamente (evita recomposiciones innecesarias)
        val existingRect = _targets.value[id]
        if (existingRect == null || existingRect.topLeft != position || existingRect.size != size) {
            _targets.update { it + (id to Rect(position, size)) }
        }
    }

    fun unregisterTarget(id: String) {
        _targets.update { it - id }
    }
}

val LocalTourState = compositionLocalOf<TourState> { error("No TourState provided") }