package com.renova.mobile.screens

import android.content.Context
import android.content.res.Configuration
import com.renova.mobile.R
import java.util.Locale

object ErrorMessageMapper {

    // ==================== LOGIN ERRORS ====================

    fun mapLoginError(backendMessage: String, statusCode: Int, context: Context): String {
        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val appLanguage = prefs.getString("Locale.Helper.Selected.Language", "en") ?: "en"

        val locale = Locale(appLanguage)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        val localizedContext = context.createConfigurationContext(config)

        return mapLoginErrorInternal(backendMessage, statusCode, localizedContext)
    }

    private fun mapLoginErrorInternal(backendMessage: String, statusCode: Int, context: Context): String {
        return when (statusCode) {
            401 -> context.getString(R.string.err_login_invalid_credentials)
            403 -> context.getString(R.string.err_login_account_disabled)
            422 -> context.getString(R.string.err_http_validation_failed)
            500 -> {
                when {
                    backendMessage.contains("Error interno del servidor", ignoreCase = true) -> {
                        context.getString(R.string.err_http_server_internal)
                    }
                    else -> {
                        context.getString(R.string.err_http_server_error)
                    }
                }
            }
            -1 -> context.getString(R.string.err_http_network)
            else -> context.getString(R.string.err_http_generic)
        }
    }

    fun mapLoginErrorByCode(statusCode: Int, context: Context): String {
        return when (statusCode) {
            401 -> context.getString(R.string.err_login_invalid_credentials)
            403 -> context.getString(R.string.err_login_account_disabled)
            422 -> context.getString(R.string.err_http_validation_failed)
            500 -> context.getString(R.string.err_http_server_error)
            -1 -> context.getString(R.string.err_http_network)
            else -> context.getString(R.string.err_http_generic)
        }
    }

    // ==================== REGISTER ERRORS ====================

    fun mapRegisterError(backendMessage: String, statusCode: Int, context: Context): String {
        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val appLanguage = prefs.getString("Locale.Helper.Selected.Language", "en") ?: "en"

        val locale = Locale(appLanguage)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        val localizedContext = context.createConfigurationContext(config)

        return mapRegisterErrorInternal(backendMessage, statusCode, localizedContext)
    }

    private fun mapRegisterErrorInternal(backendMessage: String, statusCode: Int, context: Context): String {
        return when (statusCode) {
            422 -> {
                when {
                    // Email duplicado
                    backendMessage.contains("correo electrónico ya está en uso", ignoreCase = true) ||
                            backendMessage.contains("email is already in use", ignoreCase = true) ||
                            backendMessage.contains("email has already been taken", ignoreCase = true) -> {
                        context.getString(R.string.err_reg_email_duplicate)
                    }

                    // CURP duplicado
                    backendMessage.contains("CURP ya está en uso", ignoreCase = true) ||
                            backendMessage.contains("CURP is already in use", ignoreCase = true) ||
                            backendMessage.contains("curp has already been taken", ignoreCase = true) -> {
                        context.getString(R.string.err_reg_curp_duplicate)
                    }

                    // Teléfono duplicado
                    backendMessage.contains("teléfono ya está en uso", ignoreCase = true) ||
                            backendMessage.contains("número de teléfono ya está en uso", ignoreCase = true) ||
                            backendMessage.contains("phone is already in use", ignoreCase = true) ||
                            backendMessage.contains("phone has already been taken", ignoreCase = true) -> {
                        context.getString(R.string.err_reg_phone_duplicate)
                    }

                    // Error de contraseña
                    backendMessage.contains("password", ignoreCase = true) ||
                            backendMessage.contains("contraseña", ignoreCase = true) -> {
                        context.getString(R.string.err_reg_password_requirements)
                    }

                    else -> {
                        context.getString(R.string.err_http_validation_failed)
                    }
                }
            }

            400 -> context.getString(R.string.err_http_bad_request)

            500 -> {
                when {
                    backendMessage.contains("Error interno del servidor", ignoreCase = true) ||
                            backendMessage.contains("Internal server error", ignoreCase = true) -> {
                        context.getString(R.string.err_http_server_internal)
                    }
                    else -> {
                        context.getString(R.string.err_http_server_error)
                    }
                }
            }

            -1 -> context.getString(R.string.err_http_network)

            else -> context.getString(R.string.err_http_generic)
        }
    }

