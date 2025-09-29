package com.renova.mobile.navigation

import com.renova.mobile.R

/**
 * Sealed class that defines the navigation items for the top bar
 */
sealed class TopNavigationItem(
    val route: String,
    val selectedIcon: Int,
    val unselectedIcon: Int,
    val titleRes: Int // Usar recurso de string
) {
    object Activity : TopNavigationItem(
        route = "activity",
        selectedIcon = R.drawable.history,
        unselectedIcon = R.drawable.history_w,
        titleRes = R.string.activity
    )
    object Profile : TopNavigationItem(
        route = "profile",
        selectedIcon = R.drawable.user_full,
        unselectedIcon = R.drawable.user,
        titleRes = R.string.profile
    )
    object Streak : TopNavigationItem(
        route = "streak",
        selectedIcon = R.drawable.flame_full,
        unselectedIcon = R.drawable.flame,
        titleRes = R.string.streak
    )
}
