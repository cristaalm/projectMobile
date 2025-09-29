package com.renova.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.renova.mobile.ui.screens.LoginScreen
import com.renova.mobile.ui.screens.ForgotPasswordScreen
import com.renova.mobile.ui.theme.RenovaTheme
import com.renova.mobile.navigation.AppNavigation
import com.renova.mobile.utils.SessionManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RenovaTheme {  // Usando el nuevo tema personalizado
                val sessionManager = SessionManager(this)
                HideSystemNavigation()

                // Verificar si hay sesión guardada al iniciar
                var isLoggedIn by remember { mutableStateOf(sessionManager.isLoggedIn()) }

                if (isLoggedIn) {
                    // App principal con BottomBar y navegación completa
                    AppNavigation(
                        onLogout = {
                            sessionManager.logout()
                            isLoggedIn = false
                        }
                    )
                } else {
                    // Pantallas de autenticación
                    AuthNavigation(
                        sessionManager = sessionManager,
                        onLoginSuccess = { isLoggedIn = true }
                    )
                }
            }
        }
    }
}
@Composable
private fun HideSystemNavigation() {
    val view = LocalView.current
    DisposableEffect(Unit) {
        val window =
            (view.context as? ComponentActivity)?.window ?: return@DisposableEffect onDispose {}
        val insetsController = WindowCompat.getInsetsController(window, view)
        // Ocultar la barra de navegación del sistema
        insetsController.hide(WindowInsetsCompat.Type.navigationBars())

        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        onDispose {}
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AuthNavigation(
    sessionManager: SessionManager,
    onLoginSuccess: () -> Unit
) {
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
                    onLoginSuccess = { user, token, tokenType, expiresAt ->
                        // Guardar la sesión
                        sessionManager.saveSession(
                            accessToken = token,
                            tokenType = tokenType,
                            expiresAt = expiresAt,
                            user = user
                        )
                        onLoginSuccess()
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
        }
    }
}