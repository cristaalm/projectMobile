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
    val STORE_GRAPH = "store_graph"
    val STORE_LIST = "store_list"
    val REWARDS = "reward_screen/{allianceId}"
    val QR = NavigationItem.QR.route
    val PROFILE = NavigationItem.Profile.route
    val ACTIVITY = TopNavigationItem.Activity.route
}

/**
 * Define la estructura de un solo paso en el tour.
 */
data class TourStep(
    val id: String,
    @StringRes val titleResId: Int,
    @StringRes val descriptionResId: Int,
    val screenRoute: String,
    val targetId: String,
    val isWelcomeStep: Boolean = false
)

@Composable
fun HandleTourOnError(
    error: String?,
    isTourActiveFlow: StateFlow<Boolean>,
    endTour: () -> Unit
) {
    val isTourActive by isTourActiveFlow.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(error, isTourActive) {
        if (isTourActive && error != null) {
            val isAuthError = error.contains("401", ignoreCase = true) ||
                    error.contains("Unauthorized", ignoreCase = true) ||
                    error.contains("autenticación", ignoreCase = true) ||
                    error.contains("sesión", ignoreCase = true) ||
                    error.contains("token", ignoreCase = true)

            if (isAuthError) {
                endTour()
                Toast.makeText(context, "Tour finalizado por error de sesión", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

/**
 * Clase principal que gestiona el estado y la lógica del tour guiado.
 */
class TourState {
    private val _isTourActive = MutableStateFlow(false)
    val isTourActive = _isTourActive.asStateFlow()

    private val _currentStepIndex = MutableStateFlow(0)
    val currentStepIndex = _currentStepIndex.asStateFlow()

    private val _targets = MutableStateFlow<Map<String, Rect>>(emptyMap())
    val targets = _targets.asStateFlow()

    // --- INICIO DE MODIFICACIÓN: Lógica de listas separadas ---

    // Esta lista contendrá los pasos del tour *actualmente en curso*
    private val _tourSteps = MutableStateFlow<List<TourStep>>(emptyList())
    val tourSteps = _tourSteps.asStateFlow()

    // Lista EXCLUSIVA para el tour completo de primera vez
    private val fullTourSteps: List<TourStep> = listOf(
        TourStep(
            id = "step_welcome",
            titleResId = R.string.tour_title_welcome,
            descriptionResId = R.string.tour_desc_welcome,
            screenRoute = TourRoutes.HOME,
            targetId = "welcome_dummy",
            isWelcomeStep = true
        ),
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
            targetId = "bottom_bar_store"
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
            targetId = "bottom_bar_qr"
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
            targetId = "bottom_bar_profile"
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
            targetId = "top_bar_activity_button"
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
        ),
        TourStep(
            id = "step16_menu_button",
            titleResId = R.string.tour_title_menu_button,
            descriptionResId = R.string.tour_desc_menu_button,
            screenRoute = TourRoutes.ACTIVITY,
            targetId = "bottom_bar_menu"
        ),
        // Último paso del tour COMPLETO
        TourStep(
            id = "step17_full_tour_help",
            titleResId = R.string.tour_title_help_fab,
            descriptionResId = R.string.tour_desc_help_fab_full,
            screenRoute = TourRoutes.ACTIVITY,
            targetId = "help_fab"
        )
        // NOTA: "step_home_help_fab" NO está en esta lista
    )

    // Mapa de listas EXCLUSIVAS para tours por pantalla (manuales)
    val screenSpecificTourSteps: Map<String, List<TourStep>> = mapOf(
        TourRoutes.HOME to listOf(
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
            // Último paso del tour de HOME
            TourStep(
                id = "step_home_help_fab",
                titleResId = R.string.tour_title_help_fab,
                descriptionResId = R.string.tour_desc_help_fab_home,
                screenRoute = TourRoutes.HOME,
                targetId = "help_fab"
            )
        ),
        TourRoutes.STORE_LIST to listOf(
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
            )
        ),
        TourRoutes.QR to listOf(
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
            )
        ),
        TourRoutes.PROFILE to listOf(
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
            )
        ),
        TourRoutes.ACTIVITY to listOf(
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
            ),
            TourStep(
                id = "step16_menu_button",
                titleResId = R.string.tour_title_menu_button,
                descriptionResId = R.string.tour_desc_menu_button,
                screenRoute = TourRoutes.ACTIVITY,
                targetId = "bottom_bar_menu"
            )
        )
    )

    // --- FIN DE MODIFICACIÓN ---

    val currentStep: TourStep?
        get() = _tourSteps.value.getOrNull(_currentStepIndex.value) // Modificado

    val currentTargetRect: Rect?
        get() = currentStep?.let { _targets.value[it.targetId] }

    /**
     * Inicia el tour completo desde el primer paso (incluye bienvenida).
     */
    fun startTour() {
        _tourSteps.value = fullTourSteps // Modificado
        _currentStepIndex.value = 0
        _isTourActive.value = true
    }

    /**
     * MODIFICADO: Inicia tour de pantalla específica
     */
    fun startTourForScreen(currentScreenRoute: String) {
        val stepsForScreen = screenSpecificTourSteps[currentScreenRoute] // Modificado

        if (!stepsForScreen.isNullOrEmpty()) {
            _tourSteps.value = stepsForScreen // Modificado
            _currentStepIndex.value = 0
            _isTourActive.value = true
        } else {
            println("TourState: No hay pasos definidos para la ruta $currentScreenRoute")
            endTour()
        }
    }

    fun nextStep() {
        if (!_isTourActive.value) return

        if (_currentStepIndex.value < _tourSteps.value.size - 1) { // Modificado
            _currentStepIndex.value++
        } else {
            endTour()
        }
    }

    fun prevStep() {
        if (!_isTourActive.value) return

        if (_currentStepIndex.value > 0) {
            _currentStepIndex.value--
        }
    }

    fun endTour() {
        _isTourActive.value = false
        _currentStepIndex.value = 0
        _tourSteps.value = emptyList() // Modificado
        _targets.update { emptyMap() }
    }

    fun isFirstStepOfTour(): Boolean {
        if (!_isTourActive.value) return true
        return _currentStepIndex.value == 0
    }

    fun isLastStepOfTour(): Boolean {
        if (!_isTourActive.value) return true
        return _currentStepIndex.value == _tourSteps.value.size - 1 // Modificado
    }

    fun registerTarget(id: String, coordinates: LayoutCoordinates?) {
        if (coordinates == null || !coordinates.isAttached) {
            _targets.update { it - id }
            return
        }
        val position = coordinates.positionInWindow()
        val size = coordinates.size.toSize()
        val existingRect = _targets.value[id]

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
    error("No TourState provided")
}