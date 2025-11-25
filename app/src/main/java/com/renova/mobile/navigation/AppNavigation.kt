package com.renova.mobile.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.renova.mobile.ui.screens.HomeScreen
import com.renova.mobile.ui.screens.ProfileScreen
import com.renova.mobile.ui.screens.QRScreen
import com.renova.mobile.ui.screens.StoreScreen
import com.renova.mobile.ui.screens.ActivityScreen
import com.renova.mobile.ui.screens.StreakScreen
import com.renova.mobile.ui.components.CustomBottomBar
import com.renova.mobile.ui.components.CustomBottomBarBusiness
import com.renova.mobile.ui.screens.RewardScreen
import com.renova.mobile.ui.components.CustomTopBar
import com.renova.mobile.ui.screens.business.BusinessHomeScreen
import com.renova.mobile.ui.screens.business.BusinessStoreScreen
import com.renova.mobile.ui.screens.business.BusinessQRScreen
import com.renova.mobile.ui.screens.business.BusinessProfile
import com.renova.mobile.ui.screens.business.payments.PointsCashoutScreen
import com.renova.mobile.navigation.NavigationItemBusiness
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.renova.mobile.utils.SessionManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.renova.mobile.viewmodel.BusinessSaleViewModel
import com.renova.mobile.ui.viewmodels.LanguageViewModel
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.background
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import kotlinx.coroutines.delay
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.unit.dp
import com.renova.mobile.ui.tour.LocalTourState
import com.renova.mobile.ui.tour.TourOverlay
import android.util.Log
import androidx.compose.runtime.derivedStateOf
import com.renova.mobile.ui.theme.LocalRenovaColors
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.res.painterResource
import com.renova.mobile.R
import com.renova.mobile.network.ApiClient
import com.renova.mobile.network.TourCompleteRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.runtime.DisposableEffect

object StoreGraph {
    const val ROUTE = "store_graph"
    const val STORE_LIST = "store_list"
    const val REWARDS = "reward_screen/{allianceId}"
}

