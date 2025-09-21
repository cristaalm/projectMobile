package com.renova.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import com.renova.mobile.ui.screens.LoginScreen
import com.renova.mobile.ui.screens.ForgotPasswordScreen
import com.renova.mobile.ui.theme.RENOVAMobileTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.ui.unit.dp


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RENOVAMobileTheme {
                AuthNavigation()
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AuthNavigation() {
    var currentScreen by remember { mutableStateOf("login") }

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = {
            slideInHorizontally(
                initialOffsetX = { width ->
                    if (targetState == "forgot_password") width else -width
                },
                animationSpec = tween(300)
            ) with slideOutHorizontally(
                targetOffsetX = { width ->
                    if (targetState == "forgot_password") -width else width
                },
                animationSpec = tween(300)
            )
        },
        label = "screen_transition"
    ) { screen ->
        when (screen) {
            "login" -> {
                LoginScreen(
                    onForgotPassword = {
                        currentScreen = "forgot_password"
                    },
                    onLoginSuccess = {
                        // Aquí puedes navegar a tu pantalla principal
                        // Por ahora solo cambiaremos a una pantalla de ejemplo
                        currentScreen = "home"
                    }
                )
            }
            "forgot_password" -> {
                ForgotPasswordScreen(
                    onBackToLogin = {
                        currentScreen = "login"
                    }
                )
            }
            "home" -> {
                // Pantalla temporal después del login exitoso
                HomeScreen(
                    onLogout = {
                        currentScreen = "login"
                    }
                )
            }
        }
    }
}

@Composable
fun HomeScreen(onLogout: () -> Unit) {
    androidx.compose.foundation.layout.Box(
        modifier = androidx.compose.ui.Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        androidx.compose.foundation.layout.Column(
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            androidx.compose.material3.Text(
                "Login exitoso!",
                style = androidx.compose.material3.MaterialTheme.typography.headlineMedium
            )
            androidx.compose.foundation.layout.Spacer(
                modifier = androidx.compose.ui.Modifier.height(16.dp)
            )
            androidx.compose.material3.Button(onClick = onLogout) {
                androidx.compose.material3.Text("Cerrar Sesión")
            }
        }
    }
}
