package com.renova.mobile.utils

import android.content.Context
import android.content.SharedPreferences
import java.util.Locale

/*
Esto se define como un object, lo que significa que es un singleton (una sola instancia en toda la app),
SELECTED_LANGUAGE es la clave con la que se guardará el idioma en SharedPreferences
(para recordar la elección del usuario aunque cierre la app).
*/
object LocaleHelper {
    private const val SELECTED_LANGUAGE = "Locale.Helper.Selected.Language"

    /*
    Aqui la funcion persist guarda el idioma seleccionado en SharedPreferences bajo la
    clave SELECTED_LANGUAGE, por lo que de esta forma cuando el usuario cierre y abra la app
    todavía recordamos cuál idioma había elegido. Y por último apply() guarda los cambios de
    manera asíncrona.
    */

    fun persist(context: Context, language: String) {
        val prefs: SharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        prefs.edit().putString(SELECTED_LANGUAGE, language).apply()
    }

    /*
     Esta parte recupera el idoma guardado en SharedPreferences, revisa en SharedPreferences si
     ya hay un idioma guardado, si no encuentra nada, por defecto regresa "en" (inglés). Esto
     asegura que la app siempre tenga un idioma válido (nunca null).
    */
    fun getLanguage(context: Context): String {
        val prefs: SharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        return prefs.getString(SELECTED_LANGUAGE, "en") ?: "en"
    }

    fun setLocale(context: Context, language: String): Context {
        persist(context, language)
        /*
        Aqui aguarda el idioma y lo convierte en un objeto Locale. Llama a persist(context, language)
        para no perderlo y crea un Locale con el código de idioma ("es", "en", "fr", etc.).
        */
        val locale = Locale(language)
        Locale.setDefault(locale)

        /*
        setLocale(locale) indica qué idioma deben cargar los recursos (strings.xml).
        setLayoutDirection(locale) cambia la dirección del texto si el idioma lo
        requiere (por ejemplo, árabe o hebreo usan derecha a izquierda).
        */
        val config = context.resources.configuration
        config.setLocale(locale)
        config.setLayoutDirection(locale)

        /*
        Y por ultimo esto devuelve un Context actualizado con el idioma elegido,
        luego ese contexto es el que usamos en la Activity (attachBaseContext)
        para que toda la interfaz se dibuje con el idioma correcto.
        */
        return context.createConfigurationContext(config)
    }
}