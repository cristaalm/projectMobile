package com.renova.mobile.data.model
import com.google.gson.annotations.SerializedName

// ========== ENDPOINT 1: Stats generales ==========
data class AllianceStatsResponse(
    val success: Boolean,
    val message: String,
    val data: AllianceStatsData?,
    val errors: Any?,
    val code: Int  // Cambiado de "status" a "code" según tu API
)

data class AllianceStatsData(
    @SerializedName("total_income")
    val totalIncome: Double,

    @SerializedName("total_points_awarded")  // ✅ Corregido
    val totalPoints: Int,

    @SerializedName("average_total_income")  // ✅ Corregido
    val averageTicket: Double,

    @SerializedName("total_customers_served")
    val totalCustomersServed: Int
)

// ========== ENDPOINT 2: Actividad por día ==========
data class ActivityByDayResponse(
    val success: Boolean,
    val message: String,
    val data: ActivityByDayData?,
    val errors: Any?,
    val code: Int  // Cambiado de "status" a "code"
)

data class ActivityByDayData(
    @SerializedName("statsToWeek") val statsToWeek: List<DayActivity>,
    @SerializedName("totalSales") val totalSales: Int,
    @SerializedName("totalPoints") val totalPoints: Int
)

data class DayActivity(
    val day: String,
    val date: String,
    @SerializedName("total_activity") val totalActivity: Int
)

// ========== ENDPOINT 3: Top rewards ==========
data class TopRewardsResponse(
    val success: Boolean,
    val message: String,
    val data: List<List<TopReward>>?,
    val errors: Any?,
    val code: Int  // Cambiado de "status" a "code"
)

data class TopReward(
    @SerializedName("reward_id") val rewardId: Int,
    @SerializedName("reward_name") val rewardName: String,
    @SerializedName("total_claimed") val totalClaimed: Int
)