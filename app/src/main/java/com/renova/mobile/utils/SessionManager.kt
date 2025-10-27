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
        // DE 'develop'
        private const val KEY_REMEMBER_ME = "remember_me"
        // DE 'tour'
        private const val KEY_FIRST_LOGIN_PREFIX = "first_login_complete_user_"
    }

    // FUSIONADO: Guardar sesión con remember me y user.id
    fun saveSession(
        accessToken: String,
        tokenType: String?,
        expiresAt: String?,
        user: User?,
        rememberMe: Boolean = false // Parámetro de 'develop'
    ) {
        sharedPreferences.edit().apply {
            putString(KEY_ACCESS_TOKEN, accessToken)
            putString(KEY_TOKEN_TYPE, tokenType ?: "Bearer")
            putString(KEY_EXPIRES_AT, expiresAt)
            putString(KEY_USER, gson.toJson(user))
            putBoolean(KEY_IS_LOGGED_IN, true)
            putBoolean(KEY_REMEMBER_ME, rememberMe) // Lógica de 'develop'
            user?.let { putInt(KEY_USER_ID, it.id) } // Lógica de 'tour'
            apply()
        }
        Log.d("SessionManager", "Sesión guardada con rememberMe=$rememberMe")
    }

    //NUEVO: (de 'develop') Verificar si tiene remember me activado
    fun hasRememberMe(): Boolean {
        return sharedPreferences.getBoolean(KEY_REMEMBER_ME, false)
    }

    // FUSIONADO: (de 'develop') Verificar sesión considerando remember me y expiración
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
            // No llamar a logout() aquí, eso podría causar bucles.
            // La sesión se limpiará en el próximo inicio de sesión o reinicio.
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
                Log.e("SessionManager", "Error parsing User JSON", e)
                null
            }
        } else {
            Log.w("SessionManager", "User JSON is null")
            null
        }
    }

    // FUSIONADO: (de 'tour') Obtener token completo para headers HTTP (con chequeo de prefijo)
    fun getAuthToken(): String? {
        val token = getAccessToken()
        Log.d("SessionManager", "getAuthToken: token = $token")
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

    // FUSIONADO: (de 'tour') Cerrar sesión sin borrar banderas de tour
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
            remove(KEY_REMEMBER_ME) // Añadido de la lógica de 'develop'
            // NO borramos las banderas de tour (KEY_FIRST_LOGIN_PREFIX)
            apply()
        }
        Log.d("SessionManager", "Sesión cerrada. isLoggedIn: ${isLoggedIn()}")
    }

    //NUEVO: (de 'develop') Verificar si el token expiró
    fun isTokenExpired(): Boolean {
        val expiresAt = getExpiresAt() ?: return false // Si no hay fecha, no se puede expirar

        return try {
            val expiryDate = parseExpiryDate(expiresAt)
            if (expiryDate == Long.MAX_VALUE) return false // No se pudo parsear, asumir que no expira

            val currentTime = System.currentTimeMillis()
            val isExpired = currentTime >= expiryDate

            Log.d("SessionManager", "Token expiry check: expiresAt=$expiresAt, expired=$isExpired")
            isExpired
        } catch (e: Exception) {
            Log.e("SessionManager", "Error parsing expiry date: ${e.message}")
            false // En caso de error, asumir que no expiró
        }
    }

    //NUEVO: (de 'develop') Parsear fecha de expiración
    private fun parseExpiryDate(expiresAt: String): Long {
        return try {
            // Intentar parsear ISO 8601 (Formato común de API)
            val formatter = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
            formatter.timeZone = java.util.TimeZone.getTimeZone("UTC")
            formatter.parse(expiresAt)?.time ?: Long.MAX_VALUE
        } catch (e: Exception) {
            try {
                // Intentar parsear formato alternativo "yyyy-MM-dd HH:mm:ss"
                val formatter = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US)
                formatter.parse(expiresAt)?.time ?: Long.MAX_VALUE
            } catch (e2: Exception) {
                Log.e("SessionManager", "No se pudo parsear fecha: $expiresAt")
                Long.MAX_VALUE // Si no se puede parsear, asumir que nunca expira
            }
        }
    }

    // FUSIONADO: (de 'develop') Verificar si está próximo a expirar (con lógica de remember me)
    fun isTokenExpiringSoon(): Boolean {
        if (hasRememberMe()) return false // No aplicar si tiene remember me

        val expiresAt = getExpiresAt() ?: return false

        return try {
            val expiryDate = parseExpiryDate(expiresAt)
            if (expiryDate == Long.MAX_VALUE) return false // No se pudo parsear

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

    // FUSIONADO: (de 'tour') Getter de UserID más robusto
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

    // FUSIONADO: (de 'develop') Limpiar token temporal (incluye flags de login)
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
        Log.d("SessionManager", "FCM Token guardado: ${token.substring(0, minOf(10, token.length))}...")
    }

    // FUSIONADO: (de 'develop') Getter de FCM Token más robusto
    fun getFcmToken(): String? {
        val t = sharedPreferences.getString(KEY_FCM_TOKEN, null)
        // Si está vacío o es un placeholder, intentar obtener uno nuevo
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
        return sharedPreferences.getString(KEY_FCM_TOKEN, null) // Leer de nuevo por si se actualizó
    }

    fun clearFcmToken() {
        sharedPreferences.edit().apply {
            remove(KEY_FCM_TOKEN)
            apply()
        }
        Log.d("SessionManager", "FCM Token limpiado.")
    }

    // --- FUNCIONES PARA EL TOUR (POR USUARIO) (de 'tour') ---

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