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

object StoreGraph {
    const val ROUTE = "store_graph"
    const val STORE_LIST = "store_list"
    const val REWARDS = "reward_screen/{allianceId}"
}

@Composable
fun AppNavigation(
    onLogout: () -> Unit,
    languageViewModel: LanguageViewModel,
    isUpdatingLanguage: Boolean
) {
    val navController = rememberNavController()
    val businessSaleVM: BusinessSaleViewModel = viewModel()

    val context = LocalContext.current
    val sessionManager = remember(context) { SessionManager(context) }
    val user = sessionManager.getUser()
    val isBusiness = (user?.role?.id ?: 0) == 4

    var contentVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(100)
        contentVisible = true
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
            // Usar AnimatedVisibility para un fade-in suave
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
                            vm = businessSaleVM,
                            onNavigateToCashout = { navController.navigate("business/cashout") }
                        )
                    }

                    composable(
                        route = NavigationItemBusiness.Store.route,
                        enterTransition = { enterAnimation },
                        exitTransition = { exitAnimation },
                        popEnterTransition = { popEnterAnimation },
                        popExitTransition = { popExitAnimation }
                    ) {
                        BusinessStoreScreen(onLogout = onLogout, vm = businessSaleVM, navController = navController)
                    }

                    composable(
                        route = NavigationItemBusiness.QR.route,
                        enterTransition = { enterAnimation },
                        exitTransition = { exitAnimation },
                        popEnterTransition = { popEnterAnimation },
                        popExitTransition = { popExitAnimation }
                    ) {
                        BusinessQRScreen(onLogout = onLogout, vm = businessSaleVM, navController = navController)
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

            // Loading inicial mientras contentVisible es false
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

        // Overlay de loading global para cambio de idioma
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
    }
}