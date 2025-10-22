package com.renova.mobile.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Extensión para acceder fácilmente a los colores de Renova
val androidx.compose.material3.MaterialTheme.renovaColors: RenovaColorScheme
    @Composable
    get() = LocalRenovaColors.current

// Funciones utilitarias para gradientes
object RenovaGradients {

    @Composable
    fun backgroundGradient(): Brush {
        val colors = androidx.compose.material3.MaterialTheme.renovaColors
        return if (isSystemInDarkTheme()) {
            Brush.verticalGradient(
                colors = listOf(
                    colors.gradientStart,
                    colors.gradientMid1,
                    colors.gradientEnd
                )
            )
        } else {
            Brush.verticalGradient(
                colors = listOfNotNull(
                    colors.gradientStart,
                    colors.gradientMid1,
                    colors.gradientMid2,
                    colors.gradientEnd
                )
            )
        }
    }

    @Composable
    fun buttonGradient(): Brush {
        return Brush.horizontalGradient(
            colors = listOf(
                RenovaColors.Primary,
                RenovaColors.PrimaryVariant
            )
        )
    }

    @Composable
    fun cardBorderGradient(): Brush {
        return Brush.horizontalGradient(
            colors = listOf(
                RenovaColors.Primary,
                RenovaColors.Primary
            )
        )
    }
}

// Estados de color para componentes específicos
object RenovaComponentColors {

    @Composable
    fun textFieldColors() = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
        focusedBorderColor = androidx.compose.material3.MaterialTheme.renovaColors.borderFocused,
        unfocusedBorderColor = androidx.compose.material3.MaterialTheme.renovaColors.border,
        errorBorderColor = RenovaColors.Error,
        cursorColor = RenovaColors.Primary,
        focusedLabelColor = RenovaColors.Primary,
        unfocusedLabelColor = androidx.compose.material3.MaterialTheme.renovaColors.textSecondary,
        errorLabelColor = RenovaColors.Error
    )

    @Composable
    fun primaryButtonColors() = androidx.compose.material3.ButtonDefaults.buttonColors(
        containerColor = RenovaColors.Secondary
    )

    @Composable
    fun secondaryButtonColors() = androidx.compose.material3.ButtonDefaults.buttonColors(
        containerColor = Color.Transparent
    )

    @Composable
    fun loadingButtonColors(isDark: Boolean) = androidx.compose.material3.ButtonDefaults.buttonColors(
        containerColor = if (isDark) RenovaColors.Primary else Color(0xFF212121)
    )
}