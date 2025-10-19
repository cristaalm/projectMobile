package com.renova.mobile.screens

import android.content.Context
import android.content.res.Configuration
import com.renova.mobile.R
import java.util.Locale

object ErrorMessageMapper {
    fun mapLoginError(backendMessage: String, statusCode: Int, context: Context): String {
        // 🌍 Obtener el idioma de la app usando el mismo SharedPreferences que LocaleHelper
        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val appLanguage = prefs.getString("Locale.Helper.Selected.Language", "en") ?: "en"

        // Crear contexto con el idioma de la app (igual que LocaleHelper.setLocale)
        val locale = Locale(appLanguage)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        val localizedContext = context.createConfigurationContext(config)

        return mapError(backendMessage, statusCode, localizedContext)
    }


        private fun mapError(backendMessage: String, statusCode: Int, context: Context): String {
            return when (statusCode) {
                401 -> {
                    // API: "Correo electrónico o contraseña incorrectos."
                    context.getString(R.string.error_invalid_credentials)
                }

                403 -> {
                    // API: "Tu cuenta ha sido desactivada por un administrador."
                    context.getString(R.string.error_account_disabled)
                }

                422 -> {
                    // API: "Correo electrónico o contraseña incorrectos."
                    // También puede venir: "The selected email is invalid."
                    context.getString(R.string.error_validation_failed)
                }

                500 -> {
                    // API puede devolver dos mensajes diferentes:
                    // message: "Ocurrió un error inesperado al intentar iniciar sesión."
                    // errors: "Error interno del servidor."
                    when {
                        backendMessage.contains("Error interno del servidor", ignoreCase = true) -> {
                            context.getString(R.string.error_server_internal)
                        }
                        else -> {
                            context.getString(R.string.error_server_error)
                        }
                    }
                }

                -1 -> {
                    // Error de red/conexión (no viene de la API)
                    context.getString(R.string.error_network)
                }

                else -> {
                    // Error genérico
                    context.getString(R.string.error_generic)
                }
            }
        }

        /**
         * Versión simplificada que solo usa el status code
         * (útil si no necesitas el mensaje del backend)
         */
        fun mapLoginErrorByCode(statusCode: Int, context: Context): String {
            return when (statusCode) {
                401 -> context.getString(R.string.error_invalid_credentials)
                403 -> context.getString(R.string.error_account_disabled)
                422 -> context.getString(R.string.error_validation_failed)
                500 -> context.getString(R.string.error_server_error)
                -1 -> context.getString(R.string.error_network)
                else -> context.getString(R.string.error_generic)
            }
        }
    }