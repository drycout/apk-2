package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.OrderEntity
import com.example.ui.theme.GoldWarm
import com.example.ui.theme.Maroon40
import com.example.viewmodel.PosViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ReceiptDialog(
    order: OrderEntity,
    viewModel: PosViewModel,
    onDismiss: () -> Unit
) {
    val items = remember(order) { order.getCartItems() }
    val isPrinterConnected by viewModel.isPrinterConnected.collectAsState()
    val isPrinting by viewModel.isPrinting.collectAsState()
    val receiptFeedback by viewModel.receiptFeedback.collectAsState()
    val storeProfile by viewModel.storeProfile.collectAsState()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Dialog Title Bar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Nota Pesanan Tersimpan",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Maroon40
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("btn_close_receipt")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Realistic Paper Receipt View
                Surface(
                    color = Color(0xFFFFFDF8),
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Store Header
                        Text(
                            text = storeProfile.name.uppercase(),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center,
                            color = Maroon40,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (storeProfile.tagline.isNotBlank()) {
                            Text(
                                text = storeProfile.tagline,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                textAlign = TextAlign.Center,
                                color = Color.DarkGray,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        if (storeProfile.whatsapp.isNotBlank()) {
                            Text(
                                text = "WA: ${storeProfile.whatsapp}",
                                fontSize = 10.5.sp,
                                fontFamily = FontFamily.Monospace,
                                textAlign = TextAlign.Center,
                                color = Color.DarkGray,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Text(
                            text = "================================",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            color = Color.Gray,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Meta details
                        ReceiptMonospaceRow("No. Nota", order.orderNumber, isBold = true)
                        ReceiptMonospaceRow(
                            "Tgl Order",
                            SimpleDateFormat("dd/MM/yy HH:mm", Locale("in", "ID")).format(Date(order.createdAt))
                        )
                        ReceiptMonospaceRow("Pelanggan", order.customerName, isBold = true)

                        Spacer(modifier = Modifier.height(6.dp))

                        // Pickup Highlight
                        Surface(
                            color = Color(0xFFFCEEEF),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = "JADWAL AMBIL (24 JAM):",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = Maroon40
                                )
                                Text(
                                    text = "${order.pickupDate} • ${order.pickupTime} WIB",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Monospace,
                                    color = Maroon40
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "--------------------------------",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            color = Color.Gray,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Items list
                        items.forEach { item ->
                            Text(
                                text = item.productName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF26191B)
                            )
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "  ${item.quantity}x ${formatRupiah(item.price)}",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color.DarkGray
                                )
                                Text(
                                    text = formatRupiah(item.subtotal),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        val totalItemCount = remember(items) { items.sumOf { it.quantity } }
                        ReceiptMonospaceRow("TOTAL ITEM", "$totalItemCount Item", isBold = true)

                        Text(
                            text = "--------------------------------",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            color = Color.Gray,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Box Info
                        val isCustomVariant = order.packagingVariant.isNotBlank() && 
                                !order.packagingVariant.equals("Standard", ignoreCase = true) && 
                                !order.packagingVariant.equals(order.packagingType, ignoreCase = true)
                        val boxDisplayName = if (isCustomVariant) "${order.packagingType} (${order.packagingVariant})" else order.packagingType
                        ReceiptMonospaceRow("Kemasan/Box", boxDisplayName)
                        if (order.packagingPrice > 0) {
                            ReceiptMonospaceRow("Biaya Kemasan", formatRupiah(order.packagingPrice))
                        }

                        Text(
                            text = "================================",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            color = Color.Gray,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Subtotal & Total
                        ReceiptMonospaceRow("Subtotal", formatRupiah(order.subtotal))
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "TOTAL AKHIR:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace,
                                color = Maroon40
                            )
                            Text(
                                text = formatRupiah(order.total),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace,
                                color = Maroon40
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "--------------------------------",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            color = Color.Gray,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        // Payment Status & Details
                        ReceiptMonospaceRow("Status Bayar", order.paymentStatus, isBold = true)
                        ReceiptMonospaceRow("Metode Bayar", order.paymentMethod)

                        if (order.initialDeposit > 0L) {
                            ReceiptMonospaceRow("DP Lama/Awal", formatRupiah(order.initialDeposit))
                            val settlement = if (order.settlementPaid > 0L) order.settlementPaid else (order.amountPaid - order.initialDeposit).coerceAtLeast(0L)
                            if (order.paymentStatus == "LUNAS" || settlement > 0L) {
                                ReceiptMonospaceRow("Pelunasan", formatRupiah(settlement), isBold = true)
                            }
                            ReceiptMonospaceRow("Total Bayar", formatRupiah(order.amountPaid), isBold = true)
                        } else {
                            val paidLabel = if (order.paymentStatus == "DP") "DP Dibayar" else "Jml Dibayar"
                            ReceiptMonospaceRow(paidLabel, formatRupiah(order.amountPaid), isBold = true)
                        }

                        if (order.paymentStatus == "LUNAS") {
                            val changeText = if (order.changeOrRemaining > 0) formatRupiah(order.changeOrRemaining) else "Rp 0 (Pas)"
                            ReceiptMonospaceRow("Kembalian", changeText, isBold = true)
                        } else {
                            val remainAmount = if (order.paymentStatus == "DP") order.changeOrRemaining else order.total
                            ReceiptMonospaceRow("Sisa Kurang", formatRupiah(remainAmount), isBold = true)
                        }

                        if (storeProfile.showNotesOnReceipt && order.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Catatan: ${order.notes}",
                                fontSize = 10.5.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color.DarkGray
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        if (storeProfile.showReceiptGreeting && storeProfile.receiptGreeting.isNotBlank()) {
                            Text(
                                text = storeProfile.receiptGreeting,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                textAlign = TextAlign.Center,
                                color = Color.DarkGray,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        if (storeProfile.showSocialMedia && (storeProfile.instagram.isNotBlank() || storeProfile.tiktok.isNotBlank())) {
                            val ig = if (storeProfile.instagram.startsWith("@")) storeProfile.instagram else "@${storeProfile.instagram}"
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "IG/TikTok: $ig",
                                fontSize = 10.5.sp,
                                fontFamily = FontFamily.Monospace,
                                textAlign = TextAlign.Center,
                                color = Maroon40,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // In-Dialog Feedback / Notification Banner (Visible immediately inside Dialog)
                receiptFeedback?.let { feedback ->
                    val isSuccess = feedback.startsWith("OK:")
                    val isError = feedback.startsWith("ERR:")
                    val cleanText = feedback.removePrefix("OK:").removePrefix("ERR:").removePrefix("INFO:")

                    val bgColor = if (isSuccess) Color(0xFFE8F5E9) else if (isError) Color(0xFFFFEBEE) else Color(0xFFE3F2FD)
                    val contentColor = if (isSuccess) Color(0xFF2E7D32) else if (isError) Color(0xFFC62828) else Color(0xFF1565C0)
                    val icon = if (isSuccess) Icons.Default.CheckCircle else if (isError) Icons.Default.ErrorOutline else Icons.Default.Info

                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = bgColor,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = cleanText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = contentColor,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action 1: Print Thermal Bluetooth with Integrated Bluetooth Status
                Button(
                    onClick = {
                        if (!isPrinterConnected) {
                            viewModel.openPrinterSettings()
                        } else {
                            viewModel.printCurrentReceipt()
                        }
                    },
                    enabled = !isPrinting,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPrinterConnected) Maroon40 else Color(0xFF1976D2),
                        disabledContainerColor = Maroon40.copy(alpha = 0.6f),
                        disabledContentColor = Color.White.copy(alpha = 0.8f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_print_thermal")
                ) {
                    if (isPrinting) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sedang Mencetak...",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Icon(
                            imageVector = if (isPrinterConnected) Icons.Default.Print else Icons.Default.Bluetooth,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isPrinterConnected) "Cetak ke Thermal Bluetooth" else "Sambungkan Bluetooth & Cetak",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Action 2: 2 Compact Buttons: Simpan & Share
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FilledTonalButton(
                        onClick = { viewModel.saveCurrentReceiptAsImage() },
                        enabled = !isPrinting,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Maroon40.copy(alpha = 0.12f),
                            contentColor = Maroon40
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("btn_save_receipt_image")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(17.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Simpan",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    FilledTonalButton(
                        onClick = { viewModel.shareCurrentReceiptAsImage() },
                        enabled = !isPrinting,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xFF25D366).copy(alpha = 0.15f),
                            contentColor = Color(0xFF075E54)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("btn_share_whatsapp_image")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(17.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Share",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Action 3: Done
                OutlinedButton(
                    onClick = onDismiss,
                    enabled = !isPrinting,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_done_receipt")
                ) {
                    Text("Tutup", fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun ReceiptMonospaceRow(label: String, value: String, isBold: Boolean = false) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = Color.DarkGray
        )
        Text(
            text = value,
            fontSize = 11.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF26191B)
        )
    }
}
