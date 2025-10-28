package com.renova.mobile.ui.screens.business.payments

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.renova.mobile.ui.components.BusinessSectionHeader
import com.renova.mobile.ui.theme.PoppinsFontFamily
import com.renova.mobile.ui.theme.RenovaColors
import com.renova.mobile.ui.theme.LocalRenovaColors
import com.renova.mobile.viewmodel.BusinessHistoryViewModel
import java.text.NumberFormat
import java.util.Locale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.History
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.style.TextOverflow
import com.renova.mobile.ui.components.formatFriendlyDate
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.ui.res.painterResource
import com.renova.mobile.network.ApiClient
import com.renova.mobile.utils.SessionManager
import kotlinx.coroutines.launch
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.zIndex

private const val POINT_TO_MXN = 0.01  // 1 centavo por punto

@Composable
fun PointsCashoutScreen(
    onLogout: () -> Unit,
    onNavigateBack: () -> Unit = {},
    historyViewModel: BusinessHistoryViewModel = viewModel()
) {
    val colors = LocalRenovaColors.current
    val historyState by historyViewModel.state.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sessionManager = remember { SessionManager(context) }

    // Estados para los puntos disponibles
    var currentMonthPoints by remember { mutableStateOf(0) }
    var isLoadingPoints by remember { mutableStateOf(true) }
    var pointsError by remember { mutableStateOf<String?>(null) }

    // Estado para el modal de pago
    var showPaymentModal by remember { mutableStateOf(false) }

    // Función para cargar puntos
    fun loadPoints() {
        scope.launch {
            val user = sessionManager.getUser()
            val allianceId = user?.alliance_id

            android.util.Log.d("PointsCashout", "Usuario: $user")
            android.util.Log.d("PointsCashout", "Alliance ID: $allianceId")

            if (allianceId == null) {
                pointsError = "No se encontró el ID de alianza"
                isLoadingPoints = false
                Toast.makeText(context, "Error: Usuario sin alianza asignada", Toast.LENGTH_LONG).show()
                return@launch
            }

            try {
                isLoadingPoints = true

                android.util.Log.d("PointsCashout", "Llamando API cashCut con Alliance ID: $allianceId (only_return=true)")

                // Solo consultar puntos disponibles (NO hacer corte)
                val response = ApiClient.apiService.getCashCut(
                    allianceId = allianceId,
                    onlyReturn = true
                )

                android.util.Log.d("PointsCashout", "Response code: ${response.code()}")
                android.util.Log.d("PointsCashout", "Response body: ${response.body()}")

                if (response.isSuccessful && response.body()?.success == true) {
                    currentMonthPoints = response.body()?.data?.total_points ?: 0
                    pointsError = null
                    android.util.Log.d("PointsCashout", "Puntos obtenidos: $currentMonthPoints")
                } else {
                    val errorMsg = response.body()?.message ?: response.errorBody()?.string() ?: "Error desconocido"
                    pointsError = errorMsg
                    android.util.Log.e("PointsCashout", "Error API: $errorMsg")
                    Toast.makeText(context, "Error: $errorMsg", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                pointsError = "Error de conexión: ${e.message}"
                android.util.Log.e("PointsCashout", "Excepción: ${e.message}", e)
                Toast.makeText(context, pointsError, Toast.LENGTH_LONG).show()
            } finally {
                isLoadingPoints = false
            }
        }
    }

    // Cargar puntos al iniciar
    LaunchedEffect(Unit) {
        loadPoints()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box {
            BusinessSectionHeader(
                title = "     Cobrar Puntos",
                onLogout = onLogout,
                textColor = Color.White
            )

            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 4.dp)
                    .zIndex(1f)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Atrás",
                    tint = Color.White
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card de conversión y cobro
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Puntos disponibles",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold
                            ),
                            color = RenovaColors.Primary
                        )

                        Text(
                            text = "Tasa de conversión: 1 punto = $0.01 MXN",
                            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = PoppinsFontFamily),
                            color = colors.textSecondary
                        )

                        HorizontalDivider(color = colors.textSecondary.copy(alpha = 0.2f), thickness = 1.dp)

                        // Mostrar loading o puntos
                        if (isLoadingPoints) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(80.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = RenovaColors.Primary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        } else {
                            // Campo de puntos (no editable)
                            OutlinedTextField(
                                value = NumberFormat.getIntegerInstance(Locale("es", "MX"))
                                    .format(currentMonthPoints),
                                onValueChange = { },
                                label = { Text("Puntos disponibles") },
                                enabled = false,
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledTextColor = colors.textPrimary,
                                    disabledBorderColor = RenovaColors.Primary.copy(alpha = 0.5f),
                                    disabledLabelColor = colors.textSecondary
                                )
                            )

                            val pesos = currentMonthPoints * POINT_TO_MXN
                            val pesosFormatted = NumberFormat.getCurrencyInstance(Locale("es", "MX")).format(pesos)

                            // Equivalente en MXN destacado
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = RenovaColors.Primary.copy(alpha = 0.1f)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Equivalente:",
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.Medium
                                        ),
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = pesosFormatted,
                                        style = MaterialTheme.typography.headlineMedium.copy(
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = RenovaColors.Primary
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    if (currentMonthPoints <= 0) {
                                        Toast.makeText(context, "No hay puntos disponibles para cobrar", Toast.LENGTH_SHORT).show()
                                    } else {
                                        showPaymentModal = true
                                    }
                                },
                                enabled = currentMonthPoints > 0 && pointsError == null,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = RenovaColors.Primary,
                                    disabledContainerColor = colors.textSecondary.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = when {
                                        pointsError != null -> "Error al cargar puntos"
                                        currentMonthPoints > 0 -> "Generar cobro"
                                        else -> "No hay puntos disponibles"
                                    },
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = Color.White,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Título de historial
            item {
                Text(
                    text = "Historial de ventas",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold
                    ),
                    color = colors.textPrimary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Contenido del historial
            if (historyState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = RenovaColors.Primary)
                    }
                }
            } else if (historyState.error != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
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
                                color = colors.textSecondary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { historyViewModel.retry() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = RenovaColors.Primary
                                )
                            ) {
                                Text(
                                    text = "Reintentar",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = PoppinsFontFamily
                                    ),
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            } else if (historyState.activities.isEmpty()) {
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
                                color = colors.textSecondary
                            )
                        }
                    }
                }
            } else {
                itemsIndexed(historyState.activities) { index, activity ->
                    HistorySaleItem(
                        activity = activity,
                        colors = colors
                    )
                    if (index < historyState.activities.size - 1) {
                        HorizontalDivider(
                            color = RenovaColors.PrimaryColor,
                            thickness = 1.dp,
                            modifier = Modifier.padding(vertical = 3.dp)
                        )
                    }
                }
            }

            // Controles de paginación
            if (historyState.totalPages > 1) {
                item {
                    com.renova.mobile.ui.components.PaginationControls(
                        currentPage = historyState.currentPage,
                        totalPages = historyState.totalPages,
                        isLoading = historyState.isLoading,
                        renovaColors = colors.copy(
                            buttonEnabled = RenovaColors.SecondaryHoverColor,
                            buttonDisabled = RenovaColors.SecondaryColor
                        ),
                        onPreviousPage = { historyViewModel.previousPage() },
                        onNextPage = { historyViewModel.nextPage() }
                    )
                }
            }
        }
    }

    // Modal de pago simulado
    if (showPaymentModal) {
        PaymentSimulationModal(
            onDismiss = { showPaymentModal = false },
            onPaymentComplete = { transactionId ->
                Toast.makeText(
                    context,
                    "Pago completado exitosamente\nID: $transactionId",
                    Toast.LENGTH_LONG
                ).show()
                showPaymentModal = false

                // Recargar puntos después del cobro
                loadPoints()
            }
        )
    }
}

