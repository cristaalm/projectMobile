package com.renova.mobile.ui.tour

import android.widget.Toast
import androidx.annotation.StringRes
// --- INICIO MODIFICACIÓN: Imports añadidos ---
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
// --- FIN MODIFICACIÓN ---
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
import com.renova.mobile.utils.SessionManager
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
    val STREAK = TopNavigationItem.Streak.route
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

/**
 * Contiene la información de un elemento UI registrado para el tour,
 * incluyendo su rectángulo (Rect) y el estado de scroll (ScrollState o LazyListState) de su contenedor.
 */
data class TargetInfo(
    val rect: Rect,
    val scrollState: ScrollState? = null,
    val lazyListState: LazyListState? = null,
    val itemIndex: Int? = null
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
class TourState(private val sessionManager: SessionManager) {
    private val _isTourActive = MutableStateFlow(false)
    val isTourActive = _isTourActive.asStateFlow()

    private val _currentStepIndex = MutableStateFlow(0)
    val currentStepIndex = _currentStepIndex.asStateFlow()

    private val _targets = MutableStateFlow<Map<String, TargetInfo>>(emptyMap())
    val targets = _targets.asStateFlow()

    private val _tourSteps = MutableStateFlow<List<TourStep>>(emptyList())
    val tourSteps = _tourSteps.asStateFlow()

    // Lista EXCLUSIVA para el tour completo de primera vez
    private val fullTourSteps: List<TourStep> = listOf(
        // 1. BIENVENIDA
        TourStep(
            id = "step_welcome",
            titleResId = R.string.tour_title_welcome,
            descriptionResId = R.string.tour_desc_welcome,
            screenRoute = TourRoutes.HOME,
            targetId = "welcome_dummy",
            isWelcomeStep = true
        ),
        // 2. HOME - PUNTOS
        TourStep(
            id = "step_home_points",
            titleResId = R.string.tour_title_home_points,
            descriptionResId = R.string.tour_desc_home_points,
            screenRoute = TourRoutes.HOME,
            targetId = "home_points_card"
        ),
        // 3. HOME - ACTIVIDAD
        TourStep(
            id = "step_home_activity",
            titleResId = R.string.tour_title_home_activity,
            descriptionResId = R.string.tour_desc_home_activity,
            screenRoute = TourRoutes.HOME,
            targetId = "home_recent_activity"
        ),
        // 4. HOME - LOGROS
        TourStep(
            id = "step_home_achievements",
            titleResId = R.string.tour_title_home_achievements,
            descriptionResId = R.string.tour_desc_home_achievements,
            screenRoute = TourRoutes.HOME,
            targetId = "home_achievements_section"
        ),
        // 5. NAV (HOME -> TIENDA)
        TourStep(
            id = "step1_home_to_store",
            titleResId = R.string.tour_title_nav_main,
            descriptionResId = R.string.tour_desc_nav_main,
            screenRoute = TourRoutes.HOME,
            targetId = "bottom_bar_store"
        ),
        // 6. TIENDA - CATEGORÍAS
        TourStep(
            id = "step2_store_categories",
            titleResId = R.string.tour_title_store_categories,
            descriptionResId = R.string.tour_desc_store_categories,
            screenRoute = TourRoutes.STORE_LIST,
            targetId = "store_categories"
        ),
        // --- INICIO DE LA MODIFICACIÓN (Paso 7) ---
        // 7. TIENDA - SECCIÓN DE ALIANZAS (TÍTULO + LISTA)
        TourStep(
            id = "step3_store_alliances", // ID de paso (puede ser el mismo)
            titleResId = R.string.tour_title_store_alliances,
            descriptionResId = R.string.tour_desc_store_alliances,
            screenRoute = TourRoutes.STORE_LIST,
            targetId = "store_alliances_section" // <-- ID DEL TARGET CORREGIDO
        ),
        // --- FIN DE LA MODIFICACIÓN ---
        // 8. TIENDA - ALIANZA (PRIMERA)
        TourStep(
            id = "step4_store_alliance",
            titleResId = R.string.tour_title_store_rewards,
            descriptionResId = R.string.tour_desc_store_rewards,
            screenRoute = TourRoutes.STORE_LIST,
            targetId = "store_first_alliance"
        ),
        // 9. NAV (TIENDA -> QR)
        TourStep(
            id = "step5_store_to_qr",
            titleResId = R.string.tour_title_qr_button,
            descriptionResId = R.string.tour_desc_qr_button,
            screenRoute = TourRoutes.STORE_LIST,
            targetId = "bottom_bar_qr"
        ),
        // 10. QR - INFO
        TourStep(
            id = "step6_qr_info",
            titleResId = R.string.tour_title_qr_container,
            descriptionResId = R.string.tour_desc_qr_container,
            screenRoute = TourRoutes.QR,
            targetId = "qr_card"
        ),
        // 11. QR - SWITCH
        TourStep(
            id = "step7_qr_switch",
            titleResId = R.string.tour_title_qr_barcode,
            descriptionResId = R.string.tour_desc_qr_barcode,
            screenRoute = TourRoutes.QR,
            targetId = "qr_switch_button"
        ),
        // 12. NAV (QR -> PERFIL)
        TourStep(
            id = "step8_qr_to_profile",
            titleResId = R.string.tour_title_profile_button,
            descriptionResId = R.string.tour_desc_profile_button,
            screenRoute = TourRoutes.QR,
            targetId = "bottom_bar_profile"
        ),
        // 13. PERFIL - IDIOMA
        TourStep(
            id = "step9_profile_language",
            titleResId = R.string.tour_title_profile_language,
            descriptionResId = R.string.tour_desc_profile_language,
            screenRoute = TourRoutes.PROFILE,
            targetId = "profile_language_toggle"
        ),
        // 14. PERFIL - ESTADO DE VERIFICACIÓN
        TourStep(
            id = "step_profile_verification",
            titleResId = R.string.tour_title_profile_verification,
            descriptionResId = R.string.tour_desc_profile_verification,
            screenRoute = TourRoutes.PROFILE,
            targetId = "profile_verification_banner"
        ),
        // 15. PERFIL - INFO PERSONAL
        TourStep(
            id = "step10_profile_info",
            titleResId = R.string.tour_title_profile_info,
            descriptionResId = R.string.tour_desc_profile_info,
            screenRoute = TourRoutes.PROFILE,
            targetId = "profile_info_card"
        ),
        // 16. PERFIL - SEGURIDAD (NUEVO)
        TourStep(
            id = "step_profile_security",
            titleResId = R.string.tour_title_profile_security,
            descriptionResId = R.string.tour_desc_profile_security,
            screenRoute = TourRoutes.PROFILE,
            targetId = "profile_security_card"
        ),
        // 17. PERFIL - DOCUMENTOS
        TourStep(
            id = "step_profile_documents",
            titleResId = R.string.tour_title_profile_documents,
            descriptionResId = R.string.tour_desc_profile_documents,
            screenRoute = TourRoutes.PROFILE,
            targetId = "profile_documents_section"
        ),
        // 18. NAV (PERFIL -> ACTIVIDAD)
        TourStep(
            id = "step11_profile_to_topbar",
            titleResId = R.string.tour_title_activity_button,
            descriptionResId = R.string.tour_desc_activity_button,
            screenRoute = TourRoutes.PROFILE,
            targetId = "top_bar_activity_button"
        ),
        // 19. ACTIVIDAD - PUNTOS
        TourStep(
            id = "step13_activity_points",
            titleResId = R.string.tour_title_activity_points,
            descriptionResId = R.string.tour_desc_activity_points,
            screenRoute = TourRoutes.ACTIVITY,
            targetId = "activity_points_card"
        ),
        // 20. ACTIVIDAD - MATERIALES
        TourStep(
            id = "step14_activity_materials",
            titleResId = R.string.tour_title_activity_materials,
            descriptionResId = R.string.tour_desc_activity_materials,
            screenRoute = TourRoutes.ACTIVITY,
            targetId = "activity_materials_row"
        ),
        // 21. ACTIVIDAD - HISTORIAL
        TourStep(
            id = "step15_activity_history",
            titleResId = R.string.tour_title_activity_history,
            descriptionResId = R.string.tour_desc_activity_history,
            screenRoute = TourRoutes.ACTIVITY,
            targetId = "activity_history_title"
        ),
        // 22. NAV (ACTIVIDAD -> RACHA)
        TourStep(
            id = "step_activity_to_streak",
            titleResId = R.string.tour_title_streak_button,
            descriptionResId = R.string.tour_desc_streak_button,
            screenRoute = TourRoutes.ACTIVITY,
            targetId = "top_bar_streak_button"
        ),
        // 23. RACHA - TARJETA
        TourStep(
            id = "step_streak_card",
            titleResId = R.string.tour_title_streak_card,
            descriptionResId = R.string.tour_desc_streak_card,
            screenRoute = TourRoutes.STREAK,
            targetId = "streak_card_main"
        ),
        // --- INICIO MODIFICACIÓN: Paso eliminado porque el componente no existe ---
        // 24. RACHA - RETO
        /*
        TourStep(
            id = "step_streak_challenge",
            titleResId = R.string.tour_title_streak_challenge,
            descriptionResId = R.string.tour_desc_streak_challenge,
            screenRoute = TourRoutes.STREAK,
            targetId = "streak_weekly_challenge"
        ),
        */
        // --- FIN MODIFICACIÓN ---
        // 25. RACHA - INSIGNIAS
        TourStep(
            id = "step_streak_badges",
            titleResId = R.string.tour_title_streak_badges,
            descriptionResId = R.string.tour_desc_streak_badges,
            screenRoute = TourRoutes.STREAK,
            targetId = "streak_monthly_badges"
        ),
        // 26. RACHA - PROGRESO
        TourStep(
            id = "step_streak_progress",
            titleResId = R.string.tour_title_streak_progress,
            descriptionResId = R.string.tour_desc_streak_progress,
            screenRoute = TourRoutes.STREAK,
            targetId = "streak_weekly_progress"
        ),
        TourStep(
            id = "step16_menu_button",
            titleResId = R.string.tour_title_menu_button,
            descriptionResId = R.string.tour_desc_menu_button,
            screenRoute = TourRoutes.STREAK,
            targetId = "bottom_bar_menu"
        ),
        TourStep(
            id = "step17_full_tour_help",
            titleResId = R.string.tour_title_help_fab,
            descriptionResId = R.string.tour_desc_help_fab_full,
            screenRoute = TourRoutes.STREAK,
            targetId = "help_fab"
        )
    )

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
            // --- INICIO DE LA MODIFICACIÓN (Tour específico de pantalla) ---
            TourStep(
                id = "step3_store_alliances",
                titleResId = R.string.tour_title_store_alliances,
                descriptionResId = R.string.tour_desc_store_alliances,
                screenRoute = TourRoutes.STORE_LIST,
                targetId = "store_alliances_section" // <-- ID DEL TARGET CORREGIDO
            ),
            // --- FIN DE LA MODIFICACIÓN ---
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
            // 1. Botón de Idioma
            TourStep(
                id = "step9_profile_language",
                titleResId = R.string.tour_title_profile_language,
                descriptionResId = R.string.tour_desc_profile_language,
                screenRoute = TourRoutes.PROFILE,
                targetId = "profile_language_toggle"
            ),
            // 2. Banner de Verificación
            TourStep(
                id = "step_profile_verification",
                titleResId = R.string.tour_title_profile_verification,
                descriptionResId = R.string.tour_desc_profile_verification,
                screenRoute = TourRoutes.PROFILE,
                targetId = "profile_verification_banner"
            ),
            // 3. Tarjeta de Info Personal
            TourStep(
                id = "step10_profile_info",
                titleResId = R.string.tour_title_profile_info,
                descriptionResId = R.string.tour_desc_profile_info,
                screenRoute = TourRoutes.PROFILE,
                targetId = "profile_info_card"
            ),
            // 4. Sección de Seguridad (NUEVO)
            TourStep(
                id = "step_profile_security",
                titleResId = R.string.tour_title_profile_security,
                descriptionResId = R.string.tour_desc_profile_security,
                screenRoute = TourRoutes.PROFILE,
                targetId = "profile_security_card"
            ),
            // 5. Sección de Documentos
            TourStep(
                id = "step_profile_documents",
                titleResId = R.string.tour_title_profile_documents,
                descriptionResId = R.string.tour_desc_profile_documents,
                screenRoute = TourRoutes.PROFILE,
                targetId = "profile_documents_section"
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
            )
        ),
        TourRoutes.STREAK to listOf(
            TourStep(
                id = "step_streak_card",
                titleResId = R.string.tour_title_streak_card,
                descriptionResId = R.string.tour_desc_streak_card,
                screenRoute = TourRoutes.STREAK,
                targetId = "streak_card_main"
            ),
            // --- INICIO MODIFICACIÓN: Paso eliminado porque el componente no existe ---
            /*
            TourStep(
                id = "step_streak_challenge",
                titleResId = R.string.tour_title_streak_challenge,
                descriptionResId = R.string.tour_desc_streak_challenge,
                screenRoute = TourRoutes.STREAK,
                targetId = "streak_weekly_challenge"
            ),
            */
            // --- FIN MODIFICACIÓN ---
            TourStep(
                id = "step_streak_badges",
                titleResId = R.string.tour_title_streak_badges,
                descriptionResId = R.string.tour_desc_streak_badges,
                screenRoute = TourRoutes.STREAK,
                targetId = "streak_monthly_badges"
            ),
            TourStep(
                id = "step_streak_progress",
                titleResId = R.string.tour_title_streak_progress,
                descriptionResId = R.string.tour_desc_streak_progress,
                screenRoute = TourRoutes.STREAK,
                targetId = "streak_weekly_progress"
            )
        )
    )

    private fun isCitizen(): Boolean {
        val user = sessionManager.getUser()
        val userRoleId = user?.role?.id ?: 0
        val isBusiness = (userRoleId == 4)
        return !isBusiness
    }

    val currentStep: TourStep?
        get() = _tourSteps.value.getOrNull(_currentStepIndex.value)

    val currentTargetInfo: TargetInfo?
        get() = currentStep?.let { _targets.value[it.targetId] }

    val currentTargetRect: Rect?
        get() = currentTargetInfo?.rect

    fun startTour() {
        if (!isCitizen()) return

        _tourSteps.value = fullTourSteps
        _currentStepIndex.value = 0
        _isTourActive.value = true
    }

    fun startTourForScreen(currentScreenRoute: String) {
        if (!isCitizen()) return

        val stepsForScreen = screenSpecificTourSteps[currentScreenRoute]

        if (!stepsForScreen.isNullOrEmpty()) {
            _tourSteps.value = stepsForScreen
            _currentStepIndex.value = 0
            _isTourActive.value = true
        } else {
            println("TourState: No hay pasos definidos para la ruta $currentScreenRoute")
            endTour()
        }
    }

    fun nextStep() {
        if (!_isTourActive.value) return

        if (_currentStepIndex.value < _tourSteps.value.size - 1) {
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
        _tourSteps.value = emptyList()
        _targets.update { emptyMap() }
    }

    fun isFirstStepOfTour(): Boolean {
        if (!_isTourActive.value) return true
        return _currentStepIndex.value == 0
    }

    fun isLastStepOfTour(): Boolean {
        if (!_isTourActive.value) return true
        return _currentStepIndex.value == _tourSteps.value.size - 1
    }

    fun registerTarget(
        id: String,
        coordinates: LayoutCoordinates?,
        scrollState: ScrollState? = null,
        lazyListState: LazyListState? = null,
        itemIndex: Int? = null
    ) {
        if (coordinates == null || !coordinates.isAttached) {
            _targets.update { it - id }
            return
        }
        val position = coordinates.positionInWindow()
        val size = coordinates.size.toSize()
        val newRect = Rect(position, size)

        // Crea el TargetInfo con los estados de scroll correspondientes
        val newTargetInfo = TargetInfo(
            rect = newRect,
            scrollState = scrollState,
            lazyListState = lazyListState,
            itemIndex = itemIndex
        )

        val existingInfo = _targets.value[id]

        // Solo actualiza si la información es realmente nueva
        if (existingInfo != newTargetInfo) {
            _targets.update { currentTargets ->
                currentTargets + (id to newTargetInfo)
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