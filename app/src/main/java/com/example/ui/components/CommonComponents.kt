package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.CartItem
import com.example.data.model.OrderEntity
import com.example.ui.theme.ChocoBrown
import com.example.ui.theme.GoldWarm
import com.example.ui.theme.Maroon40
import java.text.NumberFormat
import java.util.Locale

fun formatRupiah(amount: Long): String {
    val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
    format.maximumFractionDigits = 0
    return format.format(amount).replace("Rp", "Rp ")
}

@Composable
fun StatusBadge(
    text: String,
    containerColor: Color = Color(0xFF801B34),
    contentColor: Color = Color.White,
    modifier: Modifier = Modifier
) {
    Surface(
        color = containerColor,
        shape = RoundedCornerShape(6.dp),
        modifier = modifier
    ) {
        Text(
            text = text,
            color = contentColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun StatusBadge(
    text: String,
    backgroundColor: Color,
    textColor: Color
) {
    StatusBadge(
        text = text,
        containerColor = backgroundColor,
        contentColor = textColor
    )
}

@Composable
fun PrinterStatusChip(
    isConnected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (isConnected) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
        tonalElevation = 1.dp,
        modifier = modifier.testTag("btn_printer_status")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = if (isConnected) Icons.Default.BluetoothConnected else Icons.Default.BluetoothDisabled,
                contentDescription = "Bluetooth Status",
                tint = if (isConnected) Color(0xFF2E7D32) else Color(0xFFC62828),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (isConnected) "Printer Terhubung" else "Printer Disconnect",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = if (isConnected) Color(0xFF2E7D32) else Color(0xFFC62828)
            )
        }
    }
}

@Composable
fun CartItemRow(
    item: CartItem,
    onAdd: () -> Unit,
    onMinus: () -> Unit,
    onRemove: () -> Unit,
    onImageClick: () -> Unit
) {
    val isParcel = item.productName.contains("parcel", ignoreCase = true)

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onImageClick() }
            ) {
                AsyncImage(
                    model = item.img,
                    contentDescription = item.productName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Surface(
                    color = Color.Black.copy(alpha = 0.5f),
                    shape = CircleShape,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(2.dp)
                        .size(18.dp)
                ) {
                    Icon(
                        Icons.Default.ZoomIn,
                        contentDescription = "Zoom",
                        tint = Color.White,
                        modifier = Modifier.padding(2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.productName,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (isParcel) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = GoldWarm.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "Parcel",
                                color = GoldWarm,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    text = "${formatRupiah(item.price)} x ${item.quantity} = ${formatRupiah(item.subtotal)}",
                    fontSize = 12.sp,
                    color = Maroon40,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Quantity buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                IconButton(
                    onClick = onMinus,
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Kurang", modifier = Modifier.size(14.dp))
                }
                Text(
                    text = "${item.quantity}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                IconButton(
                    onClick = onAdd,
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Tambah", modifier = Modifier.size(14.dp))
                }
            }

            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(32.dp).padding(start = 4.dp)
            ) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Hapus", tint = Color(0xFFC62828), modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun OrderHistoryCard(
    order: OrderEntity,
    onViewReceipt: () -> Unit,
    onEditOrder: () -> Unit,
    onDeleteOrder: () -> Unit,
    onLoadToCart: () -> Unit
) {
    val items = remember(order) { order.getCartItems() }
    val totalQty = remember(items) { items.sumOf { it.quantity } }

    val statusColor = when (order.paymentStatus) {
        "LUNAS" -> Color(0xFF2E7D32)
        "DP" -> Color(0xFFE65100)
        else -> Color(0xFFC62828)
    }

    val statusBg = when (order.paymentStatus) {
        "LUNAS" -> Color(0xFFE8F5E9)
        "DP" -> Color(0xFFFFF3E0)
        else -> Color(0xFFFFEBEE)
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("order_card_${order.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Order Number & Payment Status Badge
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = order.orderNumber,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Maroon40
                )
                Surface(
                    color = statusBg,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, statusColor.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "STATUS: ${order.paymentStatus}",
                        color = statusColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Pelanggan: ${order.customerName}",
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Pickup Date & 24h Time Banner
            Surface(
                color = Color(0xFFFBF3EB),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = GoldWarm,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Ambil: ${order.pickupDate} • ${order.pickupTime} WIB",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldWarm
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Box: ${order.packagingType} (${order.packagingVariant}) • $totalQty pcs item",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (order.notes.isNotBlank()) {
                Text(
                    text = "Catatan: ${order.notes}",
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Bottom row: Total & Payment Info + Action Buttons
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "Total: ${formatRupiah(order.total)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Maroon40
                    )
                    Text(
                        text = when (order.paymentStatus) {
                            "LUNAS" -> "Dibayar: ${formatRupiah(order.amountPaid)}"
                            "DP" -> "DP: ${formatRupiah(order.amountPaid)} (Sisa: ${formatRupiah(order.changeOrRemaining)})"
                            else -> "Belum Dibayar (Rp 0)"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = statusColor
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Delete Button
                    IconButton(
                        onClick = onDeleteOrder,
                        modifier = Modifier.size(32.dp).testTag("btn_delete_order_${order.id}")
                    ) {
                        Icon(
                            Icons.Default.DeleteOutline,
                            contentDescription = "Hapus Nota",
                            tint = Color(0xFFC62828),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Edit / Pelunasan Order Button
                    if (order.paymentStatus != "LUNAS") {
                        Button(
                            onClick = onEditOrder,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (order.paymentStatus == "DP") Color(0xFFE65100) else Color(0xFFC62828)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp).testTag("btn_edit_order_${order.id}")
                        ) {
                            Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Pelunasan", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        OutlinedButton(
                            onClick = onEditOrder,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp).testTag("btn_edit_order_${order.id}")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Edit", fontSize = 11.5.sp)
                        }
                    }

                    // View Receipt Button
                    Button(
                        onClick = onViewReceipt,
                        colors = ButtonDefaults.buttonColors(containerColor = Maroon40),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp).testTag("btn_view_receipt_${order.id}")
                    ) {
                        Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Nota", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
