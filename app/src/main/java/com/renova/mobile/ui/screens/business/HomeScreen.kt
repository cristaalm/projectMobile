package com.renova.mobile.ui.screens.business

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.renova.mobile.R
import com.renova.mobile.ui.components.SectionHeader
import com.renova.mobile.ui.components.BusinessSectionHeader
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import com.renova.mobile.ui.components.SaleDetailModal
import com.renova.mobile.ui.components.SaleSummary
import com.renova.mobile.ui.components.SaleItem
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import com.renova.mobile.viewmodel.BusinessSaleViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.Button
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import com.renova.mobile.ui.components.PrinterSelectionModal
import com.renova.mobile.utils.TicketPrinter
import com.renova.mobile.utils.PrinterModel
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Close
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.renova.mobile.ui.theme.PoppinsFontFamily
import com.renova.mobile.ui.theme.RenovaColors
import com.renova.mobile.ui.theme.LocalRenovaColors
import com.renova.mobile.viewmodel.BusinessHistoryViewModel
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import com.renova.mobile.ui.components.business.home.HistoryActivityCard
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.text.style.TextOverflow
import com.renova.mobile.ui.theme.RenovaColorScheme
import java.text.SimpleDateFormat
import java.text.NumberFormat
import java.util.Locale
import androidx.compose.ui.draw.shadow
import com.renova.mobile.ui.components.formatFriendlyDate



