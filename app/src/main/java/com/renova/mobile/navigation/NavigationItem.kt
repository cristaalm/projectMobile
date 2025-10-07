package com.renova.mobile.navigation

import com.renova.mobile.R

// Usamos una sealed class para representar cada pantalla en la barra de navegación
sealed class NavigationItem(val route: String, val selectedIcon: Int, val unselectedIcon: Int, val title: String) {
    object Home : NavigationItem("home", R.drawable.hogar_relleno, R.drawable.hogar_relleno, "Inicio")
    object Store : NavigationItem(StoreGraph.STORE_LIST, R.drawable.tienda2, R.drawable.tienda2,"Tienda")
    object QR : NavigationItem("qr", R.drawable.qr_relleno, R.drawable.qr_relleno, "Escaner")
    object Profile : NavigationItem("profile", R.drawable.usuario_relleno, R.drawable.usuario_relleno,"Perfil")

}