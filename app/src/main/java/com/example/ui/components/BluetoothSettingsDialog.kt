package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.BluetoothPrinterDevice
import com.example.ui.theme.GoldWarm
import com.example.ui.theme.Maroon40
import com.example.viewmodel.PosViewModel

@Composable
fun BluetoothSettingsDialog(
    viewModel: PosViewModel,
    onDismiss: () -> Unit
) {
    val isConnected by viewModel.isPrinterConnected.collectAsState()
    val pairedDevices by viewModel.pairedDevices.collectAsState()
    val autoPrint by viewModel.autoPrint.collectAsState()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Print,
                        contentDescription = null,
                        tint = Maroon40,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Pengaturan Printer Thermal",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Maroon40
                        )
                        Text(
                            text = if (isConnected) "Status: Terhubung" else "Status: Belum Terhubung",
                            fontSize = 12.sp,
                            color = if (isConnected) Color(0xFF2E7D32) else Color(0xFFC62828)
                        )
                    }
                    IconButton(
                        onClick = { viewModel.refreshPairedDevices() },
                        modifier = Modifier.testTag("btn_refresh_bt")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // Auto-print switch
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Cetak Otomatis Saat Checkout",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Struk langsung dikirim ke printer saat order dibuat",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = autoPrint,
                        onCheckedChange = { viewModel.setAutoPrint(it) },
                        modifier = Modifier.testTag("switch_auto_print")
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Perangkat Bluetooth Tersanding (Paired):",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (pairedDevices.isEmpty()) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                Icons.Default.BluetoothSearching,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Tidak ada printer yang tersanding.",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = "Pastikan printer thermal aktif dan telah dipasangkan (paired) di pengaturan Bluetooth HP.",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                    ) {
                        items(pairedDevices) { device ->
                            DeviceItemRow(
                                device = device,
                                isConnected = isConnected && device.isConnected,
                                onConnect = { viewModel.connectPrinter(device.address) },
                                onDisconnect = { viewModel.disconnectPrinter() }
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons: Test Print & Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.testPrint() },
                        enabled = isConnected,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_test_print")
                    ) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Uji Cetak", fontSize = 13.sp)
                    }

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Maroon40),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_close_printer_dialog")
                    ) {
                        Text("Selesai", fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun DeviceItemRow(
    device: BluetoothPrinterDevice,
    isConnected: Boolean,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isConnected) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Print,
                contentDescription = null,
                tint = if (isConnected) Color(0xFF2E7D32) else Maroon40,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
                Text(
                    text = device.address,
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            if (isConnected) {
                TextButton(
                    onClick = onDisconnect,
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFC62828)),
                    modifier = Modifier.testTag("btn_disconnect_printer")
                ) {
                    Text("Putus", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = onConnect,
                    colors = ButtonDefaults.buttonColors(containerColor = Maroon40),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("btn_connect_${device.address}")
                ) {
                    Text("Sambung", fontSize = 12.sp)
                }
            }
        }
    }
}