    /**
     * Mapea errores de validación de campos específicos
     * Usado cuando errors viene como Map<String, List<String>>
     */
    fun mapValidationError(fieldName: String, errorMessage: String, context: Context): String {
        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val appLanguage = prefs.getString("Locale.Helper.Selected.Language", "en") ?: "en"

        val locale = Locale(appLanguage)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        val localizedContext = context.createConfigurationContext(config)

        return when (fieldName) {
            "email" -> {
                when {
                    errorMessage.contains("already been taken", ignoreCase = true) ||
                            errorMessage.contains("ya está en uso", ignoreCase = true) -> {
                        localizedContext.getString(R.string.err_reg_email_duplicate)
                    }
                    errorMessage.contains("invalid", ignoreCase = true) ||
                            errorMessage.contains("inválido", ignoreCase = true) -> {
                        localizedContext.getString(R.string.validation_err_email_invalid)
                    }
                    else -> {
                        localizedContext.getString(R.string.err_reg_email_format)
                    }
                }
            }

            "curp" -> {
                when {
                    errorMessage.contains("already been taken", ignoreCase = true) ||
                            errorMessage.contains("ya está en uso", ignoreCase = true) -> {
                        localizedContext.getString(R.string.err_reg_curp_duplicate)
                    }
                    errorMessage.contains("invalid", ignoreCase = true) ||
                            errorMessage.contains("inválido", ignoreCase = true) -> {
                        localizedContext.getString(R.string.validation_err_curp_invalid)
                    }
                    else -> {
                        localizedContext.getString(R.string.err_reg_curp_format)
                    }
                }
            }

            "phone" -> {
                when {
                    errorMessage.contains("already been taken", ignoreCase = true) ||
                            errorMessage.contains("ya está en uso", ignoreCase = true) -> {
                        localizedContext.getString(R.string.err_reg_phone_duplicate)
                    }
                    errorMessage.contains("invalid", ignoreCase = true) ||
                            errorMessage.contains("inválido", ignoreCase = true) -> {
                        localizedContext.getString(R.string.validation_err_phone_invalid)
                    }
                    else -> {
                        localizedContext.getString(R.string.err_reg_phone_format)
                    }
                }
            }

            "password", "password_confirmation" -> {
                when {
                    errorMessage.contains("must be at least", ignoreCase = true) ||
                            errorMessage.contains("debe tener al menos", ignoreCase = true) -> {
                        localizedContext.getString(R.string.validation_err_pwd_min_length)
                    }
                    errorMessage.contains("match", ignoreCase = true) ||
                            errorMessage.contains("coincidir", ignoreCase = true) -> {
                        localizedContext.getString(R.string.validation_err_pwd_no_match)
                    }
                    else -> {
                        localizedContext.getString(R.string.err_reg_password_requirements)
                    }
                }
            }

            "name", "last_name" -> {
                localizedContext.getString(R.string.err_reg_name_format)
            }

            else -> {
                localizedContext.getString(R.string.err_reg_field_generic, fieldName)
            }
        }
    }

    // ==================== DOCUMENTS ERRORS ====================

    fun mapDocumentsError(backendMessage: String, statusCode: Int, context: Context): String {
        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val appLanguage = prefs.getString("Locale.Helper.Selected.Language", "en") ?: "en"

        val locale = Locale(appLanguage)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        val localizedContext = context.createConfigurationContext(config)

        return when (statusCode) {
            422 -> {
                when {
                    backendMessage.contains("image", ignoreCase = true) ||
                            backendMessage.contains("imagen", ignoreCase = true) -> {
                        localizedContext.getString(R.string.err_doc_invalid_image)
                    }
                    backendMessage.contains("size", ignoreCase = true) ||
                            backendMessage.contains("tamaño", ignoreCase = true) -> {
                        localizedContext.getString(R.string.err_doc_file_too_large)
                    }
                    else -> {
                        localizedContext.getString(R.string.err_doc_upload_failed)
                    }
                }
            }
            413 -> localizedContext.getString(R.string.err_doc_file_too_large)
            500 -> localizedContext.getString(R.string.err_http_server_error)
            -1 -> localizedContext.getString(R.string.err_http_network)
            else -> localizedContext.getString(R.string.err_http_generic)
        }
    }

    fun mapDocumentValidationError(errorMessage: String, context: Context): String {
        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val appLanguage = prefs.getString("Locale.Helper.Selected.Language", "en") ?: "en"

        val locale = Locale(appLanguage)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        val localizedContext = context.createConfigurationContext(config)

        return when {
            errorMessage.contains("required", ignoreCase = true) ||
                    errorMessage.contains("requerido", ignoreCase = true) -> {
                localizedContext.getString(R.string.err_doc_required)
            }
            else -> {
                localizedContext.getString(R.string.err_doc_upload_failed)
            }
        }
    }

    // ==================== SELFIE ERRORS ====================

    fun mapSelfieError(backendMessage: String, statusCode: Int, context: Context): String {
        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val appLanguage = prefs.getString("Locale.Helper.Selected.Language", "en") ?: "en"

        val locale = Locale(appLanguage)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        val localizedContext = context.createConfigurationContext(config)

        return when (statusCode) {
            422 -> {
                when {
                    backendMessage.contains("image", ignoreCase = true) ||
                            backendMessage.contains("imagen", ignoreCase = true) -> {
                        localizedContext.getString(R.string.err_selfie_invalid_image)
                    }
                    backendMessage.contains("size", ignoreCase = true) ||
                            backendMessage.contains("tamaño", ignoreCase = true) -> {
                        localizedContext.getString(R.string.err_doc_file_too_large)
                    }
                    else -> {
                        localizedContext.getString(R.string.err_selfie_upload_failed)
                    }
                }
            }
            413 -> localizedContext.getString(R.string.err_doc_file_too_large)
            500 -> localizedContext.getString(R.string.err_http_server_error)
            -1 -> localizedContext.getString(R.string.err_http_network)
            else -> localizedContext.getString(R.string.err_http_generic)
        }
    }

    fun mapSelfieValidationError(errorMessage: String, context: Context): String {
        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val appLanguage = prefs.getString("Locale.Helper.Selected.Language", "en") ?: "en"

        val locale = Locale(appLanguage)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        val localizedContext = context.createConfigurationContext(config)

        return when {
            errorMessage.contains("required", ignoreCase = true) ||
                    errorMessage.contains("requerido", ignoreCase = true) -> {
                localizedContext.getString(R.string.err_selfie_required)
            }
            else -> {
                localizedContext.getString(R.string.err_selfie_upload_failed)
            }
        }
    }
}