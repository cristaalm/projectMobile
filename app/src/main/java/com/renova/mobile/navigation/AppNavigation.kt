package com.renova.mobile.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.renova.mobile.ui.screens.HomeScreen
import com.renova.mobile.ui.screens.ProfileScreen
import com.renova.mobile.ui.screens.QRScreen
import com.renova.mobile.ui.screens.StoreScreen
import com.renova.mobile.ui.components.CustomBottomBar
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // Scaffold nos da la estructura básica de Material Design
    Scaffold(
        bottomBar = { CustomBottomBar(navController = navController) }
    ) { innerPadding ->
        // NavHost donde las pantallas se muestrar
        NavHost(
            navController = navController,
            startDestination = NavigationItem.Home.route, // pantalla inicial
            modifier = Modifier.padding(innerPadding) // Padding para que el contenido no quede debajo de la barra
        ) {
            // Definimos las animaciones una sola vez para reutilizarlas
            val enterAnimation = slideInHorizontally(
                initialOffsetX = { 1000 },
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))

            val exitAnimation = slideOutHorizontally(
                targetOffsetX = { -1000 },
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))

            val popEnterAnimation = slideInHorizontally(
                initialOffsetX = { -1000 },
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))

            val popExitAnimation = slideOutHorizontally(
                targetOffsetX = { 1000 },
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))

            // Aplicamos las animaciones a cada pantalla
            composable(
                route = NavigationItem.Home.route,
                enterTransition = { enterAnimation },
                exitTransition = { exitAnimation },
                popEnterTransition = { popEnterAnimation },
                popExitTransition = { popExitAnimation }
            ) {
                HomeScreen()
            }

            composable(
                route = NavigationItem.Store.route,
                enterTransition = { enterAnimation },
                exitTransition = { exitAnimation },
                popEnterTransition = { popEnterAnimation },
                popExitTransition = { popExitAnimation }
            ) {
                StoreScreen()
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
                ProfileScreen()
            }
        }
    }
}