package com.renova.mobile.utils

import android.content.Context
import android.content.SharedPreferences
import com.renova.mobile.network.User
import com.renova.mobile.network.UserData
import com.google.gson.Gson
import android.util.Log

class SessionManager(context: Context) {
    private val appContext: Context = context.applicationContext
    private val sharedPreferences: SharedPreferences =
        appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PREF_NAME = "renova_session"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_TOKEN_TYPE = "token_type"
        private const val KEY_EXPIRES_AT = "expires_at"
        private const val KEY_USER = "user"
        private const val KEY_USER_DATA = "user_data"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_FCM_TOKEN = "fcm_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_REMEMBER_ME = "remember_me"
        private const val KEY_FIRST_LOGIN_PREFIX = "first_login_complete_user_"
        private const val KEY_TOUR_SYNC_PREFIX = "tour_sync_user_"
        // NUEVO: Para verificar si ya hemos sincronizado el tour con la API
        private const val KEY_TOUR_VERIFIED_PREFIX = "tour_verified_user_"
    }

    fun saveSession(
        accessToken: String,
        tokenType: String?,
        expiresAt: String?,
        user: User?,
        rememberMe: Boolean = false
    ) {
        sharedPreferences.edit().apply {
            putString(KEY_ACCESS_TOKEN, accessToken)
            putString(KEY_TOKEN_TYPE, tokenType ?: "Bearer")
            putString(KEY_EXPIRES_AT, expiresAt)
            putString(KEY_USER, gson.toJson(user))
            putBoolean(KEY_IS_LOGGED_IN, true)
            putBoolean(KEY_REMEMBER_ME, rememberMe)
            user?.let { putInt(KEY_USER_ID, it.id) }
            apply()
        }
        Log.d("SessionManager", "Sesión guardada con rememberMe=$rememberMe")

        // NUEVO: Sincronizar estado del tour con la API
        user?.let { syncTourState(it) }
    }

    fun saveUser(user: UserData) {
        sharedPreferences.edit().apply {
            putString(KEY_USER_DATA, gson.toJson(user))
            putInt(KEY_USER_ID, user.id)
            apply()
        }
        Log.d("SessionManager", "UserData guardado: ${user.name} - Points: ${user.points_month}")
    }

    fun hasRememberMe(): Boolean {
        return sharedPreferences.getBoolean(KEY_REMEMBER_ME, false)
    }

    fun isLoggedIn(): Boolean {
        val hasSession = sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false) &&
                !getAccessToken().isNullOrEmpty()

        if (!hasSession) return false

        if (hasRememberMe()) {
            Log.d("SessionManager", "Sesión válida con Remember Me activo")
            return true
        }

        val isExpired = isTokenExpired()
        if (isExpired) {
            Log.d("SessionManager", "Token expirado sin Remember Me")
            return false
        }

        return true
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

    fun logout() {
        Log.d("SessionManager", "Cerrando sesión...")
        try {
            sharedPreferences.edit().clear().apply()
            clearAppStorage()
        } catch (e: Exception) {
            Log.e("SessionManager", "Error limpiando datos/cache de la app en logout", e)
        }
        Log.d("SessionManager", "Sesión cerrada. isLoggedIn: ${isLoggedIn()}")
    }

    private fun clearAppStorage() {
        try {
            deleteChildren(appContext.cacheDir)
            deleteChildren(appContext.codeCacheDir)
            appContext.externalCacheDir?.let { deleteChildren(it) }
            deleteChildren(appContext.filesDir)
        } catch (e: Exception) {
            Log.e("SessionManager", "Error limpiando almacenamiento de la app", e)
        }
    }

    private fun deleteChildren(dir: java.io.File?) {
        if (dir == null || !dir.exists()) return
        dir.listFiles()?.forEach { file ->
            try {
                if (file.isDirectory) {
                    deleteChildren(file)
                }
                if (!file.delete()) {
                    Log.w("SessionManager", "No se pudo borrar: ${file.absolutePath}")
                }
            } catch (e: Exception) {
                Log.w("SessionManager", "Error borrando: ${file.absolutePath}", e)
            }
        }
    }

    fun isTokenExpired(): Boolean {
        val expiresAt = getExpiresAt() ?: return false

        return try {
            val expiryDate = parseExpiryDate(expiresAt)
            if (expiryDate == Long.MAX_VALUE) return false

            val currentTime = System.currentTimeMillis()
            val isExpired = currentTime >= expiryDate

            Log.d("SessionManager", "Token expiry check: expiresAt=$expiresAt, expired=$isExpired")
            isExpired
        } catch (e: Exception) {
            Log.e("SessionManager", "Error parsing expiry date: ${e.message}")
            false
        }
    }

    private fun parseExpiryDate(expiresAt: String): Long {
        return try {
            val formatter = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
            formatter.timeZone = java.util.TimeZone.getTimeZone("UTC")
            formatter.parse(expiresAt)?.time ?: Long.MAX_VALUE
        } catch (e: Exception) {
            try {
                val formatter = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US)
                formatter.parse(expiresAt)?.time ?: Long.MAX_VALUE
            } catch (e2: Exception) {
                Log.e("SessionManager", "No se pudo parsear fecha: $expiresAt")
                Long.MAX_VALUE
            }
        }
    }

    fun isTokenExpiringSoon(): Boolean {
        if (hasRememberMe()) return false

        val expiresAt = getExpiresAt() ?: return false

        return try {
            val expiryDate = parseExpiryDate(expiresAt)
            if (expiryDate == Long.MAX_VALUE) return false

            val currentTime = System.currentTimeMillis()
            val thirtyMinutesInMillis = 30 * 60 * 1000
            val timeUntilExpiry = expiryDate - currentTime

            timeUntilExpiry in 1..thirtyMinutesInMillis
        } catch (e: Exception) {
            false
        }
    }

    fun saveAuthToken(token: String, tokenType: String) {
        sharedPreferences.edit().apply {
            putString(KEY_ACCESS_TOKEN, token)
            putString(KEY_TOKEN_TYPE, tokenType)
            apply()
        }
    }

    fun getUserId(): Int? {
        val userData = getUserData()
        if (userData != null) {
            return userData.id
        }

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
            remove(KEY_IS_LOGGED_IN)
            remove(KEY_REMEMBER_ME)
            apply()
        }
    }

    fun saveFcmToken(token: String) {
        sharedPreferences.edit().apply {
            putString(KEY_FCM_TOKEN, token)
            apply()
        }
        Log.d("SessionManager", "FCM Token guardado: ${token.substring(0, minOf(10, token.length))}...")
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
        return sharedPreferences.getString(KEY_FCM_TOKEN, null)
    }

    fun clearFcmToken() {
        sharedPreferences.edit().apply {
            remove(KEY_FCM_TOKEN)
            apply()
        }
        Log.d("SessionManager", "FCM Token limpiado.")
    }

    // --- FUNCIONES PARA EL TOUR MEJORADAS ---

    private fun getTourKeyForUser(userId: Int): String {
        return "$KEY_FIRST_LOGIN_PREFIX$userId"
    }

    private fun getTourSyncKeyForUser(userId: Int): String {
        return "$KEY_TOUR_SYNC_PREFIX$userId"
    }

    private fun getTourVerifiedKeyForUser(userId: Int): String {
        return "$KEY_TOUR_VERIFIED_PREFIX$userId"
    }

    /**
     * Verifica si es la primera vez que ESTE USUARIO inicia sesión.
     * Ahora verifica tanto el estado local como el de la API
     */
    fun isFirstLogin(): Boolean {
        val userId = getUserId()
        if (userId == null) {
            Log.w("SessionManager", "isFirstLogin: No hay userId, retornando false")
            return false
        }

        // NUEVO: Primero verificar si ya hemos sincronizado con la API en esta sesión
        val verifiedKey = getTourVerifiedKeyForUser(userId)
        val alreadyVerified = sharedPreferences.getBoolean(verifiedKey, false)

        if (alreadyVerified) {
            Log.d("SessionManager", "isFirstLogin: Ya verificamos con la API para userId=$userId")
            val tourKey = getTourKeyForUser(userId)
            val isComplete = sharedPreferences.getBoolean(tourKey, false)
            Log.d("SessionManager", "isFirstLogin: Estado local - tourKey=$tourKey, isComplete=$isComplete")
            return !isComplete
        }

        // Si no hemos verificado, verificar el estado del usuario actual
        val user = getUser()
        val userData = getUserData()

        // NUEVO: Verificar si el usuario ya completó el tour según la API
        val tourCompletedFromApi = user?.tour_completed == true

        Log.d("SessionManager", "🔍 Verificación completa de tour - userId: $userId")
        Log.d("SessionManager", "🔍 tour_completed from API: $tourCompletedFromApi")
        Log.d("SessionManager", "🔍 User: ${user != null}, UserData: ${userData != null}")

        if (tourCompletedFromApi) {
            Log.d("SessionManager", "✅ API indica que el tour YA fue completado para userId=$userId")
            setFirstLoginComplete()
            markTourAsVerified(userId)
            return false
        }

        // Si la API no tiene información, verificar bandera local
        val tourKey = getTourKeyForUser(userId)
        val isComplete = sharedPreferences.getBoolean(tourKey, false)

        Log.d("SessionManager", "🔍 Estado local - tourKey=$tourKey, isComplete=$isComplete")

        // Marcar como verificado para no volver a verificar en esta sesión
        markTourAsVerified(userId)

        return !isComplete
    }

    /**
     * Marca que ESTE USUARIO ya completó el tour.
     */
    fun setFirstLoginComplete() {
        val userId = getUserId()
        if (userId == null) {
            Log.w("SessionManager", "setFirstLoginComplete: No hay userId, no se puede marcar")
            return
        }

        val tourKey = getTourKeyForUser(userId)
        val syncKey = getTourSyncKeyForUser(userId)

        sharedPreferences.edit().apply {
            putBoolean(tourKey, true)
            putBoolean(syncKey, true)
            apply()
        }
        Log.d("SessionManager", "✅ setFirstLoginComplete para userId=$userId: $tourKey = true, $syncKey = true")
    }

    /**
     * NUEVO: Marca que ya verificamos el estado del tour con la API
     */
    private fun markTourAsVerified(userId: Int) {
        val verifiedKey = getTourVerifiedKeyForUser(userId)
        sharedPreferences.edit().putBoolean(verifiedKey, true).apply()
        Log.d("SessionManager", "✅ Tour verificado para userId=$userId")
    }

    /**
     * NUEVO: Limpia la verificación del tour (útil para testing)
     */
    fun clearTourVerification() {
        val userId = getUserId()
        if (userId == null) {
            Log.w("SessionManager", "clearTourVerification: No hay userId")
            return
        }

        val verifiedKey = getTourVerifiedKeyForUser(userId)
        sharedPreferences.edit().remove(verifiedKey).apply()
        Log.d("SessionManager", "🔄 Verificación de tour limpiada para userId=$userId")
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
        val syncKey = getTourSyncKeyForUser(userId)
        val verifiedKey = getTourVerifiedKeyForUser(userId)

        sharedPreferences.edit().apply {
            remove(tourKey)
            remove(syncKey)
            remove(verifiedKey)
            apply()
        }
        Log.d("SessionManager", "DEBUG: Bandera $tourKey, $syncKey y $verifiedKey reseteada para userId=$userId")
    }

    /**
     * OPCIONAL: Limpia las banderas de tour de TODOS los usuarios
     */
    fun clearAllTourFlags() {
        val editor = sharedPreferences.edit()
        sharedPreferences.all.keys
            .filter { it.startsWith(KEY_FIRST_LOGIN_PREFIX) ||
                    it.startsWith(KEY_TOUR_SYNC_PREFIX) ||
                    it.startsWith(KEY_TOUR_VERIFIED_PREFIX) }
            .forEach { editor.remove(it) }
        editor.apply()
        Log.d("SessionManager", "DEBUG: Todas las banderas de tour limpiadas")
    }

    // NUEVO: Sincronizar estado del tour desde User
    private fun syncTourState(user: User) {
        val userId = user.id
        val tourCompleted = user.tour_completed == true

        val syncKey = getTourSyncKeyForUser(userId)
        val currentSyncState = sharedPreferences.getBoolean(syncKey, false)

        Log.d("SessionManager", "🔄 syncTourState - userId: $userId, tourCompleted: $tourCompleted, currentSyncState: $currentSyncState")

        // Solo actualizar si el estado ha cambiado
        if (currentSyncState != tourCompleted) {
            sharedPreferences.edit().putBoolean(syncKey, tourCompleted).apply()

            // Si la API dice que el tour está completo, actualizar la bandera local
            if (tourCompleted) {
                setFirstLoginComplete()
            }

            Log.d("SessionManager", "✅ Tour state sincronizado para userId=$userId: completed=$tourCompleted")
        }

        // Limpiar verificación para forzar nueva verificación
        clearTourVerification()
    }

    fun getUserData(): UserData? {
        val userJson = sharedPreferences.getString(KEY_USER_DATA, null)
        return if (userJson != null) {
            try {
                gson.fromJson(userJson, UserData::class.java)
            } catch (e: Exception) {
                Log.e("SessionManager", "Error parsing UserData JSON", e)
                null
            }
        } else {
            Log.w("SessionManager", "UserData JSON is null")
            null
        }
    }
}