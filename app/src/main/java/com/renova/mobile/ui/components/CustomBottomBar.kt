package com.renova.mobile.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.renova.mobile.R
import com.renova.mobile.navigation.NavigationItem
import com.renova.mobile.navigation.StoreGraph
import com.renova.mobile.ui.screens.PoppinsFontFamily
import com.renova.mobile.navigation.TopNavigationItem

// --- NUEVO: Imports para el Tour ---
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.runtime.DisposableEffect
import com.renova.mobile.ui.tour.LocalTourState
// --- FIN DE IMPORTS ---

private val primaryColor = Color(0xFF08b662)
private val qrBackgroundColor = Color(0xFF05D16E).copy(alpha = 0.5f)

@Composable
fun NavItem(
    item: NavigationItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier // <-- MODIFICADO: Añadir modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = modifier // <-- MODIFICADO: Aplicar modifier
            .size(72.dp)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isPressed) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(primaryColor.copy(alpha = 0.15f))
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedContent(
                targetState = isSelected,
                transitionSpec = {
                    (slideInVertically { height -> height } + fadeIn() togetherWith
                            slideOutVertically { height -> -height } + fadeOut())
                        .using(SizeTransform(clip = false))
                },
                label = "NavItemAnimation"
            ) { selected ->
                val titleResId = when (item) {
                    is NavigationItem.Home -> R.string.bottom_nav_home
                    is NavigationItem.Store -> R.string.bottom_nav_store
                    is NavigationItem.Profile -> R.string.bottom_nav_profile
                    is NavigationItem.QR -> R.string.bottom_nav_qr
                    else -> 0
                }
                val titleText = if (titleResId != 0) stringResource(id = titleResId) else ""

                if (selected && titleText.isNotEmpty()) {
                    Text(
                        text = titleText,
                        color = primaryColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = PoppinsFontFamily
                    )
                } else {
                    Icon(
                        painter = painterResource(id = item.unselectedIcon),
                        contentDescription = titleText,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}


@Composable
fun CustomBottomBar(
    navController: NavController,
    onLogout: () -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    var showLogoutModal by remember { mutableStateOf(false) }

    // --- NUEVO: Obtener el estado del Tour ---
    val tourState = LocalTourState.current
    // --- FIN ---

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Image(
            painter = painterResource(id = R.drawable.bottom_bar_shape),
            contentDescription = "Bottom bar background",
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-12).dp),
            colorFilter = ColorFilter.tint(Color(0xFF44E382)) ,
            contentScale = ContentScale.FillWidth
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .offset(y = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // --- INICIO ---
            NavItem(
                item = NavigationItem.Home,
                isSelected = currentDestination?.route == NavigationItem.Home.route,
                onClick = {
                    navController.navigate(NavigationItem.Home.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
                // NOTA: No registramos 'Home' en el tour por ahora
            )

            // --- TIENDA ---
            val isStoreSelected = currentDestination?.hierarchy?.any {
                it.route == StoreGraph.ROUTE || it.route == StoreGraph.STORE_LIST
            } == true

            // --- MODIFICADO: Añadir modifier y DisposableEffect ---
            NavItem(
                item = NavigationItem.Store,
                isSelected = isStoreSelected,
                onClick = {
                    navController.navigate(StoreGraph.ROUTE) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                modifier = Modifier.onGloballyPositioned { coords ->
                    tourState.registerTarget("bottom_bar_store", coords)
                }
            )
            DisposableEffect("bottom_bar_store") {
                onDispose { tourState.unregisterTarget("bottom_bar_store") }
            }
            // --- FIN DE MODIFICACIÓN ---

            Spacer(modifier = Modifier.width(72.dp))

            // --- PERFIL ---
            // --- MODIFICADO: Añadir modifier y DisposableEffect ---
            NavItem(
                item = NavigationItem.Profile,
                isSelected = currentDestination?.route == NavigationItem.Profile.route ||
                        currentDestination?.route == TopNavigationItem.Activity.route ||
                        currentDestination?.route == TopNavigationItem.Streak.route,
                onClick = {
                    navController.navigate(NavigationItem.Profile.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                modifier = Modifier.onGloballyPositioned { coords ->
                    tourState.registerTarget("bottom_bar_profile", coords)
                }
            )
            DisposableEffect("bottom_bar_profile") {
                onDispose { tourState.unregisterTarget("bottom_bar_profile") }
            }
            // --- FIN DE MODIFICACIÓN ---

            // --- LOGOUT ---
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isPressed) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(primaryColor.copy(alpha = 0.15f))
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) { showLogoutModal = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.salida),
                        contentDescription = "Logout",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        val isQrSelected = currentDestination?.route == NavigationItem.QR.route
        // --- MODIFICADO: Añadir modifier y DisposableEffect ---
        Box(
            modifier = Modifier
                .size(64.dp)
                .align(Alignment.TopCenter)
                .offset(y = (-12).dp)
                .clip(CircleShape)
                .background(if (isQrSelected) Color.Transparent else qrBackgroundColor)
                .clickable {
                    navController.navigate(NavigationItem.QR.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
                .onGloballyPositioned { coords -> // <-- Añadido
                    tourState.registerTarget("bottom_bar_qr", coords)
                },
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = isQrSelected,
                transitionSpec = {
                    (slideInVertically { height -> height } + fadeIn() togetherWith
                            slideOutVertically { height -> -height } + fadeOut())
                        .using(SizeTransform(clip = false))
                },
                label = "QrButtonAnimation"
            ) { selected ->
                if (selected) {
                    Text(
                        text = stringResource(R.string.bottom_nav_qr),
                        color = primaryColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = PoppinsFontFamily
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.qr_relleno),
                        contentDescription = stringResource(id = R.string.bottom_nav_qr),
                        modifier = Modifier.size(32.dp),
                        tint = Color.Unspecified
                    )
                }
            }
        }
        DisposableEffect("bottom_bar_qr") { // <-- Añadido
            onDispose { tourState.unregisterTarget("bottom_bar_qr") }
        }
        // --- FIN DE MODIFICACIÓN ---
    }

    LogoutModal(
        isVisible = showLogoutModal,
        onDismiss = { showLogoutModal = false },
        onConfirm = {
            showLogoutModal = false
            onLogout()
        }
    )
}