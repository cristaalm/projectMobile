package com.renova.mobile.utils

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.*

class WithdrawalManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "renova_withdrawals"
        private const val KEY_LAST_WITHDRAWAL_PREFIX = "last_withdrawal_"
    }

    /**
     * Verifica si el usuario puede realizar un cobro este mes
     * Solo puede cobrar el último día del mes
     * @param allianceId ID de la alianza del usuario
     * @return Pair<Boolean, String?> - (puede cobrar, mensaje de error si no puede)
     */
    fun canWithdrawThisMonth(allianceId: Int): Pair<Boolean, String?> {
        val now = Calendar.getInstance()
        val today = now.get(Calendar.DAY_OF_MONTH)
        val lastDayOfMonth = now.getActualMaximum(Calendar.DAY_OF_MONTH)

        // Verificar si es el último día del mes
        if (today != lastDayOfMonth) {
            val monthName = SimpleDateFormat("MMMM", Locale("es", "MX")).format(now.time)
            val message = "Solo puedes cobrar el último día del mes. El próximo cobro estará disponible el $lastDayOfMonth de $monthName."
            return Pair(false, message)
        }

        // Si es el último día, verificar si ya cobró este mes
        val lastWithdrawalDate = getLastWithdrawalDate(allianceId) ?: return Pair(true, null)

        val lastWithdrawal = Calendar.getInstance().apply {
            time = lastWithdrawalDate
        }

        // Verificar si es el mismo mes y año
        val sameMonth = lastWithdrawal.get(Calendar.MONTH) == now.get(Calendar.MONTH)
        val sameYear = lastWithdrawal.get(Calendar.YEAR) == now.get(Calendar.YEAR)

        return if (sameMonth && sameYear) {
            val nextMonth = Calendar.getInstance().apply {
                add(Calendar.MONTH, 1)
                set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            }

            val dateFormat = SimpleDateFormat("dd 'de' MMMM 'de' yyyy", Locale("es", "MX"))
            val lastWithdrawalFormatted = SimpleDateFormat("dd/MM/yyyy", Locale("es", "MX")).format(lastWithdrawalDate)

            val message = "Ya realizaste un cobro este mes (${lastWithdrawalFormatted}). El próximo cobro estará disponible el ${dateFormat.format(nextMonth.time)}."

            Pair(false, message)
        } else {
            Pair(true, null)
        }
    }

    /**
     * Registra un nuevo cobro exitoso
     * @param allianceId ID de la alianza del usuario
     */
    fun registerWithdrawal(allianceId: Int) {
        val currentDate = System.currentTimeMillis()
        sharedPreferences.edit()
            .putLong("${KEY_LAST_WITHDRAWAL_PREFIX}$allianceId", currentDate)
            .apply()
    }

    /**
     * Obtiene la fecha del último cobro
     * @param allianceId ID de la alianza del usuario
     * @return Date o null si no hay registro
     */
    fun getLastWithdrawalDate(allianceId: Int): Date? {
        val timestamp = sharedPreferences.getLong("${KEY_LAST_WITHDRAWAL_PREFIX}$allianceId", -1L)
        return if (timestamp != -1L) Date(timestamp) else null
    }

    /**
     * Obtiene información detallada sobre el estado de cobro
     * @param allianceId ID de la alianza del usuario
     */
    fun getWithdrawalStatus(allianceId: Int): WithdrawalStatus {
        val now = Calendar.getInstance()
        val today = now.get(Calendar.DAY_OF_MONTH)
        val lastDayOfMonth = now.getActualMaximum(Calendar.DAY_OF_MONTH)

        val lastDate = getLastWithdrawalDate(allianceId)

        // Si no es el último día del mes
        if (today != lastDayOfMonth) {
            val nextAvailable = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_MONTH, lastDayOfMonth)
            }.time

            val monthName = SimpleDateFormat("MMMM", Locale("es", "MX")).format(now.time)

            return WithdrawalStatus(
                canWithdraw = false,
                lastWithdrawalDate = lastDate,
                nextAvailableDate = nextAvailable,
                message = "Los cobros solo están disponibles el último día de cada mes (día $lastDayOfMonth de $monthName)."
            )
        }

        // Si es el último día del mes, verificar si ya cobró
        if (lastDate == null) {
            return WithdrawalStatus(
                canWithdraw = true,
                lastWithdrawalDate = null,
                nextAvailableDate = null,
                message = "Puedes realizar tu cobro del mes"
            )
        }

        val (canWithdraw, errorMessage) = canWithdrawThisMonth(allianceId)

        val nextAvailable = if (!canWithdraw) {
            Calendar.getInstance().apply {
                add(Calendar.MONTH, 1)
                set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            }.time
        } else null

        return WithdrawalStatus(
            canWithdraw = canWithdraw,
            lastWithdrawalDate = lastDate,
            nextAvailableDate = nextAvailable,
            message = errorMessage ?: "Puedes realizar tu cobro del mes"
        )
    }

    /**
     * Limpia el historial de cobros (solo para testing/debug)
     */
    fun clearWithdrawalHistory(allianceId: Int) {
        sharedPreferences.edit()
            .remove("${KEY_LAST_WITHDRAWAL_PREFIX}$allianceId")
            .apply()
    }
}

data class WithdrawalStatus(
    val canWithdraw: Boolean,
    val lastWithdrawalDate: Date?,
    val nextAvailableDate: Date?,
    val message: String
)