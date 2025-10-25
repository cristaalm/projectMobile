package com.renova.mobile.utils

import android.content.Context
import android.content.SharedPreferences
import com.renova.mobile.network.User
import com.google.gson.Gson
import android.util.Log

class SessionManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PREF_NAME = "renova_session"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_TOKEN_TYPE = "token_type"
        private const val KEY_EXPIRES_AT = "expires_at"
        private const val KEY_USER = "user"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_FCM_TOKEN = "fcm_token"
        private const val KEY_USER_ID = "user_id"
        // NUEVO: Para manejar remember me
        private const val KEY_REMEMBER_ME = "remember_me"
    }

    // MODIFICADO: Guardar sesión con remember me
    fun saveSession(
        accessToken: String,
        tokenType: String?,
        expiresAt: String?,
        user: User?,
        rememberMe: Boolean = false // NUEVO parámetro
    ) {
        sharedPreferences.edit().apply {
            putString(KEY_ACCESS_TOKEN, accessToken)
            putString(KEY_TOKEN_TYPE, tokenType ?: "Bearer")
            putString(KEY_EXPIRES_AT, expiresAt)
            putString(KEY_USER, gson.toJson(user))
            putBoolean(KEY_IS_LOGGED_IN, true)
            putBoolean(KEY_REMEMBER_ME, rememberMe) // NUEVO: Guardar preferencia
            user?.let { putInt(KEY_USER_ID, it.id) }
            apply()
        }
        Log.d("SessionManager", "Sesión guardada con rememberMe=$rememberMe")
    }

    //NUEVO: Verificar si tiene remember me activado
    fun hasRememberMe(): Boolean {
        return sharedPreferences.getBoolean(KEY_REMEMBER_ME, false)
    }

    //MODIFICADO: Verificar sesión considerando remember me y expiración
    fun isLoggedIn(): Boolean {
        val hasSession = sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false) &&
                !getAccessToken().isNullOrEmpty()

        if (!hasSession) return false

        // Si tiene remember me, no verificar expiración
        if (hasRememberMe()) {
            Log.d("SessionManager", "Sesión válida con Remember Me activo")
            return true
        }

        // Si no tiene remember me, verificar si el token expiró
        val isExpired = isTokenExpired()
        if (isExpired) {
            Log.d("SessionManager", "Token expirado sin Remember Me")
            logout() // Limpiar sesión expirada
            return false
        }

        return true
    }

    // Obtener token de acceso
    fun getAccessToken(): String? {
        return sharedPreferences.getString(KEY_ACCESS_TOKEN, null)
    }

    // Obtener tipo de token
    fun getTokenType(): String? {
        return sharedPreferences.getString(KEY_TOKEN_TYPE, "Bearer")
    }

    // Obtener fecha de expiración
    fun getExpiresAt(): String? {
        return sharedPreferences.getString(KEY_EXPIRES_AT, null)
    }

    // Obtener usuario
    fun getUser(): User? {
        val userJson = sharedPreferences.getString(KEY_USER, null)
        return if (userJson != null) {
            try {
                gson.fromJson(userJson, User::class.java)
            } catch (e: Exception) {
                null
            }
        } else null
    }

    // Obtener token completo para headers HTTP
    fun getAuthToken(): String? {
        val token = getAccessToken()
        Log.d("SessionManager", "getAuthToken: token = $token")
        val type = getTokenType()
        return if (token != null && type != null) {
            "$type $token"
        } else null
    }

    // Cerrar sesión
    fun logout() {
        sharedPreferences.edit().apply {
            clear()
            apply()
        }
    }

    //NUEVO: Verificar si el token expiró
    fun isTokenExpired(): Boolean {
        val expiresAt = getExpiresAt() ?: return false

        return try {
            // Formato esperado: "2025-10-24 12:00:00" o ISO 8601
            val expiryDate = parseExpiryDate(expiresAt)
            val currentTime = System.currentTimeMillis()
            val isExpired = currentTime >= expiryDate

            Log.d("SessionManager", "Token expiry check: expiresAt=$expiresAt, expired=$isExpired")
            isExpired
        } catch (e: Exception) {
            Log.e("SessionManager", "Error parsing expiry date: ${e.message}")
            false // En caso de error, asumir que no expiró
        }
    }

    //NUEVO: Parsear fecha de expiración
    private fun parseExpiryDate(expiresAt: String): Long {
        return try {
            // Intentar parsear ISO 8601
            val formatter = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
            formatter.timeZone = java.util.TimeZone.getTimeZone("UTC")
            formatter.parse(expiresAt)?.time ?: 0L
        } catch (e: Exception) {
            try {
                // Intentar parsear formato alternativo "yyyy-MM-dd HH:mm:ss"
                val formatter = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US)
                formatter.parse(expiresAt)?.time ?: 0L
            } catch (e2: Exception) {
                Log.e("SessionManager", "No se pudo parsear fecha: $expiresAt")
                Long.MAX_VALUE // Si no se puede parsear, asumir que nunca expira
            }
        }
    }

    // MODIFICADO: Verificar si está próximo a expirar (30 minutos antes)
    fun isTokenExpiringSoon(): Boolean {
        if (hasRememberMe()) return false // No aplicar si tiene remember me

        val expiresAt = getExpiresAt() ?: return false

        return try {
            val expiryDate = parseExpiryDate(expiresAt)
            val currentTime = System.currentTimeMillis()
            val thirtyMinutesInMillis = 30 * 60 * 1000
            val timeUntilExpiry = expiryDate - currentTime

            timeUntilExpiry in 1..thirtyMinutesInMillis
        } catch (e: Exception) {
            false
        }
    }

    // Guardar solo el token (para registro temporal)
    fun saveAuthToken(token: String, tokenType: String) {
        sharedPreferences.edit().apply {
            putString(KEY_ACCESS_TOKEN, token)
            putString(KEY_TOKEN_TYPE, tokenType)
            apply()
        }
    }

    fun getUserId(): Int? {
        val userId = sharedPreferences.getInt(KEY_USER_ID, -1)
        return if (userId != -1) { userId } else { null }
    }

    fun saveUserId(userId: Int) {
        sharedPreferences.edit().putInt(KEY_USER_ID, userId).apply()
    }

    // Limpiar solo el token temporal
    fun clearAuthToken() {
        sharedPreferences.edit().apply {
            remove(KEY_ACCESS_TOKEN)
            remove(KEY_TOKEN_TYPE)
            remove(KEY_IS_LOGGED_IN)
            remove(KEY_REMEMBER_ME)
            apply()
        }
    }

    // ===== FCM token helpers =====
    fun saveFcmToken(token: String) {
        sharedPreferences.edit().apply {
            putString(KEY_FCM_TOKEN, token)
            apply()
        }
    }

    fun getFcmToken(): String? {
        val t = sharedPreferences.getString(KEY_FCM_TOKEN, null)
        if (t.isNullOrBlank() || t == "fcm_token") {
            try {
                com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val fresh = task.result
                        if (!fresh.isNullOrBlank()) {
                            saveFcmToken(fresh)
                        }
                    }
                }
            } catch (_: Exception) { /* no-op */ }
        }
        return t
    }

    fun clearFcmToken() {
        sharedPreferences.edit().apply {
            remove(KEY_FCM_TOKEN)
            apply()
        }
    }
}