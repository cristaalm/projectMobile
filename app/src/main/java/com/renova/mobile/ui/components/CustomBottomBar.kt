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
import com.renova.mobile.navigation.NavigationItem
import androidx.compose.animation.*
import androidx.compose.ui.graphics.ColorFilter

private val primaryColor = Color(0xFF08b662)
private val qrBackgroundColor = Color(0xFF05D16E).copy(alpha = 0.5f)

@Composable
fun NavItem(item: NavigationItem, isSelected: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = Modifier
            .size(72.dp)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        // Círculo de fondo cuando está presionado
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
                label = "IconAnimation"
            ) { selected ->
                Icon(
                    painter = painterResource(id = if (selected) item.selectedIcon else item.unselectedIcon),
                    contentDescription = item.route,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(28.dp)
                )
            }

            AnimatedVisibility(
                visible = isSelected && item.title.isNotEmpty(),
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                Text(
                    text = item.title,
                    color = primaryColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
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
    val currentRoute = navBackStackEntry?.destination?.route

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
            NavItem(
                item = NavigationItem.Home,
                isSelected = currentRoute == NavigationItem.Home.route,
                onClick = { navController.navigate(NavigationItem.Home.route) }
            )
            NavItem(
                item = NavigationItem.Store,
                isSelected = currentRoute == NavigationItem.Store.route,
                onClick = { navController.navigate(NavigationItem.Store.route) }
            )

            Spacer(modifier = Modifier.width(72.dp))

            NavItem(
                item = NavigationItem.Profile,
                isSelected = currentRoute == NavigationItem.Profile.route,
                onClick = { navController.navigate(NavigationItem.Profile.route) }
            )

            // Botón de logout con mismo estilo
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

        // Botón central QR
        Box(
            modifier = Modifier
                .size(64.dp)
                .align(Alignment.TopCenter)
                .offset(y = (-12).dp)
                .clip(CircleShape)
                .background(qrBackgroundColor)
                .clickable { navController.navigate(NavigationItem.QR.route) },
            contentAlignment = Alignment.Center
        ) {
            val isQrSelected = currentRoute == NavigationItem.QR.route
            Icon(
                painter = painterResource(
                    id = if (isQrSelected) R.drawable.qr_relleno else R.drawable.qr
                ),
                contentDescription = "QR Screen",
                modifier = Modifier.size(32.dp),
                tint = Color.Unspecified
            )
        }
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