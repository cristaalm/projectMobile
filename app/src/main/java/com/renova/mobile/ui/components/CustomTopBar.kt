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
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith

@Composable
fun CustomTopBar(
    navController: NavController,
    isLoading: Boolean = false
) {
    val systemUiController = rememberSystemUiController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val isProfileSection = currentRoute == NavigationItem.Profile.route ||
            currentRoute == TopNavigationItem.Activity.route ||
            currentRoute == TopNavigationItem.Streak.route

    if (!isProfileSection || isLoading) {
        return
    }

    // TopBar con opciones
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(LocalRenovaColors.current.primaryColor)
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
                icon = R.drawable.user_full,
                title = stringResource(R.string.profile),
                isSelected = currentRoute == TopNavigationItem.Profile.route,
                onClick = { navController.navigate(TopNavigationItem.Profile.route) },
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Opción 2: Actividad
            TopBarOption(
                icon = R.drawable.history,
                title = stringResource(R.string.activity),
                isSelected = currentRoute == TopNavigationItem.Activity.route,
                onClick = { navController.navigate(TopNavigationItem.Activity.route) },
                modifier = Modifier.weight(1f)
            )

            /*Spacer(modifier = Modifier.width(8.dp))

            // Opción 3: Racha
            TopBarOption(
                icon = R.drawable.flame_full,
                title = stringResource(R.string.streak),
                isSelected = currentRoute == TopNavigationItem.Streak.route,
                onClick = { navController.navigate(TopNavigationItem.Streak.route) },
                modifier = Modifier.weight(1f)
            )*/
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
        AnimatedContent(
            targetState = isSelected,
            transitionSpec = {
                (slideInVertically { height ->  -height } + fadeIn() togetherWith
                        slideOutVertically { height ->  height } + fadeOut())
                    .using(SizeTransform(clip = false))
            },
            label = "TopBarOptionAnimation"
        ) {selected ->
            if (selected) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.labelLarge,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        painter = painterResource(id = icon),
                        contentDescription = title,
                        tint = LocalRenovaColors.current.primaryHoverColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = title,
                        color = LocalRenovaColors.current.primaryHoverColor,
                        style = MaterialTheme.typography.labelLarge,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier.padding(start = 4.dp),
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(3.dp)
                .background(if (isSelected) MaterialTheme.colorScheme.onPrimary else LocalRenovaColors.current.primaryHoverColor)
        )
    }
}