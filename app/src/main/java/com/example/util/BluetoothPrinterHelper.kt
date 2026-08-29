package com.example.util

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import com.example.data.model.BluetoothPrinterDevice
import com.example.data.model.OrderEntity
import com.example.data.model.StoreProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.text.NumberFormat
import java.util.Locale
import java.util.UUID

class BluetoothPrinterHelper(private val context: Context) {

    private val bluetoothManager: BluetoothManager? =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

    private var connectedSocket: BluetoothSocket? = null
    private var connectedDevice: BluetoothDevice? = null
    private var outputStream: OutputStream? = null

    private val sppUuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    val isBluetoothSupported: Boolean
        get() = bluetoothAdapter != null

    val isBluetoothEnabled: Boolean
        get() = bluetoothAdapter?.isEnabled == true

    val isConnected: Boolean
        get() = connectedSocket?.isConnected == true

    val currentConnectedAddress: String?
        get() = connectedDevice?.address

    @SuppressLint("MissingPermission")
    fun getPairedDevices(): List<BluetoothPrinterDevice> {
        val list = mutableListOf<BluetoothPrinterDevice>()
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) return list

        try {
            val paired = bluetoothAdapter.bondedDevices
            if (paired != null) {
                for (dev in paired) {
                    list.add(
                        BluetoothPrinterDevice(
                            name = dev.name ?: "Unknown Device",
                            address = dev.address,
                            isConnected = dev.address == connectedDevice?.address && isConnected
                        )
                    )
                }
            }
        } catch (e: Exception) {
            // Permission or hardware exception
        }
        return list
    }

    @SuppressLint("MissingPermission")
    suspend fun connectToDevice(address: String): Result<String> = withContext(Dispatchers.IO) {
        if (bluetoothAdapter == null) {
            return@withContext Result.failure(Exception("Bluetooth tidak didukung pada perangkat ini"))
        }

        try {
            disconnect()
            val device = bluetoothAdapter.getRemoteDevice(address)
            val socket = device.createRfcommSocketToServiceRecord(sppUuid)
            socket.connect()

            connectedSocket = socket
            connectedDevice = device
            outputStream = socket.outputStream

            Result.success("Terhubung ke ${device.name ?: address}")
        } catch (e: Exception) {
            disconnect()
            Result.failure(Exception("Gagal menghubungkan: ${e.localizedMessage ?: "Periksa apakah printer aktif dan dalam jangkauan."}"))
        }
    }

    fun disconnect() {
        try {
            outputStream?.close()
        } catch (e: Exception) {
            // ignore
        }
        try {
            connectedSocket?.close()
        } catch (e: Exception) {
            // ignore
        }
        connectedSocket = null
        connectedDevice = null
        outputStream = null
    }

    suspend fun printReceipt(order: OrderEntity, store: StoreProfile = StoreProfile()): Result<String> =
        withContext(Dispatchers.IO) {
            if (!isConnected || outputStream == null) {
                return@withContext Result.failure(Exception("Printer Bluetooth belum terhubung. Silakan pilih dan hubungkan printer."))
            }

            try {
                val os = outputStream!!
                val bytes = generateEscPosReceiptBytes(order, store)
                os.write(bytes)
                os.flush()
                Result.success("Struk berhasil dicetak!")
            } catch (e: Exception) {
                disconnect()
                Result.failure(Exception("Gagal mencetak: ${e.localizedMessage}"))
            }
        }

    suspend fun printTestPage(store: StoreProfile = StoreProfile()): Result<String> =
        withContext(Dispatchers.IO) {
            if (!isConnected || outputStream == null) {
                return@withContext Result.failure(Exception("Printer Bluetooth belum terhubung."))
            }
            try {
                val os = outputStream!!
                val bytes = generateTestReceiptBytes(store)
                os.write(bytes)
                os.flush()
                Result.success("Uji cetak berhasil!")
            } catch (e: Exception) {
                disconnect()
                Result.failure(Exception("Gagal uji cetak: ${e.localizedMessage}"))
            }
        }

    private fun generateEscPosReceiptBytes(order: OrderEntity, store: StoreProfile): ByteArray {
        val out = mutableListOf<Byte>()

        fun add(vararg b: Int) {
            for (byteVal in b) {
                out.add(byteVal.toByte())
            }
        }

        fun text(str: String) {
            val raw = str.toByteArray(charset("GBK"))
            for (b in raw) out.add(b)
        }

        fun line(str: String = "") {
            text(str)
            add(0x0A)
        }

        fun alignLeft() = add(0x1B, 0x61, 0x00)
        fun alignCenter() = add(0x1B, 0x61, 0x01)
        fun alignRight() = add(0x1B, 0x61, 0x02)
        fun boldOn() = add(0x1B, 0x45, 0x01)
        fun boldOff() = add(0x1B, 0x45, 0x00)
        fun doubleSize() = add(0x1D, 0x21, 0x11)
        fun normalSize() = add(0x1D, 0x21, 0x00)

        // Init printer
        add(0x1B, 0x40)

        // Header: Store info
        alignCenter()
        doubleSize()
        boldOn()
        line(store.name.uppercase())
        normalSize()
        boldOff()
        if (store.tagline.isNotBlank()) {
            line(store.tagline)
        }
        if (store.address.isNotBlank()) {
            val addrLines = store.address.split(",")
            if (addrLines.size > 1) {
                addrLines.forEach { l ->
                    val trimmed = l.trim()
                    if (trimmed.isNotBlank()) line(trimmed)
                }
            } else {
                line(store.address)
            }
        } else {
            line("Jl. Anggrek RT 004 / RW 013")
            line("Tegalrejo - Sawentar")
            line("Kanigoro - Blitar")
        }
        if (store.whatsapp.isNotBlank()) {
            line("WhatsApp: ${store.whatsapp}")
        }
        line("================================")

        // Order Info
        alignLeft()
        boldOn()
        line("NO NOTA : ${order.orderNumber}")
        boldOff()
        line("TGL     : ${formatDate(order.createdAt)}")
        line("PEMESAN : ${order.customerName}")
        boldOn()
        line("AMBIL   : ${order.pickupDate}")
        line("JAM     : ${order.pickupTime} WIB")
        boldOff()
        line("--------------------------------")

        // Items Header
        line("PRODUK                   TOTAL")
        line("--------------------------------")

        val items = order.getCartItems()
        val totalItemCount = items.sumOf { it.quantity }
        for (item in items) {
            line(item.productName)
            val qtyPrice = "${item.quantity}x ${formatRupiah(item.price)}"
            val subtotal = formatRupiah(item.subtotal)
            line(formatTwoColumns(qtyPrice, subtotal, 32))
        }

        line("--------------------------------")
        boldOn()
        line(formatTwoColumns("TOTAL ITEM :", "$totalItemCount Item", 32))
        boldOff()
        // Box & Packaging (Rata kiri tanpa label 'VARIAN:' agar tidak terpotong)
        val isCustomVariant = order.packagingVariant.isNotBlank() && 
                !order.packagingVariant.equals("Standard", ignoreCase = true) && 
                !order.packagingVariant.equals(order.packagingType, ignoreCase = true)
        
        if (isCustomVariant) {
            line("${order.packagingType} (${order.packagingVariant})")
        } else {
            line("Kemasan: ${order.packagingType}")
        }
        val boxPriceText = if (order.packagingPrice > 0) formatRupiah(order.packagingPrice) else "Gratis (Rp 0)"
        line(formatTwoColumns("  Biaya Kemasan:", boxPriceText, 32))

        line("================================")
        // Subtotal & Total
        line(formatTwoColumns("Subtotal Produk:", formatRupiah(order.subtotal), 32))
        if (order.packagingPrice > 0) {
            line(formatTwoColumns("Biaya Kemasan:", formatRupiah(order.packagingPrice), 32))
        }

        // Total on 32-column width with Bold & Double-Height (GS ! 0x01) so 7+ digit totals never wrap or truncate
        add(0x1D, 0x21, 0x01) // Double height only, normal width (32 cols preserved)
        boldOn()
        line(formatTwoColumns("TOTAL :", formatRupiah(order.total), 32))
        normalSize()
        boldOff()

        // Payment & Status Section (Status & Metode Bayar Rata Kanan Sejajar Nominal)
        line("--------------------------------")
        boldOn()
        line(formatTwoColumns("STATUS BAYAR :", order.paymentStatus, 32))
        boldOff()
        line(formatTwoColumns("METODE BAYAR :", order.paymentMethod, 32))

        if (order.initialDeposit > 0L) {
            line(formatTwoColumns("DP LAMA/AWAL :", formatRupiah(order.initialDeposit), 32))
            val settlement = if (order.settlementPaid > 0L) order.settlementPaid else (order.amountPaid - order.initialDeposit).coerceAtLeast(0L)
            if (order.paymentStatus == "LUNAS" || settlement > 0L) {
                line(formatTwoColumns("PELUNASAN    :", formatRupiah(settlement), 32))
            }
            line(formatTwoColumns("TOTAL BAYAR  :", formatRupiah(order.amountPaid), 32))
        } else {
            val paidLabel = if (order.paymentStatus == "DP") "DP DIBAYAR   :" else "DIBAYAR      :"
            line(formatTwoColumns(paidLabel, formatRupiah(order.amountPaid), 32))
        }

        if (order.paymentStatus == "LUNAS") {
            if (order.changeOrRemaining > 0) {
                line(formatTwoColumns("KEMBALIAN    :", formatRupiah(order.changeOrRemaining), 32))
            } else {
                line(formatTwoColumns("KEMBALIAN    :", "Rp 0 (Pas)", 32))
            }
        } else if (order.paymentStatus == "DP") {
            boldOn()
            line(formatTwoColumns("SISA KURANG  :", formatRupiah(order.changeOrRemaining), 32))
            boldOff()
        } else {
            boldOn()
            line(formatTwoColumns("SISA KURANG  :", formatRupiah(order.total), 32))
            boldOff()
        }

        if (store.showNotesOnReceipt && order.notes.isNotBlank()) {
            line("--------------------------------")
            alignLeft()
            line("Catatan: ${order.notes}")
        }

        line("================================")
        alignCenter()
        if (store.showReceiptGreeting && store.receiptGreeting.isNotBlank()) {
            store.receiptGreeting.split("\n").forEach { gLine ->
                val trimmed = gLine.trim()
                if (trimmed.isNotBlank()) line(trimmed)
            }
        }
        if (store.showSocialMedia && (store.instagram.isNotBlank() || store.tiktok.isNotBlank())) {
            val ig = if (store.instagram.startsWith("@")) store.instagram else "@${store.instagram}"
            line("Instagram / TikTok: $ig")
        }
        line("\n\n\n")

        // Cut paper if supported
        add(0x1D, 0x56, 0x42, 0x00)

        return out.toByteArray()
    }

    private fun generateTestReceiptBytes(store: StoreProfile): ByteArray {
        val out = mutableListOf<Byte>()
        fun add(vararg b: Int) = b.forEach { out.add(it.toByte()) }
        fun line(str: String) {
            str.toByteArray(charset("GBK")).forEach { out.add(it) }
            add(0x0A)
        }

        add(0x1B, 0x40) // init
        add(0x1B, 0x61, 0x01) // center
        add(0x1D, 0x21, 0x11) // double
        add(0x1B, 0x45, 0x01) // bold
        line(store.name)
        add(0x1D, 0x21, 0x00) // normal
        add(0x1B, 0x45, 0x00)
        line("TEST CETAK THERMAL BLUETOOTH")
        line("================================")
        add(0x1B, 0x61, 0x00) // left
        line("Koneksi printer Bluetooth OK!")
        line("Status: SIAP CETAK NOTA")
        line("================================")
        add(0x1B, 0x61, 0x01)
        line("Djandes Sweet & Savoury")
        line("\n\n\n")
        add(0x1D, 0x56, 0x42, 0x00)
        return out.toByteArray()
    }

    private fun formatTwoColumns(left: String, right: String, totalWidth: Int = 32): String {
        val spaces = totalWidth - left.length - right.length
        return if (spaces > 0) {
            left + " ".repeat(spaces) + right
        } else {
            "$left $right"
        }
    }

    private fun formatRupiah(amount: Long): String {
        val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        format.maximumFractionDigits = 0
        return format.format(amount).replace("Rp", "Rp ")
    }

    private fun formatDate(timestamp: Long): String {
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("in", "ID"))
        return sdf.format(java.util.Date(timestamp))
    }
}
