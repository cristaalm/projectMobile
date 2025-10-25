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
import android.content.pm.ActivityInfo
import androidx.lifecycle.viewmodel.compose.viewModel
import com.renova.mobile.ui.viewmodels.LanguageViewModel
import com.renova.mobile.ui.viewmodels.RegisterViewModel
import androidx.compose.runtime.LaunchedEffect
import android.util.Log
import android.view.WindowManager
import com.renova.mobile.ui.tour.LocalTourState
import com.renova.mobile.ui.tour.TourState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.renova.mobile.network.RegisterFcmTokenRequest

// Importa la clase User que tu SessionManager espera
import com.renova.mobile.network.User
// Importa las clases de Auth (si no están ya importadas globalmente en screens.*)
import com.renova.mobile.screens.LoginScreen
import com.renova.mobile.screens.ForgotPasswordScreen
import com.renova.mobile.screens.RegisterScreen
import com.renova.mobile.screens.DocumentsScreen
import com.renova.mobile.screens.VerificationScreen
import com.renova.mobile.screens.RegisterData
import com.renova.mobile.screens.DocumentsData


class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        val language = LocaleHelper.getLanguage(newBase)
        val localizedContext = LocaleHelper.setLocale(newBase, language)
        super.attachBaseContext(localizedContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        ApiClient.init(this)
        enableEdgeToEdge()
        setContent {
            RenovaTheme {
                val sessionManager = remember { SessionManager(this) } // remember sessionManager

                // --- DEBUG: Descomenta esta línea SOLO para forzar el tour ---
//                sessionManager.resetFirstLoginFlag()

                val languageViewModel: LanguageViewModel = viewModel()

                LaunchedEffect(Unit) {
                    languageViewModel.resetUpdating()
                }

                val isUpdatingLanguage by languageViewModel.isUpdating.collectAsState()

                HideSystemNavigation()

                var isLoggedIn by remember { mutableStateOf(sessionManager.isLoggedIn()) }

                LaunchedEffect(isLoggedIn) {
                    if (isLoggedIn) {
                        val fcmToken = sessionManager.getFcmToken()
                        val userId = sessionManager.getUser()?.id
                        if (!fcmToken.isNullOrBlank() && userId != null) {
                            try {
                                ApiClient.init(this@MainActivity)
                                withContext(Dispatchers.IO) {
                                    val resp = ApiClient.apiService.registerFcmToken(
                                        RegisterFcmTokenRequest(userId = userId, token = fcmToken)
                                    )
                                    if (resp.isSuccessful && resp.body()?.success == true) {
                                        Log.d("FCM", "Token registrado tras login para userId=$userId")
                                    } else {
                                        Log.e("FCM", "Error registrando token tras login: ${resp.code()} ${resp.body()?.message}")
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("FCM", "Excepción registrando token tras login", e)
                            }
                        }
                    }
                }

                val tourState = remember { TourState() }

                // Ya no necesitamos el LaunchedEffect para 'triggerFirstLoginTour'

                CompositionLocalProvider(LocalTourState provides tourState) {
                    if (isLoggedIn) {
                        AppNavigation(
                            sessionManager = sessionManager, // Pasamos sessionManager a AppNavigation
                            onLogout = {
                                sessionManager.logout()
                                isLoggedIn = false
                                // shouldStartTour = false // Ya no existe
                            },
                            languageViewModel = languageViewModel,
                            isUpdatingLanguage = isUpdatingLanguage
                        )
                    } else {
                        AuthNavigation(
                            sessionManager = sessionManager,
                            onLoginSuccess = {

                                isLoggedIn = true
                            }
                        )
                    }
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
    onLoginSuccess: () -> Unit // Esta lambda se llama desde LoginScreen
) {
    var currentScreen by remember { mutableStateOf("login") }
    var registerData by remember { mutableStateOf<RegisterData?>(null) }
    var documentsData by remember { mutableStateOf<DocumentsData?>(null) }

    // ViewModel compartido para todo el flujo de registro
    val registerViewModel: RegisterViewModel = viewModel()

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
                        // 1. Guardar la sesión (usando tu tipo 'User')
                        sessionManager.saveSession(
                            accessToken = token,
                            tokenType = tokenType,
                            expiresAt = expiresAt,
                            user = user
                        )

                        // 2. Llamar al callback de MainActivity (que contiene la lógica del tour y el cambio de isLoggedIn)
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
                    },
                    viewModel = registerViewModel
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
                    },
                    viewModel = registerViewModel
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
                        // Limpiar la sesión temporal del registro
                        registerViewModel.clearSession()
                        registerViewModel.resetStates()
                        currentScreen = "login"
                    },
                    viewModel = registerViewModel
                )
            }
        }
    }
}