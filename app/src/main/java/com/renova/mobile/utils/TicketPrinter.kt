package com.renova.mobile.utils

import android.content.Context
import android.widget.Toast
import com.renova.mobile.ui.components.SaleSummary
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import java.nio.charset.Charset

object TicketPrinter {
    private const val TICKET_WIDTH = 32 // Ancho típico de impresora térmica
    
    // Comandos ESC/POS para impresoras térmicas
    private val ESC = 0x1B.toByte()
    private val GS = 0x1D.toByte()
    private val LF = 0x0A.toByte()
    private val CR = 0x0D.toByte()
    
    // Comandos de formato
    private val INIT_PRINTER = byteArrayOf(ESC, '@'.code.toByte()) // Inicializar impresora
    private val CENTER_ALIGN = byteArrayOf(ESC, 'a'.code.toByte(), 1) // Centrar texto
    private val LEFT_ALIGN = byteArrayOf(ESC, 'a'.code.toByte(), 0) // Alinear izquierda
    private val BOLD_ON = byteArrayOf(ESC, 'E'.code.toByte(), 1) // Negrita ON
    private val BOLD_OFF = byteArrayOf(ESC, 'E'.code.toByte(), 0) // Negrita OFF
    private val CUT_PAPER = byteArrayOf(GS, 'V'.code.toByte(), 66, 0) // Cortar papel
    private val FEED_LINES = byteArrayOf(LF, LF, LF) // Avanzar líneas

    /**
     * Imprime un ticket de venta en la impresora seleccionada
     */
    suspend fun printTicket(summary: SaleSummary, printer: PrinterModel, context: Context) {
        try {
            android.util.Log.d("TicketPrinter", "Iniciando impresión con ${printer.model} (${printer.address})")
            
            val bluetoothManager = BluetoothPrinterManager(context)
            
            // Verificar conexión antes de imprimir
            val isConnected = bluetoothManager.connectToPrinter(printer.address)
            if (!isConnected) {
                android.util.Log.e("TicketPrinter", "No se pudo conectar a la impresora")
                throw Exception("No se pudo conectar a la impresora ${printer.name}")
            }
            
            when {
                printer.model.startsWith("RT-") -> {
                    android.util.Log.d("TicketPrinter", "Usando método RT para ${printer.model}")
                    printToRTPrinter(summary, printer, bluetoothManager)
                }
                printer.isCompatible -> {
                    android.util.Log.d("TicketPrinter", "Usando método compatible para ${printer.model}")
                    printToCompatiblePrinter(summary, printer, bluetoothManager)
                }
                else -> {
                    android.util.Log.d("TicketPrinter", "Usando método genérico para ${printer.model}")
                    printGeneric(summary, printer, bluetoothManager)
                }
            }
            
            android.util.Log.d("TicketPrinter", "Impresión completada exitosamente")
            
        } catch (e: Exception) {
            android.util.Log.e("TicketPrinter", "Error en impresión: ${e.message}")
            throw e
        }
    }

    /**
     * Genera el contenido del ticket con formato de voucher
     */
    private fun generateTicketContent(summary: SaleSummary): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        val currentDate = dateFormat.format(Date())
        
