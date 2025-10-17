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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.renova.mobile.ui.theme.PoppinsFontFamily
import com.renova.mobile.ui.theme.RenovaColors
import com.renova.mobile.ui.theme.LocalRenovaColors
import com.renova.mobile.viewmodel.BusinessHistoryViewModel
import androidx.compose.foundation.lazy.items
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



@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun BusinessHomeScreen(
    onLogout: () -> Unit,
    vm: BusinessSaleViewModel = viewModel(),
    onNavigateToCashout: () -> Unit = {}
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
    var showUserProfile by remember { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Column(modifier = Modifier.fillMaxSize()) {
        BusinessSectionHeader(
            title = stringResource(id = R.string.bottom_nav_home),
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
                        .clickable { showUserProfile = true },
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
                                text = "Mi Perfil",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Editar información personal",
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
                                text = "No hay ventas recientes",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = colors.textSecondary,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Las ventas aparecerán aquí",
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
                                    text = "Última Venta",
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
                            val pesosValue = lastSaleSummary!!.totalPoints * 0.10
                            val pesosFormatted = java.text.NumberFormat.getCurrencyInstance(java.util.Locale.forLanguageTag("es-MX")).format(pesosValue)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    if (firstItem != null) {
                                        Text(
                                            text = "Producto: ${firstItem.name}",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontFamily = PoppinsFontFamily
                                            ),
                                            color = colors.textPrimary,
                                            maxLines = 2,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "Cantidad: ${firstItem.quantity}",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontFamily = PoppinsFontFamily
                                            ),
                                            color = colors.textPrimary
                                        )
                                    } else {
                                        Text(
                                            text = "Producto: Sin especificar",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontFamily = PoppinsFontFamily
                                            ),
                                            color = colors.textPrimary
                                        )
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Total Puntos",
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
                                        text = "Equivalente MXN: $pesosFormatted",
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
                                    text = "Ver Detalle Completo",
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
                                text = "Cobrar Puntos",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = colors.textPrimary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Card de estadísticas
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .shadow(
                                elevation = 3.dp,
                                shape = RoundedCornerShape(16.dp),
                                spotColor = RenovaColors.Light.ActivityShadowColor
                            )
                            .clickable { /* TODO: Implementar estadísticas */ },
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
                                text = "Estadísticas",
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

                Spacer(modifier = Modifier.height(24.dp))

                // Sección de Historial del Comercio
                Text(
                    text = "Historial de Actividades",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold
                    ),
                    color = colors.textPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Cards de historial
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
                                 text = "Error al cargar historial",
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
                                     text = "Reintentar",
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
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(historyState.activities) { activity ->
                            HistoryActivityCard(activity = activity, colors = colors)
                        }
                        
                        if (historyState.activities.isEmpty()) {
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
                                             text = "No hay actividades registradas",
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
                    // Controles de paginación (si hay más de 1 página)
                    if (historyState.totalPages > 1) {
                        com.renova.mobile.ui.components.PaginationControls(
                            currentPage = historyState.currentPage,
                            totalPages = historyState.totalPages,
                            isLoading = historyState.isLoading,
                            renovaColors = colors,
                            onPreviousPage = { historyViewModel.previousPage() },
                            onNextPage = { historyViewModel.nextPage() }
                        )
                    }
                }
            }
        }
    }

    // Detalle de compra en bottom sheet
    if (showDetail && lastSaleSummary != null) {
        androidx.compose.material3.ModalBottomSheet(
            onDismissRequest = { showDetail = false },
            sheetState = bottomSheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Detalle de compra",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold
                    ),
                    color = colors.textPrimary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Comercio: ${lastSaleSummary!!.allianceName ?: "Sin especificar"}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = PoppinsFontFamily),
                        color = colors.textPrimary
                    )
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Total Puntos",
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = PoppinsFontFamily),
                            color = colors.textSecondary
                        )
                        Text(
                            text = "${lastSaleSummary!!.totalPoints}",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold
                            ),
                            color = RenovaColors.Primary
                        )
                    }
                }
                val mxn = NumberFormat.getCurrencyInstance(Locale("es", "MX")).format(lastSaleSummary!!.totalPoints * 0.10)
                Text(
                    text = "Equivalente MXN: $mxn",
                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = PoppinsFontFamily),
                    color = RenovaColors.Primary
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    lastSaleSummary!!.items.forEach { item ->
                        Text(
                            text = "• ${item.name} x${item.quantity}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = PoppinsFontFamily),
                            color = colors.textPrimary
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    androidx.compose.material3.OutlinedButton(
                        onClick = {
                            showDetail = false
                            showPrinterSelection = true
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Imprimir")
                    }
                    Button(
                        onClick = { showDetail = false },
                        modifier = Modifier.weight(1f),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = RenovaColors.Primary)
                    ) {
                        Text("Cerrar", color = Color.White)
                    }
                }
            }
        }
    }

    // Close BusinessHomeScreen composable
}

@Composable
fun HistoryActivityCard(
    activity: com.renova.mobile.network.ActivityItem,
    colors: com.renova.mobile.ui.theme.RenovaColorScheme
) {
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono según el tipo de actividad
            val (icon, iconColor) = when (activity.type_history) {
                1 -> Icons.Default.Recycling to RenovaColors.Success // Reciclaje
                2 -> Icons.Default.Star to RenovaColors.Warning // Recompensa
                else -> Icons.Default.ShoppingCart to RenovaColors.Primary // Compra
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
                        1 -> "Reciclaje - ${activity.material_type?.name ?: "Material"}"
                        2 -> activity.reward?.name ?: "Recompensa"
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
                

                
                // Fecha
                Text(
                    text = formatDate(activity.created_at),
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
                Text(
                    text = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("es","MX")).format(activity.points * 0.10),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold
                    ),
                    color = RenovaColors.Primary
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
    }
}

fun formatDate(dateString: String): String {
    return try {
        val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", java.util.Locale.getDefault())
        val outputFormat = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
        val date = inputFormat.parse(dateString)
        if (date == null) return dateString
        outputFormat.format(date)
    } catch (e: Exception) {
        dateString
    }
}

