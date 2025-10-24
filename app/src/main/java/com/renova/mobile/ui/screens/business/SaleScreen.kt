package com.renova.mobile.ui.screens.business

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.renova.mobile.ui.components.BusinessHeader
import com.renova.mobile.viewmodel.BusinessSaleViewModel
import com.renova.mobile.ui.theme.PoppinsFontFamily
import com.renova.mobile.ui.theme.RenovaColors
import androidx.activity.compose.rememberLauncherForActivityResult
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.renova.mobile.ui.components.SectionHeader
import com.renova.mobile.R
import androidx.compose.ui.res.stringResource
import com.renova.mobile.ui.components.BusinessSectionHeader
import com.renova.mobile.ui.components.SaleDetailModal
import com.renova.mobile.ui.components.SaleSummary
import com.renova.mobile.ui.components.SaleItem
import android.widget.Toast
import androidx.navigation.NavController
import com.renova.mobile.navigation.NavigationItemBusiness
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import kotlinx.coroutines.launch
import com.renova.mobile.ui.components.PrinterSelectionModal
import com.renova.mobile.utils.TicketPrinter
import com.renova.mobile.utils.PrinterModel

import com.renova.mobile.utils.SessionManager
import com.renova.mobile.network.ApiClient
import com.renova.mobile.network.RegisterFcmTokenRequest

