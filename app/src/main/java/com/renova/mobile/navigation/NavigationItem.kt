package com.renova.mobile.navigation

import com.renova.mobile.R

// Usamos una sealed class para representar cada pantalla en la barra de navegación
sealed class NavigationItem(val route: String, val selectedIcon: Int, val unselectedIcon: Int, val title: String) {
    object Home : NavigationItem("home", R.drawable.hogar_relleno, R.drawable.hogar, "Inicio")
    object Store : NavigationItem("store", R.drawable.tienda2, R.drawable.tienda2_relleno,"Tienda")
    object QR : NavigationItem("qr", R.drawable.qr_relleno, R.drawable.qr, "")
    object Profile : NavigationItem("profile", R.drawable.usuario_relleno, R.drawable.usuario,"Perfil")
}