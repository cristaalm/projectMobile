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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.renova.mobile.network.RegisterFcmTokenRequest
import com.google.firebase.messaging.FirebaseMessaging
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import android.widget.Toast

// --- IMPORTS AÑADIDOS DE LA RAMA 'tour' ---
import com.renova.mobile.ui.tour.LocalTourState
import com.renova.mobile.ui.tour.TourState
import com.renova.mobile.network.User // Import necesario para `saveSession`

class MainActivity : ComponentActivity() {
    val valorPuntos = 0.1

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

        // --- LÓGICA DE 'develop': Pedir permiso de notificaciones ---
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            if (!hasPermission) {
                androidx.core.app.ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }
        // --- FIN LÓGICA 'develop' ---

        enableEdgeToEdge()
        setContent {
            RenovaTheme {
                // --- LÓGICA DE 'tour': Usar `remember` para el SessionManager ---
                val sessionManager = remember { SessionManager(this) }
                val languageViewModel: LanguageViewModel = viewModel()

                LaunchedEffect(Unit) {
                    languageViewModel.resetUpdating()
                }

                val isUpdatingLanguage by languageViewModel.isUpdating.collectAsState()

                // --- MODIFICACIÓN: Pasar sessionManager a TourState ---
                val tourState = remember(sessionManager) { TourState(sessionManager) }

                HideSystemNavigation()

                var isLoggedIn by remember { mutableStateOf(sessionManager.isLoggedIn()) }

                // --- LÓGICA DE 'develop': Registro de token FCM robusto (con obtención de token fresco) ---
                LaunchedEffect(isLoggedIn) {
                    if (isLoggedIn) {
                        val userId = sessionManager.getUser()?.id

                        try {
                            ApiClient.init(this@MainActivity)
                        } catch (e: Exception) {
                            Log.e("FCM", "Error inicializando ApiClient", e)
                        }

                        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                            val candidate = if (task.isSuccessful) task.result else null
                            val cached = sessionManager.getFcmToken()
                            val token = if (!candidate.isNullOrBlank()) candidate else cached

                            if (token.isNullOrBlank() || userId == null) {
                                Log.w("FCM", "No se obtuvo token o userId para registrar tras login")
                                return@addOnCompleteListener
                            }

                            if (token != cached) {
                                sessionManager.saveFcmToken(token)
                            }

                            // Toast.makeText(this@MainActivity, "FCM Token fresco: ${token}", Toast.LENGTH_LONG).show()

                            this@MainActivity.lifecycleScope.launch(Dispatchers.IO) {
                                try {
                                    val resp = ApiClient.apiService.registerFcmToken(
                                        RegisterFcmTokenRequest(userId = userId, token = token)
                                    )
                                    if (resp.isSuccessful && resp.body()?.success == true) {
                                        Log.d("FCM", "Token FCM registrado/actualizado para userId=$userId")
                                    } else {
                                        Log.e("FCM", "Error registrando token FCM: ${resp.code()} ${resp.body()?.message}")
                                    }
                                } catch (e: Exception) {
                                    Log.e("FCM", "Excepción registrando token FCM", e)
                                }
                            }
                        }
                    }
                }
                // --- FIN LÓGICA 'develop' ---

                // --- LÓGICA DE 'tour': Proveedor de TourState ---
                CompositionLocalProvider(LocalTourState provides tourState) {
                    if (isLoggedIn) {
                        AppNavigation(
                            // --- LÓGICA DE 'tour': Pasar sessionManager ---
                            sessionManager = sessionManager,
                            // --- LÓGICA DE 'develop': Anular registro FCM en Logout ---
                            onLogout = {
                                val userId = sessionManager.getUser()?.id
                                val token = sessionManager.getFcmToken()
                                try {
                                    ApiClient.init(this@MainActivity)
                                } catch (_: Exception) {}
                                this@MainActivity.lifecycleScope.launch(Dispatchers.IO) {
                                    try {
                                        if (userId != null && !token.isNullOrBlank()) {
                                            val resp = ApiClient.apiService.unregisterFcmToken(
                                                com.renova.mobile.network.UnregisterFcmTokenRequest(userId = userId, token = token!!)
                                            )
                                            android.util.Log.d(
                                                "FCM",
                                                "Unregister token: HTTP ${resp.code()}, success=${resp.body()?.success}, message='${resp.body()?.message}'"
                                            )
                                        } else {
                                            android.util.Log.w("FCM", "No userId/token to unregister on logout")
                                        }
                                    } catch (e: Exception) {
                                        android.util.Log.e("FCM", "Error unregistering FCM token", e)
                                    }
                                }
                                sessionManager.clearFcmToken()
                                sessionManager.logout()
                                isLoggedIn = false
                            },
                            // --- FIN LÓGICA 'develop' ---
                            languageViewModel = languageViewModel,
                            isUpdatingLanguage = isUpdatingLanguage,
                            pointToMxn = valorPuntos
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
                        // ✅ Solo resetear estados de UI al iniciar
                        registerViewModel.resetUIStates()
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
                        //  Limpiar todo cuando vuelve al login
                        registerViewModel.resetAll()
                        registerData = null
                        documentsData = null
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
                        // ✅ Solo resetear estado de documentos, mantener datos del registro
                        registerViewModel.resetUIStates()
                        documentsData = null
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
                        // Solo resetear estado de selfie, mantener documentos
                        registerViewModel.resetUIStates()
                        currentScreen = "register_documents"
                    },
                    onComplete = {
                        // SOLO navegar, NO limpiar todavía
                        currentScreen = "login"
                        // La limpieza se hará desde VerificationScreen después de navegar
                    },
                    viewModel = registerViewModel
                )
            }}
    }
}