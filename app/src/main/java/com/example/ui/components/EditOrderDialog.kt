package com.example.ui.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.OrderEntity
import com.example.ui.theme.ChocoBrown
import com.example.ui.theme.GoldWarm
import com.example.ui.theme.Maroon40
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun EditOrderDialog(
    order: OrderEntity,
    onDismiss: () -> Unit,
    onSave: (OrderEntity) -> Unit
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    var orderNumber by remember { mutableStateOf(order.orderNumber) }
    var customerName by remember { mutableStateOf(order.customerName) }
    var pickupDate by remember { mutableStateOf(order.pickupDate) }
    var pickupTime by remember { mutableStateOf(order.pickupTime) }
    var notes by remember { mutableStateOf(order.notes) }
    var paymentMethod by remember { mutableStateOf(order.paymentMethod) }

    val total = order.total
    val previousPaid = order.amountPaid
    val initialRemaining = (total - previousPaid).coerceAtLeast(0L)

    // Whether to use Pelunasan mode (if previously DP or PENDING) or Direct Total Edit
    var isPelunasanMode by remember { mutableStateOf(order.paymentStatus != "LUNAS" && previousPaid > 0) }
    var additionalPaymentText by remember { mutableStateOf(if (initialRemaining > 0) initialRemaining.toString() else "0") }
    var directAmountPaidText by remember { mutableStateOf(order.amountPaid.toString()) }

    val additionalPayment = additionalPaymentText.toLongOrNull() ?: 0L
    val directAmountPaid = directAmountPaidText.toLongOrNull() ?: 0L

    val effectiveTotalPaid = if (isPelunasanMode) {
        previousPaid + additionalPayment
    } else {
        directAmountPaid
    }

    // Computed payment status
    val paymentStatus = when {
        effectiveTotalPaid == 0L -> "PENDING"
        effectiveTotalPaid < total -> "DP"
        else -> "LUNAS"
    }

    val changeOrRemaining = when {
        effectiveTotalPaid == 0L -> total
        effectiveTotalPaid < total -> total - effectiveTotalPaid
        else -> effectiveTotalPaid - total
    }

    Dialog(
        onDismissRequest = {
            keyboardController?.hide()
            focusManager.clearFocus()
            onDismiss()
        },
        properties = DialogProperties(
            decorFitsSystemWindows = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(10.dp),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 16.dp)
                .imePadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = if (order.paymentStatus != "LUNAS") "Pelunasan & Edit Nota" else "Edit Data Nota",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Maroon40
                        )
                        Text(
                            text = "No. Nota: ${order.orderNumber}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // No. Nota (Editable)
                OutlinedTextField(
                    value = orderNumber,
                    onValueChange = { orderNumber = it },
                    label = { Text("No. Nota / Invoice") },
                    leadingIcon = { Icon(Icons.Default.Receipt, contentDescription = null, tint = Maroon40) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_order_number")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Customer Name
                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text("Nama Pelanggan") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_customer_name")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Pickup Date & Time
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Date picker button
                    OutlinedCard(
                        onClick = {
                            val c = Calendar.getInstance()
                            DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    val selCal = Calendar.getInstance().apply {
                                        set(year, month, dayOfMonth)
                                    }
                                    pickupDate = SimpleDateFormat("dd MMMM yyyy", Locale("in", "ID")).format(selCal.time)
                                },
                                c.get(Calendar.YEAR),
                                c.get(Calendar.MONTH),
                                c.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.3f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Tgl Ambil", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(pickupDate, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Maroon40)
                        }
                    }

                    // Time picker button
                    OutlinedCard(
                        onClick = {
                            val c = Calendar.getInstance()
                            TimePickerDialog(
                                context,
                                { _, hourOfDay, minute ->
                                    pickupTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)
                                },
                                c.get(Calendar.HOUR_OF_DAY),
                                c.get(Calendar.MINUTE),
                                true
                            ).show()
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(0.9f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Jam (24h)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$pickupTime WIB", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Maroon40)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Payment & Pelunasan Section
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Status Pembayaran",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Maroon40
                            )
                            Surface(
                                color = when (order.paymentStatus) {
                                    "LUNAS" -> Color(0xFFE8F5E9)
                                    "DP" -> Color(0xFFFFF3E0)
                                    else -> Color(0xFFFFEBEE)
                                },
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "STATUS SAAT INI: ${order.paymentStatus}",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = when (order.paymentStatus) {
                                        "LUNAS" -> Color(0xFF2E7D32)
                                        "DP" -> Color(0xFFE65100)
                                        else -> Color(0xFFC62828)
                                    },
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Summary rows
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Total Tagihan:", fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(formatRupiah(total), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Maroon40)
                        }

                        if (previousPaid > 0) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("DP / Dibayar Sebelumnya:", fontSize = 12.5.sp, color = Color(0xFFE65100))
                                Text(formatRupiah(previousPaid), fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFE65100))
                            }
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Sisa Tagihan Sebelumnya:", fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(formatRupiah(initialRemaining), fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // If previous payment exists and was DP/Pending, offer toggle
                        if (previousPaid > 0 && order.paymentStatus != "LUNAS") {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                FilterChip(
                                    selected = isPelunasanMode,
                                    onClick = { isPelunasanMode = true },
                                    label = { Text("Bayar Pelunasan", fontSize = 11.5.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                                FilterChip(
                                    selected = !isPelunasanMode,
                                    onClick = { isPelunasanMode = false },
                                    label = { Text("Edit Total Bayar", fontSize = 11.5.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        if (isPelunasanMode) {
                            // Pelunasan input
                            OutlinedTextField(
                                value = additionalPaymentText,
                                onValueChange = { additionalPaymentText = it.filter { ch -> ch.isDigit() } },
                                label = { Text("Bayar Pelunasan Sekarang (Rp)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("edit_additional_payment")
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // Quick button for full remainder
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Button(
                                    onClick = { additionalPaymentText = initialRemaining.toString() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.weight(1f).height(34.dp)
                                ) {
                                    Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Lunasi Sisa (${formatRupiah(initialRemaining)})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            // Direct total paid edit
                            OutlinedTextField(
                                value = directAmountPaidText,
                                onValueChange = { directAmountPaidText = it.filter { ch -> ch.isDigit() } },
                                label = { Text("Total Akumulasi Uang Masuk (Rp)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("edit_amount_paid")
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedButton(
                                    onClick = { directAmountPaidText = total.toString() },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                    modifier = Modifier.weight(1f).height(32.dp)
                                ) {
                                    Text("Lunas Pas", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                OutlinedButton(
                                    onClick = { directAmountPaidText = (total / 2).toString() },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                    modifier = Modifier.weight(1f).height(32.dp)
                                ) {
                                    Text("DP 50%", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                OutlinedButton(
                                    onClick = { directAmountPaidText = "0" },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                    modifier = Modifier.weight(1f).height(32.dp)
                                ) {
                                    Text("Rp 0 (Pending)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Payment Method Selector
                        Text("Metode Pembayaran:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("Tunai / Cash", "Transfer Bank", "QRIS").forEach { method ->
                                val isSelected = paymentMethod == method
                                Surface(
                                    color = if (isSelected) Maroon40 else MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, if (isSelected) Maroon40 else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { paymentMethod = method }
                                ) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 6.dp)) {
                                        Text(
                                            text = method,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Calculation Result Banner
                        Surface(
                            color = when (paymentStatus) {
                                "LUNAS" -> Color(0xFFE8F5E9)
                                "DP" -> Color(0xFFFFF3E0)
                                else -> Color(0xFFFFEBEE)
                            },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(
                                1.dp,
                                when (paymentStatus) {
                                    "LUNAS" -> Color(0xFF4CAF50)
                                    "DP" -> Color(0xFFFF9800)
                                    else -> Color(0xFFE53935)
                                }
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "STATUS BARU: $paymentStatus",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.sp,
                                        color = when (paymentStatus) {
                                            "LUNAS" -> Color(0xFF2E7D32)
                                            "DP" -> Color(0xFFE65100)
                                            else -> Color(0xFFC62828)
                                        }
                                    )
                                    Text(
                                        text = "Total Masuk: ${formatRupiah(effectiveTotalPaid)}",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = when (paymentStatus) {
                                        "LUNAS" -> if (changeOrRemaining > 0) "Kembalian untuk Pelanggan: ${formatRupiah(changeOrRemaining)}" else "Pembayaran Lunas Pas (Rp 0)"
                                        "DP" -> "Sisa Tagihan Belum Lunas: ${formatRupiah(changeOrRemaining)}"
                                        else -> "Belum Ada Pembayaran (Sisa: ${formatRupiah(total)})"
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (paymentStatus) {
                                        "LUNAS" -> Color(0xFF2E7D32)
                                        "DP" -> Color(0xFFE65100)
                                        else -> Color(0xFFC62828)
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Catatan Pesanan") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_order_notes")
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Save button
                Button(
                    onClick = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        val oldDeposit = if (order.initialDeposit > 0L) {
                            order.initialDeposit
                        } else if (order.paymentStatus == "DP" || previousPaid > 0L) {
                            previousPaid
                        } else {
                            0L
                        }

                        val finalSettlement = if (isPelunasanMode) {
                            additionalPayment
                        } else if (effectiveTotalPaid > oldDeposit && oldDeposit > 0L) {
                            effectiveTotalPaid - oldDeposit
                        } else {
                            order.settlementPaid
                        }

                        val updated = order.copy(
                            orderNumber = if (orderNumber.isNotBlank()) orderNumber.trim() else order.orderNumber,
                            customerName = customerName.trim(),
                            pickupDate = pickupDate.trim(),
                            pickupTime = pickupTime.trim(),
                            paymentMethod = paymentMethod,
                            amountPaid = effectiveTotalPaid,
                            paymentStatus = paymentStatus,
                            changeOrRemaining = changeOrRemaining,
                            notes = notes.trim(),
                            initialDeposit = oldDeposit,
                            settlementPaid = finalSettlement
                        )
                        onSave(updated)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Maroon40),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_save_edit_order")
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (paymentStatus == "LUNAS" && order.paymentStatus != "LUNAS") "Simpan & Lunasi Nota" else "Simpan Perubahan Nota",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