@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun BusinessHomeScreen(
    onLogout: () -> Unit,
    onNavigateToProfile: () -> Unit = {},
    vm: BusinessSaleViewModel = viewModel(),
    onNavigateToCashout: () -> Unit = {},
    pointToMxn: Double
) {
    val colors = LocalRenovaColors.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // ViewModels
    val historyViewModel: BusinessHistoryViewModel = viewModel()
    val historyState by historyViewModel.state.collectAsState()

    val lastSaleSummary by vm.lastSaleSummary.collectAsState()
    var showDetail by remember { mutableStateOf(false) }
    var showPrinterSelection by remember { mutableStateOf(false) }

    // Helper para construir SaleSummary desde ActivityItem agrupado
    fun toSaleSummary(activity: com.renova.mobile.network.ActivityItem): SaleSummary {
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
                val name = activity.reward?.name ?: context.getString(R.string.reward)
                items.add(SaleItem(name = name, quantity = activity.quantity ?: 1, pointsRequired = 0))
            }
        } else {
            val name = activity.reward?.name ?: activity.material_type?.name ?: context.getString(R.string.item)
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

    Column(modifier = Modifier.fillMaxSize()) {
        BusinessSectionHeader(
            title = context.getString(R.string.tab_home),
            onLogout = onLogout,
            textColor = Color.White
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card de perfil de usuario
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 3.dp,
                            shape = RoundedCornerShape(16.dp),
                            spotColor = RenovaColors.Light.ActivityShadowColor
                        )
                        .clickable { onNavigateToProfile() },
                    colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Perfil",
                            tint = RenovaColors.Primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = context.getString(R.string.my_profile),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = colors.textPrimary
                            )
                            Text(
                                text = context.getString(R.string.my_profile_description),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = PoppinsFontFamily
                                ),
                                color = colors.textSecondary
                            )
                        }
                    }
                }
            }

            // Card de última venta
            item {
                if (lastSaleSummary == null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                elevation = 3.dp,
                                shape = RoundedCornerShape(16.dp),
                                spotColor = RenovaColors.Light.ActivityShadowColor
                            ),
                        colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "Historial",
                                tint = colors.textSecondary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = context.getString(R.string.no_recent_sales),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = colors.textSecondary,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = context.getString(R.string.recent_sales_description),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = PoppinsFontFamily
                                ),
                                color = colors.textSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                elevation = 3.dp,
                                shape = RoundedCornerShape(16.dp),
                                spotColor = RenovaColors.Light.ActivityShadowColor
                            ),
                        colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = "Última venta",
                                    tint = RenovaColors.Primary,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = (runCatching {
                                        val millis = lastSaleSummary!!.id.toLong()
                                        val fmt = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                                        "${fmt.format(java.util.Date(millis))} · ${context.getString(R.string.last_sale)}"
                                    }.getOrNull()) ?: context.getString(R.string.last_sale),
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = colors.textPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))

                            // Detalles de producto y totales
                            val firstItem = lastSaleSummary!!.items.firstOrNull()
                            val formattedPoints = java.text.NumberFormat.getIntegerInstance(java.util.Locale.forLanguageTag("es-MX")).format(lastSaleSummary!!.totalPoints)
                            val pesosValue = lastSaleSummary!!.totalPoints * pointToMxn
                            val pesosFormatted = java.text.NumberFormat.getCurrencyInstance(java.util.Locale.forLanguageTag("es-MX")).format(pesosValue)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    if (firstItem != null) {
                                        val extra = if (lastSaleSummary!!.items.size > 1) " +${lastSaleSummary!!.items.size - 1}" else ""
                                        Text(
                                            text = context.getString(R.string.hist_detail_product_label) + " ${firstItem.name}$extra",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontFamily = PoppinsFontFamily
                                            ),
                                            color = colors.textPrimary,
                                            maxLines = 2,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = context.getString(R.string.hist_detail_quantity_label) + " ${firstItem.quantity}",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontFamily = PoppinsFontFamily
                                            ),
                                            color = colors.textPrimary
                                        )
                                    } else {
                                        Text(
                                            text = context.getString(R.string.hist_detail_product_unspecified),
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontFamily = PoppinsFontFamily
                                            ),
                                            color = colors.textPrimary
                                        )
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = context.getString(R.string.total_points),
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.Medium
                                        ),
                                        color = colors.textSecondary
                                    )
                                    Text(
                                        text = "$formattedPoints",
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = RenovaColors.Primary
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = context.getString(R.string.hist_detail_equivalent_mxn_prefix) + " $pesosFormatted",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = RenovaColors.Primary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = { showDetail = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                    containerColor = RenovaColors.Primary
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.view_full_detail),
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // Cards de funciones adicionales
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Card de configuración
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .shadow(
                                elevation = 3.dp,
                                shape = RoundedCornerShape(16.dp),
                                spotColor = RenovaColors.Light.ActivityShadowColor
                            )
                            .clickable { onNavigateToCashout() },
                        colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CreditCard,
                                contentDescription = "Cobrar Puntos",
                                tint = RenovaColors.Primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = context.getString(R.string.earn_points),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = colors.textPrimary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Card de estadísticas - ✅ CORREGIDO
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .shadow(
                                elevation = 3.dp,
                                shape = RoundedCornerShape(16.dp),
                                spotColor = RenovaColors.Light.ActivityShadowColor
                            )
                            .clickable {
                                // Obtener allianceId del ViewModel
                                val allianceId = vm.businessAllianceId.value

                                if (allianceId != null) {
                                    // Si existe en el ViewModel, usarlo
                                    com.renova.mobile.ui.activities.BusinessStatisticsActivity.start(
                                        context = context,
                                        allianceId = allianceId
                                    )
                                } else {
                                    // Si no existe, intentar obtenerlo del historial
                                    val historyAllianceId = historyState.activities.firstOrNull()?.alliance?.id

                                    if (historyAllianceId != null) {
                                        com.renova.mobile.ui.activities.BusinessStatisticsActivity.start(
                                            context = context,
                                            allianceId = historyAllianceId
                                        )
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "No se pudo obtener el ID de la alianza. Intenta escanear un código primero.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            },
                        colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Analytics,
                                contentDescription = "Estadísticas",
                                tint = RenovaColors.Primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = context.getString(R.string.statistics),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = colors.textPrimary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Sección de Historial del Comercio
            item {
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = context.getString(R.string.tour_title_activity_button),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold
                    ),
                    color = colors.textPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Cards de historial
            item {
                if (historyState.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = RenovaColors.Primary)
                    }
                } else if (historyState.error != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = context.getString(R.string.cashout_error_history),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = historyState.error!!,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = PoppinsFontFamily
                                ),
                                color = colors.textSecondary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { historyViewModel.retry() },
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                    containerColor = RenovaColors.Primary
                                )
                            ) {
                                Text(
                                    text = context.getString(R.string.retry),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = Color.White
                                )
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp),
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        itemsIndexed(historyState.activities.filter { it.reward != null }.take(3)) { index, activity ->
                            HistoryActivityCard(
                                activity = activity,
                                colors = colors,
                                pointToMxn = pointToMxn
                            )
                            if (index < historyState.activities.filter { it.reward != null }.take(3).size - 1) {
                                Divider(
                                    color = RenovaColors.PrimaryColor,
                                    thickness = 1.dp,
                                    modifier = Modifier.padding(vertical = 3.dp)
                                )
                            }
                        }
                         
                         if (historyState.activities.filter { it.reward != null }.isEmpty()) {
                              item {
                                  Card(
                                      modifier = Modifier.fillMaxWidth(),
                                      colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                                      shape = RoundedCornerShape(16.dp),
                                      elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                                  ) {
                                      Column(
                                          modifier = Modifier
                                              .fillMaxWidth()
                                              .padding(24.dp),
                                          horizontalAlignment = Alignment.CenterHorizontally
                                      ) {
                                          Icon(
                                              imageVector = Icons.Default.History,
                                              contentDescription = "Sin historial",
                                              tint = colors.textSecondary,
                                              modifier = Modifier.size(48.dp)
                                          )
                                          Spacer(modifier = Modifier.height(12.dp))
                                          Text(
                                              text = context.getString(R.string.no_recent_activities),
                                              style = MaterialTheme.typography.bodyMedium.copy(
                                                  fontFamily = PoppinsFontFamily,
                                                  fontWeight = FontWeight.Medium
                                              ),
                                              color = colors.textSecondary,
                                              textAlign = TextAlign.Center
                                          )
                                      }
                                  }
                              }
                          }
                     }

                }
            }
        }
    }

    // Detalle completo de última venta
    if (showDetail && lastSaleSummary != null) {
        SaleDetailModal(
            summary = lastSaleSummary!!,
            onClose = { showDetail = false },
            onPrint = { showDetail = false }
        )
    }

    // Detalle de compra ahora gestionado por HistoryActivityCard (bottom sheet interno)

    // Printer Selection Modal desde Home
    if (showPrinterSelection && lastSaleSummary != null) {
        PrinterSelectionModal(
            summary = lastSaleSummary!!,
            onClose = { showPrinterSelection = false },
            onPrintSelected = { printer ->
                scope.launch {
                    try {
                        Toast.makeText(context, context.getString(R.string.printing_in) + " ${printer.getDisplayName()}...", Toast.LENGTH_SHORT).show()
                        TicketPrinter.printTicket(lastSaleSummary!!, printer, context)
                        Toast.makeText(context, context.getString(R.string.printing_success), Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, context.getString(R.string.printing_error) + " ${e.message}", Toast.LENGTH_LONG).show()
                    }
                    showPrinterSelection = false
                }
            }
        )
    }
}