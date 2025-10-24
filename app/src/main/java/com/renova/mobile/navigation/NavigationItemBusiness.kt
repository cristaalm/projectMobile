package com.renova.mobile.navigation

import com.renova.mobile.R

// Usamos una sealed class para representar cada pantalla en la barra de navegación (rol: comerciante)
sealed class NavigationItemBusiness(
    val route: String,
    val selectedIcon: Int,
    val unselectedIcon: Int,
    val title: String
) {
    object Home : NavigationItemBusiness("business/home", R.drawable.hogar_relleno, R.drawable.hogar_relleno, "Inicio")
    object Store : NavigationItemBusiness("business/store", R.drawable.tienda2, R.drawable.tienda2, "Tienda")
    object QR : NavigationItemBusiness("business/qr", R.drawable.qr_relleno, R.drawable.qr_relleno, "Escaneo")
    object Profile : NavigationItemBusiness("business/profile", R.drawable.usuario_relleno, R.drawable.usuario_relleno, "Perfil")
}