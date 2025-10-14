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

@Composable
fun BusinessStoreScreen(onLogout: () -> Unit, vm: BusinessSaleViewModel = viewModel()) {
    val user by vm.scannedUser.collectAsState()
    val ticket by vm.ticket.collectAsState()
    val isRewardLoading by vm.isRewardLoading.collectAsState()
    val error by vm.error.collectAsState()
    val alliance by vm.businessAlliance.collectAsState()
    val allianceId by vm.businessAllianceId.collectAsState()

    val scrollState = rememberScrollState()

    // Establecer una alianza por defecto para mostrar el nombre del comercio desde el inicio
    LaunchedEffect(Unit) {
        vm.setBusinessAllianceId(114)
    }

    // Limpiar errores automáticamente después de 3 segundos
    LaunchedEffect(error) {
        if (error != null) {
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

    val context = LocalContext.current

    // Permiso de notificaciones para Android 13+
    var pendingNotify by remember { mutableStateOf(false) }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted && pendingNotify) {
            pendingNotify = false
            // Enviar notificación si estaba pendiente
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
            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Venta finalizada")
                .setContentText("La venta se ha completado correctamente.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .build()
            NotificationManagerCompat.from(context).notify(1001, notification)
            vm.finalizeSale()
        } else if (!granted) {
            vm.setError("Permiso de notificaciones denegado")
        }
    }

    fun sendNotificationAndFinalize() {
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
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Venta finalizada")
            .setContentText("La venta se ha completado correctamente.")
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(1001, notification)
        val merchantNotification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Venta finalizada")
            .setContentText("La venta se ha completado correctamente.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(1001, merchantNotification)
        if (user != null) {
            val consumerNotification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Venta finalizada")
                .setContentText("Gracias ${user!!.name}, tu compra fue registrada.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setAutoCancel(true)
                .build()
            NotificationManagerCompat.from(context).notify(1002, consumerNotification)
        }
        vm.finalizeSale()
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
        BusinessSectionHeader(
            title = stringResource(id = R.string.bottom_nav_sale),
            onLogout = onLogout,
            textColor = Color.White
        )
        Spacer(modifier = Modifier.height(20.dp))

        // Datos del consumidor
        if (user != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = RenovaColors.BackgroundMint),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Consumidor",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = RenovaColors.Primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
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
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Consumidor",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                )
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
                        OutlinedButton(onClick = { vm.clearTicket() }) { Text("Limpiar ticket") }
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
                            enabled = allianceId != null && ticket.isNotEmpty()
                        ) {
                            Text("Finalizar compra")
                        }
                    }
                }
            }
        }
    }
}

