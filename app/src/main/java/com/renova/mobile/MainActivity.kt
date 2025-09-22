package com.renova.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.renova.mobile.ui.screens.LoginScreen
import com.renova.mobile.ui.screens.ForgotPasswordScreen
import com.renova.mobile.ui.theme.RENOVAMobileTheme
import com.renova.mobile.navigation.AppNavigation

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RENOVAMobileTheme {
                var isLoggedIn by remember { mutableStateOf(false) }

                if (isLoggedIn) {
                    // App principal con BottomBar y navegación completa
                    AppNavigation()
                } else {
                    // Pantallas de autenticación
                    AuthNavigation(
                        onLoginSuccess = { isLoggedIn = true }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AuthNavigation(onLoginSuccess: () -> Unit) {
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
                        onLoginSuccess() // Notifica a MainActivity que el login fue exitoso
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
