package com.renova.mobile.ui.theme

import androidx.compose.ui.graphics.Color

// Colores principales de la app
object RenovaColors {

    // Verdes principales
    val Primary = Color(0xFF00C851)
    val PrimaryVariant = Color(0xFF00A843)
    val Secondary = Color(0xFF1B4F5C)
    val SecondaryVariant = Color(0xFF004D40)

    // Colores específicos para ActivityScreen
    val TealPrimary = Color(0xFF009688)
    val TealLight = Color(0xFFB2DFDB)
    val GreenShadow = Color(0xFF4CAF50)
    val GreenPositive = Color(0xFF43A047)
    val RedNegative = Color(0xFFD32F2F)
    val BackgroundMint = Color(0xFFF8FFFB)

    // Colores para materiales
    val PlasticBackground = Color(0xFFE0F7FA)
    val AluminumBackground = Color(0xFFFFF9C4)
    val AluminumIcon = Color(0xFFE0AA24)

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

        // Colores específicos para ActivityScreen en modo claro
        val ActivityBackground = BackgroundMint
        val ActivityPrimary = TealPrimary
        val ActivitySecondary = Color.Gray
        val ActivityCardBackground = Color.White
        val PlasticCard = PlasticBackground
        val AluminumCard = AluminumBackground
        val AluminumCardIcon = AluminumIcon
        val PositivePoints = GreenPositive
        val NegativePoints = RedNegative
        val ButtonEnabled = TealPrimary
        val ButtonDisabled = TealLight
        val ActivityShadowColor = GreenShadow

        val DiscoverGradientStart = Color(0xFFAEFFD4)
        val DiscoverGradientEnd = Color(0xFFF6FFE8)

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

        // Colores específicos para ActivityScreen en modo oscuro
        val ActivityBackground = Color(0xFF0A1A0F)
        val ActivityPrimary = Color(0xFF26A69A)
        val ActivitySecondary = Color(0xFFBBBBBB)
        val ActivityCardBackground = Color(0xFF1E1E1E)
        val PlasticCard = Color(0xFF1A2E2A)
        val AluminumCard = Color(0xFF2E2A1A)
        val AluminumCardIcon = Color(0xFFFFD54F)
        val PositivePoints = Color(0xFF66BB6A)
        val NegativePoints = Color(0xFFEF5350)
        val ButtonEnabled = Color(0xFF26A69A)
        val ButtonDisabled = Color(0xFF4A5F5A)
        val ShadowColor = Color(0xFF2E7D32)
    }

    // Colores de estado (iguales para ambos temas)
    val Error = Color(0xFFFF5252)
    val Success = Primary
    val Warning = Color(0xFFFFC107)
    val Info = Color(0xFF2196F3)

    // Colores para StoreScreen
    val GifBoxGradientStart = Color(0xFFe9ff61)
    val GifBoxGradientEnd = Color(0xFFccff3a)
    val PrimaryColor = Color(0xFF05d16e)
    val PrimaryHoverColor = Color(0xFF08b662)
    val SecondaryColor = Color(0xFF024653)
    val SecondaryHoverColor = Color(0xFF005e70)
    val TertiaryColor = Color(0xFFcdff10)
    val TertiaryHoverColor = Color(0xFFddff50)

    val RewardCardBackgrounds = listOf(
        Color(0xFFFFF59D), // Amarillo claro
        Color(0xFFCCFF90), // Verde lima
        Color(0xFF81D4FA), // Azul claro
        Color(0xFF80CBC4), // Verde azulado
        Color(0xFFFFCC80)  // Naranja claro
    )
    val RewardCardBackgroundsDark = listOf(
        Color(0xFF3E2723), // Café oscuro
        Color(0xFF1B5E20), // Verde oscuro
        Color(0xFF0D47A1), // Azul oscuro
        Color(0xFF004D40), // Verde azulado oscuro
        Color(0xFF4E342E)  // Marrón oscuro
    )

    val AllianceLogoBackgrounds = listOf(
        Primary,
        Color(0xFFE1BEE7),
        Info,
        Warning,
        Error
    )
    val CategoryCardBackgrounds = listOf(
        Color(0xFFFF9800), Color(0xFFF44336), Color(0xFF00BCD4),
        Color(0xFF4CAF50), Color(0xFF9C27B0), Color(0xFF3F51B5)
    )
}
