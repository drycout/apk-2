package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.net.Uri
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

object ReceiptImageHelper {

    suspend fun generateReceiptBitmap(
        context: Context,
        order: OrderEntity,
        store: StoreProfile = StoreProfile()
    ): Bitmap = withContext(Dispatchers.Default) {
        val width = 600
        // Dynamically compute height based on items + payment section
        val items = order.getCartItems()
        val totalItemCount = items.sumOf { it.quantity }
        val estimatedHeight = 1150 + (items.size * 65) + (if (store.showNotesOnReceipt && order.notes.isNotBlank()) 80 else 0)
        val height = estimatedHeight.coerceAtLeast(980)

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Draw Paper Background (Thermal receipt off-white)
        val bgPaint = Paint().apply {
            color = Color.parseColor("#FFFDF9")
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Draw Receipt Header Banner (Maroon)
        val headerPaint = Paint().apply {
            color = Color.parseColor("#801B34")
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), 120f, headerPaint)

        // Header Text
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 34f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(store.name.uppercase(), width / 2f, 50f, titlePaint)

        val subTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FFE0A4")
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Sweet & Savoury", width / 2f, 80f, subTitlePaint)
        
        val contactPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 17f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("WA: ${store.whatsapp} | IG: @${store.instagram}", width / 2f, 106f, contactPaint)

        // Content Area
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#26191B")
            textSize = 22f
            typeface = Typeface.DEFAULT
        }
        val boldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#26191B")
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val rightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#26191B")
            textSize = 22f
            textAlign = Paint.Align.RIGHT
        }
        val boldRightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#26191B")
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }
        val linePaint = Paint().apply {
            color = Color.parseColor("#D4C2BA")
            strokeWidth = 2f
            pathEffect = DashPathEffect(floatArrayOf(8f, 6f), 0f)
        }

        var y = 160f
        val leftX = 35f
        val rightX = width - 35f

        // Order metadata
        canvas.drawText("No. Nota:", leftX, y, boldPaint)
        canvas.drawText(order.orderNumber, rightX, y, boldRightPaint)
        y += 35f

        canvas.drawText("Tanggal Order:", leftX, y, textPaint)
        canvas.drawText(formatDate(order.createdAt), rightX, y, rightPaint)
        y += 35f

        canvas.drawText("Pelanggan:", leftX, y, textPaint)
        canvas.drawText(order.customerName, rightX, y, boldRightPaint)
        y += 45f

        // Pickup Banner (Highlighted box)
        val bannerPaint = Paint().apply {
            color = Color.parseColor("#FCEEEF")
            style = Paint.Style.FILL
        }
        val bannerBorder = Paint().apply {
            color = Color.parseColor("#801B34")
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        canvas.drawRoundRect(RectF(leftX, y, rightX, y + 80f), 12f, 12f, bannerPaint)
        canvas.drawRoundRect(RectF(leftX, y, rightX, y + 80f), 12f, 12f, bannerBorder)

        val scheduleTitle = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#801B34")
            textSize = 19f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val scheduleValue = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#26191B")
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        canvas.drawText("JADWAL PENGAMBILAN (24 JAM):", leftX + 16f, y + 30f, scheduleTitle)
        canvas.drawText("${order.pickupDate} • Jam ${order.pickupTime} WIB", leftX + 16f, y + 62f, scheduleValue)
        y += 105f

        // Divider
        canvas.drawLine(leftX, y, rightX, y, linePaint)
        y += 30f

        // Table Header
        val colHeader = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#7A5548")
            textSize = 19f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val colHeaderRight = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#7A5548")
            textSize = 19f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("RINCIAN PESANAN ($totalItemCount ITEM)", leftX, y, colHeader)
        canvas.drawText("SUBTOTAL", rightX, y, colHeaderRight)
        y += 20f

        canvas.drawLine(leftX, y, rightX, y, linePaint)
        y += 35f

        // Items
        for (item in items) {
            canvas.drawText(item.productName, leftX, y, boldPaint)
            canvas.drawText(formatRupiah(item.subtotal), rightX, y, boldRightPaint)
            y += 28f

            val qtyText = "${item.quantity} pcs x ${formatRupiah(item.price)}"
            val subTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#6B5E5B")
                textSize = 18f
            }
            canvas.drawText(qtyText, leftX, y, subTextPaint)
            y += 35f
        }

        // Total Item count highlight row
        canvas.drawLine(leftX, y, rightX, y, linePaint)
        y += 30f
        canvas.drawText("TOTAL ITEM:", leftX, y, boldPaint)
        canvas.drawText("$totalItemCount Item", rightX, y, boldRightPaint)
        y += 35f

        // Packaging
        canvas.drawLine(leftX, y, rightX, y, linePaint)
        y += 35f

        canvas.drawText("Kemasan / Box:", leftX, y, boldPaint)
        canvas.drawText(order.packagingType, rightX, y, boldRightPaint)
        y += 28f

        val varText = "Varian: ${order.packagingVariant}"
        val varPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#801B34")
            textSize = 19f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText(varText, leftX, y, varPaint)
        canvas.drawText(formatRupiah(order.packagingPrice), rightX, y, rightPaint)
        y += 40f

        // Summary Divider (Solid Line)
        val solidLine = Paint().apply {
            color = Color.parseColor("#801B34")
            strokeWidth = 3f
        }
        canvas.drawLine(leftX, y, rightX, y, solidLine)
        y += 35f

        // Totals
        canvas.drawText("Subtotal Produk:", leftX, y, textPaint)
        canvas.drawText(formatRupiah(order.subtotal), rightX, y, rightPaint)
        y += 30f

        canvas.drawText("Biaya Box/Kemasan:", leftX, y, textPaint)
        canvas.drawText(formatRupiah(order.packagingPrice), rightX, y, rightPaint)
        y += 40f

        // Grand Total (Big Highlight)
        val grandTotalBox = Paint().apply {
            color = Color.parseColor("#FFF3E0")
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(RectF(leftX, y - 30f, rightX, y + 25f), 8f, 8f, grandTotalBox)

        val totalLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#590D22")
            textSize = 26f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val totalValuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#801B34")
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("TOTAL AKHIR:", leftX + 16f, y + 8f, totalLabelPaint)
        canvas.drawText(formatRupiah(order.total), rightX - 16f, y + 8f, totalValuePaint)
        y += 55f

        // Payment & Status Section
        canvas.drawLine(leftX, y, rightX, y, linePaint)
        y += 30f

        // Status Badge Box
        val statusColor = when (order.paymentStatus) {
            "LUNAS" -> Color.parseColor("#2E7D32")
            "DP" -> Color.parseColor("#E65100")
            else -> Color.parseColor("#C62828")
        }
        val statusBgColor = when (order.paymentStatus) {
            "LUNAS" -> Color.parseColor("#E8F5E9")
            "DP" -> Color.parseColor("#FFF3E0")
            else -> Color.parseColor("#FFEBEE")
        }
        val statusBadgePaint = Paint().apply {
            color = statusBgColor
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(RectF(leftX, y - 24f, leftX + 220f, y + 14f), 6f, 6f, statusBadgePaint)

        val statusTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = statusColor
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("STATUS: ${order.paymentStatus}", leftX + 12f, y + 2f, statusTextPaint)

        val methodPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#555555")
            textSize = 19f
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("Metode: ${order.paymentMethod}", rightX, y + 2f, methodPaint)
        y += 38f

        // Payment amounts
        if (order.initialDeposit > 0L) {
            canvas.drawText("DP Lama / Awal:", leftX, y, textPaint)
            canvas.drawText(formatRupiah(order.initialDeposit), rightX, y, rightPaint)
            y += 30f

            val settlement = if (order.settlementPaid > 0L) order.settlementPaid else (order.amountPaid - order.initialDeposit).coerceAtLeast(0L)
            if (order.paymentStatus == "LUNAS" || settlement > 0L) {
                canvas.drawText("Pelunasan:", leftX, y, textPaint)
                canvas.drawText(formatRupiah(settlement), rightX, y, boldRightPaint)
                y += 30f
            }

            canvas.drawText("Total Terbayar:", leftX, y, textPaint)
            canvas.drawText(formatRupiah(order.amountPaid), rightX, y, boldRightPaint)
            y += 30f
        } else {
            val paidLabel = if (order.paymentStatus == "DP") "DP Dibayar:" else "Jumlah Dibayar:"
            canvas.drawText(paidLabel, leftX, y, textPaint)
            canvas.drawText(formatRupiah(order.amountPaid), rightX, y, boldRightPaint)
            y += 30f
        }

        if (order.paymentStatus == "LUNAS") {
            val changePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#2E7D32")
                textSize = 21f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val changeRightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#2E7D32")
                textSize = 21f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.RIGHT
            }
            canvas.drawText("Kembalian:", leftX, y, changePaint)
            val changeText = if (order.changeOrRemaining > 0) formatRupiah(order.changeOrRemaining) else "Rp 0 (Pas)"
            canvas.drawText(changeText, rightX, y, changeRightPaint)
            y += 35f
        } else {
            val remainPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = statusColor
                textSize = 21f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val remainRightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = statusColor
                textSize = 21f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.RIGHT
            }
            canvas.drawText("Sisa Kurang Bayar:", leftX, y, remainPaint)
            val remainAmount = if (order.paymentStatus == "DP") order.changeOrRemaining else order.total
            canvas.drawText(formatRupiah(remainAmount), rightX, y, remainRightPaint)
            y += 35f
        }

        if (store.showNotesOnReceipt && order.notes.isNotBlank()) {
            val notesPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#555555")
                textSize = 18f
            }
            canvas.drawText("Catatan: ${order.notes}", leftX, y, notesPaint)
            y += 35f
        }

        // Footer
        canvas.drawLine(leftX, y, rightX, y, linePaint)
        y += 35f

        val footerCenter = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#5A4749")
            textSize = 18f
            textAlign = Paint.Align.CENTER
        }
        val footerBold = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#801B34")
            textSize = 19f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Terima kasih telah memesan di DJANDES!", width / 2f, y, footerBold)
        y += 26f
        canvas.drawText("Harap tunjukkan nota ini saat pengambilan kue.", width / 2f, y, footerCenter)
        y += 24f
        canvas.drawText(store.address, width / 2f, y, footerCenter)

        bitmap
    }

    suspend fun saveReceiptImage(
        context: Context,
        order: OrderEntity,
        store: StoreProfile = StoreProfile()
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val bitmap = generateReceiptBitmap(context, order, store)
            val cleanOrderNo = order.orderNumber.replace("/", "-").replace(" ", "_")
            val fileName = "Nota_${cleanOrderNo}.png"
            AppStorageHelper.saveReceiptImageToStorage(context, fileName, bitmap)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun shareReceiptImage(
        context: Context,
        order: OrderEntity,
        store: StoreProfile = StoreProfile()
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val bitmap = generateReceiptBitmap(context, order, store)
            val imagesDir = File(context.cacheDir, "images")
            if (!imagesDir.exists()) imagesDir.mkdirs()

            val cleanOrderNo = order.orderNumber.replace("/", "-").replace(" ", "_")
            val file = File(imagesDir, "Nota_${cleanOrderNo}.png")
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.flush()
            fos.close()

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            // Trigger Share Intent
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Nota Pesanan ${store.name} - ${order.orderNumber}\n" +
                            "Pelanggan: ${order.customerName}\n" +
                            "Jadwal Ambil: ${order.pickupDate} Jam ${order.pickupTime} WIB\n" +
                            "Total: ${formatRupiah(order.total)}\n" +
                            "Status: ${order.paymentStatus}"
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Bagikan Nota").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })

            Result.success(uri)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveAndShareReceiptImage(
        context: Context,
        order: OrderEntity,
        store: StoreProfile = StoreProfile()
    ): Result<Uri> {
        return shareReceiptImage(context, order, store)
    }

    private fun formatRupiah(amount: Long): String {
        val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        format.maximumFractionDigits = 0
        return format.format(amount).replace("Rp", "Rp ")
    }

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("in", "ID"))
        return sdf.format(Date(timestamp))
    }
}
