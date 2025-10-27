package com.renova.mobile.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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

object StoreGraph {
    const val ROUTE = "store_graph"
    const val STORE_LIST = "store_list"
    const val REWARDS = "reward_screen/{allianceId}"
}

/**
 * Helper para recordar el valor anterior de un estado en Compose.
 */
@Composable
private fun <T> rememberPrevious(current: T): T? {
    val ref = remember { mutableStateOf<T?>(null) }
    SideEffect {
        ref.value = current
    }
    return ref.value
}

@Composable
fun AppNavigation(
    sessionManager: SessionManager,
    onLogout: () -> Unit,
    languageViewModel: LanguageViewModel,
    isUpdatingLanguage: Boolean
) {
    val navController = rememberNavController()
    val businessSaleVM: BusinessSaleViewModel = viewModel()

    val user = sessionManager.getUser()
    val isBusiness = (user?.role?.id ?: 0) == 4

    var contentVisible by remember { mutableStateOf(false) }

    val tourState = LocalTourState.current
    val isTourActive by tourState.isTourActive.collectAsState()

    // --- LÓGICA DEL TOUR MEJORADA ---

    // 1. Obtenemos el userId actual (cambia cuando cambia de usuario)
    val currentUserId = sessionManager.getUserId()

    // 2. Verificamos si ESTE usuario específico ya completó el tour
    val isFirstLoginForCurrentUser = remember(currentUserId) {
        sessionManager.isFirstLogin()
    }

    // 3. Scope y context para la llamada a la API
    val scope = rememberCoroutineScope()
    val context = LocalContext.current.applicationContext

    // 4. Estado para trackear si ya llamamos a la API (evitar llamadas duplicadas)
    var apiCallMade by remember { mutableStateOf(false) }

    // 5. Detectar cuando el tour termina
    val previousIsTourActive = rememberPrevious(isTourActive)

    LaunchedEffect(isTourActive, currentUserId) {
        // Detectar transición TRUE -> FALSE (tour terminado)
        if (previousIsTourActive == true && !isTourActive && !apiCallMade) {

            // Verificar que sea primera vez para ESTE usuario
            if (isFirstLoginForCurrentUser && currentUserId != null) {
                Log.d("AppNavigation", "Tour finalizado para userId=$currentUserId (primera vez). Llamando API...")

                apiCallMade = true // Marcar que ya llamamos a la API

                // A. Marcar como completo LOCALMENTE primero (crítico)
                sessionManager.setFirstLoginComplete()

                // B. Llamar a la API
                scope.launch(Dispatchers.IO) {
                    try {
                        ApiClient.init(context)
                        val request = TourCompleteRequest(user_id = currentUserId)
                        val response = ApiClient.apiService.completeTour(currentUserId, request)

                        if (response.isSuccessful && response.body()?.success == true) {
                            Log.i("AppNavigation", "✅ API tourComplete exitosa para userId=$currentUserId")
                        } else {
                            Log.w("AppNavigation", "⚠️ API tourComplete falló: ${response.code()} - ${response.errorBody()?.string()}")
                        }
                    } catch (e: Exception) {
                        Log.e("AppNavigation", "❌ Excepción al llamar API tourComplete", e)
                    }
                }
            } else {
                Log.d("AppNavigation", "Tour finalizado pero no era primera vez o userId es nulo. No se llama a API.")
            }
        }
    }

    // 6. Iniciar el tour automáticamente si es primera vez
    LaunchedEffect(currentUserId, isFirstLoginForCurrentUser) {
        contentVisible = true

        // Resetear el flag de API cuando cambia el usuario
        apiCallMade = false

        if (isFirstLoginForCurrentUser && !isTourActive) {
            Log.d("AppNavigation", "🎯 Primer login detectado para userId=$currentUserId. Iniciando tour...")
            delay(500)
            tourState.startTour()
        } else {
            Log.d("AppNavigation", "No es primer login para userId=$currentUserId o tour ya activo.")
        }
    }

    // --- FIN LÓGICA DEL TOUR ---

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val renovaColors = LocalRenovaColors.current

    val currentRouteHasSteps by remember(currentRoute, tourState.tourSteps) {
        derivedStateOf {
            currentRoute?.let { route ->
                tourState.tourSteps.any { step -> step.screenRoute == route }
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
            }
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
                            }
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

                    // Nueva ruta: Perfil del comercio
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
                        PointsCashoutScreen(onLogout = onLogout)
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

        // Botón flotante para iniciar el tour manualmente
        if (!isTourActive && currentRouteHasSteps) {
            FloatingActionButton(
                onClick = {
                    currentRoute?.let { route ->
                        tourState.startTourForScreen(route)
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .padding(bottom = 80.dp),
                containerColor = renovaColors.primaryColor,
                contentColor = Color.White
            ) {
                Icon(painter = painterResource(id = R.drawable.help), contentDescription = "Iniciar tour")
            }
        }
    }
}