@Composable
fun BusinessStoreScreen(onLogout: () -> Unit, vm: BusinessSaleViewModel = viewModel(), navController: NavController) {
    val user by vm.scannedUser.collectAsState()
    val ticket by vm.ticket.collectAsState()
    val isRewardLoading by vm.isRewardLoading.collectAsState()
    val error by vm.error.collectAsState()
    val lastSaleSummary by vm.lastSaleSummary.collectAsState()

    val scrollState = rememberScrollState()
    val context = LocalContext.current

    val sessionManager = remember { SessionManager(context) }
    val userCommerce = sessionManager.getUser()

    // Limpiar errores automáticamente después de 3 segundos
    LaunchedEffect(error) {
        if (!error.isNullOrBlank()) {
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            kotlinx.coroutines.delay(3000)
            vm.clearError()
        }
    }

    // Toast para indicar proceso de escaneo/agregado de recompensa
    LaunchedEffect(isRewardLoading) {
        if (isRewardLoading) {
            Toast.makeText(context, "Agregando recompensa al ticket...", Toast.LENGTH_SHORT).show()
        }
    }

    val rewardScanner = rememberLauncherForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            if (user != null) {
                Toast.makeText(context, "Recompensa detectada, agregando...", Toast.LENGTH_SHORT).show()
                vm.addRewardByCode(result.contents.trim())
            } else {
                vm.setError("Primero escanee al consumidor para agregar recompensas")
            }
        }
    }

    fun startRewardScanner() {
        val options = ScanOptions().apply {
            setDesiredBarcodeFormats(ScanOptions.ONE_D_CODE_TYPES)
            setPrompt("Escanee el código de barras de la recompensa")
            setBeepEnabled(true)
            setOrientationLocked(true)
            setCaptureActivity(com.renova.mobile.scan.PortraitCaptureActivity::class.java)
        }
        rewardScanner.launch(options)
    }

    val scope = rememberCoroutineScope()

    var showSaleDetail by remember { mutableStateOf(false) }
    var showPrinterSelection by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var localPrintSummary by remember { mutableStateOf<SaleSummary?>(null) }

    // Permiso de notificaciones para Android 13+
    var pendingNotify by remember { mutableStateOf(false) }

    val sendNotificationAndFinalize = {
        val channelId = "venta_finalizada_channel"
        val notificationManager = context.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                channelId,
                "Ventas",
                android.app.NotificationManager.IMPORTANCE_HIGH
            ).apply {
                enableVibration(true)
                setShowBadge(true)
                description = "Notificaciones de ventas finalizadas"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Construir resumen de venta para el modal antes de enviar push
        val grouped = ticket.groupBy { it.code ?: it.id?.toString() ?: it.name }
        val totalPoints = grouped.values.sumOf { group -> group.size * group.first().pointsRequired }
        val items = grouped.map { (_, items) ->
            val reward = items.first()
            SaleItem(name = reward.name, quantity = items.size, pointsRequired = reward.pointsRequired)
        }
        val summary = SaleSummary(
            id = System.currentTimeMillis().toString(),
            allianceName = userCommerce?.name ?: "N/A",
            consumerName = user?.name,
            totalPoints = totalPoints,
            items = items
        )
        vm.setLastSaleSummary(summary)
        isSubmitting = true
        Toast.makeText(context, "Finalizando la compra...", Toast.LENGTH_SHORT).show()

        // Asegurar registro del token FCM del comercio justo antes del claim
        val merchantId = userCommerce?.id
        val fcmToken = sessionManager.getFcmToken()
        if (merchantId != null && !fcmToken.isNullOrBlank()) {
            scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    ApiClient.init(context)
                    ApiClient.apiService.registerFcmToken(
                        RegisterFcmTokenRequest(userId = merchantId, token = fcmToken!!)
                    )
                } catch (_: Exception) {
                    // no-op: no bloquear la venta por fallo de registro
                }
            }
        }

        // Llamar API reward/claim
        vm.claimRewards(
            merchantUserId = userCommerce?.id,
            fcmToken = null, // El backend maneja las notificaciones usando tokens registrados
            onSuccess = { message ->
                isSubmitting = false
                // Limpiar estado local y mover al Home para visualizar última venta
                vm.finalizeSale()
                navController.navigate(NavigationItemBusiness.Home.route)
                Toast.makeText(context, "Venta finalizada", Toast.LENGTH_SHORT).show()
            },
            onError = { err ->
                isSubmitting = false
                Toast.makeText(context, err, Toast.LENGTH_LONG).show()
            },
            onPushFeedback = { feedback ->
                // Mostrar resultado de push si aplica (ya no se envía desde cliente)
                if (feedback.isNotBlank()) {
                    Toast.makeText(context, feedback, Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted && pendingNotify) {
            pendingNotify = false
            // Enviar notificación y mostrar modal de detalle con resumen
            sendNotificationAndFinalize()
        } else if (!granted) {
            vm.setError("Permiso de notificaciones denegado")
        }
    }

    // var lastSaleSummary by remember { mutableStateOf<SaleSummary?>(null) }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
        BusinessSectionHeader(
            title = stringResource(id = R.string.bottom_nav_sale),
            onLogout = onLogout,
            textColor = Color.White
        )

        // Datos del consumidor
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = RenovaColors.BackgroundMint),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Consumidor",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = RenovaColors.Primary
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        if (user != null && ticket.isEmpty()) {
                            IconButton(onClick = {
                                vm.finalizeSale()
                                Toast.makeText(context, "Venta limpiada. Escanee un nuevo consumidor.", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Limpiar venta",
                                    tint = RenovaColors.Primary
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    if (user != null) {
                        Text(
                            text = "Nombre: ${user!!.name} ${user!!.last_name ?: ""}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = Color.Black
                        )
                        Text(
                            text = "Puntos disponibles: " + java.text.NumberFormat.getIntegerInstance(java.util.Locale("es","MX")).format(user!!.total_points),
                            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = PoppinsFontFamily),
                            color = Color.Black
                        )
                    } else {
                        Text(
                            text = "Aún no se ha escaneado al consumidor",
                            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = PoppinsFontFamily),
                            color = Color.DarkGray
                        )
                    }
                }
            }

        // Sección: Comercio identificado (sin mostrar ID)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = RenovaColors.BackgroundMint)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Comercio",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold
                    ),
                    color = RenovaColors.Primary
                )
                if (userCommerce != null) {
                    Text(
                        text = "Nombre: ${userCommerce.name}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color.Black
                    )
                } else {
                    Text(
                        text = "No hay comercio identificado",
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = PoppinsFontFamily),
                        color = Color.DarkGray
                    )
                }
            }
        }

        // Ticket y acciones
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = RenovaColors.BackgroundMint),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ticket",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = RenovaColors.Primary
                    )
                    Button(
                        onClick = {
                            if (user != null) {
                                startRewardScanner()
                            } else {
                                vm.setError("Primero escanee al consumidor para agregar recompensas")
                            }
                        }
                    ) { Text("Agregar recompensa") }
                }
                Spacer(modifier = Modifier.height(8.dp))
                if (ticket.isEmpty()) {
                    Text(
                        text = "No hay recompensas en el ticket",
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = PoppinsFontFamily),
                        color = Color.DarkGray
                    )
                } else {
                    val groupedByName = remember(ticket) { ticket.groupBy { it.name } }
                    groupedByName.entries.forEachIndexed { index, (name, items) ->
                        val quantity = items.size
                        val pointsPerItem = items.first().pointsRequired
                        val subtotalPoints = quantity * pointsPerItem

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = Color.Black
                                )
                                Text(
                                    text = "Cantidad: $quantity",
                                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = PoppinsFontFamily),
                                    color = Color.DarkGray
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Puntos",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = Color.DarkGray
                                )
                                Text(
                                    text = java.text.NumberFormat.getIntegerInstance(java.util.Locale("es","MX")).format(subtotalPoints),
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = RenovaColors.Primary
                                )
                            }
                        }

                        if (index < groupedByName.size - 1) {
                            Divider(modifier = Modifier.padding(vertical = 8.dp), thickness = 1.dp, color = Color.LightGray)
                        }
                    }

                    val totalPoints = groupedByName.values.sumOf { group -> group.size * group.first().pointsRequired }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total de puntos",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Medium
                            ),
                            color = Color.DarkGray
                        )
                        Text(
                            text = java.text.NumberFormat.getIntegerInstance(java.util.Locale("es","MX")).format(totalPoints),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold
                            ),
                            color = RenovaColors.Primary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                vm.finalizeSale()
                                Toast.makeText(context, "Venta limpiada. Escanee un nuevo consumidor.", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f)
                        ) { Text("Limpiar venta") }

                        Button(
                            onClick = {
                                if (userCommerce == null) {
                                    vm.setError("Identifica el comercio antes de finalizar la compra")
                                    return@Button
                                }
                                if (ticket.isEmpty()) {
                                    vm.setError("Agrega recompensas al ticket")
                                    return@Button
                                }

                                val userPts = user?.total_points ?: 0
                                val totalTicketPoints = ticket.sumOf { it.pointsRequired }
                                if (userPts < totalTicketPoints) {
                                    vm.setError("Puntos insuficientes para finalizar la compra")
                                    return@Button
                                }

                                if (Build.VERSION.SDK_INT >= 33) {
                                    val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                                    if (!granted) {
                                        pendingNotify = true
                                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        sendNotificationAndFinalize()
                                    }
                                } else {
                                    sendNotificationAndFinalize()
                                }
                            },
                            enabled = userCommerce != null && ticket.isNotEmpty() && !isSubmitting,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Finalizar compra")
                        }
                    }

                    if (isSubmitting) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(color = RenovaColors.Primary, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }

        // Sale detail modal should only render if summary is non-null
        if (showSaleDetail && lastSaleSummary != null) {
            SaleDetailModal(
                summary = lastSaleSummary!!,
                onClose = {
                    showSaleDetail = false
                    vm.finalizeSale()
                },
                onPrint = {
                    localPrintSummary = lastSaleSummary
                    showSaleDetail = false
                    vm.finalizeSale()
                    showPrinterSelection = true
                }
            )
        }
    }

    // Printer Selection Modal
    if (showPrinterSelection && localPrintSummary != null) {
        PrinterSelectionModal(
            summary = localPrintSummary!!,
            onClose = { showPrinterSelection = false },
            onPrintSelected = { printer ->
                scope.launch {
                    try {
                        Toast.makeText(context, "Imprimiendo en ${printer.getDisplayName()}...", Toast.LENGTH_SHORT).show()
                        TicketPrinter.printTicket(localPrintSummary!!, printer, context)
                        Toast.makeText(context, "Ticket impreso correctamente", Toast.LENGTH_SHORT).show()

                        navController.navigate(NavigationItemBusiness.Home.route)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error al imprimir: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                    showPrinterSelection = false
                }
            }
        )
    }
}

