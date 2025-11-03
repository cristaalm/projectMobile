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
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
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
            Text(
                text = when (activity.type_history) {
                    2 -> "Reciclaje"
                    1 -> "Canjeo de recompensa"
                    else -> "Actividad"
                },
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
                    val name = activity.reward?.name ?: "Recompensa"
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
            val mxnColor = if (mxnValue < 0.0) colors.negativePoints else colors.primaryColor
            Text(
                text = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("es","MX")).format(mxnValue),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold
                ),
                color = mxnColor
            )
            Text(
                text = "equivalente MXN",
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
    val summary = toSaleSummary(activity)
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White
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
            Text(
                text = when (activity.type_history) {
                    1 -> stringResource(com.renova.mobile.R.string.reward_exchange)
                    2 -> stringResource(com.renova.mobile.R.string.recycling)
                    3 -> stringResource(com.renova.mobile.R.string.points_adjustment)
                    else -> stringResource(com.renova.mobile.R.string.activity)
                },
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold
                ),
                color = colors.textPrimary
            )

            // Comercio, Cliente, Vendedor
            val allianceName = activity.alliance?.name ?: "—"
            val clientName = activity.user?.let { u ->
                val last = u.last_name
                if (last.isNullOrBlank()) u.name else "${u.name} ${last}"
            } ?: "—"
            val sellerName = activity.alliance?.contact_name ?: "—"
            Text(
                text = "Comercio: $allianceName",
                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = PoppinsFontFamily),
                color = colors.textPrimary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Text(
                text = "Cliente: $clientName",
                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = PoppinsFontFamily),
                color = colors.textPrimary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Text(
                text = "Vendedor: $sellerName",
                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = PoppinsFontFamily),
                color = colors.textPrimary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            // Título del producto en gris
            val productTitle = activity.reward?.name ?: activity.material_type?.name ?: context.getString(com.renova.mobile.R.string.item)
            Text(
                text = productTitle,
                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = PoppinsFontFamily),
                color = colors.textSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            // Descripción del producto
            val productDesc = activity.reward?.description ?: activity.material_type?.description ?: activity.description
            if (!productDesc.isNullOrBlank()) {
                Text(
                    text = productDesc,
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = PoppinsFontFamily),
                    color = colors.textSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            // Cantidad
            val qty = activity.quantity ?: 1
            Text(
                text = "Cantidad: $qty",
                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = PoppinsFontFamily),
                color = colors.textPrimary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            // Puntos en grande sin signo positivo
            val pointsText = if (activity.points < 0) "${activity.points}" else "${activity.points}"
            Text(
                text = pointsText,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold
                ),
                color = RenovaColors.Primary
            )

            // Equivalente en MXN
            val mxnFormatted = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("es", "MX"))
                .format(activity.points * pointToMxn)
            Text(
                text = "Equivalente MXN: $mxnFormatted",
                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = PoppinsFontFamily),
                color = RenovaColors.Primary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
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
                text = "Realizado: $formattedDate",
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = PoppinsFontFamily),
                color = colors.textSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
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