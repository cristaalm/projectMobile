package com.renova.mobile.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.renova.mobile.ui.theme.PoppinsFontFamily
import com.renova.mobile.ui.theme.RenovaColors
import com.renova.mobile.utils.BluetoothPrinterManager
import com.renova.mobile.utils.PrinterModel
import kotlinx.coroutines.launch
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import android.Manifest
import android.os.Build

@Composable
fun PrinterSelectionModal(
    summary: SaleSummary,
    onClose: () -> Unit,
    onPrintSelected: (PrinterModel) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val bluetoothManager = remember { BluetoothPrinterManager(context) }
    
    var availablePrinters by remember { mutableStateOf<List<PrinterModel>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var connectionStatus by remember { mutableStateOf<Map<String, Boolean>>(emptyMap()) }
    var showPermissionDialog by remember { mutableStateOf(false) }

    // Función para verificar disponibilidad de una impresora
    val checkConnection = { printer: PrinterModel ->
        scope.launch {
            try {
                val isReachable = bluetoothManager.isDeviceReachable(printer.address)
                connectionStatus = connectionStatus + (printer.address to isReachable)
                
                // Actualizar el estado de la impresora
                availablePrinters = availablePrinters.map { p ->
                    if (p.address == printer.address) {
                        p.copy(isConnected = isReachable)
                    } else p
                }
            } catch (e: Exception) {
                connectionStatus = connectionStatus + (printer.address to false)
                availablePrinters = availablePrinters.map { p ->
                    if (p.address == printer.address) {
                        p.copy(isConnected = false)
                    } else p
                }
            }
        }
    }

    // Función para buscar impresoras
    val searchPrinters = {
        scope.launch {
            isSearching = true
            errorMessage = null
            connectionStatus = emptyMap()
            try {
                // Verificar permisos de Bluetooth
                if (!bluetoothManager.hasBluetoothPermissions()) {
                    showPermissionDialog = true
                    isSearching = false
                    return@launch
                }
                
                // Verificar si Bluetooth está disponible
                if (!bluetoothManager.isBluetoothAvailable()) {
                    errorMessage = "Bluetooth no está disponible o no está habilitado."
                    isSearching = false
                    return@launch
                }
                
                val printers = bluetoothManager.detectRTPrinters()
                availablePrinters = printers
                
                if (printers.isEmpty()) {
                    errorMessage = "No se encontraron impresoras RT-3300PB o similares. Asegúrate de que estén emparejadas."
                } else {
                    // Verificar disponibilidad de cada impresora encontrada
                    printers.forEach { printer ->
                        checkConnection(printer)
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Error buscando impresoras: ${e.message}"
            } finally {
                isSearching = false
            }
        }
        Unit
    }

    // Launcher para solicitar permisos
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            // Permisos concedidos, buscar impresoras
            scope.launch { searchPrinters() }
        } else {
            errorMessage = "Se requieren permisos de Bluetooth para buscar impresoras."
        }
        showPermissionDialog = false
    }

    // Función para solicitar permisos
    val requestPermissions = {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
        permissionLauncher.launch(permissions)
    }

    // Buscar impresoras al abrir el modal
    LaunchedEffect(Unit) {
        searchPrinters()
    }

    Dialog(onDismissRequest = onClose) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth(0.95f)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Encabezado
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Print,
                        contentDescription = "Imprimir",
                        tint = RenovaColors.Primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Impresoras RT-3300PB",
                        color = RenovaColors.Primary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = PoppinsFontFamily,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = searchPrinters) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Actualizar",
                            tint = RenovaColors.Primary
                        )
                    }
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Cerrar",
                            tint = RenovaColors.Primary
                        )
                    }
                }

                // Información del ticket a imprimir
                Card(
                    colors = CardDefaults.cardColors(containerColor = RenovaColors.BackgroundMint),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "Ticket a imprimir",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = RenovaColors.Primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Cliente: ${summary.consumerName ?: "N/A"}",
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = PoppinsFontFamily),
                            color = Color.DarkGray
                        )
                        Text(
                            text = "Total puntos: ${summary.totalPoints}",
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = PoppinsFontFamily),
                            color = Color.DarkGray
                        )
                        Text(
                            text = "Cantidad: ${summary.items.size} ${if (summary.items.size == 1) "elemento" else "elementos"}",
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = PoppinsFontFamily),
                            color = Color.DarkGray
                        )
                    }
                }

                // Estado de búsqueda
                if (isSearching) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            color = RenovaColors.Primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Buscando impresoras RT-3300PB...",
                            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = PoppinsFontFamily),
                            color = RenovaColors.Primary
                        )
                    }
                }

                // Mensaje de error
                errorMessage?.let { error ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = PoppinsFontFamily),
                            color = Color.Red,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                // Lista de impresoras disponibles
                if (!isSearching && availablePrinters.isNotEmpty()) {
                    Text(
                        text = "Impresoras disponibles:",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color.Black
                    )

                    LazyColumn(
                        modifier = Modifier.heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(availablePrinters) { printer ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (printer.isConnected) Color.White else Color.Gray.copy(alpha = 0.3f)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = printer.name,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontFamily = PoppinsFontFamily,
                                                fontWeight = FontWeight.SemiBold
                                            ),
                                            color = if (printer.isConnected) Color.Black else Color.Gray,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "Modelo: ${printer.model}",
                                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = PoppinsFontFamily),
                                            color = if (printer.isConnected) Color.DarkGray else Color.Gray
                                        )
                                        Text(
                                            text = "Dirección: ${printer.address}",
                                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = PoppinsFontFamily),
                                            color = if (printer.isConnected) Color.DarkGray else Color.Gray
                                        )
                                        Text(
                                            text = printer.getConnectionStatus(),
                                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = PoppinsFontFamily),
                                            color = if (printer.isConnected) RenovaColors.Primary else Color.Red
                                        )
                                    }
                                    Button(
                                        onClick = { onPrintSelected(printer) },
                                        enabled = printer.isConnected,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = RenovaColors.Primary,
                                            disabledContainerColor = Color.Gray
                                        )
                                    ) {
                                        Text(
                                            text = "Imprimir",
                                            color = Color.White,
                                            fontFamily = PoppinsFontFamily
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Botones de acción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onClose,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Gray
                        ),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(
                            text = "Cancelar",
                            color = Color.White,
                            fontFamily = PoppinsFontFamily
                        )
                    }
                }
            }
        }
    }
    
    // Diálogo de permisos
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = {
                Text(
                    text = "Permisos requeridos",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Para buscar impresoras Bluetooth, necesitamos acceso a dispositivos cercanos. ¿Deseas conceder los permisos?",
                    fontFamily = PoppinsFontFamily
                )
            },
            confirmButton = {
                TextButton(onClick = requestPermissions) {
                    Text(
                        text = "Conceder",
                        color = RenovaColors.Primary,
                        fontFamily = PoppinsFontFamily
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text(
                        text = "Cancelar",
                        color = Color.Gray,
                        fontFamily = PoppinsFontFamily
                    )
                }
            }
        )
    }
}