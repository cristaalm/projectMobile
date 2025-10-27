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

        // --- PREFIJO PARA EL TOUR POR USUARIO ---
        private const val KEY_FIRST_LOGIN_PREFIX = "first_login_complete_user_"
    }

    // Guardar sesión completa
    fun saveSession(
        accessToken: String,
        tokenType: String?,
        expiresAt: String?,
        user: User?,
    ) {
        sharedPreferences.edit().apply {
            putString(KEY_ACCESS_TOKEN, accessToken)
            putString(KEY_TOKEN_TYPE, tokenType ?: "Bearer")
            putString(KEY_EXPIRES_AT, expiresAt)
            if (user != null) {
                putString(KEY_USER, gson.toJson(user))
                putInt(KEY_USER_ID, user.id)
            } else {
                remove(KEY_USER)
                remove(KEY_USER_ID)
            }
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
        Log.d("SessionManager", "Sesión guardada. AccessToken: ${accessToken.substring(0, minOf(10, accessToken.length))}...")
    }

    fun isLoggedIn(): Boolean {
        val loggedIn = sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
        val tokenExists = !getAccessToken().isNullOrEmpty()
        return loggedIn && tokenExists
    }

    fun getAccessToken(): String? {
        return sharedPreferences.getString(KEY_ACCESS_TOKEN, null)
    }

    fun getTokenType(): String? {
        return sharedPreferences.getString(KEY_TOKEN_TYPE, "Bearer")
    }

    fun getExpiresAt(): String? {
        return sharedPreferences.getString(KEY_EXPIRES_AT, null)
    }

    fun getUser(): User? {
        val userJson = sharedPreferences.getString(KEY_USER, null)
        return if (userJson != null) {
            try {
                gson.fromJson(userJson, User::class.java)
            } catch (e: Exception) {
                Log.e("SessionManager", "Error parsing User JSON", e)
                null
            }
        } else {
            Log.w("SessionManager", "User JSON is null")
            null
        }
    }

    fun getAuthToken(): String? {
        val token = getAccessToken()
        val type = getTokenType()
        return if (token != null && type != null) {
            if (token.startsWith("$type ", ignoreCase = true)) {
                token
            } else {
                "$type $token"
            }
        } else {
            null
        }
    }

    fun logout() {
        Log.d("SessionManager", "Cerrando sesión...")
        sharedPreferences.edit().apply {
            remove(KEY_ACCESS_TOKEN)
            remove(KEY_TOKEN_TYPE)
            remove(KEY_EXPIRES_AT)
            remove(KEY_USER)
            remove(KEY_IS_LOGGED_IN)
            remove(KEY_USER_ID)
            remove(KEY_FCM_TOKEN)
            // NO borramos las banderas de tour porque son por usuario
            apply()
        }
        Log.d("SessionManager", "Sesión cerrada. isLoggedIn: ${isLoggedIn()}")
    }

    fun isTokenExpiringSoon(): Boolean {
        return false
    }

    fun saveAuthToken(token: String, tokenType: String) {
        sharedPreferences.edit().apply {
            putString(KEY_ACCESS_TOKEN, token)
            putString(KEY_TOKEN_TYPE, tokenType)
            apply()
        }
    }

    fun getUserId(): Int? {
        val user = getUser()
        if (user != null) {
            return user.id
        }
        val userIdFromPref = sharedPreferences.getInt(KEY_USER_ID, -1)
        return if (userIdFromPref != -1) userIdFromPref else null
    }

    fun saveUserId(userId: Int) {
        sharedPreferences.edit().putInt(KEY_USER_ID, userId).apply()
    }

    fun clearAuthToken() {
        sharedPreferences.edit().apply {
            remove(KEY_ACCESS_TOKEN)
            remove(KEY_TOKEN_TYPE)
            apply()
        }
    }

    // ===== FCM token helpers =====
    fun saveFcmToken(token: String) {
        sharedPreferences.edit().apply {
            putString(KEY_FCM_TOKEN, token)
            apply()
        }
        Log.d("SessionManager", "FCM Token guardado: ${token.substring(0, minOf(10, token.length))}...")
    }

    fun getFcmToken(): String? {
        return sharedPreferences.getString(KEY_FCM_TOKEN, null)
    }

    fun clearFcmToken() {
        sharedPreferences.edit().apply {
            remove(KEY_FCM_TOKEN)
            apply()
        }
        Log.d("SessionManager", "FCM Token limpiado.")
    }

    // --- FUNCIONES PARA EL TOUR (POR USUARIO) ---

    /**
     * Genera la clave única para este usuario
     */
    private fun getTourKeyForUser(userId: Int): String {
        return "$KEY_FIRST_LOGIN_PREFIX$userId"
    }

    /**
     * Verifica si es la primera vez que ESTE USUARIO inicia sesión.
     * Requiere que ya haya un usuario logueado (getUser() != null)
     * @return `true` si este usuario NO ha completado el tour
     */
    fun isFirstLogin(): Boolean {
        val userId = getUserId()
        if (userId == null) {
            Log.w("SessionManager", "isFirstLogin: No hay userId, retornando false")
            return false
        }

        val tourKey = getTourKeyForUser(userId)
        val isComplete = sharedPreferences.getBoolean(tourKey, false)
        Log.d("SessionManager", "isFirstLogin para userId=$userId: $tourKey = $isComplete. Returning: ${!isComplete}")
        return !isComplete
    }

    /**
     * Marca que ESTE USUARIO ya completó el tour.
     * Requiere que ya haya un usuario logueado.
     */
    fun setFirstLoginComplete() {
        val userId = getUserId()
        if (userId == null) {
            Log.w("SessionManager", "setFirstLoginComplete: No hay userId, no se puede marcar")
            return
        }

        val tourKey = getTourKeyForUser(userId)
        sharedPreferences.edit().putBoolean(tourKey, true).apply()
        Log.d("SessionManager", "setFirstLoginComplete para userId=$userId: $tourKey = true")
    }

    /**
     * SOLO PARA DEBUG: Resetea la bandera del usuario actual
     */
    fun resetFirstLoginFlag() {
        val userId = getUserId()
        if (userId == null) {
            Log.w("SessionManager", "resetFirstLoginFlag: No hay userId")
            return
        }

        val tourKey = getTourKeyForUser(userId)
        sharedPreferences.edit().remove(tourKey).apply()
        Log.d("SessionManager", "DEBUG: Bandera $tourKey reseteada para userId=$userId")
    }

    /**
     * OPCIONAL: Limpia las banderas de tour de TODOS los usuarios
     * (útil si quieres hacer limpieza general)
     */
    fun clearAllTourFlags() {
        val editor = sharedPreferences.edit()
        sharedPreferences.all.keys
            .filter { it.startsWith(KEY_FIRST_LOGIN_PREFIX) }
            .forEach { editor.remove(it) }
        editor.apply()
        Log.d("SessionManager", "DEBUG: Todas las banderas de tour limpiadas")
    }
}