package com.renova.mobile.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.renova.mobile.R
import com.renova.mobile.navigation.NavigationItemBusiness
import androidx.compose.animation.*
import androidx.compose.ui.graphics.ColorFilter
import com.renova.mobile.navigation.TopNavigationItem
import androidx.compose.ui.res.stringResource

private val primaryColor = Color(0xFF08b662)
private val qrBackgroundColor = Color(0xFF05D16E).copy(alpha = 0.5f)

@Composable
fun NavItemBusiness(item: NavigationItemBusiness, isSelected: Boolean, onClick: () -> Unit) {
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
                    (slideInVertically { it } + fadeIn() togetherWith
                            slideOutVertically { it } + fadeOut())
                        .using(SizeTransform(clip = false))
                },
                label = "IconOrTitle"
            ) { selected ->
                if (selected) {
                    val titleText = when (item) {
                        NavigationItemBusiness.Home -> stringResource(id = R.string.tab_home)
                        NavigationItemBusiness.Store -> stringResource(id = R.string.tab_sale)
                        NavigationItemBusiness.QR -> stringResource(id = R.string.tab_qr)
                        NavigationItemBusiness.Profile -> stringResource(id = R.string.my_profile)
                    }
                    Text(
                        text = titleText,
                        color = primaryColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Icon(
                        painter = painterResource(id = item.unselectedIcon),
                        contentDescription = item.route,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CustomBottomBarBusiness(
    navController: NavController,
    onLogout: () -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Estado para controlar la visibilidad del modal
    var showLogoutModal by remember { mutableStateOf(false) }

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
            NavItemBusiness(
                item = NavigationItemBusiness.Home,
                isSelected = currentRoute == NavigationItemBusiness.Home.route,
                onClick = { navController.navigate(NavigationItemBusiness.Home.route) }
            )

            Spacer(modifier = Modifier.width(72.dp))


            NavItemBusiness(
                item = NavigationItemBusiness.Store,
                isSelected = currentRoute == NavigationItemBusiness.Store.route,
                onClick = { navController.navigate(NavigationItemBusiness.Store.route) }
            )

            // Botón de logout con mismo estilo
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()

        }

        // Botón central QR
        Box(
            modifier = Modifier
                .size(64.dp)
                .align(Alignment.TopCenter)
                .offset(y = (-12).dp)
                .clip(CircleShape)
                .background(qrBackgroundColor)
                .clickable { navController.navigate(NavigationItemBusiness.QR.route) },
            contentAlignment = Alignment.Center
        ) {
            val isQrSelected = currentRoute == NavigationItemBusiness.QR.route
            AnimatedContent(
                targetState = isQrSelected,
                transitionSpec = {
                    (slideInVertically { it } + fadeIn() togetherWith
                            slideOutVertically { it } + fadeOut())
                        .using(SizeTransform(clip = false))
                },
                label = "QrIconOrTitle"
            ) { selected ->
                if (selected) {
                    Text(
                        text = stringResource(id = R.string.bottom_nav_qr),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Icon(
                        painter = painterResource(id = NavigationItemBusiness.QR.unselectedIcon),
                        contentDescription = "QR Screen",
                        modifier = Modifier.size(32.dp),
                        tint = Color.Unspecified
                    )
                }
            }
        }
    }

    // Modal de logout
    LogoutModal(
        isVisible = showLogoutModal,
        onDismiss = { showLogoutModal = false },
        onConfirm = {
            showLogoutModal = false
            onLogout()
        }
    )
}