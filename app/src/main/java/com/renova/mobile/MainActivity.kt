package com.renova.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.renova.mobile.ui.theme.RENOVAMobileTheme
import androidx.compose.material3.MaterialTheme
import com.renova.mobile.navigation.AppNavigation
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // <-- API más moderna y recomendada
        setContent {
            RENOVAMobileTheme {
                HideSystemNavigation()
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}
@Composable
private fun HideSystemNavigation() {
    val view = LocalView.current

    DisposableEffect(Unit) {
        val window = (view.context as? ComponentActivity)?.window ?: return@DisposableEffect onDispose {}
        val insetsController = WindowCompat.getInsetsController(window, view)
        // Ocultar la barra de navegación del sistema
        insetsController.hide(WindowInsetsCompat.Type.navigationBars())

        // Configurar el comportamiento para que la barra de navegación permanezca oculta
        // incluso si el usuario interactúa con la pantalla (swipe desde el borde, etc.)
        // SYSTEM_UI_BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE permite que la barra aparezca
        // temporalmente con un swipe y luego se oculte de nuevo.
        insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        onDispose {}

    }
}