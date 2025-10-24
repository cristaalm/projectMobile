package com.renova.mobile.utils

import android.content.Context
import android.content.SharedPreferences
import com.renova.mobile.network.User
import com.google.gson.Gson
import android.util.Log // Asegúrate de tener este import

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

        // --- CLAVE PARA EL TOUR (REQUERIMIENTO 1) ---
        private const val KEY_FIRST_LOGIN_COMPLETE = "first_login_complete_v2" // v2 para asegurar que no haya datos viejos
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
            // Guardar usuario como JSON solo si no es nulo
            if (user != null) {
                putString(KEY_USER, gson.toJson(user))
                putInt(KEY_USER_ID, user.id) // Guardar ID también aquí
            } else {
                remove(KEY_USER) // Limpiar si el usuario es nulo
                remove(KEY_USER_ID)
            }
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
        Log.d("SessionManager", "Sesión guardada. AccessToken: ${accessToken.substring(0, minOf(10, accessToken.length))}...")
    }


    // Verificar si hay sesión activa
    fun isLoggedIn(): Boolean {
        val loggedIn = sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
        val tokenExists = !getAccessToken().isNullOrEmpty()
        // Log.d("SessionManager", "isLoggedIn Check: loggedIn=$loggedIn, tokenExists=$tokenExists")
        return loggedIn && tokenExists
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
        // Log.d("SessionManager", "getUser JSON: $userJson") // Log para depurar
        return if (userJson != null) {
            try {
                gson.fromJson(userJson, User::class.java)
            } catch (e: Exception) {
                Log.e("SessionManager", "Error parsing User JSON", e)
                null // Error al parsear
            }
        } else {
            Log.w("SessionManager", "User JSON is null")
            null // No hay JSON guardado
        }
    }


    // Obtener token completo para headers HTTP
    fun getAuthToken(): String? {
        val token = getAccessToken()
        val type = getTokenType()
        return if (token != null && type != null) {
            // Asegurarse de que el tipo no se duplique si ya viene incluido
            if (token.startsWith("$type ", ignoreCase = true)) {
                token
            } else {
                "$type $token"
            }
        } else {
            null
        }
    }


    // Cerrar sesión
    fun logout() {
        Log.d("SessionManager", "Cerrando sesión...")
        sharedPreferences.edit().apply {
            // Borrar claves específicas en lugar de clear()
            remove(KEY_ACCESS_TOKEN)
            remove(KEY_TOKEN_TYPE)
            remove(KEY_EXPIRES_AT)
            remove(KEY_USER)
            remove(KEY_IS_LOGGED_IN)
            remove(KEY_USER_ID)
            // Mantenemos KEY_FIRST_LOGIN_COMPLETE
            // Mantenemos KEY_FCM_TOKEN (quizás quieras limpiarlo también)
            remove(KEY_FCM_TOKEN) // Limpiamos FCM token al cerrar sesión
            apply()
        }
        Log.d("SessionManager", "Sesión cerrada. isLoggedIn: ${isLoggedIn()}")
    }


    // Verificar si el token está próximo a expirar (opcional)
    fun isTokenExpiringSoon(): Boolean {
        // Implementa lógica si necesitas verificar expiración
        return false
    }

    // Guardar solo el token (para registro temporal)
    fun saveAuthToken(token: String, tokenType: String) {
        sharedPreferences.edit().apply {
            putString(KEY_ACCESS_TOKEN, token)
            putString(KEY_TOKEN_TYPE, tokenType)
            // No marcamos como logged in aquí
            apply()
        }
    }

    fun getUserId(): Int? {
        // Intenta obtenerlo primero del User object guardado
        val user = getUser()
        if (user != null) {
            // Log.d("SessionManager", "getUserId from User object: ${user.id}")
            return user.id
        }
        // Si no, intenta desde la clave separada (fallback)
        val userIdFromPref = sharedPreferences.getInt(KEY_USER_ID, -1)
        // Log.d("SessionManager", "getUserId from KEY_USER_ID pref: $userIdFromPref")
        return if (userIdFromPref != -1) userIdFromPref else null
    }


    fun saveUserId(userId: Int) {
        // Obsoleto si guardamos el User completo, pero lo mantenemos por si acaso
        sharedPreferences.edit().putInt(KEY_USER_ID, userId).apply()
    }

    // Limpiar solo el token temporal (para registro)
    fun clearAuthToken() {
        sharedPreferences.edit().apply {
            remove(KEY_ACCESS_TOKEN)
            remove(KEY_TOKEN_TYPE)
            // No tocamos KEY_IS_LOGGED_IN aquí
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

    // --- FUNCIONES PARA EL TOUR (REQUERIMIENTO 1) ---

    /**
     * Verifica si es la primera vez que el usuario inicia sesión DESPUÉS de instalar la app
     * (o después de borrar datos).
     * @return `true` si la bandera 'first_login_complete_v2' NO está marcada como 'true'.
     */
    fun isFirstLogin(): Boolean {
        val isComplete = sharedPreferences.getBoolean(KEY_FIRST_LOGIN_COMPLETE, false)
        Log.d("SessionManager", "isFirstLogin Check: $KEY_FIRST_LOGIN_COMPLETE = $isComplete. Returning: ${!isComplete}")
        return !isComplete
    }

    /**
     * Marca que el tour de bienvenida ya se ha mostrado (o se va a mostrar).
     * Guarda 'true' en la bandera 'first_login_complete_v2'.
     */
    fun setFirstLoginComplete() {
        sharedPreferences.edit().putBoolean(KEY_FIRST_LOGIN_COMPLETE, true).apply()
        Log.d("SessionManager", "setFirstLoginComplete: $KEY_FIRST_LOGIN_COMPLETE = true")
    }

    // --- Función de DEBUG para resetear la bandera del tour ---
    fun resetFirstLoginFlag() {
        sharedPreferences.edit().remove(KEY_FIRST_LOGIN_COMPLETE).apply()
        Log.d("SessionManager", "DEBUG: Bandera $KEY_FIRST_LOGIN_COMPLETE reseteada.")
    }
}

