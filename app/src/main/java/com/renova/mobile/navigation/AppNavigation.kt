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
// <<< PASO 1: OBJETO PARA ORGANIZAR LAS RUTAS DEL GRAFO DE TIENDA >>>
object StoreGraph {
    const val ROUTE = "store_graph"
    const val STORE_LIST = "store_list"
    const val REWARDS = "reward_screen/{allianceId}"
}

@Composable
fun AppNavigation(onLogout: () -> Unit) {
    val navController = rememberNavController()
    val businessSaleVM: BusinessSaleViewModel = viewModel()

    val context = LocalContext.current
    val sessionManager = remember(context) { SessionManager(context) }
    val user = sessionManager.getUser()
    val isBusiness = (user?.role?.id ?: 0) == 4

    Scaffold(
        topBar = {
            CustomTopBar(navController = navController)
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
        NavHost(
            navController = navController,
            startDestination = if (isBusiness) NavigationItemBusiness.Home.route else NavigationItem.Home.route, // pantalla inicial según rol
            modifier = Modifier.padding(innerPadding) // Padding para que el contenido no quede debajo de la barra
        ) {
            val enterAnimation = slideInHorizontally(initialOffsetX = { 1000 }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300))
            val exitAnimation = slideOutHorizontally(targetOffsetX = { -1000 }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
            val popEnterAnimation = slideInHorizontally(initialOffsetX = { -1000 }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300))
            val popExitAnimation = slideOutHorizontally(targetOffsetX = { 1000 }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))

            // Rutas de negocio (rol: comerciante)
            composable(
                route = NavigationItemBusiness.Home.route,
                enterTransition = { enterAnimation }, exitTransition = { exitAnimation },
                popEnterTransition = { popEnterAnimation }, popExitTransition = { popExitAnimation }
            ) {
                BusinessHomeScreen(onLogout = onLogout)
            }

            composable(
                route = NavigationItemBusiness.Store.route,
                enterTransition = { enterAnimation }, exitTransition = { exitAnimation },
                popEnterTransition = { popEnterAnimation }, popExitTransition = { popExitAnimation }
            ) {
                BusinessStoreScreen(onLogout = onLogout, vm = businessSaleVM)
            }

            composable(
                route = NavigationItemBusiness.QR.route,
                enterTransition = { enterAnimation }, exitTransition = { exitAnimation },
                popEnterTransition = { popEnterAnimation }, popExitTransition = { popExitAnimation }
            ) {
                BusinessQRScreen(onLogout = onLogout, vm = businessSaleVM)
            }

            composable(
                route = NavigationItem.Home.route,
                enterTransition = { enterAnimation }, exitTransition = { exitAnimation },
                popEnterTransition = { popEnterAnimation }, popExitTransition = { popExitAnimation }
            ) {
                HomeScreen()
            }

            // <<< PASO 2: SE REEMPLAZA EL COMPOSABLE DE "STORE" POR UN GRAFO ANIDADO >>>
            navigation(
                startDestination = StoreGraph.STORE_LIST,
                route = StoreGraph.ROUTE
            ) {
                // Pantalla de la lista de tiendas (dentro del grafo)
                composable(
                    route = StoreGraph.STORE_LIST,
                    enterTransition = { enterAnimation }, exitTransition = { exitAnimation },
                    popEnterTransition = { popEnterAnimation }, popExitTransition = { popExitAnimation }
                ) {
                    StoreScreen(navController = navController)
                }

                // Pantalla de recompensas (dentro del grafo)
                composable(
                    route = StoreGraph.REWARDS,
                    arguments = listOf(navArgument("allianceId") { type = NavType.IntType }),
                    enterTransition = { enterAnimation }, exitTransition = { exitAnimation },
                    popEnterTransition = { popEnterAnimation }, popExitTransition = { popExitAnimation }
                ) { backStackEntry ->
                    val allianceId = backStackEntry.arguments?.getInt("allianceId") ?: 0
                    RewardScreen(navController = navController, allianceId = allianceId)
                }
            }

            composable(
                route = NavigationItem.QR.route,
                enterTransition = { enterAnimation }, exitTransition = { exitAnimation },
                popEnterTransition = { popEnterAnimation }, popExitTransition = { popExitAnimation }
            ) {
                QRScreen()
            }

            composable(
                route = NavigationItem.Profile.route,
                enterTransition = { enterAnimation }, exitTransition = { exitAnimation },
                popEnterTransition = { popEnterAnimation }, popExitTransition = { popExitAnimation }
            ) {
                ProfileScreen()
            }

            composable(
                route = TopNavigationItem.Activity.route,
                enterTransition = { enterAnimation }, exitTransition = { exitAnimation },
                popEnterTransition = { popEnterAnimation }, popExitTransition = { popExitAnimation }
            ) {
                ActivityScreen()
            }

            composable(
                route = TopNavigationItem.Streak.route,
                enterTransition = { enterAnimation }, exitTransition = { exitAnimation },
                popEnterTransition = { popEnterAnimation }, popExitTransition = { popExitAnimation }
            ) {
                StreakScreen()
            }

        }
    }
}