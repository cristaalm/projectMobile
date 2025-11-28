package com.renova.mobile.ui.screens.business

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.gestures.detectTapGestures
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
import com.renova.mobile.ui.theme.LocalRenovaColors
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.background

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
    val colors = LocalRenovaColors.current

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
            Toast.makeText(context, context.getString(R.string.adding_reward_to_ticket), Toast.LENGTH_SHORT).show()
        }
    }

    val rewardScanner = rememberLauncherForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            if (user != null) {
                Toast.makeText(context, context.getString(R.string.product_reward_detected), Toast.LENGTH_SHORT).show()
                vm.addRewardByCode(result.contents.trim())
            } else {
                vm.setError(context.getString(R.string.first_scan_consumer))
            }
        }
    }

    fun startRewardScanner() {
        val options = ScanOptions().apply {
            setDesiredBarcodeFormats(ScanOptions.ONE_D_CODE_TYPES)
            setPrompt(context.getString(R.string.product_reward))
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
        Toast.makeText(context, context.getString(R.string.finishing_purchase), Toast.LENGTH_SHORT).show()

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
                Toast.makeText(context, context.getString(R.string.sale_finished), Toast.LENGTH_SHORT).show()
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
            vm.setError(context.getString(R.string.notification_permission_denied))
        }
    }

    // var lastSaleSummary by remember { mutableStateOf<SaleSummary?>(null) }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
        BusinessSectionHeader(
            title = stringResource(id = R.string.tab_sale),
            onLogout = onLogout,
            textColor = Color.White
        )

        // Datos del consumidor
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 16.dp, end = 16.dp)
                    .shadow(
                        elevation = 3.dp,
                        shape = RoundedCornerShape(16.dp),
                        spotColor = RenovaColors.Light.ActivityShadowColor
                    ),
                colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(id = R.string.consumer),
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
                                Toast.makeText(context, context.getString(R.string.sale_cleaned), Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = stringResource(R.string.clear_sale),
                                    tint = RenovaColors.Primary
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    if (user != null) {
                        Text(
                            text = stringResource(id = R.string.name_label) + " ${user!!.name} ${user!!.last_name ?: ""}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = colors.textPrimary
                        )
                        Text(
                            text = stringResource(id = R.string.available_points_label) + " " + java.text.NumberFormat.getIntegerInstance(java.util.Locale("es","MX")).format(user!!.total_points),
                            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = PoppinsFontFamily),
                            color = colors.textPrimary
                        )
                    } else {
                        Text(
                            text = stringResource(id = R.string.consumer_not_scanned),
                            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = PoppinsFontFamily),
                            color = colors.textSecondary
                        )
                    }
                }
            }

        // Ticket y acciones
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 16.dp, end = 16.dp)
                .shadow(
                    elevation = 3.dp,
                    shape = RoundedCornerShape(16.dp),
                    spotColor = RenovaColors.Light.ActivityShadowColor
                ),
            colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.ticket),
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
                                vm.setError(context.getString(R.string.consumer_not_scanned))
                            }
                        }
                    ) { Text(context.getString(R.string.add_reward)) }
                }
                Spacer(modifier = Modifier.height(8.dp))
                if (ticket.isEmpty()) {
                    Text(
                        text = context.getString(R.string.no_rewards_in_ticket),
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = PoppinsFontFamily),
                        color = colors.textSecondary
                    )
                } else {
                    var selectedName by remember { mutableStateOf<String?>(null) }
                    val groupedByName = remember(ticket) { ticket.groupBy { it.name } }
                    groupedByName.entries.forEachIndexed { index, (name, items) ->
                        val quantity = items.size
                        val pointsPerItem = items.first().pointsRequired
                        val subtotalPoints = quantity * pointsPerItem

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (selectedName == name) colors.negativePoints.copy(alpha = 0.10f) else Color.Transparent)
                                .padding(8.dp)
                                .pointerInput(name) {
                                    detectTapGestures(
                                        onTap = {
                                            selectedName = name
                                        },
                                        onDoubleTap = {
                                            vm.removeRewardGroupByName(name)
                                            if (selectedName == name) selectedName = null
                                        }
                                    )
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = if (selectedName == name) colors.negativePoints else colors.textPrimary
                                )
                                Text(
                                    text = context.getString(R.string.quantity_label) + " $quantity",
                                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = PoppinsFontFamily),
                                    color = colors.textSecondary
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = stringResource(R.string.available_points_label),
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.Medium
                                        ),
                                        color = colors.textSecondary
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
                                IconButton(onClick = {
                                    vm.removeRewardGroupByName(name)
                                    if (selectedName == name) selectedName = null
                                }) {
                                    Icon(Icons.Filled.Close, contentDescription = null, tint = colors.textSecondary)
                                }
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
                            text = context.getString(R.string.total_points_label),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Medium
                            ),
                            color = colors.textSecondary
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
                                Toast.makeText(context, context.getString(R.string.sale_cleaned), Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f)
                        ) { Text( context.getString(R.string.clear_sale)) }

                        Button(
                            onClick = {
                                if (userCommerce == null) {
                                    vm.setError("Identifica el comercio antes de finalizar la compra")
                                    return@Button
                                }
                                if (ticket.isEmpty()) {
                                    vm.setError(context.getString(R.string.notification_add_reward_to_ticket))
                                    return@Button
                                }

                                val userPts = user?.total_points ?: 0
                                val totalTicketPoints = ticket.sumOf { it.pointsRequired }
                                if (userPts < totalTicketPoints) {
                                    vm.setError(context.getString(R.string.notification_insufficient_points))
                                    return@Button
                                }

                                // Validar que no haya recompensas expiradas en el ticket
                                if (vm.hasExpiredRewardsInTicket()) {
                                    vm.setError(context.getString(R.string.expired_reward_in_ticket))
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
                            Text(context.getString(R.string.finish_purchase))
                        }
                    }

                    if (isSubmitting) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(color = RenovaColors.Primary, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }
 
        if (showSaleDetail && lastSaleSummary != null) {
            SaleDetailModal(
                viewModel = vm, // ✅ Ahora pasamos el ViewModel directamente
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
                        Toast.makeText(context, context.getString(R.string.printing_in) + " ${printer.getDisplayName()}...", Toast.LENGTH_SHORT).show()
                        TicketPrinter.printTicket(localPrintSummary!!, printer, context)
                        Toast.makeText(context, context.getString(R.string.printing_success), Toast.LENGTH_SHORT).show()

                        navController.navigate(NavigationItemBusiness.Home.route)
                    } catch (e: Exception) {
                        Toast.makeText(context, context.getString(R.string.printing_error) + ": ${e.message}", Toast.LENGTH_LONG).show()
                    }
                    showPrinterSelection = false
                }
            }
        )
    }
}

