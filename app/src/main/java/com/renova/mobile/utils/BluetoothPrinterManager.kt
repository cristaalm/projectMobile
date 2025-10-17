package com.renova.mobile.utils

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import com.renova.mobile.utils.PrinterModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.UUID

class BluetoothPrinterManager(private val context: Context) {
    
    private val bluetoothManager: BluetoothManager? = 
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter
    
    // UUID estándar para impresoras Bluetooth (Serial Port Profile)
    private val printerUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    
    // Mapa para mantener conexiones activas
    private val activeConnections = mutableMapOf<String, BluetoothSocket>()
    
    /**
     * Detecta impresoras RT-320PB/RT-330PB conectadas por Bluetooth
     */
    suspend fun detectRTPrinters(): List<PrinterModel> = withContext(Dispatchers.IO) {
        val printers = mutableListOf<PrinterModel>()
        
        if (!isBluetoothAvailable()) {
            return@withContext printers
        }
        
        if (!hasBluetoothPermissions()) {
            return@withContext printers
        }
        
        try {
            // Obtener dispositivos Bluetooth emparejados
            val pairedDevices = bluetoothAdapter?.bondedDevices ?: emptySet()
            
            pairedDevices.forEach { device ->
                if (isRTPrinter(device)) {
                    val printer = createPrinterModel(device)
                    printers.add(printer)
                }
            }
            
        } catch (e: SecurityException) {
            // Manejar excepción de permisos
            println("Error de permisos Bluetooth: ${e.message}")
        } catch (e: Exception) {
            println("Error detectando impresoras: ${e.message}")
        }
        
        return@withContext printers
    }
    
    /**
     * Verifica si el dispositivo es una impresora RT compatible
     * Específicamente busca RT-3300PB-3F9F y modelos similares
     */
    private fun isRTPrinter(device: BluetoothDevice): Boolean {
        try {
            val deviceName = device.name?.uppercase() ?: ""
            val deviceAddress = device.address ?: ""
            
            // Prioridad 1: Buscar específicamente la dirección que termina en 3F:9F
            val hasTargetAddress = deviceAddress.contains("3F:9F", ignoreCase = true) ||
                                 deviceAddress.endsWith(":3F:9F", ignoreCase = true) ||
                                 deviceAddress.contains("3F9F", ignoreCase = true)
            
            // Prioridad 2: Buscar modelos RT específicos (incluyendo RT-3300PB)
            val isTargetRTModel = deviceName.contains("RT-320") || 
                                deviceName.contains("RT-330") ||
                                deviceName.contains("RT-3300") ||
                                deviceName.contains("RT320") ||
                                deviceName.contains("RT330") ||
                                deviceName.contains("RT3300")
            
            // Prioridad 3: Buscar nombres que contengan "RT" y números de modelo
            val isRTPattern = deviceName.contains("RT") && 
                            (deviceName.contains("320") || 
                             deviceName.contains("330") || 
                             deviceName.contains("3300"))
            
            // Prioridad 4: Buscar por clase de dispositivo de impresora
            val deviceClass = device.bluetoothClass?.majorDeviceClass
            val isPrinterClass = deviceClass == 1536 // Clase de impresora
            val isRTPrinterByClass = isPrinterClass && deviceName.contains("RT")
            
            // Aceptar dispositivos que cumplan con cualquiera de los criterios
            return hasTargetAddress || isTargetRTModel || isRTPattern || isRTPrinterByClass
            
        } catch (e: SecurityException) {
            return false
        }
    }
    
    /**
     * Crea un modelo de impresora desde un dispositivo Bluetooth
     */
    private fun createPrinterModel(device: BluetoothDevice): PrinterModel {
        return try {
            val deviceName = device.name ?: "Impresora RT"
            val deviceAddress = device.address ?: ""
            val isConnected = device.bondState == BluetoothDevice.BOND_BONDED
            
            // Determinar el modelo basado en el nombre del dispositivo
            val model = when {
                deviceName.contains("RT-3300", ignoreCase = true) -> "RT-3300PB"
                deviceName.contains("RT-330", ignoreCase = true) -> "RT-330PB"
                deviceName.contains("RT-320", ignoreCase = true) -> "RT-320PB"
                deviceAddress.contains("3F:9F", ignoreCase = true) -> "RT-3300PB"
                else -> "RT-3300PB"
            }
            
            PrinterModel(
                name = "$deviceName ($deviceAddress)",
                address = deviceAddress,
                model = model,
                isConnected = isConnected,
                isCompatible = true
            )
        } catch (e: SecurityException) {
            PrinterModel(
                name = "Impresora RT (Sin permisos)",
                address = "unknown",
                model = "RT-320PB",
                isConnected = false,
                isCompatible = true
            )
        }
    }
    
