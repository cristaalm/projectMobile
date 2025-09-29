package com.renova.mobile.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.renova.mobile.navigation.TopNavigationItem
import com.renova.mobile.ui.theme.LocalRenovaColors
import androidx.compose.ui.res.stringResource

@Composable
fun CustomTopBar(
    navController: NavController,
    onNotificationClick: () -> Unit = {}
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val isProfileSection = currentRoute == NavigationItem.Profile.route ||
            currentRoute == TopNavigationItem.Activity.route ||
            currentRoute == TopNavigationItem.Streak.route

    val renovaColors = LocalRenovaColors.current

    // Solo mostrar el topbar expandido si estamos en la sección de perfil
    if (!isProfileSection) {
        // TopBar minimalista (solo logo)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_secundario),
                contentDescription = "Logo Renova",
                modifier = Modifier.height(35.dp),
                contentScale = ContentScale.Fit
            )
        }
        return
    }

    // TopBar expandido
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_secundario),
            contentDescription = "Logo Renova",
            modifier = Modifier
                .height(35.dp)
                .padding(vertical = 8.dp),
            contentScale = ContentScale.Fit
        )

        // Fila 2: Opciones en Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Opción 1: Perfil
            TopBarOption(
                icon = if (currentRoute == TopNavigationItem.Profile.route) R.drawable.user_full else R.drawable.user,
                title = stringResource(R.string.profile),
                isSelected = currentRoute == TopNavigationItem.Profile.route,
                onClick = { navController.navigate(TopNavigationItem.Profile.route) },
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Opción 2: Actividad
            TopBarOption(
                icon = if (currentRoute == TopNavigationItem.Activity.route) R.drawable.history else R.drawable.history_w,
                title = stringResource(R.string.activity),
                isSelected = currentRoute == TopNavigationItem.Activity.route,
                onClick = { navController.navigate(TopNavigationItem.Activity.route) },
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Opción 3: Racha
            TopBarOption(
                icon = if (currentRoute == TopNavigationItem.Streak.route) R.drawable.flame_full else R.drawable.flame,
                title = stringResource(R.string.streak),
                isSelected = currentRoute == TopNavigationItem.Streak.route,
                onClick = { navController.navigate(TopNavigationItem.Streak.route) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TopBarOption(
    icon: Int,
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val renovaColors = LocalRenovaColors.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(vertical = 1.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(bottom = 2.dp)
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = title,
                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(20.dp)
            )

            Text(
                text = title,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.tertiary,
                fontSize = 16.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        Box(
            modifier = Modifier
                .width(when(title) {
                    "Perfil" -> 70.dp
                    "Actividad" -> 105.dp
                    "Racha" -> 80.dp
                    else -> 80.dp
                })
                .height(3.dp)
                .background(if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.tertiary)
        )
    }
}