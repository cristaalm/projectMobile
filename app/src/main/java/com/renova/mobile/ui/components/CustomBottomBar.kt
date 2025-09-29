package com.renova.mobile.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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

private val primaryColor = Color(0xFF08b662)
private val qrBackgroundColor = Color(0xFF05D16E).copy(alpha = 0.5f)

@Composable
fun NavItem(item: NavigationItem, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(72.dp)
            .heightIn(min = 64.dp)
            .clip(CircleShape)
            .offset(y = 8.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally

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

@Composable
fun CustomBottomBar(
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
            .height(80.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Image(
            painter = painterResource(id = R.drawable.bottom_bar_shape),
            contentDescription = "Bottom bar background",
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-16).dp),
            contentScale = ContentScale.FillWidth
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
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

            Spacer(modifier = Modifier.width(64.dp))

            NavItem(
                item = NavigationItem.Profile,
                isSelected = currentRoute == NavigationItem.Profile.route,
                onClick = { navController.navigate(NavigationItem.Profile.route) }
            )

            // Botón de logout - ahora muestra el modal
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .offset(y = -8.dp)
                    .clip(CircleShape)
                    .clickable {
                        showLogoutModal = true  // Mostrar modal en lugar de logout directo
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.salida),
                    contentDescription = "Logout",
                    tint = Color.Unspecified
                )
            }
        }

        // Botón central QR
        Box(
            modifier = Modifier
                .size(64.dp)
                .align(Alignment.TopCenter)
                .offset(y = (-10).dp)
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