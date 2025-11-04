package com.renova.mobile.ui.activities

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.renova.mobile.ui.screens.business.statistics.BusinessStatisticsScreen
import com.renova.mobile.ui.theme.RenovaTheme
import com.renova.mobile.utils.LocaleHelper
import androidx.core.view.WindowCompat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class BusinessStatisticsActivity : ComponentActivity() {

    // Aplica el idioma guardado antes de crear la interfaz
    override fun attachBaseContext(newBase: Context) {
        val localeUpdatedContext = LocaleHelper.setLocale(
            newBase,
            LocaleHelper.getLanguage(newBase)
        )
        super.attachBaseContext(localeUpdatedContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Respeta los márgenes superiores e inferiores del sistema
        WindowCompat.setDecorFitsSystemWindows(window, true)

        setContent {
            RenovaTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    BusinessStatisticsScreen(
                        onLogout = {},
                        onNavigateBack = { finish() }
                    )
                }
            }
        }
    }

    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)
        recreate()
    }
}

@Composable
private fun HideSystemNavigation() {
    val view = LocalView.current
    DisposableEffect(Unit) {
        val window =
            (view.context as? ComponentActivity)?.window ?: return@DisposableEffect onDispose {}
        val insetsController = WindowCompat.getInsetsController(window, view)
        insetsController.hide(WindowInsetsCompat.Type.navigationBars())
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        onDispose {}
    }
}