package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.model.OrderEntity
import com.example.data.model.StoreProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

object ExportHelper {

    private fun buildCsvString(
        dateLabel: String,
        orders: List<OrderEntity>,
        totalRevenue: Long,
        store: StoreProfile
    ): String {
        val totalActualCash = orders.sumOf { it.getActualCashReceived() }
        val totalOutstanding = orders.sumOf { it.getOutstandingRemaining() }

        val sb = StringBuilder()
        sb.append("LAPORAN PENJUALAN - ${store.name.uppercase()}\n")
        sb.append("Tanggal / Periode:;\"$dateLabel\"\n")
        sb.append("Total Transaksi:;${orders.size}\n")
        sb.append("Total Kas Masuk (Riil Diterima):;\"${formatRupiah(totalActualCash)}\"\n")
        sb.append("Total Sisa Belum Lunas (Piutang):;\"${formatRupiah(totalOutstanding)}\"\n")
        sb.append("Total Nilai Pesanan (Omset):;\"${formatRupiah(totalRevenue)}\"\n\n")

        // Table headers
        sb.append("No;No Nota;Tgl Transaksi;Nama Pelanggan;Jadwal Ambil;Jam Ambil;Tipe Box;Varian Box;Item Produk;Subtotal;Biaya Box;Total Akhir;Metode Bayar;Status Bayar;DP Awal;Pelunasan;Total Terbayar;Sisa Kurang;Kembalian;Catatan\n")

        orders.forEachIndexed { index, order ->
            val itemsSummary = order.getCartItems().joinToString(" | ") {
                "${it.productName} (${it.quantity}x @ ${it.price})"
            }.replace("\"", "\"\"")

            val settlement = if (order.settlementPaid > 0L) order.settlementPaid else (order.amountPaid - order.initialDeposit).coerceAtLeast(0L)
            val sisaKurang = if (order.paymentStatus == "DP") order.changeOrRemaining else if (order.paymentStatus != "LUNAS") order.total else 0L
            val kembalian = if (order.paymentStatus == "LUNAS") order.changeOrRemaining else 0L

            sb.append("${index + 1};")
            sb.append("\"${order.orderNumber}\";")
            sb.append("\"${formatFullDate(order.createdAt)}\";")
            sb.append("\"${order.customerName}\";")
            sb.append("\"${order.pickupDate}\";")
            sb.append("\"${order.pickupTime}\";")
            sb.append("\"${order.packagingType}\";")
            sb.append("\"${order.packagingVariant}\";")
            sb.append("\"$itemsSummary\";")
            sb.append("${order.subtotal};")
            sb.append("${order.packagingPrice};")
            sb.append("${order.total};")
            sb.append("\"${order.paymentMethod}\";")
            sb.append("\"${order.paymentStatus}\";")
            sb.append("${order.initialDeposit};")
            sb.append("${settlement};")
            sb.append("${order.amountPaid};")
            sb.append("${sisaKurang};")
            sb.append("${kembalian};")
            sb.append("\"${order.notes.replace("\"", "\"\"")}\"\n")
        }
        return sb.toString()
    }

    suspend fun saveDailyReportToCsv(
        context: Context,
        dateLabel: String,
        orders: List<OrderEntity>,
        totalRevenue: Long,
        store: StoreProfile = StoreProfile()
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val cleanDate = dateLabel.replace(" ", "_").replace("/", "-")
            val fileName = "Laporan_Penjualan_${cleanDate}.csv"
            val csvData = buildCsvString(dateLabel, orders, totalRevenue, store)

            AppStorageHelper.saveToDownloads(
                context = context,
                subFolder = AppStorageHelper.SUBFOLDER_CSV,
                fileName = fileName,
                mimeType = "text/csv"
            ) { os ->
                // UTF-8 BOM for Excel
                os.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
                os.write(csvData.toByteArray(Charsets.UTF_8))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun shareDailyReportCsv(
        context: Context,
        dateLabel: String,
        orders: List<OrderEntity>,
        totalRevenue: Long,
        store: StoreProfile = StoreProfile()
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val reportsDir = File(context.cacheDir, "reports")
            if (!reportsDir.exists()) reportsDir.mkdirs()

            val cleanDate = dateLabel.replace(" ", "_").replace("/", "-")
            val file = File(reportsDir, "Laporan_Penjualan_${cleanDate}.csv")
            val fos = FileOutputStream(file)

            // UTF-8 BOM for Excel
            fos.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
            val csvData = buildCsvString(dateLabel, orders, totalRevenue, store)
            fos.write(csvData.toByteArray(Charsets.UTF_8))
            fos.flush()
            fos.close()

            AppStorageHelper.shareFile(context, file, "text/csv", "Laporan Penjualan Excel ($dateLabel)")
            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun exportDailyReportToCsv(
        context: Context,
        dateLabel: String,
        orders: List<OrderEntity>,
        totalRevenue: Long,
        store: StoreProfile = StoreProfile()
    ): Result<File> = shareDailyReportCsv(context, dateLabel, orders, totalRevenue, store)

    private fun generatePdfDocument(
        dateLabel: String,
        orders: List<OrderEntity>,
        totalRevenue: Long,
        store: StoreProfile
    ): PdfDocument {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 standard
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        // Header Background
        val headerPaint = Paint().apply {
            color = Color.parseColor("#801B34")
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, 595f, 90f, headerPaint)

        // Header Text
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("${store.name.uppercase()} - LAPORAN PENJUALAN", 30f, 40f, titlePaint)

        val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FFE0A4")
            textSize = 13f
        }
        canvas.drawText("Tanggal: $dateLabel  |  Dicetak: ${formatFullDate(System.currentTimeMillis())}", 30f, 65f, subtitlePaint)

        val totalActualCash = orders.sumOf { it.getActualCashReceived() }
        val totalOutstanding = orders.sumOf { it.getOutstandingRemaining() }
        val totalItems = orders.sumOf { order -> order.getCartItems().sumOf { it.quantity } }

        // Summary Card
        val summaryCard = Paint().apply {
            color = Color.parseColor("#F5ECE9")
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(RectF(30f, 100f, 565f, 185f), 8f, 8f, summaryCard)

        val summaryLabel = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#705D61")
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val summaryGreen = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#2E7D32")
            textSize = 13.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val summaryRed = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#C62828")
            textSize = 13.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val summaryValue = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#801B34")
            textSize = 13.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        // Row 1: Kas Masuk & Piutang
        canvas.drawText("KAS MASUK (RIIL DITERIMA)", 45f, 122f, summaryLabel)
        canvas.drawText(formatRupiah(totalActualCash), 45f, 140f, summaryGreen)

        canvas.drawText("PIUTANG / BELUM LUNAS", 305f, 122f, summaryLabel)
        canvas.drawText(formatRupiah(totalOutstanding), 305f, 140f, summaryRed)

        // Row 2: Total Nilai Pesanan & Total Transaksi
        canvas.drawText("NILAI PESANAN (OMSET)", 45f, 160f, summaryLabel)
        canvas.drawText(formatRupiah(totalRevenue), 45f, 176f, summaryValue)

        canvas.drawText("TOTAL TRANSAKSI & ITEM", 305f, 160f, summaryLabel)
        canvas.drawText("${orders.size} Nota  ($totalItems pcs kue)", 305f, 176f, summaryValue)

        // Table Header
        var currentY = 210f
        val tableHeaderPaint = Paint().apply {
            color = Color.parseColor("#801B34")
            style = Paint.Style.FILL
        }
        canvas.drawRect(30f, currentY - 15f, 565f, currentY + 10f, tableHeaderPaint)

        val thText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("NO", 35f, currentY, thText)
        canvas.drawText("NOTA / PEMESAN", 65f, currentY, thText)
        canvas.drawText("JADWAL AMBIL", 210f, currentY, thText)
        canvas.drawText("BOX & VARIAN", 330f, currentY, thText)
        canvas.drawText("TOTAL", 510f, currentY, thText)

        currentY += 25f

        val rowText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#26191B")
            textSize = 9.5f
        }
        val rowBold = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#26191B")
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val linePaint = Paint().apply {
            color = Color.parseColor("#E0D6D3")
            strokeWidth = 1f
        }

        orders.forEachIndexed { i, order ->
            if (currentY > 780f) {
                // prevent overflowing single page in basic PDF
                return@forEachIndexed
            }
            canvas.drawText("${i + 1}", 35f, currentY, rowText)
            canvas.drawText("${order.orderNumber} - ${order.customerName}", 65f, currentY, rowBold)
            canvas.drawText("${order.pickupDate} (${order.pickupTime})", 210f, currentY, rowText)
            canvas.drawText("${order.packagingType} - ${order.packagingVariant}", 330f, currentY, rowText)
            canvas.drawText(formatRupiah(order.total), 505f, currentY, rowBold)

            currentY += 15f
            // Products in order
            val itemsStr = order.getCartItems().joinToString(", ") { "${it.productName} (${it.quantity}x)" }
            val smallItemText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#705D61")
                textSize = 8.5f
            }
            val shortened = if (itemsStr.length > 80) itemsStr.take(77) + "..." else itemsStr
            canvas.drawText("   Item: $shortened", 65f, currentY, smallItemText)

            currentY += 12f
            canvas.drawLine(30f, currentY, 565f, currentY, linePaint)
            currentY += 16f
        }

        // Footer
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#8C7B7E")
            textSize = 9f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Laporan ini dibuat otomatis oleh Sistem POS & Katalog Djandes", 595f / 2f, 820f, footerPaint)

        pdfDocument.finishPage(page)
        return pdfDocument
    }

    suspend fun saveDailyReportToPdf(
        context: Context,
        dateLabel: String,
        orders: List<OrderEntity>,
        totalRevenue: Long,
        store: StoreProfile = StoreProfile()
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val cleanDate = dateLabel.replace(" ", "_").replace("/", "-")
            val fileName = "Laporan_Penjualan_${cleanDate}.pdf"
            val pdfDoc = generatePdfDocument(dateLabel, orders, totalRevenue, store)

            AppStorageHelper.saveToDownloads(
                context = context,
                subFolder = AppStorageHelper.SUBFOLDER_PDF,
                fileName = fileName,
                mimeType = "application/pdf"
            ) { os ->
                pdfDoc.writeTo(os)
                pdfDoc.close()
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun shareDailyReportPdf(
        context: Context,
        dateLabel: String,
        orders: List<OrderEntity>,
        totalRevenue: Long,
        store: StoreProfile = StoreProfile()
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val reportsDir = File(context.cacheDir, "reports")
            if (!reportsDir.exists()) reportsDir.mkdirs()

            val cleanDate = dateLabel.replace(" ", "_").replace("/", "-")
            val file = File(reportsDir, "Laporan_Penjualan_${cleanDate}.pdf")

            val pdfDoc = generatePdfDocument(dateLabel, orders, totalRevenue, store)
            val fos = FileOutputStream(file)
            pdfDoc.writeTo(fos)
            fos.flush()
            fos.close()
            pdfDoc.close()

            AppStorageHelper.shareFile(context, file, "application/pdf", "Laporan Penjualan PDF ($dateLabel)")
            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun exportDailyReportToPdf(
        context: Context,
        dateLabel: String,
        orders: List<OrderEntity>,
        totalRevenue: Long,
        store: StoreProfile = StoreProfile()
    ): Result<File> = shareDailyReportPdf(context, dateLabel, orders, totalRevenue, store)

    fun generateOrdersCsvText(
        orders: List<OrderEntity>,
        store: StoreProfile = StoreProfile()
    ): String {
        val sb = StringBuilder()
        sb.append("LAPORAN PENJUALAN NOTA - ${store.name.uppercase()}\n")
        sb.append("No;No Nota;Tgl Transaksi;Nama Pelanggan;Jadwal Ambil;Jam Ambil;Tipe Box;Varian Box;Item Produk;Subtotal;Biaya Box;Total Akhir;Metode Bayar;Status Bayar;DP Awal;Pelunasan;Total Terbayar;Sisa Kurang;Kembalian;Catatan;ItemsJson\n")

        orders.forEachIndexed { index, order ->
            val itemsSummary = order.getCartItems().joinToString(" | ") {
                "${it.productName} (${it.quantity}x @ ${it.price})"
            }.replace("\"", "\"\"")

            val settlement = if (order.settlementPaid > 0L) order.settlementPaid else (order.amountPaid - order.initialDeposit).coerceAtLeast(0L)
            val sisaKurang = if (order.paymentStatus == "DP") order.changeOrRemaining else if (order.paymentStatus != "LUNAS") order.total else 0L
            val kembalian = if (order.paymentStatus == "LUNAS") order.changeOrRemaining else 0L

            sb.append("${index + 1};")
            sb.append("\"${order.orderNumber}\";")
            sb.append("\"${formatFullDate(order.createdAt)}\";")
            sb.append("\"${order.customerName}\";")
            sb.append("\"${order.pickupDate}\";")
            sb.append("\"${order.pickupTime}\";")
            sb.append("\"${order.packagingType}\";")
            sb.append("\"${order.packagingVariant}\";")
            sb.append("\"$itemsSummary\";")
            sb.append("${order.subtotal};")
            sb.append("${order.packagingPrice};")
            sb.append("${order.total};")
            sb.append("\"${order.paymentMethod}\";")
            sb.append("\"${order.paymentStatus}\";")
            sb.append("${order.initialDeposit};")
            sb.append("${settlement};")
            sb.append("${order.amountPaid};")
            sb.append("${sisaKurang};")
            sb.append("${kembalian};")
            sb.append("\"${order.notes.replace("\"", "\"\"")}\";")
            sb.append("\"${order.itemsJson.replace("\"", "\"\"")}\"\n")
        }
        return sb.toString()
    }

    fun parseOrdersFromCsv(csvContent: String): Result<List<OrderEntity>> {
        return try {
            val lines = csvContent.lines().filter { it.isNotBlank() }
            val orders = mutableListOf<OrderEntity>()

            for (line in lines) {
                if (line.startsWith("LAPORAN") || line.startsWith("No;") || line.startsWith("No,")) {
                    continue
                }
                val cols = parseCsvLine(line)
                if (cols.size >= 10) {
                    val orderNumber = cols.getOrNull(1)?.trim() ?: continue
                    if (orderNumber.isBlank() || orderNumber.equals("No Nota", ignoreCase = true)) continue

                    val customerName = cols.getOrNull(3)?.trim() ?: "Pelanggan"
                    val pickupDate = cols.getOrNull(4)?.trim() ?: ""
                    val pickupTime = cols.getOrNull(5)?.trim() ?: "14:00"
                    val packagingType = cols.getOrNull(6)?.trim() ?: "Standard"
                    val packagingVariant = cols.getOrNull(7)?.trim() ?: "Standard"
                    val subtotal = cols.getOrNull(9)?.replace(".", "")?.replace("Rp", "")?.trim()?.toLongOrNull() ?: 0L
                    val packagingPrice = cols.getOrNull(10)?.replace(".", "")?.replace("Rp", "")?.trim()?.toLongOrNull() ?: 0L
                    val total = cols.getOrNull(11)?.replace(".", "")?.replace("Rp", "")?.trim()?.toLongOrNull() ?: (subtotal + packagingPrice)
                    val paymentMethod = cols.getOrNull(12)?.trim() ?: "Tunai"
                    val paymentStatus = cols.getOrNull(13)?.trim() ?: "LUNAS"
                    val initialDeposit = cols.getOrNull(14)?.replace(".", "")?.replace("Rp", "")?.trim()?.toLongOrNull() ?: 0L
                    val settlementPaid = cols.getOrNull(15)?.replace(".", "")?.replace("Rp", "")?.trim()?.toLongOrNull() ?: 0L
                    val amountPaid = cols.getOrNull(16)?.replace(".", "")?.replace("Rp", "")?.trim()?.toLongOrNull() ?: total
                    val changeOrRemaining = cols.getOrNull(17)?.replace(".", "")?.replace("Rp", "")?.trim()?.toLongOrNull() ?: 0L
                    val notes = cols.getOrNull(19)?.trim() ?: ""
                    val rawItemsJson = cols.getOrNull(20)?.trim() ?: ""

                    val itemsJson = if (rawItemsJson.startsWith("[") && rawItemsJson.endsWith("]")) {
                        rawItemsJson
                    } else {
                        "[]"
                    }

                    orders.add(
                        OrderEntity(
                            orderNumber = orderNumber,
                            customerName = customerName,
                            pickupDate = pickupDate,
                            pickupTime = pickupTime,
                            packagingType = packagingType,
                            packagingVariant = packagingVariant,
                            packagingPrice = packagingPrice,
                            itemsJson = itemsJson,
                            subtotal = subtotal,
                            total = total,
                            paymentMethod = paymentMethod,
                            amountPaid = amountPaid,
                            paymentStatus = paymentStatus,
                            changeOrRemaining = changeOrRemaining,
                            notes = notes,
                            initialDeposit = initialDeposit,
                            settlementPaid = settlementPaid,
                            createdAt = System.currentTimeMillis()
                        )
                    )
                }
            }

            if (orders.isEmpty()) {
                Result.failure(Exception("Tidak ditemukan baris transaksi yang valid dalam file CSV"))
            } else {
                Result.success(orders)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val sb = java.lang.StringBuilder()
        var inQuotes = false
        val delimiter = if (line.contains(";")) ';' else ','

        var i = 0
        while (i < line.length) {
            val c = line[i]
            if (c == '\"') {
                if (inQuotes && i + 1 < line.length && line[i + 1] == '\"') {
                    sb.append('\"')
                    i++ // Skip escaped quote
                } else {
                    inQuotes = !inQuotes
                }
            } else if (c == delimiter && !inQuotes) {
                result.add(sb.toString().trim())
                sb.setLength(0)
            } else {
                sb.append(c)
            }
            i++
        }
        result.add(sb.toString().trim())
        return result
    }

    private fun shareFile(context: Context, file: File, mimeType: String, title: String) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, title).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    private fun formatRupiah(amount: Long): String {
        val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        format.maximumFractionDigits = 0
        return format.format(amount).replace("Rp", "Rp ")
    }

    private fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale("in", "ID"))
        return sdf.format(Date(timestamp))
    }

    private fun formatFullDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMMM yyyy HH:mm", Locale("in", "ID"))
        return sdf.format(Date(timestamp))
    }
}