    /**
     * Verifica si Bluetooth está disponible y habilitado
     */
    fun isBluetoothAvailable(): Boolean {
        return bluetoothAdapter?.isEnabled == true
    }
    
    /**
     * Verifica si se tienen los permisos necesarios de Bluetooth
     */
    fun hasBluetoothPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+ requiere permisos específicos
            ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == 
                PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == 
                PackageManager.PERMISSION_GRANTED
        } else {
            // Android 11 y anteriores
            ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) == 
                PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADMIN) == 
                PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * Obtiene los permisos necesarios según la versión de Android
     */
    fun getRequiredPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }
    
    /**
     * Verifica si un dispositivo específico está conectado
     */
    suspend fun isPrinterConnected(deviceAddress: String): Boolean = withContext(Dispatchers.IO) {
        try {
            if (!hasBluetoothPermissions()) return@withContext false
            
            // Verificar si hay una conexión activa
            val activeSocket = activeConnections[deviceAddress]
            if (activeSocket?.isConnected == true) {
                return@withContext true
            }
            
            val device = bluetoothAdapter?.getRemoteDevice(deviceAddress)
            return@withContext device?.bondState == BluetoothDevice.BOND_BONDED
        } catch (e: Exception) {
            return@withContext false
        }
    }
    
    /**
     * Verifica si un dispositivo está disponible sin mantener la conexión abierta
     */
    suspend fun isDeviceReachable(deviceAddress: String): Boolean = withContext(Dispatchers.IO) {
        try {
            if (!hasBluetoothPermissions()) {
                android.util.Log.e("BluetoothPrinter", "No hay permisos de Bluetooth para verificar dispositivo")
                return@withContext false
            }
            
            val device = bluetoothAdapter?.getRemoteDevice(deviceAddress) ?: run {
                android.util.Log.e("BluetoothPrinter", "No se pudo obtener el dispositivo $deviceAddress")
                return@withContext false
            }
            
            android.util.Log.d("BluetoothPrinter", "Verificando disponibilidad de ${device.name} ($deviceAddress)")
            
            // Crear socket Bluetooth temporal
            val socket = device.createRfcommSocketToServiceRecord(printerUUID)
            
            try {
                // Cancelar descubrimiento para mejorar la conexión
                bluetoothAdapter.cancelDiscovery()
                
                // Intentar conectar con timeout corto
                socket.connect()
                
                // Verificar que la conexión esté establecida
                val isReachable = socket.isConnected
                
                // Cerrar inmediatamente la conexión temporal
                socket.close()
                
                android.util.Log.d("BluetoothPrinter", "Dispositivo $deviceAddress ${if (isReachable) "disponible" else "no disponible"}")
                return@withContext isReachable
                
            } catch (e: IOException) {
                android.util.Log.e("BluetoothPrinter", "Error verificando disponibilidad: ${e.message}")
                socket.close()
                return@withContext false
            }
            
        } catch (e: SecurityException) {
            android.util.Log.e("BluetoothPrinter", "Error de seguridad verificando disponibilidad: ${e.message}")
            return@withContext false
        } catch (e: Exception) {
            android.util.Log.e("BluetoothPrinter", "Error general verificando disponibilidad: ${e.message}")
            return@withContext false
        }
    }

    /**
     * Conecta a una impresora específica
     */
    suspend fun connectToPrinter(deviceAddress: String): Boolean = withContext(Dispatchers.IO) {
        try {
            if (!hasBluetoothPermissions()) {
                android.util.Log.e("BluetoothPrinter", "No hay permisos de Bluetooth")
                return@withContext false
            }
            
            // Si ya está conectado, retornar true
            if (activeConnections[deviceAddress]?.isConnected == true) {
                android.util.Log.d("BluetoothPrinter", "Ya conectado a $deviceAddress")
                return@withContext true
            }
            
            val device = bluetoothAdapter?.getRemoteDevice(deviceAddress) ?: run {
                android.util.Log.e("BluetoothPrinter", "No se pudo obtener el dispositivo $deviceAddress")
                return@withContext false
            }
            
            android.util.Log.d("BluetoothPrinter", "Intentando conectar a ${device.name} ($deviceAddress)")
            
            // Cerrar conexión previa si existe
            activeConnections[deviceAddress]?.close()
            activeConnections.remove(deviceAddress)
            
            // Crear socket Bluetooth
            val socket = device.createRfcommSocketToServiceRecord(printerUUID)
            
            try {
                // Cancelar descubrimiento para mejorar la conexión
                bluetoothAdapter.cancelDiscovery()
                
                // Conectar al dispositivo con timeout
                socket.connect()
                
                // Verificar que la conexión esté realmente establecida
                if (socket.isConnected) {
                    activeConnections[deviceAddress] = socket
                    android.util.Log.d("BluetoothPrinter", "Conectado exitosamente a $deviceAddress")
                    return@withContext true
                } else {
                    socket.close()
                    android.util.Log.e("BluetoothPrinter", "Socket no está conectado después de connect()")
                    return@withContext false
                }
                
            } catch (e: IOException) {
                android.util.Log.e("BluetoothPrinter", "Error de IO al conectar: ${e.message}")
                socket.close()
                return@withContext false
            }
            
        } catch (e: SecurityException) {
            android.util.Log.e("BluetoothPrinter", "Error de seguridad: ${e.message}")
            return@withContext false
        } catch (e: Exception) {
            android.util.Log.e("BluetoothPrinter", "Error general al conectar: ${e.message}")
            return@withContext false
        }
    }
    
    /**
     * Desconecta de una impresora específica
     */
    suspend fun disconnectFromPrinter(deviceAddress: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val socket = activeConnections[deviceAddress]
            if (socket != null) {
                socket.close()
                activeConnections.remove(deviceAddress)
                return@withContext true
            }
            return@withContext false
        } catch (e: Exception) {
            return@withContext false
        }
    }
    
    /**
     * Envía datos a una impresora conectada
     */
    suspend fun sendDataToPrinter(deviceAddress: String, data: ByteArray): Boolean = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d("BluetoothPrinter", "Enviando ${data.size} bytes a $deviceAddress")
            
            val socket = activeConnections[deviceAddress]
            if (socket?.isConnected != true) {
                android.util.Log.w("BluetoothPrinter", "No hay conexión activa, intentando conectar...")
                // Intentar conectar si no está conectado
                if (!connectToPrinter(deviceAddress)) {
                    android.util.Log.e("BluetoothPrinter", "No se pudo conectar para enviar datos")
                    return@withContext false
                }
            }
            
            val outputStream = activeConnections[deviceAddress]?.outputStream
            if (outputStream == null) {
                android.util.Log.e("BluetoothPrinter", "No se pudo obtener el OutputStream")
                return@withContext false
            }
            
            outputStream.write(data)
            outputStream.flush()
            
            android.util.Log.d("BluetoothPrinter", "Datos enviados exitosamente")
            return@withContext true
        } catch (e: IOException) {
            android.util.Log.e("BluetoothPrinter", "Error de IO al enviar datos: ${e.message}")
            // Si hay error, cerrar la conexión
            disconnectFromPrinter(deviceAddress)
            return@withContext false
        } catch (e: Exception) {
            android.util.Log.e("BluetoothPrinter", "Error general al enviar datos: ${e.message}")
            return@withContext false
        }
    }
    
    /**
     * Desconecta todas las impresoras
     */
    suspend fun disconnectAll() = withContext(Dispatchers.IO) {
        activeConnections.values.forEach { socket ->
            try {
                socket.close()
            } catch (e: Exception) {
                // Ignorar errores al cerrar
            }
        }
        activeConnections.clear()
    }
}