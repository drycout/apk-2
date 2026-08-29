package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.OrderEntity
import com.example.ui.components.formatRupiah
import com.example.ui.theme.GoldWarm
import com.example.ui.theme.Maroon40
import com.example.ui.theme.MaroonDark
import com.example.viewmodel.PosViewModel
import java.text.SimpleDateFormat
import java.util.*

enum class ReportFilterMode(val label: String) {
    TODAY("Hari Ini"),
    WEEKLY("7 Hari Terakhir"),
    MONTHLY("Bulan Ini"),
    YEARLY("Tahun Ini"),
    CUSTOM("Rentang Manual")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: PosViewModel
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val allOrders by viewModel.allOrders.collectAsState()

    var filterMode by remember { mutableStateOf(ReportFilterMode.TODAY) }
    var singleDateMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var startDateMillis by remember {
        mutableLongStateOf(Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -7) }.timeInMillis)
    }
    var endDateMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }

    var isReportBackupDialogOpen by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("dd MMMM yyyy", Locale("in", "ID")) }
    val shortDateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale("in", "ID")) }
    val monthFormat = remember { SimpleDateFormat("MMMM yyyy", Locale("in", "ID")) }
    val yearFormat = remember { SimpleDateFormat("yyyy", Locale("in", "ID")) }

    // Dynamic date label based on filter mode
    val dateLabel = remember(filterMode, singleDateMillis, startDateMillis, endDateMillis) {
        when (filterMode) {
            ReportFilterMode.TODAY -> dateFormat.format(Date(singleDateMillis))
            ReportFilterMode.WEEKLY -> "7 Hari (${shortDateFormat.format(Date(System.currentTimeMillis() - 7L * 24 * 3600 * 1000))} - ${shortDateFormat.format(Date())})"
            ReportFilterMode.MONTHLY -> monthFormat.format(Date())
            ReportFilterMode.YEARLY -> "Tahun ${yearFormat.format(Date())}"
            ReportFilterMode.CUSTOM -> "${shortDateFormat.format(Date(startDateMillis))} - ${shortDateFormat.format(Date(endDateMillis))}"
        }
    }

    // Filter orders based on active period
    val filteredOrders = remember(allOrders, filterMode, singleDateMillis, startDateMillis, endDateMillis) {
        when (filterMode) {
            ReportFilterMode.TODAY -> {
                val calSelected = Calendar.getInstance().apply { timeInMillis = singleDateMillis }
                allOrders.filter { order ->
                    val calOrder = Calendar.getInstance().apply { timeInMillis = order.createdAt }
                    calOrder.get(Calendar.YEAR) == calSelected.get(Calendar.YEAR) &&
                            calOrder.get(Calendar.DAY_OF_YEAR) == calSelected.get(Calendar.DAY_OF_YEAR)
                }
            }
            ReportFilterMode.WEEKLY -> {
                val cal7DaysAgo = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, -7)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                }
                allOrders.filter { it.createdAt >= cal7DaysAgo.timeInMillis }
            }
            ReportFilterMode.MONTHLY -> {
                val calNow = Calendar.getInstance()
                allOrders.filter { order ->
                    val calOrder = Calendar.getInstance().apply { timeInMillis = order.createdAt }
                    calOrder.get(Calendar.YEAR) == calNow.get(Calendar.YEAR) &&
                            calOrder.get(Calendar.MONTH) == calNow.get(Calendar.MONTH)
                }
            }
            ReportFilterMode.YEARLY -> {
                val calNow = Calendar.getInstance()
                allOrders.filter { order ->
                    val calOrder = Calendar.getInstance().apply { timeInMillis = order.createdAt }
                    calOrder.get(Calendar.YEAR) == calNow.get(Calendar.YEAR)
                }
            }
            ReportFilterMode.CUSTOM -> {
                val calStart = Calendar.getInstance().apply {
                    timeInMillis = startDateMillis
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                }
                val calEnd = Calendar.getInstance().apply {
                    timeInMillis = endDateMillis
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                }
                allOrders.filter { it.createdAt in calStart.timeInMillis..calEnd.timeInMillis }
            }
        }
    }

    val totalRevenue = remember(filteredOrders) { filteredOrders.sumOf { it.total } }
    val totalActualCash = remember(filteredOrders) { filteredOrders.sumOf { it.getActualCashReceived() } }
    val totalUnpaid = remember(filteredOrders) { filteredOrders.sumOf { it.getOutstandingRemaining() } }
    val totalItemCount = remember(filteredOrders) {
        filteredOrders.sumOf { order -> order.getCartItems().sumOf { it.quantity } }
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
            Icon(
                Icons.Default.Analytics,
                contentDescription = null,
                tint = Maroon40,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Laporan & Rekap Penjualan",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Maroon40
                )
                Text(
                    text = "Harian, Mingguan, Bulanan, Tahunan & Rentang Manual",
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Tab Row
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = Maroon40,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Rekap Laporan", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                icon = { Icon(Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.testTag("tab_daily_report")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Analitik Tren", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                icon = { Icon(Icons.Default.TrendingUp, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.testTag("tab_analytics_trend")
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (selectedTab == 0) {
            // REPORT REKAP TAB
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Period Mode Filter Chips
                Text("Pilih Periode Laporan:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(ReportFilterMode.values()) { mode ->
                        val isSelected = filterMode == mode
                        FilterChip(
                            selected = isSelected,
                            onClick = { filterMode = mode },
                            label = { Text(mode.label, fontSize = 11.5.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            leadingIcon = {
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Date Selector / Controls Box
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Maroon40, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Periode Aktif:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = dateLabel,
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Maroon40
                                    )
                                }
                            }

                            if (filterMode == ReportFilterMode.TODAY) {
                                Button(
                                    onClick = {
                                        val cal = Calendar.getInstance().apply { timeInMillis = singleDateMillis }
                                        DatePickerDialog(
                                            context,
                                            { _, year, month, dayOfMonth ->
                                                val picked = Calendar.getInstance().apply { set(year, month, dayOfMonth) }
                                                singleDateMillis = picked.timeInMillis
                                            },
                                            cal.get(Calendar.YEAR),
                                            cal.get(Calendar.MONTH),
                                            cal.get(Calendar.DAY_OF_MONTH)
                                        ).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Maroon40),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                    modifier = Modifier.height(34.dp).testTag("btn_pick_single_date")
                                ) {
                                    Icon(Icons.Default.EditCalendar, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Pilih Tanggal", fontSize = 11.sp)
                                }
                            }
                        }

                        // If Manual Custom Range: Show Start & End Date Pickers
                        if (filterMode == ReportFilterMode.CUSTOM) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedCard(
                                    onClick = {
                                        val cal = Calendar.getInstance().apply { timeInMillis = startDateMillis }
                                        DatePickerDialog(
                                            context,
                                            { _, year, month, dayOfMonth ->
                                                val picked = Calendar.getInstance().apply { set(year, month, dayOfMonth) }
                                                startDateMillis = picked.timeInMillis
                                            },
                                            cal.get(Calendar.YEAR),
                                            cal.get(Calendar.MONTH),
                                            cal.get(Calendar.DAY_OF_MONTH)
                                        ).show()
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text("Mulai Dari:", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(shortDateFormat.format(Date(startDateMillis)), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Maroon40)
                                    }
                                }

                                OutlinedCard(
                                    onClick = {
                                        val cal = Calendar.getInstance().apply { timeInMillis = endDateMillis }
                                        DatePickerDialog(
                                            context,
                                            { _, year, month, dayOfMonth ->
                                                val picked = Calendar.getInstance().apply { set(year, month, dayOfMonth) }
                                                endDateMillis = picked.timeInMillis
                                            },
                                            cal.get(Calendar.YEAR),
                                            cal.get(Calendar.MONTH),
                                            cal.get(Calendar.DAY_OF_MONTH)
                                        ).show()
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text("Sampai Tanggal:", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(shortDateFormat.format(Date(endDateMillis)), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Maroon40)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Summary Metric Cards
                // Row 1: Real Cash & Receivables
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        color = Color(0xFF2E7D32).copy(alpha = 0.09f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFF2E7D32).copy(alpha = 0.3f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Kas Masuk (Riil)", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = formatRupiah(totalActualCash),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1B5E20)
                            )
                            Text("DP & pelunasan diterima", fontSize = 9.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Surface(
                        color = (if (totalUnpaid > 0) Color(0xFFC62828) else Color.Gray).copy(alpha = 0.09f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, (if (totalUnpaid > 0) Color(0xFFC62828) else Color.Gray).copy(alpha = 0.3f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.PendingActions,
                                    contentDescription = null,
                                    tint = if (totalUnpaid > 0) Color(0xFFC62828) else Color.Gray,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Belum Lunas / Piutang",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (totalUnpaid > 0) Color(0xFFC62828) else Color.Gray
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = formatRupiah(totalUnpaid),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (totalUnpaid > 0) Color(0xFFB71C1C) else Color.DarkGray
                            )
                            Text("Sisa tagihan pelanggan", fontSize = 9.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Row 2: Total Omset, Transaksi & Item Terjual
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    MetricCard(
                        title = "Nilai Pesanan",
                        value = formatRupiah(totalRevenue),
                        icon = Icons.Default.MonetizationOn,
                        color = Maroon40,
                        modifier = Modifier.weight(1.2f)
                    )
                    MetricCard(
                        title = "Transaksi",
                        value = "${filteredOrders.size} Order",
                        icon = Icons.Default.Receipt,
                        color = GoldWarm,
                        modifier = Modifier.weight(0.9f)
                    )
                    MetricCard(
                        title = "Item Terjual",
                        value = "$totalItemCount pcs",
                        icon = Icons.Default.BakeryDining,
                        color = Color(0xFF00796B),
                        modifier = Modifier.weight(0.9f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Export Cards for Excel (.csv) & PDF (.pdf) with 2 Simple Buttons: Simpan & Share
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Ekspor Laporan Penjualan:",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Row 1: Excel (.csv)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    color = Color(0xFF107C41).copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Icon(
                                        Icons.Default.TableChart,
                                        contentDescription = null,
                                        tint = Color(0xFF107C41),
                                        modifier = Modifier.padding(6.dp).size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Laporan Excel (CSV)", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                                    Text("Folder: DP/CSV", fontSize = 10.sp, color = Color.Gray)
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(
                                    onClick = { viewModel.saveReportToCsv(filteredOrders, totalRevenue, dateLabel) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF107C41)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.height(34.dp).testTag("btn_save_csv")
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Simpan", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                }

                                FilledTonalButton(
                                    onClick = { viewModel.shareReportCsv(filteredOrders, totalRevenue, dateLabel) },
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = Color(0xFF107C41).copy(alpha = 0.15f),
                                        contentColor = Color(0xFF107C41)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.height(34.dp).testTag("btn_share_csv")
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Share", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                        // Row 2: PDF (.pdf)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    color = Color(0xFFD32F2F).copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Icon(
                                        Icons.Default.PictureAsPdf,
                                        contentDescription = null,
                                        tint = Color(0xFFD32F2F),
                                        modifier = Modifier.padding(6.dp).size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Laporan Dokumen PDF", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                                    Text("Folder: DP/PDF", fontSize = 10.sp, color = Color.Gray)
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(
                                    onClick = { viewModel.saveReportToPdf(filteredOrders, totalRevenue, dateLabel) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.height(34.dp).testTag("btn_save_pdf")
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Simpan", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                }

                                FilledTonalButton(
                                    onClick = { viewModel.shareReportPdf(filteredOrders, totalRevenue, dateLabel) },
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = Color(0xFFD32F2F).copy(alpha = 0.15f),
                                        contentColor = Color(0xFFD32F2F)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.height(34.dp).testTag("btn_share_pdf")
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Share", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // JSON Backup & Import for Reports / Orders
                OutlinedButton(
                    onClick = { isReportBackupDialogOpen = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Maroon40),
                    border = BorderStroke(1.5.dp, Maroon40),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("btn_report_backup_import")
                ) {
                    Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Impor & Ekspor Data Laporan (JSON)", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Transaction List Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Rincian Transaksi (${filteredOrders.size})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (filteredOrders.isNotEmpty()) {
                        Text(
                            text = "Kas Masuk: ${formatRupiah(totalActualCash)}",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (filteredOrders.isEmpty()) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(Icons.Default.Inbox, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Tidak ada transaksi pada periode ini", fontSize = 13.sp, color = Color.Gray)
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        filteredOrders.forEach { order ->
                            val actualCash = order.getActualCashReceived()
                            val unpaid = order.getOutstandingRemaining()

                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(2.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = "${order.orderNumber} • ${order.customerName}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = Maroon40
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                color = when (order.paymentStatus) {
                                                    "LUNAS" -> Color(0xFFE8F5E9)
                                                    "DP" -> Color(0xFFFFF3E0)
                                                    else -> Color(0xFFFFEBEE)
                                                },
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = order.paymentStatus,
                                                    fontSize = 9.5.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = when (order.paymentStatus) {
                                                        "LUNAS" -> Color(0xFF2E7D32)
                                                        "DP" -> Color(0xFFE65100)
                                                        else -> Color(0xFFC62828)
                                                    },
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }

                                        Text(
                                            text = formatRupiah(order.total),
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 13.5.sp,
                                            color = Maroon40
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "Ambil: ${order.pickupDate} (${order.pickupTime}) • Box: ${order.packagingType}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.weight(1f)
                                        )

                                        TextButton(
                                            onClick = { viewModel.showReceipt(order) },
                                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                                            modifier = Modifier.height(26.dp)
                                        ) {
                                            Text("Lihat Nota", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    // Payment Breakdown line
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.padding(top = 2.dp)
                                    ) {
                                        Text(
                                            text = "Kas Masuk: ${formatRupiah(actualCash)}",
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF2E7D32)
                                        )
                                        if (unpaid > 0) {
                                            Text(
                                                text = "•",
                                                fontSize = 10.5.sp,
                                                color = Color.Gray
                                            )
                                            Text(
                                                text = "Sisa Tagihan: ${formatRupiah(unpaid)}",
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFC62828)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // MONTHLY ANALYTICS & TRENDS TAB
            MonthlyAnalyticsView(allOrders = allOrders)
        }

        if (isReportBackupDialogOpen) {
            ReportsBackupRestoreDialog(
                viewModel = viewModel,
                onDismiss = { isReportBackupDialogOpen = false }
            )
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = color.copy(alpha = 0.09f),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder(),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = title, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun MonthlyAnalyticsView(allOrders: List<OrderEntity>) {
    // Generate 6 months of data
    val monthlyData = remember(allOrders) {
        val map = mutableMapOf<String, Long>()
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat("MMM yy", Locale("in", "ID"))

        // Prepare last 6 months
        val labels = mutableListOf<String>()
        for (i in 5 downTo 0) {
            val c = Calendar.getInstance().apply { add(Calendar.MONTH, -i) }
            val label = sdf.format(c.time)
            labels.add(label)
            map[label] = 0L
        }

        allOrders.forEach { order ->
            val orderCal = Calendar.getInstance().apply { timeInMillis = order.createdAt }
            val label = sdf.format(orderCal.time)
            if (map.containsKey(label)) {
                map[label] = (map[label] ?: 0L) + order.total
            }
        }

        labels.map { label -> Pair(label, map[label] ?: 0L) }
    }

    val totalLifetimeRevenue = remember(allOrders) { allOrders.sumOf { it.total } }
    val totalLifetimeCash = remember(allOrders) { allOrders.sumOf { it.getActualCashReceived() } }
    val totalLifetimeUnpaid = remember(allOrders) { allOrders.sumOf { it.getOutstandingRemaining() } }
    val totalLifetimeOrders = remember(allOrders) { allOrders.size }

    // Top 5 Best-Selling Products calculation
    val topSelling = remember(allOrders) {
        val countMap = mutableMapOf<String, Int>()
        allOrders.forEach { order ->
            order.getCartItems().forEach { item ->
                countMap[item.productName] = (countMap[item.productName] ?: 0) + item.quantity
            }
        }
        countMap.toList().sortedByDescending { it.second }.take(5)
    }

    val maxRevenue = remember(monthlyData) {
        (monthlyData.maxOfOrNull { it.second } ?: 1L).coerceAtLeast(100000L)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Analytics Summary
        Surface(
            color = Maroon40,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "TOTAL AKUMULASI KAS MASUK (RIIL)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFD9DF)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = formatRupiah(totalLifetimeCash),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Omset: ${formatRupiah(totalLifetimeRevenue)}",
                        fontSize = 11.5.sp,
                        color = Color(0xFFFFE0A4),
                        fontWeight = FontWeight.SemiBold
                    )
                    if (totalLifetimeUnpaid > 0) {
                        Text(
                            text = "Piutang: ${formatRupiah(totalLifetimeUnpaid)}",
                            fontSize = 11.5.sp,
                            color = Color(0xFFFFCDD2),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Total $totalLifetimeOrders transaksi tercatat",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Visual Monthly Sales Bar Chart
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(3.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.BarChart, contentDescription = null, tint = Maroon40, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tren Penjualan Bulanan (6 Bulan Terakhir)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Maroon40
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Canvas Bar Chart
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height - 30.dp.toPx()
                        val barWidth = (canvasWidth / (monthlyData.size * 2))

                        monthlyData.forEachIndexed { index, (label, amount) ->
                            val fraction = (amount.toFloat() / maxRevenue.toFloat()).coerceIn(0.05f, 1f)
                            val barHeight = canvasHeight * fraction
                            val x = (index * (canvasWidth / monthlyData.size)) + (barWidth / 2)
                            val y = canvasHeight - barHeight

                            // Draw Bar
                            drawRoundRect(
                                color = if (index == monthlyData.lastIndex) Maroon40 else GoldWarm.copy(alpha = 0.75f),
                                topLeft = Offset(x, y),
                                size = Size(barWidth, barHeight),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                            )
                        }
                    }

                    // Month Labels below chart
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                    ) {
                        monthlyData.forEach { (label, _) ->
                            Text(
                                text = label,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Top 5 Best Selling Products Ranking
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(3.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = GoldWarm, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Top 5 Produk Terlaris",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Maroon40
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (topSelling.isEmpty()) {
                    Text("Belum ada data penjualan", fontSize = 12.sp, color = Color.Gray)
                } else {
                    val maxSold = topSelling.maxOf { it.second }.coerceAtLeast(1)
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        topSelling.forEachIndexed { rank, (name, count) ->
                            Column {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "${rank + 1}. $name",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "$count pcs terjual",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Maroon40
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { count.toFloat() / maxSold.toFloat() },
                                    color = if (rank == 0) GoldWarm else Maroon40,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReportsBackupRestoreDialog(
    viewModel: PosViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var importJsonText by remember { mutableStateOf("") }
    var replaceAll by remember { mutableStateOf(false) }

    val exportJsonText = remember { viewModel.exportOrdersToJson() }

    Dialog(
        onDismissRequest = {
            keyboardController?.hide()
            focusManager.clearFocus()
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .padding(vertical = 20.dp)
                .imePadding()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = Maroon40)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Data Laporan (JSON)",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Maroon40
                        )
                    }

                    IconButton(onClick = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        onDismiss()
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Khusus riwayat transaksi & nota penjualan pesanan.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Tab Switcher
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Ekspor JSON", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Impor Laporan", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (selectedTab == 0) {
                    // Export Mode
                    Text(
                        text = "Data seluruh nota/transaksi siap disalin atau dibagikan:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                    ) {
                        Text(
                            text = exportJsonText,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier
                                .padding(10.dp)
                                .verticalScroll(rememberScrollState())
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                viewModel.saveOrdersBackupJson()
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Maroon40),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("btn_save_orders_json")
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Simpan", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                        }

                        FilledTonalButton(
                            onClick = {
                                viewModel.shareOrdersBackupJson()
                            },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = Maroon40.copy(alpha = 0.12f),
                                contentColor = Maroon40
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("btn_share_orders_json")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Share", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    // Import Mode
                    Text(
                        text = "Tempelkan data JSON riwayat nota/laporan di bawah ini:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = importJsonText,
                        onValueChange = { importJsonText = it },
                        placeholder = { Text("{\"version\":1, \"orders\":[...]}", fontSize = 11.sp) },
                        minLines = 6,
                        maxLines = 8,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = replaceAll,
                            onCheckedChange = { replaceAll = it }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Text("Ganti seluruh laporan saat ini (Restore)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("Jika tidak dicentang, nota baru akan digabungkan", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            if (importJsonText.isNotBlank()) {
                                viewModel.importOrdersFromJson(importJsonText, replaceAll)
                                onDismiss()
                            } else {
                                viewModel.showMessage("Silakan tempel teks JSON terlebih dahulu")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Maroon40),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                    ) {
                        Icon(Icons.Default.FileUpload, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Impor & Terapkan Laporan", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}


