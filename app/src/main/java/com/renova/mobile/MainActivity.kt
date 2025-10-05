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
import com.renova.mobile.screens.*
import com.renova.mobile.ui.theme.RenovaTheme
import com.renova.mobile.navigation.AppNavigation
import com.renova.mobile.network.ApiClient
import com.renova.mobile.utils.SessionManager
import com.renova.mobile.utils.LocaleHelper
import android.content.Context

class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        val language = LocaleHelper.getLanguage(newBase)
        val localizedContext = LocaleHelper.setLocale(newBase, language)
        super.attachBaseContext(localizedContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ApiClient.init(this)
        enableEdgeToEdge()
        setContent {
            RenovaTheme {
                val sessionManager = SessionManager(this)
                HideSystemNavigation()

                var isLoggedIn by remember { mutableStateOf(sessionManager.isLoggedIn()) }

                if (isLoggedIn) {
                    AppNavigation(
                        onLogout = {
                            sessionManager.logout()
                            isLoggedIn = false
                        }
                    )
                } else {
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
    var registerData by remember { mutableStateOf<RegisterData?>(null) }
    var documentsData by remember { mutableStateOf<DocumentsData?>(null) }

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = {
            slideInHorizontally(
                initialOffsetX = { width ->
                    when {
                        targetState == "register" && initialState == "login" -> width
                        targetState == "login" && initialState == "register" -> -width
                        targetState == "forgot_password" -> width
                        targetState == "login" && initialState == "forgot_password" -> -width
                        targetState == "register_documents" -> width
                        targetState == "register" && initialState == "register_documents" -> -width
                        targetState == "register_verification" -> width
                        targetState == "register_documents" && initialState == "register_verification" -> -width
                        else -> width
                    }
                },
                animationSpec = tween(300)
            ) with slideOutHorizontally(
                targetOffsetX = { width ->
                    when {
                        targetState == "register" && initialState == "login" -> -width
                        targetState == "login" && initialState == "register" -> width
                        targetState == "forgot_password" -> -width
                        targetState == "login" && initialState == "forgot_password" -> width
                        targetState == "register_documents" -> -width
                        targetState == "register" && initialState == "register_documents" -> width
                        targetState == "register_verification" -> -width
                        targetState == "register_documents" && initialState == "register_verification" -> width
                        else -> -width
                    }
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
                    onCreateAccount = {
                        currentScreen = "register"
                    },
                    onLoginSuccess = { user, token, tokenType, expiresAt ->
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
            "register" -> {
                RegisterScreen(
                    onBackToLogin = {
                        currentScreen = "login"
                    },
                    onContinueToDocuments = { data ->
                        registerData = data
                        currentScreen = "register_documents"
                    }
                )
            }
            "register_documents" -> {
                DocumentsScreen(
                    registerData = registerData!!,
                    onBackToRegister = {
                        currentScreen = "register"
                    },
                    onContinueToVerification = { data ->
                        documentsData = data
                        currentScreen = "register_verification"
                    }
                )
            }
            "register_verification" -> {
                VerificationScreen(
                    registerData = registerData!!,
                    documentsData = documentsData!!,
                    onBackToDocuments = {
                        currentScreen = "register_documents"
                    },
                    onComplete = {
                        // Aquí irá el registro final al backend
                        // Por ahora regresa al login
                        currentScreen = "login"
                    }
                )
            }
        }
    }
}