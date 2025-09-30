package com.renova.mobile.ui.theme

import androidx.compose.ui.graphics.Color

// Colores principales de la app
object RenovaColors {

    // Verdes principales
    val Primary = Color(0xFF00D084)
    val PrimaryVariant = Color(0xFF019c61)
    val Secondary = Color(0xFF1B4F5C)
    val SecondaryVariant = Color(0xFF004D40)

    // Colores de superficie - Modo Claro
    object Light {
        val Surface = Color.White
        val Background = Color.White
        val OnSurface = Color.Black
        val OnBackground = Color.Black
        val OnPrimary = Color.White
        val OnSecondary = Color.White
        val CardBackground = Color.White
        val TextPrimary = Color.Black
        val TextSecondary = Color(0xFF666666)
        val Border = Primary.copy(alpha = 0.5f)
        val BorderFocused = Primary
        val IconTint = Primary

        val ShadowColor = Primary.copy(alpha = 0.4f)

        // Gradientes
        val GradientStart = Color(0xFF00E676)
        val GradientMid1 = Color(0xFF00C853)
        val GradientMid2 = Color(0xFF00A843)
        val GradientEnd = Color(0xFF1B5E20)
    }

    // Colores de superficie - Modo Oscuro
    object Dark {
        val Surface = Color(0xFF1E1E1E)
        val Background = Color(0xFF121212)
        val OnSurface = Color(0xFFE0E0E0)
        val OnBackground = Color(0xFFE0E0E0)
        val OnPrimary = Color.Black
        val OnSecondary = Primary
        val CardBackground = Color(0xFF1E1E1E)
        val TextPrimary = Color(0xFFE0E0E0)
        val TextSecondary = Color(0xFFBBBBBB)
        val Border = Primary.copy(alpha = 0.5f)
        val BorderFocused = Primary
        val IconTint = Primary

        // Gradientes
        val GradientStart = Color(0xFF004D40)
        val GradientMid1 = Color(0xFF00695C)
        val GradientEnd = Color(0xFF00796B)
    }

    // Colores de estado (iguales para ambos temas)
    val Error = Color(0xFFE62929)
    val Success = Primary
    val Warning = Color(0xFFFFC107)
    val Info = Color(0xFF2196F3)
}