@Composable
fun AppNavigation(
    sessionManager: SessionManager,
    onLogout: () -> Unit,
    languageViewModel: LanguageViewModel,
    isUpdatingLanguage: Boolean,
    pointToMxn: Double
) {
    val navController = rememberNavController()
    val businessSaleVM: BusinessSaleViewModel = viewModel()

    val user = sessionManager.getUser()
    val isBusiness = (user?.role?.id ?: 0) == 4

    var contentVisible by remember { mutableStateOf(false) }

    val tourState = LocalTourState.current
    val isTourActive by tourState.isTourActive.collectAsState()

    val currentStepIndex by tourState.currentStepIndex.collectAsState()
    val tourSteps by tourState.tourSteps.collectAsState()
    val currentStep by remember(currentStepIndex, tourSteps) {
        derivedStateOf { tourSteps.getOrNull(currentStepIndex) }
    }

    // --- LÓGICA DEL TOUR COMPLETAMENTE REDISEÑADA ---
    val currentUserId = sessionManager.getUserId()

    // Estados para controlar el flujo del tour
    var hasCheckedTour by remember { mutableStateOf(false) }
    var shouldStartTour by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current.applicationContext

    // NUEVO: Efecto para verificar el estado del tour SOLO UNA VEZ por sesión
    LaunchedEffect(currentUserId, contentVisible) {
        if (currentUserId != null && contentVisible && !hasCheckedTour && !isTourActive) {
            hasCheckedTour = true

            // Forzar limpieza de verificación previa para asegurar chequeo fresco
            sessionManager.clearTourVerification()

            // Verificar el estado real del tour
            val isFirstLogin = sessionManager.isFirstLogin()

            Log.d("AppNavigation", "🔍 Verificación INICIAL de tour - userId: $currentUserId, isFirstLogin: $isFirstLogin")

            if (isFirstLogin) {
                Log.d("AppNavigation", "🎯 Usuario $currentUserId necesita tour (primer login)")
                shouldStartTour = true
            } else {
                Log.d("AppNavigation", "✅ Usuario $currentUserId YA completó el tour anteriormente")
                shouldStartTour = false
            }
        }
    }

    // NUEVO: Efecto separado para iniciar el tour después de la verificación
    LaunchedEffect(shouldStartTour, isTourActive) {
        if (shouldStartTour && !isTourActive && currentUserId != null) {
            Log.d("AppNavigation", "🚀 Iniciando tour para userId=$currentUserId")
            delay(800) // Esperar a que la UI esté completamente cargada
            tourState.startTour()
            shouldStartTour = false // Prevenir múltiples inicios
        }
    }

    // NUEVO: Efecto para manejar la finalización del tour
    LaunchedEffect(isTourActive) {
        if (!isTourActive && currentUserId != null) {
            // Verificar si acabamos de completar un tour
            val wasFirstLogin = sessionManager.isFirstLogin()

            if (wasFirstLogin) {
                Log.d("AppNavigation", "🏁 Tour COMPLETADO para userId=$currentUserId")

                // NUEVO: Actualizar inmediatamente el estado local
                sessionManager.setFirstLoginComplete()

                // Llamar a la API para sincronizar
                scope.launch(Dispatchers.IO) {
                    try {
                        ApiClient.init(context)
                        val request = TourCompleteRequest(user_id = currentUserId)
                        val response = ApiClient.apiService.completeTour(currentUserId, request)
                        if (response.isSuccessful && response.body()?.success == true) {
                            Log.i("AppNavigation", "✅ API tourComplete exitosa para userId=$currentUserId")
                            // Forzar nueva verificación en el próximo inicio
                            sessionManager.clearTourVerification()
                        } else {
                            Log.w("AppNavigation", "⚠️ API tourComplete falló: ${response.code()} - ${response.errorBody()?.string()}")
                        }
                    } catch (e: Exception) {
                        Log.e("AppNavigation", "❌ Excepción al llamar API tourComplete", e)
                    }
                }
            }
        }
    }

    // Efecto para mostrar contenido
    LaunchedEffect(Unit) {
        delay(300)
        contentVisible = true
        Log.d("AppNavigation", "📱 Contenido visible: true")
    }
    // --- FIN LÓGICA DEL TOUR ---

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val renovaColors = LocalRenovaColors.current

    val currentRouteHasSteps by remember(currentRoute, tourState.screenSpecificTourSteps) {
        derivedStateOf {
            currentRoute?.let { route ->
                tourState.screenSpecificTourSteps.containsKey(route)
            } ?: false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                CustomTopBar(
                    navController = navController,
                    isLoading = isUpdatingLanguage
                )
            },
            bottomBar = {
                if (isBusiness) {
                    CustomBottomBarBusiness(
                        navController = navController,
                        onLogout = onLogout
                    )
                } else {
                    CustomBottomBar(
                        navController = navController,
                        onLogout = onLogout
                    )
                }
            },
        ) { innerPadding ->
            AnimatedVisibility(
                visible = contentVisible,
                enter = fadeIn(animationSpec = tween(durationMillis = 500)),
                exit = fadeOut(animationSpec = tween(durationMillis = 100))
            ) {
                NavHost(
                    navController = navController,
                    startDestination = if (isBusiness) NavigationItemBusiness.Home.route else NavigationItem.Home.route,
                    modifier = Modifier.padding(innerPadding)
                ) {
                    // ... (Todas las rutas composable se mantienen igual) ...
                    val enterAnimation = slideInHorizontally(
                        initialOffsetX = { 1000 },
                        animationSpec = tween(400)
                    ) + fadeIn(animationSpec = tween(400))
                    val exitAnimation = slideOutHorizontally(
                        targetOffsetX = { -1000 },
                        animationSpec = tween(200)
                    ) + fadeOut(animationSpec = tween(200))
                    val popEnterAnimation = slideInHorizontally(
                        initialOffsetX = { -1000 },
                        animationSpec = tween(400)
                    ) + fadeIn(animationSpec = tween(400))
                    val popExitAnimation = slideOutHorizontally(
                        targetOffsetX = { 1000 },
                        animationSpec = tween(200)
                    ) + fadeOut(animationSpec = tween(200))

                    // Rutas de negocio
                    composable(
                        route = NavigationItemBusiness.Home.route,
                        enterTransition = { enterAnimation },
                        exitTransition = { exitAnimation },
                        popEnterTransition = { popEnterAnimation },
                        popExitTransition = { popExitAnimation }
                    ) {
                        BusinessHomeScreen(
                            onLogout = onLogout,
                            onNavigateToProfile = {
                                navController.navigate("business/profile")
                            },
                            vm = businessSaleVM,
                            onNavigateToCashout = {
                                navController.navigate("business/cashout")
                            },
                            pointToMxn = pointToMxn
                        )
                    }

                    composable(
                        route = NavigationItemBusiness.Store.route,
                        enterTransition = { enterAnimation },
                        exitTransition = { exitAnimation },
                        popEnterTransition = { popEnterAnimation },
                        popExitTransition = { popExitAnimation }
                    ) {
                        BusinessStoreScreen(
                            onLogout = onLogout,
                            vm = businessSaleVM,
                            navController = navController
                        )
                    }

                    composable(
                        route = NavigationItemBusiness.QR.route,
                        enterTransition = { enterAnimation },
                        exitTransition = { exitAnimation },
                        popEnterTransition = { popEnterAnimation },
                        popExitTransition = { popExitAnimation }
                    ) {
                        BusinessQRScreen(
                            onLogout = onLogout,
                            vm = businessSaleVM,
                            navController = navController
                        )
                    }

                    composable(
                        route = "business/profile",
                        enterTransition = { enterAnimation },
                        exitTransition = { exitAnimation },
                        popEnterTransition = { popEnterAnimation },
                        popExitTransition = { popExitAnimation }
                    ) {
                        BusinessProfile(
                            languageViewModel = languageViewModel,
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }

                    composable(
                        route = "business/cashout",
                        enterTransition = { enterAnimation },
                        exitTransition = { exitAnimation },
                        popEnterTransition = { popEnterAnimation },
                        popExitTransition = { popExitAnimation }
                    ) {
                        PointsCashoutScreen(
                            onLogout = onLogout,
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }

                    // Rutas de usuario normal
                    composable(
                        route = NavigationItem.Home.route,
                        enterTransition = { enterAnimation },
                        exitTransition = { exitAnimation },
                        popEnterTransition = { popEnterAnimation },
                        popExitTransition = { popExitAnimation }
                    ) {
                        HomeScreen()
                    }

                    navigation(
                        startDestination = StoreGraph.STORE_LIST,
                        route = StoreGraph.ROUTE
                    ) {
                        composable(
                            route = StoreGraph.STORE_LIST,
                            enterTransition = { enterAnimation },
                            exitTransition = { exitAnimation },
                            popEnterTransition = { popEnterAnimation },
                            popExitTransition = { popExitAnimation }
                        ) {
                            StoreScreen(navController = navController)
                        }

                        composable(
                            route = StoreGraph.REWARDS,
                            arguments = listOf(navArgument("allianceId") {
                                type = NavType.IntType
                            }),
                            enterTransition = { enterAnimation },
                            exitTransition = { exitAnimation },
                            popEnterTransition = { popEnterAnimation },
                            popExitTransition = { popExitAnimation }
                        ) { backStackEntry ->
                            val allianceId = backStackEntry.arguments?.getInt("allianceId") ?: 0
                            RewardScreen(navController = navController, allianceId = allianceId)
                        }
                    }

                    composable(
                        route = NavigationItem.QR.route,
                        enterTransition = { enterAnimation },
                        exitTransition = { exitAnimation },
                        popEnterTransition = { popEnterAnimation },
                        popExitTransition = { popExitAnimation }
                    ) {
                        QRScreen()
                    }

                    composable(
                        route = NavigationItem.Profile.route,
                        enterTransition = { enterAnimation },
                        exitTransition = { exitAnimation },
                        popEnterTransition = { popEnterAnimation },
                        popExitTransition = { popExitAnimation }
                    ) {
                        ProfileScreen(languageViewModel = languageViewModel)
                    }

                    composable(
                        route = TopNavigationItem.Activity.route,
                        enterTransition = { enterAnimation },
                        exitTransition = { exitAnimation },
                        popEnterTransition = { popEnterAnimation },
                        popExitTransition = { popExitAnimation }
                    ) {
                        ActivityScreen()
                    }

                    composable(
                        route = TopNavigationItem.Streak.route,
                        enterTransition = { enterAnimation },
                        exitTransition = { exitAnimation },
                        popEnterTransition = { popEnterAnimation },
                        popExitTransition = { popExitAnimation }
                    ) {
                        StreakScreen()
                    }
                }
            }

            // Loading inicial
            if (!contentVisible) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        // Overlay de loading para cambio de idioma
        AnimatedVisibility(
            visible = isUpdatingLanguage,
            enter = fadeIn(animationSpec = tween(durationMillis = 300)),
            exit = fadeOut(animationSpec = tween(durationMillis = 300))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }

        // Lógica de visibilidad del FAB
        val isStepOnCorrectScreen = currentStep?.screenRoute == currentRoute
        val isFabStep = isTourActive && currentStep?.targetId == "help_fab" && isStepOnCorrectScreen
        val shouldShowFab = (!isTourActive && currentRouteHasSteps) || isFabStep

        // Overlay del Tour
        if (isTourActive) {
            TourOverlay(
                tourState = tourState,
                currentScreenRoute = currentRoute,
                onNavigate = { route ->
                    if (currentRoute != route) {
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }

        // Botón flotante
        if (shouldShowFab) {
            FloatingActionButton(
                onClick = {
                    if (!isTourActive) {
                        currentRoute?.let { route ->
                            tourState.startTourForScreen(route)
                        }
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .padding(bottom = 80.dp)
                    .onGloballyPositioned { coords ->
                        tourState.registerTarget("help_fab", coords, null)
                    },
                containerColor = renovaColors.primaryColor,
                contentColor = Color.White
            ) {
                Icon(painter = painterResource(id = R.drawable.help), contentDescription = "Iniciar tour")
            }
        }

        // Registra/desregistra el target del FAB
        DisposableEffect("help_fab") {
            onDispose { tourState.unregisterTarget("help_fab") }
        }
    }
}