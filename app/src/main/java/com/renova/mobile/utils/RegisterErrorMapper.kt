package com.renova.mobile.utils

import android.content.Context
import android.content.res.Configuration
import com.renova.mobile.R
import java.util.Locale

object RegisterErrorMapper {

    /**
     * Mapea los mensajes de error del endpoint /api/users/register
     * según el idioma configurado en la app
     */
    fun mapRegisterError(backendMessage: String, statusCode: Int, context: Context): String {
        val localizedContext = getLocalizedContext(context)

        return when (statusCode) {
            422 -> {
                // "Error al registrar el usuario."
                localizedContext.getString(R.string.error_register_user)
            }
            500 -> {
                // "Error al registrar el usuario." o "Error interno del servidor."
                when {
                    backendMessage.contains("Error interno del servidor", ignoreCase = true) -> {
                        localizedContext.getString(R.string.error_server_internal)
                    }
                    else -> {
                        localizedContext.getString(R.string.error_register_user)
                    }
                }
            }
            -1 -> {
                localizedContext.getString(R.string.error_network)
            }
            else -> {
                localizedContext.getString(R.string.error_generic)
            }
        }
    }

    /**
     * Mapea errores específicos de validación de campos
     */
    fun mapValidationError(fieldName: String, errorMessage: String, context: Context): String {
        val localizedContext = getLocalizedContext(context)

        return when {
            // Email errors
            errorMessage.contains("email field is required", ignoreCase = true) -> {
                localizedContext.getString(R.string.error_email_required)
            }
            errorMessage.contains("email has already been taken", ignoreCase = true) -> {
                localizedContext.getString(R.string.error_email_taken)
            }
            errorMessage.contains("email must be a valid email address", ignoreCase = true) -> {
                localizedContext.getString(R.string.error_email_invalid)
            }
            errorMessage.contains("selected email is invalid", ignoreCase = true) -> {
                localizedContext.getString(R.string.error_email_invalid)
            }

            // Password errors
            errorMessage.contains("password must be at least", ignoreCase = true) -> {
                localizedContext.getString(R.string.error_password_min_length)
            }
            errorMessage.contains("password field is required", ignoreCase = true) -> {
                localizedContext.getString(R.string.error_password_required)
            }
            errorMessage.contains("password confirmation does not match", ignoreCase = true) -> {
                localizedContext.getString(R.string.error_password_confirmation)
            }

            // CURP errors
            errorMessage.contains("curp has already been taken", ignoreCase = true) -> {
                localizedContext.getString(R.string.error_curp_taken)
            }
            errorMessage.contains("curp field is required", ignoreCase = true) -> {
                localizedContext.getString(R.string.error_curp_required)
            }
            errorMessage.contains("curp must be", ignoreCase = true) ||
                    errorMessage.contains("curp format", ignoreCase = true) -> {
                localizedContext.getString(R.string.error_curp_invalid)
            }

            // Phone errors
            errorMessage.contains("phone field is required", ignoreCase = true) -> {
                localizedContext.getString(R.string.error_phone_required)
            }
            errorMessage.contains("phone must be", ignoreCase = true) -> {
                localizedContext.getString(R.string.error_phone_invalid)
            }

            // Name errors
            errorMessage.contains("name field is required", ignoreCase = true) -> {
                localizedContext.getString(R.string.error_name_required)
            }
            errorMessage.contains("last_name field is required", ignoreCase = true) ||
                    errorMessage.contains("last name field is required", ignoreCase = true) -> {
                localizedContext.getString(R.string.error_lastname_required)
            }

            // Generic field required
            errorMessage.contains("field is required", ignoreCase = true) -> {
                localizedContext.getString(R.string.error_field_required)
            }

            else -> errorMessage
        }
    }

    private fun getLocalizedContext(context: Context): Context {
        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val appLanguage = prefs.getString("Locale.Helper.Selected.Language", "en") ?: "en"

        val locale = Locale(appLanguage)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        return context.createConfigurationContext(config)
    }
}

object DocumentsErrorMapper {

