package com.example.ui.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.ChocoBrown
import com.example.ui.theme.GoldWarm
import com.example.ui.theme.Maroon40
import com.example.viewmodel.PosViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutBottomSheet(
    viewModel: PosViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val cartItems by viewModel.cartItems.collectAsState()
    val subtotal by viewModel.cartSubtotal.collectAsState()
    val types by viewModel.allPackagingTypes.collectAsState()
    val variants by viewModel.allPackagingVariants.collectAsState()

    val selectedType by viewModel.selectedType.collectAsState()
    val selectedVariant by viewModel.selectedVariant.collectAsState()
    val customerName by viewModel.customerName.collectAsState()
    val pickupDate by viewModel.pickupDateString.collectAsState()
    val pickupTime by viewModel.pickupTimeString.collectAsState()
    val notes by viewModel.orderNotes.collectAsState()
    val autoPrint by viewModel.autoPrint.collectAsState()
    val paymentMethod by viewModel.paymentMethod.collectAsState()
    val amountPaid by viewModel.amountPaid.collectAsState()

    // 2-Step Flow: Step 1 = Pilih Box & Varian, Step 2 = Jadwal & Pembayaran
    var currentStep by remember { mutableIntStateOf(1) }

    // Filter variants based on selected type
    val currentVariants = remember(selectedType, variants) {
        if (selectedType == null) variants else variants.filter { it.id_tipe == selectedType?.id_tipe }
    }

    val nonParcelCount = remember(cartItems) { viewModel.getNonParcelQuantity() }
    val parcelCount = remember(cartItems) { viewModel.getParcelQuantity() }
    val boxFee = viewModel.calculateBoxFee(selectedType)
    val grandTotal = subtotal + boxFee

    // Payment Calculations
    var amountPaidInputText by remember { mutableStateOf(grandTotal.toString()) }
    LaunchedEffect(grandTotal) {
        if (amountPaidInputText.isBlank() || amountPaidInputText == "0" || amountPaidInputText == grandTotal.toString()) {
            amountPaidInputText = grandTotal.toString()
            viewModel.setAmountPaid(grandTotal)
        }
    }

    val currentPaid = amountPaidInputText.toLongOrNull() ?: 0L
    val paymentStatus = when {
        currentPaid == 0L -> "PENDING"
        currentPaid < grandTotal -> "DP"
        else -> "LUNAS"
    }
    val changeOrRemaining = when {
        currentPaid == 0L -> grandTotal
        currentPaid < grandTotal -> grandTotal - currentPaid
        else -> currentPaid - grandTotal
    }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { sheetValue ->
            // Prevent accidental swipe down gestures from closing the bottom sheet
            sheetValue != SheetValue.Hidden
        }
    )

    ModalBottomSheet(
        onDismissRequest = {
            keyboardController?.hide()
            focusManager.clearFocus()
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = Maroon40.copy(alpha = 0.35f)
            )
        },
        modifier = Modifier
            .statusBarsPadding()
            .padding(top = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header with Stepper Indicator & Explicit Close Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (currentStep == 1) Icons.Default.Inbox else Icons.Default.EventNote,
                        contentDescription = null,
                        tint = Maroon40,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (currentStep == 1) "Langkah 1: Pilih Box Kemasan" else "Langkah 2: Jadwal & Pembayaran",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Maroon40
                        )
                        Text(
                            text = if (currentStep == 1) "Sesuaikan tipe kemasan & warna box" else "Nama pemesan, jadwal ambil & rincian bayar",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        onDismiss()
                    },
                    modifier = Modifier.testTag("btn_close_checkout")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Tutup Checkout")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Step Progress Bar
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    color = if (currentStep >= 1) Maroon40 else Color.LightGray,
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(5.dp)
                ) {}
                Surface(
                    color = if (currentStep == 2) Maroon40 else Color.LightGray.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(5.dp)
                ) {}
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (currentStep == 1) {
                // ==========================================
                // STEP 1: PILIH TIPE & VARIAN BOX KEMASAN
                // ==========================================
                Text(
                    text = "1. Pilih Tipe Kemasan Box:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Maroon40
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Packaging Type Selector
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    types.forEach { type ->
                        val isSelected = selectedType?.id_tipe == type.id_tipe
                        Surface(
                            onClick = {
                                viewModel.selectPackagingType(type)
                                val newFee = viewModel.calculateBoxFee(type)
                                amountPaidInputText = (subtotal + newFee).toString()
                                viewModel.setAmountPaid(subtotal + newFee)
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) Maroon40.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (isSelected) BorderStroke(2.dp, Maroon40) else null,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("type_${type.id_tipe}")
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Icon(
                                    imageVector = if (type.harga_tambahan > 0) Icons.Default.LocalFlorist else Icons.Default.Inbox,
                                    contentDescription = null,
                                    tint = if (isSelected) Maroon40 else Color.Gray,
                                    modifier = Modifier.size(26.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = type.id_tipe,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Maroon40 else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (type.harga_tambahan == 0L) "Gratis (Rp 0)" else "+${formatRupiah(type.harga_tambahan)}/item",
                                    fontSize = 11.sp,
                                    color = if (isSelected) GoldWarm else Color.Gray
                                )
                            }
                        }
                    }
                }

                if (selectedType?.harga_tambahan ?: 0L > 0) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        color = GoldWarm.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "ℹ️ Tambahan Box Artificial: ${formatRupiah(selectedType?.harga_tambahan ?: 0L)} x $nonParcelCount item non-parcel = ${formatRupiah(boxFee)}" +
                                    if (parcelCount > 0) " (Gratis untuk $parcelCount item parcel)" else "",
                            fontSize = 11.5.sp,
                            color = Maroon40,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Packaging Variant Horizontal Cards
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "2. Pilih Varian Box (${selectedType?.id_tipe ?: "Standard"}):",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Maroon40
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(currentVariants) { variant ->
                        val isSelected = selectedVariant?.id_varian == variant.id_varian
                        Card(
                            onClick = { viewModel.selectPackagingVariant(variant) },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Maroon40.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            border = if (isSelected) BorderStroke(2.dp, Maroon40) else null,
                            modifier = Modifier
                                .width(180.dp)
                                .testTag("variant_${variant.id_varian}")
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(110.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                ) {
                                    AsyncImage(
                                        model = variant.img,
                                        contentDescription = variant.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )

                                    if (isSelected) {
                                        Surface(
                                            color = Maroon40,
                                            shape = RoundedCornerShape(bottomStart = 8.dp),
                                            modifier = Modifier.align(Alignment.TopEnd)
                                        ) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = "Terpilih",
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp).padding(2.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "Warna: ${variant.name}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Maroon40 else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = variant.desc,
                                    fontSize = 10.5.sp,
                                    maxLines = 2,
                                    lineHeight = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 2.dp)
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                OutlinedButton(
                                    onClick = { viewModel.showBoxVariantDetail(variant) },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                    modifier = Modifier.fillMaxWidth().height(28.dp)
                                ) {
                                    Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Lihat Detail Box", fontSize = 10.5.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Price Summary Card
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Subtotal Produk:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(formatRupiah(subtotal), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Biaya Box (${selectedType?.id_tipe ?: "Standard"}):", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(formatRupiah(boxFee), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("TOTAL PEMBAYARAN:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Maroon40)
                            Text(formatRupiah(grandTotal), fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = Maroon40)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Next Step Button
                Button(
                    onClick = {
                        if (selectedVariant == null) {
                            viewModel.showMessage("Silakan pilih salah satu varian box")
                        } else {
                            viewModel.setAmountPaid(currentPaid)
                            currentStep = 2
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Maroon40),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("btn_goto_step_2")
                ) {
                    Text("Lanjut ke Jadwal & Pembayaran", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                }

            } else {
                // ==========================================
                // STEP 2: JADWAL PENGAMBILAN & SISTEM PEMBAYARAN
                // ==========================================

                // Back Button to Step 1
                OutlinedButton(
                    onClick = { currentStep = 1 },
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp).testTag("btn_back_to_step_1")
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Kembali ke Pilih Box", fontSize = 11.5.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "1. Data Pemesan & Jadwal Pengambilan:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Maroon40
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Customer Name
                OutlinedTextField(
                    value = customerName,
                    onValueChange = { viewModel.setCustomerName(it) },
                    label = { Text("Nama Pelanggan *") },
                    placeholder = { Text("Contoh: Ibu Rina / Bpk. Hadi") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Maroon40) },
                    singleLine = true,
                    isError = customerName.isBlank(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_customer_name")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Date Picker Field (Tanggal, Bulan, Tahun)
                OutlinedTextField(
                    value = pickupDate,
                    onValueChange = { viewModel.setPickupDate(it) },
                    label = { Text("Tanggal Pengambilan *") },
                    leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Maroon40) },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                val cal = Calendar.getInstance()
                                DatePickerDialog(
                                    context,
                                    { _, year, month, dayOfMonth ->
                                        val pickedCal = Calendar.getInstance().apply {
                                            set(year, month, dayOfMonth)
                                        }
                                        val formatted = SimpleDateFormat("dd MMMM yyyy", Locale("in", "ID")).format(pickedCal.time)
                                        viewModel.setPickupDate(formatted)
                                    },
                                    cal.get(Calendar.YEAR),
                                    cal.get(Calendar.MONTH),
                                    cal.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            },
                            modifier = Modifier.testTag("btn_pick_date")
                        ) {
                            Icon(Icons.Default.EditCalendar, contentDescription = "Pilih Tanggal", tint = Maroon40)
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_pickup_date")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Time Picker Field (24-Hour format)
                OutlinedTextField(
                    value = pickupTime,
                    onValueChange = { viewModel.setPickupTime(it) },
                    label = { Text("Jam Pengambilan (Format 24 Jam) *") },
                    placeholder = { Text("Contoh: 14:30") },
                    leadingIcon = { Icon(Icons.Default.AccessTime, contentDescription = null, tint = Maroon40) },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                val cal = Calendar.getInstance()
                                TimePickerDialog(
                                    context,
                                    { _, hourOfDay, minute ->
                                        val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)
                                        viewModel.setPickupTime(formattedTime)
                                    },
                                    cal.get(Calendar.HOUR_OF_DAY),
                                    cal.get(Calendar.MINUTE),
                                    true // 24 jam
                                ).show()
                            },
                            modifier = Modifier.testTag("btn_pick_time")
                        ) {
                            Icon(Icons.Default.Schedule, contentDescription = "Pilih Jam", tint = Maroon40)
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_pickup_time")
                )

                // Quick preset time chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) {
                    listOf("08:00", "10:30", "14:00", "16:30", "19:00").forEach { preset ->
                        SuggestionChip(
                            onClick = { viewModel.setPickupTime(preset) },
                            label = { Text(preset, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Payment System Section
                Text(
                    text = "2. Sistem & Status Pembayaran:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Maroon40
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Payment Method Chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf("Tunai", "Transfer Bank", "QRIS").forEach { method ->
                        val isSel = paymentMethod == method
                        FilterChip(
                            selected = isSel,
                            onClick = { viewModel.setPaymentMethod(method) },
                            label = { Text(method, fontSize = 12.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Maroon40,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Amount paid input & quick buttons
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Total Tagihan:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text(formatRupiah(grandTotal), fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Maroon40)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = amountPaidInputText,
                            onValueChange = {
                                val clean = it.filter { c -> c.isDigit() }
                                amountPaidInputText = clean
                                val num = clean.toLongOrNull() ?: 0L
                                viewModel.setAmountPaid(num)
                            },
                            label = { Text("Jumlah Uang Diterima / Dibayar (Rp)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("input_amount_paid")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Quick buttons (Pas, DP 50%, Rp 0 / Belum Bayar)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick = {
                                    amountPaidInputText = grandTotal.toString()
                                    viewModel.setAmountPaid(grandTotal)
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Lunas (Pas)", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = {
                                    val dp = grandTotal / 2
                                    amountPaidInputText = dp.toString()
                                    viewModel.setAmountPaid(dp)
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("DP 50%", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = {
                                    amountPaidInputText = "0"
                                    viewModel.setAmountPaid(0L)
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Pending (Rp 0)", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Live Payment Status Card
                        Surface(
                            color = when (paymentStatus) {
                                "LUNAS" -> Color(0xFFE8F5E9)
                                "DP" -> Color(0xFFFFF3E0)
                                else -> Color(0xFFFFEBEE)
                            },
                            shape = RoundedCornerShape(8.dp),
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
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.padding(10.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "Status: $paymentStatus",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.5.sp,
                                        color = when (paymentStatus) {
                                            "LUNAS" -> Color(0xFF2E7D32)
                                            "DP" -> Color(0xFFE65100)
                                            else -> Color(0xFFC62828)
                                        }
                                    )
                                    Text(
                                        text = when (paymentStatus) {
                                            "LUNAS" -> if (changeOrRemaining > 0) "Kembalian: ${formatRupiah(changeOrRemaining)}" else "Pembayaran Uang Pas"
                                            "DP" -> "Sisa Tagihan: ${formatRupiah(changeOrRemaining)}"
                                            else -> "Belum Dibayar: ${formatRupiah(grandTotal)}"
                                        },
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Order Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { viewModel.setOrderNotes(it) },
                    label = { Text("Catatan Khusus (Opsional)") },
                    placeholder = { Text("Misal: Pita emas, kartu ucapan...") },
                    leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_order_notes")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Auto-print toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.setAutoPrint(!autoPrint) }
                        .padding(vertical = 4.dp)
                ) {
                    Checkbox(
                        checked = autoPrint,
                        onCheckedChange = { viewModel.setAutoPrint(it) },
                        modifier = Modifier.testTag("checkbox_auto_print")
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Cetak otomatis ke printer thermal Bluetooth",
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Submit Button
                Button(
                    onClick = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        viewModel.setAmountPaid(currentPaid)
                        viewModel.submitCheckout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Maroon40),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("btn_submit_order")
                ) {
                    Icon(Icons.Default.ReceiptLong, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Proses Pesanan & Buat Nota (${formatRupiah(grandTotal)})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
