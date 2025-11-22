package com.renova.mobile.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import android.content.Intent
import com.renova.mobile.ui.activities.ManualGeneralActivity
import com.renova.mobile.ui.activities.FaqActivity
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.runtime.DisposableEffect
import com.renova.mobile.ui.tour.LocalTourState
import androidx.compose.runtime.collectAsState

private val primaryColor = Color(0xFF08b662)
private val qrBackgroundColor = Color(0xFF05D16E).copy(alpha = 0.5f)

@Composable
fun NavItem(
    item: NavigationItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = modifier
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
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }
    var showManualDialog by remember { mutableStateOf(false) }

    val tourState = LocalTourState.current
    val currentStepIndex by tourState.currentStepIndex.collectAsState()
    val isTourActive by tourState.isTourActive.collectAsState()

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
            colorFilter = ColorFilter.tint(Color(0xFF44E382)),
            contentScale = ContentScale.FillWidth
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .offset(y = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Izquierda: Inicio y Tienda
            Row(
                horizontalArrangement = Arrangement.spacedBy(0.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                )

                val isStoreSelected = currentDestination?.hierarchy?.any {
                    it.route == StoreGraph.ROUTE || it.route == StoreGraph.STORE_LIST
                } == true

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
                        tourState.registerTarget("bottom_bar_store", coords, null)
                    }
                )
                DisposableEffect("bottom_bar_store") {
                    onDispose { tourState.unregisterTarget("bottom_bar_store") }
                }
            }

            // Espacio para el botón QR en el centro
            Spacer(modifier = Modifier.width(88.dp))

            // Derecha: Perfil y Menú
            Row(
                horizontalArrangement = Arrangement.spacedBy(0.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                        tourState.registerTarget("bottom_bar_profile", coords, null)
                    }
                )
                DisposableEffect("bottom_bar_profile") {
                    onDispose { tourState.unregisterTarget("bottom_bar_profile") }
                }

                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .padding(4.dp)
                        .onGloballyPositioned { coords ->
                            tourState.registerTarget("bottom_bar_menu", coords, null)
                        },
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
                            ) { showMenu = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Dashboard,
                            contentDescription = "Menú",
                            tint = Color(0xFF05D16E),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                DisposableEffect("bottom_bar_menu") {
                    onDispose { tourState.unregisterTarget("bottom_bar_menu") }
                }
            }
        }

        val isQrSelected = currentDestination?.route == NavigationItem.QR.route
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
                .onGloballyPositioned { coords ->
                    tourState.registerTarget("bottom_bar_qr", coords, null)
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
        DisposableEffect("bottom_bar_qr") {
            onDispose { tourState.unregisterTarget("bottom_bar_qr") }
        }

        if (showMenu) {
            Dialog(
                onDismissRequest = { showMenu = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                var panelVisible by remember { mutableStateOf(false) }

                LaunchedEffect(Unit) { panelVisible = true }

                Box(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.25f))
                            .clickable { panelVisible = false }
                    )
                    AnimatedVisibility(
                        visible = panelVisible,
                        enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
                        exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(),
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(280.dp)
                                .onGloballyPositioned { coords ->
                                    tourState.registerTarget("side_menu_panel", coords, null)
                                },
                            shape = RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp),
                            color = Color(0xFF05D16E)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = stringResource(id = R.string.menu_title),
                                        color = Color.White,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    IconButton(onClick = { panelVisible = false }) {
                                        Icon(
                                            imageVector = Icons.Filled.Close,
                                            contentDescription = "Cerrar",
                                            tint = Color.White
                                        )
                                    }
                                }

                                TextButton(
                                    onClick = {
                                        showMenu = false
                                        showManualDialog = true
                                    },
                                    colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
                                ) {
                                    Text(stringResource(id = R.string.manual_general_pdf))
                                }

                                TextButton(
                                    onClick = {
                                        panelVisible = false
                                        context.startActivity(Intent(context, FaqActivity::class.java))
                                    },
                                    colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
                                ) {
                                    Text(stringResource(id = R.string.faq_title))
                                }

                                Divider(color = Color.White.copy(alpha = 0.3f))

                                Button(
                                    onClick = {
                                        panelVisible = false
                                        showLogoutModal = true
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White,
                                        contentColor = Color(0xFF05D16E)
                                    )
                                ) {
                                    Text(stringResource(id = R.string.modal_logout_title))
                                }
                            }
                        }
                    }

                    LaunchedEffect(panelVisible) {
                        if (!panelVisible) {
                            delay(250)
                            showMenu = false
                        }
                    }
                }
            }
        }

        DisposableEffect("side_menu_panel") {
            onDispose { tourState.unregisterTarget("side_menu_panel") }
        }
    }

    if (showManualDialog) {
        ManualDialog(onDismiss = { showManualDialog = false })
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