@Composable
private fun HistorySaleItem(
    activity: com.renova.mobile.network.ActivityItem,
    colors: com.renova.mobile.ui.theme.RenovaColorScheme
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Determinar ícono y color según el tipo de actividad
        when (activity.type_history) {
            4 -> {
                // COBRO - Ícono personalizado
                Icon(
                    imageVector = Icons.Default.AttachMoney,
                    contentDescription = null,
                    tint = RenovaColors.Success,
                    modifier = Modifier.size(32.dp)
                )
            }
            2 -> {
                // RECICLAJE
                Icon(
                    imageVector = Icons.Default.Recycling,
                    contentDescription = null,
                    tint = RenovaColors.Success,
                    modifier = Modifier.size(32.dp)
                )
            }
            1 -> {
                // CANJEO
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = null,
                    tint = RenovaColors.Primary,
                    modifier = Modifier.size(32.dp)
                )
            }
            else -> {
                // OTROS
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = RenovaColors.Warning,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = when (activity.type_history) {
                    4 -> "Liquidación"
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
                4 -> "Retiro de puntos"
                2 -> activity.material_type?.name ?: ""
                1 -> {
                    val name = activity.reward?.name ?: "Recompensa"
                    "1 x $name"
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

            Text(
                text = formatFriendlyDate(activity.created_at),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = PoppinsFontFamily
                ),
                color = colors.textSecondary
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            val mxnValue = activity.points * POINT_TO_MXN
            val mxnColor = if (mxnValue < 0) colors.negativePoints else colors.primaryColor
            Text(
                text = NumberFormat.getCurrencyInstance(Locale("es","MX")).format(mxnValue),
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
}

