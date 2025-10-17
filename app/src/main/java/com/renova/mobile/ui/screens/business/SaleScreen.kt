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
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
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

@Composable
fun BusinessStoreScreen(onLogout: () -> Unit, vm: BusinessSaleViewModel = viewModel(), navController: NavController) {
    val user by vm.scannedUser.collectAsState()
    val ticket by vm.ticket.collectAsState()
    val isRewardLoading by vm.isRewardLoading.collectAsState()
    val error by vm.error.collectAsState()
    val alliance by vm.businessAlliance.collectAsState()
    val allianceId by vm.businessAllianceId.collectAsState()
    val lastSaleSummary by vm.lastSaleSummary.collectAsState()

    val scrollState = rememberScrollState()
    val context = LocalContext.current



    // Limpiar errores automáticamente después de 3 segundos
    LaunchedEffect(error) {
        if (error != null) {
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            kotlinx.coroutines.delay(3000)
            vm.clearError()
        }
    }

    val rewardScanner = rememberLauncherForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            if (user != null) {
                vm.addRewardByCode(result.contents.trim())
            } else {
                vm.setError("Primero escanee al consumidor para agregar recompensas")
            }
        } else {
            vm.setError("No se detectó ningún código. Intente nuevamente.")
        }
    }

    fun startRewardScanner() {
        val options = ScanOptions().apply {
            setCaptureActivity(com.renova.mobile.scan.PortraitCaptureActivity::class.java)
            setDesiredBarcodeFormats(ScanOptions.ALL_CODE_TYPES)
            setPrompt("Escanee el código de la recompensa")
            setBeepEnabled(true)
            setOrientationLocked(true)
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
                android.app.NotificationManager.IMPORTANCE_DEFAULT
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
            allianceName = alliance?.name,
            consumerName = user?.name,
            totalPoints = totalPoints,
            items = items
        )
        vm.setLastSaleSummary(summary)
        isSubmitting = true
        Toast.makeText(context, "Procesando venta...", Toast.LENGTH_SHORT).show()

        // Llamar API reward/claim antes de enviar notificación
        vm.claimRewards(
            onSuccess = { message ->
                // Notificación local para el comerciante con más detalle
                val title = "Venta finalizada - ${user?.name ?: "Consumidor"}"
                val notification = NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle(title)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                    .setAutoCancel(true)
                    .build()
                NotificationManagerCompat.from(context).notify(1001, notification)
                // Feedback inmediato
                Toast.makeText(context, "Venta registrada", Toast.LENGTH_SHORT).show()
                isSubmitting = false
                showSaleDetail = true
            },
            onError = { errorMessage ->
                // Enviar notificación push con mensaje de error
                val notification = NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(android.R.drawable.ic_dialog_alert)
                    .setContentTitle("Error en venta")
                    .setContentText(errorMessage)
                    .setAutoCancel(true)
                    .build()
                NotificationManagerCompat.from(context).notify(1002, notification)
                vm.setError(errorMessage)
                isSubmitting = false
                Toast.makeText(context, "Error al enviar la venta", Toast.LENGTH_SHORT).show()
            }
        )

        // No limpiar inmediatamente para que modal muestre datos; limpiar al cerrar modal
        // vm.finalizeSale() // mover a onClose del modal
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
                            text = "Puntos disponibles: ${user!!.total_points}",
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
                if (alliance != null) {
                    Text(
                        text = "Nombre: ${alliance!!.name}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color.Black
                    )
                    // Se elimina la muestra de puntos en la tarjeta de comercio
                } else {
                    Text(
                        text = "Aún no se identifica el comercio. Escanea una recompensa para identificarlo.",
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = PoppinsFontFamily),
                        color = Color.DarkGray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Sección: Agregar recompensa (solo escáner, requiere consumidor)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = RenovaColors.BackgroundMint)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Agregar recompensa",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold
                    ),
                    color = RenovaColors.Primary
                )

                Button(onClick = { startRewardScanner() }, modifier = Modifier.fillMaxWidth(), enabled = user != null) {
                    Text("Escanear recompensa")
                }

                if (user == null) {
                    Text(
                        text = "Primero escanee al consumidor para agregar recompensas",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = PoppinsFontFamily),
                        color = Color.DarkGray
                    )
                }

                if (error != null) {
                    Text(text = error ?: "", color = Color.Red)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Sección: Ticket
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = RenovaColors.BackgroundMint)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Ticket",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.ExtraBold
                    )
                )

                if (ticket.isEmpty()) {
                    Text(
                        text = "Aún no hay recompensas en el ticket",
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = PoppinsFontFamily),
                        color = Color.Gray
                    )
                } else {
                    val grouped = remember(ticket) {
                        ticket.groupBy { it.code ?: it.id?.toString() ?: it.name }
                    }

                    val totalPoints = grouped.values.sumOf { group ->
                        val pts = group.first().pointsRequired
                        group.size * pts
                    }

                    grouped.forEach { (_, items) ->
                        val reward = items.first()
                        val quantity = items.size
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp),
                                ) {
                                    Text(
                                        text = quantity.toString(),
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = RenovaColors.Primary
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = reward.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = Color.Black
                                    )
                                    Text(
                                        text = "Puntos requeridos: ${reward.pointsRequired}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = PoppinsFontFamily),
                                        color = Color.DarkGray
                                    )
                                }
                            }
                        }
                    }

                    Divider()
                    Text(
                        text = "Total de puntos: $totalPoints",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = {
                            vm.finalizeSale()
                            Toast.makeText(context, "Venta limpiada. Escanee un nuevo consumidor.", Toast.LENGTH_SHORT).show()
                        }) { Text("Limpiar venta") }
                        Button(
                            onClick = {
                                // Validaciones antes de finalizar
                                if (allianceId == null) {
                                    vm.setError("Identifica el comercio antes de finalizar la compra")
                                    return@Button
                                }
                                if (ticket.isEmpty()) {
                                    vm.setError("Agrega recompensas al ticket")
                                    return@Button
                                }

                                // Validar puntos del cliente suficientes para el total del ticket
                                val userPts = user?.total_points ?: 0
                                val totalTicketPoints = ticket.sumOf { it.pointsRequired }
                                if (userPts < totalTicketPoints) {
                                    vm.setError("Puntos insuficientes para finalizar la compra")
                                    return@Button
                                }

                                if (Build.VERSION.SDK_INT >= 33) {
                                    val granted = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.POST_NOTIFICATIONS
                                    ) == PackageManager.PERMISSION_GRANTED
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
                            enabled = allianceId != null && ticket.isNotEmpty() && !isSubmitting
                        ) {
                            Text("Finalizar compra")
                        }
                        if (isSubmitting) {
                            Spacer(modifier = Modifier.width(8.dp))
                            LinearProgressIndicator(color = RenovaColors.Primary, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            }
        }
    }
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

