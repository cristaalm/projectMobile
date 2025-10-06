package com.renova.mobile.ui.components

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
import com.renova.mobile.ui.theme.LocalRenovaColors
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.renova.mobile.R
import com.renova.mobile.navigation.NavigationItem
import com.renova.mobile.navigation.TopNavigationItem
import com.renova.mobile.ui.theme.Typography

@Composable
fun CustomTopBar(navController: NavController) {
    val systemUiController = rememberSystemUiController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val isProfileSection = currentRoute == NavigationItem.Profile.route ||
            currentRoute == TopNavigationItem.Activity.route ||
            currentRoute == TopNavigationItem.Streak.route

    // Mostrar la barra de estado
    SideEffect {
        systemUiController.isStatusBarVisible = true
    }

    // Solo mostrar el topbar si estamos en la sección de perfil
    if (!isProfileSection) {
        return
    }

    // TopBar con opciones - colores ahora vienen del tema
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(LocalRenovaColors.current.topBarColor)  // Usa el color definido en Theme.kt
            .padding(start = 16.dp, end = 16.dp, top = 25.dp, bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Opciones en Row
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
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(vertical = 1.dp, horizontal = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 2.dp)
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = title,
                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else LocalRenovaColors.current.topBarUnselected,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = title,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else LocalRenovaColors.current.topBarUnselected,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(start = 4.dp),
                maxLines = 1
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(3.dp)
                .background(if (isSelected) MaterialTheme.colorScheme.onPrimary else LocalRenovaColors.current.topBarUnselected)
        )
    }
}