    /**
     * Mapea los mensajes de error del endpoint /api/users/uploadDocuments
     */
    fun mapDocumentsError(backendMessage: String, statusCode: Int, context: Context): String {
        val localizedContext = getLocalizedContext(context)

        return when (statusCode) {
            401 -> {
                // "Token de autenticación no proporcionado."
                localizedContext.getString(R.string.error_auth_token_missing)
            }
            403 -> {
                // "No tienes permiso para subir documentos."
                localizedContext.getString(R.string.error_no_permission_upload_docs)
            }
            422 -> {
                // "Error al subir los documentos."
                localizedContext.getString(R.string.error_upload_documents)
            }
            500 -> {
                when {
                    backendMessage.contains("Error interno del servidor", ignoreCase = true) -> {
                        localizedContext.getString(R.string.error_server_internal)
                    }
                    else -> {
                        localizedContext.getString(R.string.error_upload_documents)
                    }
                }
            }
            -1 -> {
                localizedContext.getString(R.string.error_network)
            }
            else -> {
                localizedContext.getString(R.string.error_generic)
            }
        }
    }

    /**
     * Mapea errores específicos de validación de documentos
     */
    fun mapDocumentValidationError(errorMessage: String, context: Context): String {
        val localizedContext = getLocalizedContext(context)

        return when {
            errorMessage.contains("selected user id is invalid", ignoreCase = true) -> {
                localizedContext.getString(R.string.error_invalid_user_id)
            }
            errorMessage.contains("document front must be an image", ignoreCase = true) -> {
                localizedContext.getString(R.string.error_document_front_must_be_image)
            }
            errorMessage.contains("document back must be an image", ignoreCase = true) -> {
                localizedContext.getString(R.string.error_document_back_must_be_image)
            }
            errorMessage.contains("document front field is required", ignoreCase = true) -> {
                localizedContext.getString(R.string.error_document_front_required)
            }
            errorMessage.contains("document back field is required", ignoreCase = true) -> {
                localizedContext.getString(R.string.error_document_back_required)
            }
            else -> errorMessage
        }
    }

    private fun getLocalizedContext(context: Context): Context {
        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val appLanguage = prefs.getString("Locale.Helper.Selected.Language", "en") ?: "en"

        val locale = Locale(appLanguage)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        return context.createConfigurationContext(config)
    }
}

object SelfieErrorMapper {

    /**
     * Mapea los mensajes de error del endpoint /api/users/uploadSelfie
     */
    fun mapSelfieError(backendMessage: String, statusCode: Int, context: Context): String {
        val localizedContext = getLocalizedContext(context)

        return when (statusCode) {
            401 -> {
                // "Token de autenticación no proporcionado."
                localizedContext.getString(R.string.error_auth_token_missing)
            }
            403 -> {
                // "No tienes permiso para subir selfie."
                localizedContext.getString(R.string.error_no_permission_upload_selfie)
            }
            422 -> {
                // "Error al subir el selfie."
                localizedContext.getString(R.string.error_upload_selfie)
            }
            500 -> {
                when {
                    backendMessage.contains("Error interno del servidor", ignoreCase = true) -> {
                        localizedContext.getString(R.string.error_server_internal)
                    }
                    else -> {
                        localizedContext.getString(R.string.error_upload_selfie)
                    }
                }
            }
            -1 -> {
                localizedContext.getString(R.string.error_network)
            }
            else -> {
                localizedContext.getString(R.string.error_generic)
            }
        }
    }

    /**
     * Mapea errores específicos de validación de selfie
     */
    fun mapSelfieValidationError(errorMessage: String, context: Context): String {
        val localizedContext = getLocalizedContext(context)

        return when {
            errorMessage.contains("selected user id is invalid", ignoreCase = true) -> {
                localizedContext.getString(R.string.error_invalid_user_id)
            }
            errorMessage.contains("selfie must be an image", ignoreCase = true) -> {
                localizedContext.getString(R.string.error_selfie_must_be_image)
            }
            errorMessage.contains("selfie field is required", ignoreCase = true) -> {
                localizedContext.getString(R.string.error_selfie_required)
            }
            else -> errorMessage
        }
    }

    private fun getLocalizedContext(context: Context): Context {
        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val appLanguage = prefs.getString("Locale.Helper.Selected.Language", "en") ?: "en"

        val locale = Locale(appLanguage)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        return context.createConfigurationContext(config)
    }
}