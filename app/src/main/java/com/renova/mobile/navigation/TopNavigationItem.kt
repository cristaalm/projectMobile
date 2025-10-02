package com.renova.mobile.navigation

import com.renova.mobile.R

sealed class TopNavigationItem(val route: String, val selectedIcon: Int, val unselectedIcon: Int, val title: String) {
    object Profile : NavigationItem("profile", R.drawable.user_full, R.drawable.user, "Profile")
    object Activity : NavigationItem("activity", R.drawable.history_w, R.drawable.history,"Activity")
    object Streak : NavigationItem("streak", R.drawable.flame_full, R.drawable.flame, "Streak")
}