        return buildString {
            // Encabezado Renova
            appendLine(centerText("RENOVA"))
            appendLine(centerText("Sistema de Recompensas"))
            appendLine(centerText("================================"))
            appendLine()
            
            // Información de la transacción
            appendLine("COMPROBANTE DE CANJE")
            appendLine("Fecha: $currentDate")
            appendLine("ID Transacción: ${summary.id}")
            appendLine()
            
            // Información del consumidor
            appendLine("CONSUMIDOR:")
            appendLine("Nombre: ${summary.consumerName ?: "N/A"}")
            appendLine()
            
            // Información del comercio
            appendLine("COMERCIO:")
            appendLine("${summary.allianceName ?: "Comercio Afiliado"}")
            appendLine()
            
            // Recompensas canjeadas
            appendLine("RECOMPENSAS CANJEADAS:")
            appendLine("--------------------------------")
            summary.items.forEach { item ->
                appendLine("${item.name}")
                appendLine("  Cantidad: ${item.quantity}")
                appendLine("  Puntos c/u: ${item.pointsRequired}")
                appendLine("  Subtotal: ${item.quantity * item.pointsRequired} pts")
                appendLine()
            }
            
            appendLine("--------------------------------")
            appendLine("TOTAL PUNTOS: ${summary.totalPoints}")
            appendLine("================================")
            appendLine()
            
            // Pie de página
            appendLine(centerText("¡Gracias por usar RENOVA!"))
            appendLine(centerText("Sigue acumulando puntos"))
            appendLine()
            appendLine(centerText("www.renova.com"))
            appendLine()
        }
    }

    /**
     * Centra el texto en el ancho del ticket
     */
    private fun centerText(text: String): String {
        val padding = (TICKET_WIDTH - text.length) / 2
        return if (padding > 0) {
            " ".repeat(padding) + text
        } else {
            text
        }
    }

    /**
     * Imprime en impresoras RT (RT-320PB/RT-330PB/RT-3300PB)
     */
    private suspend fun printToRTPrinter(summary: SaleSummary, printer: PrinterModel, bluetoothManager: BluetoothPrinterManager) {
        android.util.Log.d("TicketPrinter", "Generando ticket ESC/POS para RT")
        val ticketData = generateESCPOSTicket(summary)
        android.util.Log.d("TicketPrinter", "Ticket generado: ${ticketData.size} bytes")
        
        val success = bluetoothManager.sendDataToPrinter(printer.address, ticketData)
        if (!success) {
            android.util.Log.e("TicketPrinter", "Falló el envío de datos a impresora RT")
            throw Exception("Error enviando datos a la impresora RT")
        }
        
        android.util.Log.d("TicketPrinter", "Esperando procesamiento de impresora RT...")
        // Tiempo para que la impresora procese
        delay(3000)
    }

    /**
     * Imprime en impresoras compatibles
     */
    private suspend fun printToCompatiblePrinter(summary: SaleSummary, printer: PrinterModel, bluetoothManager: BluetoothPrinterManager) {
        val ticketData = generateESCPOSTicket(summary)
        
        val success = bluetoothManager.sendDataToPrinter(printer.address, ticketData)
        if (!success) {
            throw Exception("Error enviando datos a la impresora compatible")
        }
        
        delay(2500)
    }

    /**
     * Impresión genérica
     */
    private suspend fun printGeneric(summary: SaleSummary, printer: PrinterModel, bluetoothManager: BluetoothPrinterManager) {
        val ticketData = generateESCPOSTicket(summary)
        
        val success = bluetoothManager.sendDataToPrinter(printer.address, ticketData)
        if (!success) {
            throw Exception("Error enviando datos a la impresora")
        }
        
        delay(2000)
    }
    
    /**
     * Genera el ticket en formato ESC/POS para impresoras térmicas
     */
    private fun generateESCPOSTicket(summary: SaleSummary): ByteArray {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        val currentDate = dateFormat.format(Date())
        
        return buildList<Byte> {
            // Inicializar impresora
            addAll(INIT_PRINTER.toList())
            
            // Encabezado centrado y en negrita
            addAll(CENTER_ALIGN.toList())
            addAll(BOLD_ON.toList())
            addAll("RENOVA\n".toByteArray(Charset.forName("UTF-8")).toList())
            addAll("Sistema de Recompensas\n".toByteArray(Charset.forName("UTF-8")).toList())
            addAll(BOLD_OFF.toList())
            addAll("================================\n".toByteArray(Charset.forName("UTF-8")).toList())
            add(LF)
            
            // Contenido alineado a la izquierda
            addAll(LEFT_ALIGN.toList())
            addAll(BOLD_ON.toList())
            addAll("COMPROBANTE DE CANJE\n".toByteArray(Charset.forName("UTF-8")).toList())
            addAll(BOLD_OFF.toList())
            addAll("Fecha: $currentDate\n".toByteArray(Charset.forName("UTF-8")).toList())
            addAll("ID: ${summary.id}\n".toByteArray(Charset.forName("UTF-8")).toList())
            add(LF)
            
            // Información del consumidor
            addAll("CONSUMIDOR:\n".toByteArray(Charset.forName("UTF-8")).toList())
            addAll("${summary.consumerName ?: "N/A"}\n".toByteArray(Charset.forName("UTF-8")).toList())
            add(LF)
            
            // Información del comercio
            addAll("COMERCIO:\n".toByteArray(Charset.forName("UTF-8")).toList())
            addAll("${summary.allianceName ?: "Comercio Afiliado"}\n".toByteArray(Charset.forName("UTF-8")).toList())
            add(LF)
            
            // Recompensas
            addAll("RECOMPENSAS CANJEADAS:\n".toByteArray(Charset.forName("UTF-8")).toList())
            addAll("--------------------------------\n".toByteArray(Charset.forName("UTF-8")).toList())
            
            summary.items.forEach { item ->
                addAll("${item.name}\n".toByteArray(Charset.forName("UTF-8")).toList())
                addAll("  Cant: ${item.quantity} x ${item.pointsRequired} pts\n".toByteArray(Charset.forName("UTF-8")).toList())
                addAll("  Subtotal: ${item.quantity * item.pointsRequired} pts\n".toByteArray(Charset.forName("UTF-8")).toList())
                add(LF)
            }
            
            addAll("--------------------------------\n".toByteArray(Charset.forName("UTF-8")).toList())
            addAll(BOLD_ON.toList())
            addAll("TOTAL PUNTOS: ${summary.totalPoints}\n".toByteArray(Charset.forName("UTF-8")).toList())
            addAll(BOLD_OFF.toList())
            addAll("================================\n".toByteArray(Charset.forName("UTF-8")).toList())
            add(LF)
            
            // Pie de página centrado
            addAll(CENTER_ALIGN.toList())
            addAll("¡Gracias por usar RENOVA!\n".toByteArray(Charset.forName("UTF-8")).toList())
            addAll("Sigue acumulando puntos\n".toByteArray(Charset.forName("UTF-8")).toList())
            add(LF)
            addAll("www.renova.com\n".toByteArray(Charset.forName("UTF-8")).toList())
            
            // Avanzar papel y cortar
            addAll(FEED_LINES.toList())
            addAll(CUT_PAPER.toList())
            
        }.toByteArray()
    }
}