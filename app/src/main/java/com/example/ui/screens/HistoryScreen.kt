package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OrderEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.PosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: PosViewModel,
    onNavigateToCart: () -> Unit = {}
) {
    val orders by viewModel.allOrders.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf("Semua") }
    var orderToDelete by remember { mutableStateOf<OrderEntity?>(null) }
    var isImportHistoryOpen by remember { mutableStateOf(false) }
    var isExportHistoryOpen by remember { mutableStateOf(false) }
    var importJsonText by remember { mutableStateOf("") }
    var exportJsonText by remember { mutableStateOf("") }

    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    val statusFilterOptions = listOf("Semua", "LUNAS", "DP", "BELUM BAYAR")

    val filteredOrders = remember(orders, searchQuery, selectedStatusFilter) {
        orders.filter { order ->
            val matchQuery = if (searchQuery.isBlank()) true
            else {
                order.orderNumber.contains(searchQuery, ignoreCase = true) ||
                        order.customerName.contains(searchQuery, ignoreCase = true) ||
                        order.pickupDate.contains(searchQuery, ignoreCase = true) ||
                        order.paymentStatus.contains(searchQuery, ignoreCase = true) ||
                        order.itemsJson.contains(searchQuery, ignoreCase = true)
            }

            val matchStatus = when (selectedStatusFilter) {
                "Semua" -> true
                "LUNAS" -> order.paymentStatus.equals("LUNAS", ignoreCase = true)
                "DP" -> order.paymentStatus.equals("DP", ignoreCase = true)
                "BELUM BAYAR" -> !order.paymentStatus.equals("LUNAS", ignoreCase = true) && !order.paymentStatus.equals("DP", ignoreCase = true)
                else -> true
            }

            matchQuery && matchStatus
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                color = Maroon40.copy(alpha = 0.12f),
                shape = CircleShape,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = null,
                        tint = Maroon40,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Riwayat Nota",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Maroon40
                )
                Text(
                    text = "Kelola nota pesanan, pelunasan & cetak struk",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Search bar & Import / Export Action Buttons
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Cari no. nota, pelanggan...", fontSize = 12.5.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Maroon40, modifier = Modifier.size(18.dp)) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Hapus Pencarian", modifier = Modifier.size(16.dp))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("input_search_history")
            )

            // Import Data Button
            IconButton(
                onClick = {
                    importJsonText = ""
                    isImportHistoryOpen = true
                },
                modifier = Modifier.testTag("btn_import_history")
            ) {
                Surface(
                    color = Maroon40.copy(alpha = 0.1f),
                    shape = CircleShape,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.FileDownload, contentDescription = "Impor Riwayat", tint = Maroon40, modifier = Modifier.size(20.dp))
                    }
                }
            }

            // Export Data Button
            IconButton(
                onClick = {
                    exportJsonText = viewModel.exportOrdersToJson()
                    isExportHistoryOpen = true
                },
                modifier = Modifier.testTag("btn_export_history")
            ) {
                Surface(
                    color = GoldWarm.copy(alpha = 0.15f),
                    shape = CircleShape,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.FileUpload, contentDescription = "Ekspor Riwayat", tint = GoldWarm, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Filter status chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(statusFilterOptions) { status ->
                val isSelected = selectedStatusFilter == status
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedStatusFilter = status },
                    label = {
                        Text(
                            text = when (status) {
                                "Semua" -> "Semua (${orders.size})"
                                "LUNAS" -> "Lunas (${orders.count { it.paymentStatus == "LUNAS" }})"
                                "DP" -> "DP (${orders.count { it.paymentStatus == "DP" }})"
                                else -> "Belum Bayar (${orders.count { it.paymentStatus != "LUNAS" && it.paymentStatus != "DP" }})"
                            },
                            fontSize = 11.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Maroon40,
                        selectedLabelColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (filteredOrders.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Surface(
                        color = Maroon40.copy(alpha = 0.08f),
                        shape = CircleShape,
                        modifier = Modifier.size(70.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = Maroon40,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = if (searchQuery.isBlank() && selectedStatusFilter == "Semua") "Belum Ada Riwayat Nota" else "Nota Tidak Ditemukan",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Maroon40
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (searchQuery.isBlank() && selectedStatusFilter == "Semua")
                            "Setelah transaksi selesai diproses, semua riwayat nota dan status pelunasan akan tampil di sini."
                        else "Coba gunakan kata kunci pencarian yang lain atau ubah filter status pembayaran.",
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(filteredOrders, key = { it.id }) { order ->
                    OrderHistoryCard(
                        order = order,
                        onViewReceipt = { viewModel.showReceipt(order) },
                        onEditOrder = { viewModel.openEditOrder(order) },
                        onDeleteOrder = { orderToDelete = order },
                        onLoadToCart = {
                            viewModel.loadOrderToCart(order)
                            onNavigateToCart()
                        }
                    )
                }
            }
        }
    }

    // Delete Confirmation Dialog
    orderToDelete?.let { order ->
        AlertDialog(
            onDismissRequest = { orderToDelete = null },
            title = { Text("Hapus Nota Pesanan?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Apakah Anda yakin ingin menghapus nota ${order.orderNumber} atas nama ${order.customerName}? Tindakan ini tidak dapat dibatalkan.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteOrder(order)
                        orderToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
                ) {
                    Text("Hapus", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { orderToDelete = null }) {
                    Text("Batal")
                }
            }
        )
    }

    // Export History Dialog
    if (isExportHistoryOpen) {
        AlertDialog(
            onDismissRequest = { isExportHistoryOpen = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FileUpload, contentDescription = null, tint = GoldWarm)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ekspor Riwayat Nota (JSON)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column {
                    Text(
                        "Data riwayat nota (${orders.size} nota) siap diekspor. Anda dapat menyalin data JSON atau membagikannya:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = exportJsonText,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(exportJsonText))
                        viewModel.showMessage("Data JSON berhasil disalin ke papan klip")
                        isExportHistoryOpen = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Maroon40)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Salin JSON")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { isExportHistoryOpen = false }) {
                    Text("Tutup")
                }
            }
        )
    }

    // Import History Dialog
    if (isImportHistoryOpen) {
        AlertDialog(
            onDismissRequest = { isImportHistoryOpen = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, tint = Maroon40)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Impor Data Riwayat Nota (JSON)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column {
                    Text(
                        "Tempelkan kode JSON data cadangan yang berisi riwayat pesanan/nota:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = importJsonText,
                        onValueChange = { importJsonText = it },
                        placeholder = { Text("Tempel JSON di sini...", fontSize = 12.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedButton(
                        onClick = {
                            val clip = clipboardManager.getText()
                            if (clip != null && clip.text.isNotBlank()) {
                                importJsonText = clip.text
                            } else {
                                viewModel.showMessage("Papan klip kosong")
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(36.dp)
                    ) {
                        Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Tempel dari Clipboard", fontSize = 11.5.sp)
                    }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = {
                            if (importJsonText.isNotBlank()) {
                                viewModel.importOrdersFromJson(importJsonText, replaceAll = false)
                                isImportHistoryOpen = false
                            } else {
                                viewModel.showMessage("Data JSON tidak boleh kosong")
                            }
                        }
                    ) {
                        Text("Gabungkan", fontSize = 12.sp)
                    }
                    Button(
                        onClick = {
                            if (importJsonText.isNotBlank()) {
                                viewModel.importOrdersFromJson(importJsonText, replaceAll = true)
                                isImportHistoryOpen = false
                            } else {
                                viewModel.showMessage("Data JSON tidak boleh kosong")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Maroon40)
                    ) {
                        Text("Pulihkan Semua", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { isImportHistoryOpen = false }) {
                    Text("Batal")
                }
            }
        )
    }
}
