package com.renova.mobile.ui.components.business.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.renova.mobile.R
import com.renova.mobile.network.ActivityItem
import com.renova.mobile.ui.components.formatFriendlyDate
import com.renova.mobile.ui.components.SaleSummary
import com.renova.mobile.ui.components.SaleItem
import com.renova.mobile.ui.theme.PoppinsFontFamily
import com.renova.mobile.ui.theme.RenovaColors
import com.renova.mobile.ui.theme.RenovaColorScheme

@Composable
fun HistoryActivityCard(
    activity: ActivityItem,
    colors: RenovaColorScheme,
    pointToMxn: Double,
    onClick: () -> Unit = {}
) {
    val showDetail = remember { mutableStateOf(false) }
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
                showDetail.value = true
            }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icono según el tipo de actividad
        val (icon, iconColor) = when (activity.type_history) {
            2 -> Icons.Default.Recycling to RenovaColors.Success // Reciclaje
            1 -> Icons.Default.ShoppingCart to RenovaColors.Primary // Compra/Canjeo
            else -> Icons.Default.History to RenovaColors.Warning // Actividad
        }

        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(32.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            // Título de la actividad
            val titleText = when (activity.type_history) {
                2 -> stringResource(R.string.hist_card_recycling_title)
                1 -> stringResource(R.string.hist_card_reward_exchange_title)
                else -> stringResource(R.string.hist_card_generic_activity_title)
            }
            Text(
                text = titleText,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.SemiBold
                ),
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))
            val subtitleText = when (activity.type_history) {
                2 -> activity.material_type?.name ?: ""
                1 -> {
                    val name = activity.reward?.name ?: context.getString(R.string.reward)
                    val q = activity.quantity ?: 1
                    "$q x $name"
                }
                else -> activity.alliance?.name ?: ""
            }
            if (subtitleText.isNotBlank()) {
                Text(
                    text = subtitleText,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = PoppinsFontFamily
                    ),
                    color = colors.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Fecha (formato amigable: Hoy/Ayer/dd/MM/yyyy h:mm a)
            Text(
                text = formatFriendlyDate(activity.created_at),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = PoppinsFontFamily
                ),
                color = colors.textSecondary
            )
        }

        // Equivalente en MXN
        Column(
            horizontalAlignment = Alignment.End
        ) {
            val mxnValue = activity.points * pointToMxn
            val mxnColor = if (mxnValue < 0.0) RenovaColors.Success else colors.negativePoints
            val fmt = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("es","MX"))
            val absText = fmt.format(kotlin.math.abs(mxnValue))
            val displayText = if (mxnValue < 0.0) absText else "-$absText"
            Text(
                text = displayText,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold
                ),
                color = mxnColor
            )
            Text(
                text = stringResource(R.string.hist_card_equivalent_mxn_label),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = PoppinsFontFamily
                ),
                color = colors.textSecondary
            )
        }
    }

    if (showDetail.value) {
        HistoryDetailBottomSheet(
            activity = activity,
            colors = colors,
            pointToMxn = pointToMxn,
            onDismiss = { showDetail.value = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistoryDetailBottomSheet(
    activity: ActivityItem,
    colors: RenovaColorScheme,
    pointToMxn: Double,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.cardBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Ícono arriba
            val (icon, iconColor) = when (activity.type_history) {
                2 -> Icons.Default.Recycling to RenovaColors.Primary // Reciclaje
                1 -> Icons.Default.ShoppingCart to RenovaColors.Primary // Canjeo
                else -> Icons.Default.History to RenovaColors.Warning // Actividad
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(48.dp)
            )

            // Título del tipo de actividad
            val titleText = when (activity.type_history) {
                1 -> stringResource(R.string.reward_exchange)
                2 -> stringResource(R.string.recycling)
                3 -> stringResource(R.string.points_adjustment)
                else -> stringResource(R.string.activity)
            }
            Text(
                text = titleText,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold
                ),
                color = colors.textPrimary
            )

            // Comercio, Cliente, Vendedor
            val allianceName = activity.alliance?.name ?: stringResource(R.string.hist_detail_placeholder_dash)
            val clientName = activity.user?.let { u ->
                val last = u.last_name
                if (last.isNullOrBlank()) u.name else "${u.name} ${last}"
            } ?: stringResource(R.string.hist_detail_placeholder_dash)
            val sellerName = activity.alliance?.contact_name ?: stringResource(R.string.hist_detail_placeholder_dash)

            Text(
                text = "${stringResource(R.string.hist_detail_business_label)} $allianceName",
                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = PoppinsFontFamily),
                color = colors.textPrimary,
                textAlign = TextAlign.Center
            )
            Text(
                text = "${stringResource(R.string.hist_detail_client_label)} $clientName",
                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = PoppinsFontFamily),
                color = colors.textPrimary,
                textAlign = TextAlign.Center
            )
            Text(
                text = "${stringResource(R.string.hist_detail_seller_label)} $sellerName",
                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = PoppinsFontFamily),
                color = colors.textPrimary,
                textAlign = TextAlign.Center
            )

            // Título del producto en gris
            val productTitle = activity.reward?.name ?: activity.material_type?.name ?: context.getString(R.string.item)
            Text(
                text = productTitle,
                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = PoppinsFontFamily),
                color = colors.textSecondary,
                textAlign = TextAlign.Center
            )

            // Descripción del producto
            val productDesc = activity.reward?.description ?: activity.material_type?.description ?: activity.description
            if (!productDesc.isNullOrBlank()) {
                Text(
                    text = productDesc,
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = PoppinsFontFamily),
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center
                )
            }

            // Cantidad
            val qty = activity.quantity ?: 1
            Text(
                text = "${stringResource(R.string.hist_detail_quantity_label)} $qty",
                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = PoppinsFontFamily),
                color = colors.textPrimary,
                textAlign = TextAlign.Center
            )

            // Puntos: etiqueta y valor con el mismo énfasis que equivalente MXN
            Text(
                text = "${stringResource(R.string.hist_detail_points_label)} ${activity.points}",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold
                ),
                color = RenovaColors.Primary
            )

            // Equivalente en MXN con inversión de colores y misma jerarquía tipográfica
            val mxnValue = activity.points * pointToMxn
            val mxnColor = if (mxnValue < 0.0) RenovaColors.Success else colors.negativePoints
            val fmt = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("es", "MX"))
            val absText = fmt.format(kotlin.math.abs(mxnValue))
            val displayText = if (mxnValue < 0.0) absText else "-$absText"
            Text(
                text = "${stringResource(R.string.hist_detail_equivalent_mxn_prefix)} $displayText",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold
                ),
                color = mxnColor,
                textAlign = TextAlign.Center
            )

            // Fecha de realización dd/MM/yyyy hh:mm
            val formattedDate = run {
                val inputPatterns = listOf(
                    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                    "yyyy-MM-dd'T'HH:mm:ss'Z'",
                    "yyyy-MM-dd HH:mm:ss",
                    "yyyy-MM-dd'T'HH:mm:ssXXX"
                )
                var out = activity.created_at
                for (p in inputPatterns) {
                    try {
                        val parser = java.text.SimpleDateFormat(p, java.util.Locale("es", "MX"))
                        parser.timeZone = java.util.TimeZone.getTimeZone("UTC")
                        val date = parser.parse(activity.created_at)
                        if (date != null) {
                            val fmt = java.text.SimpleDateFormat("dd/MM/yyyy hh:mm", java.util.Locale("es", "MX"))
                            fmt.timeZone = java.util.TimeZone.getDefault()
                            out = fmt.format(date)
                            break
                        }
                    } catch (_: Exception) { }
                }
                out
            }
            Text(
                text = "${stringResource(R.string.hist_detail_performed_date_label)} $formattedDate",
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = PoppinsFontFamily),
                color = colors.textSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun toSaleSummary(activity: ActivityItem): SaleSummary {
    val allianceName = activity.alliance?.name ?: "N/A"
    val items = mutableListOf<SaleItem>()
    val desc = activity.reward?.description
    if (!desc.isNullOrBlank()) {
        desc.split(",").forEach { part ->
            val trimmed = part.trim()
            val match = Regex("(\\d+)\\s*x\\s*(.+)").find(trimmed)
            if (match != null) {
                val q = match.groupValues[1].toIntOrNull() ?: (activity.quantity ?: 1)
                val name = match.groupValues[2]
                items.add(SaleItem(name = name, quantity = q, pointsRequired = 0))
            }
        }
        if (items.isEmpty()) {
            val name = activity.reward?.name ?: "Recompensa"
            items.add(SaleItem(name = name, quantity = activity.quantity ?: 1, pointsRequired = 0))
        }
    } else {
        val name = activity.reward?.name ?: activity.material_type?.name ?: "Artículo"
        items.add(SaleItem(name = name, quantity = activity.quantity ?: 1, pointsRequired = 0))
    }
    return SaleSummary(
        id = activity.id.toString(),
        allianceName = allianceName,
        consumerName = null,
        totalPoints = activity.points,
        items = items
    )
}