package com.renova.mobile.utils

import android.content.Context
import android.content.SharedPreferences
import com.renova.mobile.network.User
import com.google.gson.Gson

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
            putString(KEY_USER, gson.toJson(user))
            putBoolean(KEY_IS_LOGGED_IN, true)
            user?.let { putInt(KEY_USER_ID, it.id) }
            apply()
        }
    }

    // Verificar si hay sesión activa
    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false) &&
                !getAccessToken().isNullOrEmpty()
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

    // Verificar si el token está próximo a expirar (opcional)
    fun isTokenExpiringSoon(): Boolean {
        val expiresAt = getExpiresAt()
        return if (expiresAt != null) {
            try {
                // Aquí puedes agregar lógica para verificar si expira pronto
                // Por ejemplo, comparar con la fecha actual
                false // Por ahora devolver false
            } catch (e: Exception) {
                false
            }
        } else false
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

    // Limpiar solo el token temporal (para cuando falla el registro o se completa)
    fun clearAuthToken() {
        sharedPreferences.edit().apply {
            remove(KEY_ACCESS_TOKEN)
            remove(KEY_TOKEN_TYPE)
            remove(KEY_IS_LOGGED_IN)
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