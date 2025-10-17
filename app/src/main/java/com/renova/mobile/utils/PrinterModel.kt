package com.renova.mobile.utils

/**
 * Modelo de datos para representar una impresora Bluetooth
 */
data class PrinterModel(
    val name: String,
    val address: String,
    val model: String = "",
    val isConnected: Boolean = false,
    val isCompatible: Boolean = false
) {
    /**
     * Obtiene una descripción legible de la impresora
     */
    fun getDisplayName(): String {
        return when {
            model.isNotEmpty() -> "$name ($model)"
            else -> name
        }
    }

    /**
     * Obtiene el estado de conexión como texto
     */
    fun getConnectionStatus(): String {
        return when {
            isConnected -> "Disponible"
            isCompatible -> "Compatible"
            else -> "No disponible"
        }
    }
}