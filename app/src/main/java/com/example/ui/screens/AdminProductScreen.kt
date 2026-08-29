package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.model.PackagingTypeEntity
import com.example.data.model.PackagingVariantEntity
import com.example.data.model.ProductEntity
import com.example.ui.components.StatusBadge
import com.example.ui.components.formatRupiah
import com.example.ui.theme.*
import com.example.util.AppStorageHelper
import com.example.viewmodel.PosViewModel
import kotlinx.coroutines.launch

@Composable
fun AdminProductScreen(
    viewModel: PosViewModel
) {
    val isLoggedIn by viewModel.isAdminLoggedIn.collectAsState()
    val products by viewModel.allProducts.collectAsState()
    val packagingTypes by viewModel.allPackagingTypes.collectAsState()
    val packagingVariants by viewModel.allPackagingVariants.collectAsState()
    val storeProfile by viewModel.storeProfile.collectAsState()

    var adminTab by remember { mutableIntStateOf(0) } // 0: Produk, 1: Box & Kemasan, 2: Toko & Struk

    // Product State
    var searchQuery by remember { mutableStateOf("") }
    var editingProduct by remember { mutableStateOf<ProductEntity?>(null) }
    var isAddingNewProduct by remember { mutableStateOf(false) }
    var productToDelete by remember { mutableStateOf<ProductEntity?>(null) }

    // Dynamic distinct categories from existing products
    val existingCategories = remember(products) {
        val list = products.map { it.cat.trim() }.filter { it.isNotBlank() }.distinct()
        if (list.isEmpty()) listOf("Hantaran", "Kue Basah", "Kue Kering", "Roti & Pastry", "Snack Box", "Tart & Cake")
        else list
    }

    // Packaging Type State
    var isAddingNewType by remember { mutableStateOf(false) }
    var editingType by remember { mutableStateOf<PackagingTypeEntity?>(null) }
    var typeToDelete by remember { mutableStateOf<PackagingTypeEntity?>(null) }

    // Packaging Variant State
    var selectedTypeFilter by remember { mutableStateOf<String?>(null) }
    var isAddingNewVariant by remember { mutableStateOf(false) }
    var editingVariant by remember { mutableStateOf<PackagingVariantEntity?>(null) }
    var variantToDelete by remember { mutableStateOf<PackagingVariantEntity?>(null) }

    // Other Dialogs
    var isChangePinOpen by remember { mutableStateOf(false) }
    var isBackupDialogOpen by remember { mutableStateOf(false) }

    if (!isLoggedIn) {
        AdminLoginView(
            onLogin = { pin -> viewModel.loginAdmin(pin) }
        )
    } else {
        val filteredProducts = remember(products, searchQuery) {
            if (searchQuery.isBlank()) products
            else products.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.cat.contains(searchQuery, ignoreCase = true)
            }
        }

        val filteredVariants = remember(packagingVariants, selectedTypeFilter) {
            if (selectedTypeFilter == null) packagingVariants
            else packagingVariants.filter { it.id_tipe.equals(selectedTypeFilter, ignoreCase = true) }
        }

        Scaffold(
            floatingActionButton = {
                if (adminTab == 0 || adminTab == 1) {
                    FloatingActionButton(
                        onClick = {
                            if (adminTab == 0) {
                                isAddingNewProduct = true
                            } else {
                                isAddingNewVariant = true
                            }
                        },
                        containerColor = ChocoBrown,
                        contentColor = Color.White,
                        modifier = Modifier.testTag("fab_admin_add")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = if (adminTab == 0) "Tambah Produk" else "Tambah Varian Box"
                        )
                    }
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                // Header Bar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = ChocoBrown,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Panel Kelola Admin",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = ChocoBrown
                            )
                            Text(
                                text = "Katalog kue, box, profil toko & struk",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = { isBackupDialogOpen = true },
                            modifier = Modifier.testTag("btn_backup_json")
                        ) {
                            Icon(Icons.Default.CloudSync, contentDescription = "Backup/Restore JSON", tint = ChocoBrown)
                        }
                        IconButton(
                            onClick = { isChangePinOpen = true },
                            modifier = Modifier.testTag("btn_change_pin")
                        ) {
                            Icon(Icons.Default.Key, contentDescription = "Ganti PIN", tint = ChocoBrown)
                        }
                        IconButton(
                            onClick = { viewModel.logoutAdmin() },
                            modifier = Modifier.testTag("btn_logout_admin")
                        ) {
                            Icon(Icons.Default.Logout, contentDescription = "Logout", tint = Color(0xFFC62828))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Primary Admin Tabs: Produk vs Box Kemasan vs Toko & Struk
                TabRow(
                    selectedTabIndex = adminTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = ChocoBrown,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = adminTab == 0,
                        onClick = { adminTab = 0 },
                        text = { Text("Katalog (${products.size})", fontWeight = FontWeight.Bold, fontSize = 11.5.sp) },
                        icon = { Icon(Icons.Default.BakeryDining, contentDescription = null, modifier = Modifier.size(17.dp)) },
                        modifier = Modifier.testTag("tab_admin_products")
                    )
                    Tab(
                        selected = adminTab == 1,
                        onClick = { adminTab = 1 },
                        text = { Text("Box Kemasan", fontWeight = FontWeight.Bold, fontSize = 11.5.sp) },
                        icon = { Icon(Icons.Default.Inventory2, contentDescription = null, modifier = Modifier.size(17.dp)) },
                        modifier = Modifier.testTag("tab_admin_packaging")
                    )
                    Tab(
                        selected = adminTab == 2,
                        onClick = { adminTab = 2 },
                        text = { Text("Toko & Struk", fontWeight = FontWeight.Bold, fontSize = 11.5.sp) },
                        icon = { Icon(Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(17.dp)) },
                        modifier = Modifier.testTag("tab_admin_store_settings")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (adminTab == 0) {
                    // ================= PRODUCT TAB =================
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Cari produk untuk diedit...", fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = ChocoBrown) },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_search_admin_product")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Daftar Produk (${filteredProducts.size})",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ChocoBrown
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedButton(
                                onClick = { isBackupDialogOpen = true },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Export / Import", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { isAddingNewProduct = true },
                                colors = ButtonDefaults.buttonColors(containerColor = ChocoBrown),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier
                                    .height(32.dp)
                                    .testTag("btn_add_product_top")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Tambah", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 96.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        items(filteredProducts, key = { it.id }) { product ->
                            AdminProductRow(
                                product = product,
                                onEdit = { editingProduct = product },
                                onDelete = { productToDelete = product }
                            )
                        }
                    }
                } else if (adminTab == 1) {
                    // ================= BOX & PACKAGING TAB =================
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(bottom = 96.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        // Section 1: Packaging Types
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column {
                                    Text(
                                        text = "1. Tipe Box / Kemasan (${packagingTypes.size})",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ChocoBrown
                                    )
                                    Text(
                                        text = "Kategori tipe box dan biaya tambahan kemasan",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Button(
                                    onClick = { isAddingNewType = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = ChocoBrown),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(34.dp).testTag("btn_add_packaging_type")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Tambah Tipe", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // List of Packaging Types
                        items(packagingTypes, key = { it.id_tipe }) { typeEntity ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(2.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Surface(
                                            color = ChocoBrown.copy(alpha = 0.12f),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(Icons.Default.Inbox, contentDescription = null, tint = ChocoBrown, modifier = Modifier.size(20.dp))
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "Tipe: ${typeEntity.id_tipe}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.5.sp
                                            )
                                            Text(
                                                text = if (typeEntity.harga_tambahan > 0) "+${formatRupiah(typeEntity.harga_tambahan)}" else "Gratis (Termasuk)",
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (typeEntity.harga_tambahan > 0) GoldWarm else Color(0xFF2E7D32)
                                            )
                                        }
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        IconButton(
                                            onClick = { editingType = typeEntity },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit Tipe", tint = ChocoBrown, modifier = Modifier.size(18.dp))
                                        }
                                        IconButton(
                                            onClick = { typeToDelete = typeEntity },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.DeleteOutline, contentDescription = "Hapus Tipe", tint = Color(0xFFC62828), modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }

                        // Section 2: Packaging Variants
                        item {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column {
                                    Text(
                                        text = "2. Varian Model Box (${packagingVariants.size})",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ChocoBrown
                                    )
                                    Text(
                                        text = "Foto, motif, deskripsi varian box kemasan",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Button(
                                    onClick = { isAddingNewVariant = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = ChocoBrown),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(34.dp).testTag("btn_add_packaging_variant")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Tambah Varian", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Type Filter Chips for variants
                        item {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                item {
                                    FilterChip(
                                        selected = selectedTypeFilter == null,
                                        onClick = { selectedTypeFilter = null },
                                        label = { Text("Semua Tipe (${packagingVariants.size})", fontSize = 11.sp) }
                                    )
                                }
                                items(packagingTypes) { pt ->
                                    val count = packagingVariants.count { it.id_tipe.equals(pt.id_tipe, ignoreCase = true) }
                                    FilterChip(
                                        selected = selectedTypeFilter.equals(pt.id_tipe, ignoreCase = true),
                                        onClick = {
                                            selectedTypeFilter = if (selectedTypeFilter == pt.id_tipe) null else pt.id_tipe
                                        },
                                        label = { Text("${pt.id_tipe} ($count)", fontSize = 11.sp) }
                                    )
                                }
                            }
                        }

                        // List of Packaging Variants
                        items(filteredVariants, key = { it.id_varian }) { variant ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(2.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(10.dp)
                                ) {
                                    AsyncImage(
                                        model = variant.img,
                                        contentDescription = variant.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(60.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                    )

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = variant.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                color = ChocoBrown.copy(alpha = 0.1f),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = variant.id_tipe,
                                                    fontSize = 9.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = ChocoBrown,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                        if (variant.desc.isNotBlank()) {
                                            Text(
                                                text = variant.desc,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                        IconButton(
                                            onClick = { editingVariant = variant },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit Varian", tint = ChocoBrown, modifier = Modifier.size(18.dp))
                                        }
                                        IconButton(
                                            onClick = { variantToDelete = variant },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.DeleteOutline, contentDescription = "Hapus Varian", tint = Color(0xFFC62828), modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else if (adminTab == 2) {
                    // ================= STORE & RECEIPT SETTINGS TAB =================
                    AdminStoreSettingsSection(
                        viewModel = viewModel,
                        storeProfile = storeProfile,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                }
            }
        }

        // ================= PRODUCT DIALOGS =================
        if (isAddingNewProduct) {
            ProductEditDialog(
                product = null,
                existingCategories = existingCategories,
                onDismiss = { isAddingNewProduct = false },
                onSave = { name, price, cat, img, desc, best, isNew, promo ->
                    viewModel.addProduct(name, price, cat, img, desc, best, isNew, promo)
                    isAddingNewProduct = false
                }
            )
        }

        editingProduct?.let { prod ->
            ProductEditDialog(
                product = prod,
                existingCategories = existingCategories,
                onDismiss = { editingProduct = null },
                onSave = { name, price, cat, img, desc, best, isNew, promo ->
                    viewModel.updateProduct(
                        prod.copy(
                            name = name,
                            price = price,
                            cat = cat,
                            img = img,
                            desc = desc,
                            bestseller = best,
                            isNew = isNew,
                            isPromo = promo
                        )
                    )
                    editingProduct = null
                }
            )
        }

        productToDelete?.let { prod ->
            AlertDialog(
                onDismissRequest = { productToDelete = null },
                icon = { Icon(Icons.Default.DeleteForever, contentDescription = null, tint = Color(0xFFC62828)) },
                title = { Text("Hapus Produk?") },
                text = { Text("Apakah Anda yakin ingin menghapus '${prod.name}' dari katalog?") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteProduct(prod)
                            productToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                        modifier = Modifier.testTag("btn_confirm_delete_product")
                    ) {
                        Text("Hapus")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { productToDelete = null }) {
                        Text("Batal")
                    }
                }
            )
        }

        // ================= PACKAGING TYPE DIALOGS =================
        if (isAddingNewType) {
            PackagingTypeEditDialog(
                typeEntity = null,
                onDismiss = { isAddingNewType = false },
                onSave = { name, additionalPrice ->
                    viewModel.addPackagingType(name, additionalPrice)
                    isAddingNewType = false
                }
            )
        }

        editingType?.let { typeEntity ->
            PackagingTypeEditDialog(
                typeEntity = typeEntity,
                onDismiss = { editingType = null },
                onSave = { name, additionalPrice ->
                    viewModel.updatePackagingType(
                        typeEntity.copy(id_tipe = name, harga_tambahan = additionalPrice)
                    )
                    editingType = null
                }
            )
        }

        typeToDelete?.let { typeEntity ->
            AlertDialog(
                onDismissRequest = { typeToDelete = null },
                icon = { Icon(Icons.Default.DeleteForever, contentDescription = null, tint = Color(0xFFC62828)) },
                title = { Text("Hapus Tipe Box?") },
                text = { Text("Apakah Anda yakin ingin menghapus tipe box '${typeEntity.id_tipe}'? Varian yang menggunakan tipe ini mungkin tidak akan terhubung.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deletePackagingType(typeEntity)
                            typeToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                        modifier = Modifier.testTag("btn_confirm_delete_packaging_type")
                    ) {
                        Text("Hapus")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { typeToDelete = null }) {
                        Text("Batal")
                    }
                }
            )
        }

        // ================= PACKAGING VARIANT DIALOGS =================
        if (isAddingNewVariant) {
            PackagingVariantEditDialog(
                variant = null,
                types = packagingTypes,
                onDismiss = { isAddingNewVariant = false },
                onSave = { id_tipe, name, img, desc ->
                    viewModel.addPackagingVariant(id_tipe, name, img, desc)
                    isAddingNewVariant = false
                }
            )
        }

        editingVariant?.let { variant ->
            PackagingVariantEditDialog(
                variant = variant,
                types = packagingTypes,
                onDismiss = { editingVariant = null },
                onSave = { id_tipe, name, img, desc ->
                    viewModel.updatePackagingVariant(
                        variant.copy(id_tipe = id_tipe, name = name, img = img, desc = desc)
                    )
                    editingVariant = null
                }
            )
        }

        variantToDelete?.let { variant ->
            AlertDialog(
                onDismissRequest = { variantToDelete = null },
                icon = { Icon(Icons.Default.DeleteForever, contentDescription = null, tint = Color(0xFFC62828)) },
                title = { Text("Hapus Varian Box?") },
                text = { Text("Apakah Anda yakin ingin menghapus varian box '${variant.name}'?") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deletePackagingVariant(variant)
                            variantToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                        modifier = Modifier.testTag("btn_confirm_delete_variant")
                    ) {
                        Text("Hapus")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { variantToDelete = null }) {
                        Text("Batal")
                    }
                }
            )
        }

        // Change PIN Dialog
        if (isChangePinOpen) {
            ChangePinDialog(
                onDismiss = { isChangePinOpen = false },
                onSave = { oldPin, newPin ->
                    val success = viewModel.authManager.changePin(oldPin, newPin)
                    if (success) {
                        viewModel.showMessage("PIN Admin berhasil diubah")
                        isChangePinOpen = false
                    } else {
                        viewModel.showMessage("PIN Lama salah!")
                    }
                }
            )
        }

        // JSON Backup / Restore Dialog
        if (isBackupDialogOpen) {
            BackupRestoreDialog(
                viewModel = viewModel,
                onDismiss = { isBackupDialogOpen = false }
            )
        }
    }
}

@Composable
fun PackagingTypeEditDialog(
    typeEntity: PackagingTypeEntity?,
    onDismiss: () -> Unit,
    onSave: (name: String, additionalPrice: Long) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    var name by remember { mutableStateOf(typeEntity?.id_tipe ?: "") }
    var priceText by remember { mutableStateOf((typeEntity?.harga_tambahan ?: 0L).toString()) }

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
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .imePadding()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = if (typeEntity == null) "Tambah Tipe Box Baru" else "Edit Tipe Box",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = ChocoBrown
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Tipe Box (misal: Standard, Artificial, Mika)") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it.filter { c -> c.isDigit() } },
                    label = { Text("Harga Tambahan Box (Rp)") },
                    placeholder = { Text("0 jika gratis") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        onDismiss()
                    }) {
                        Text("Batal")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                                val price = priceText.toLongOrNull() ?: 0L
                                onSave(name.trim(), price)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ChocoBrown),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Simpan Tipe")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackagingVariantEditDialog(
    variant: PackagingVariantEntity?,
    types: List<PackagingTypeEntity>,
    onDismiss: () -> Unit,
    onSave: (id_tipe: String, name: String, img: String, desc: String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    var selectedTypeId by remember {
        mutableStateOf(variant?.id_tipe ?: types.firstOrNull()?.id_tipe ?: "Standard")
    }
    var name by remember { mutableStateOf(variant?.name ?: "") }
    var imageUrl by remember { mutableStateOf(variant?.img ?: "") }
    var description by remember { mutableStateOf(variant?.desc ?: "") }
    var expandedTypeDropdown by remember { mutableStateOf(false) }
    var isPickingImage by remember { mutableStateOf(false) }
    var showUrlInput by remember { mutableStateOf(imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch {
                isPickingImage = true
                val result = AppStorageHelper.saveImageFromUri(context, it, "box")
                result.onSuccess { path ->
                    imageUrl = path
                }
                isPickingImage = false
            }
        }
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
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .imePadding()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (variant == null) "Tambah Varian Box Baru" else "Edit Varian Box",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = ChocoBrown
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Image Preview & Picker Section
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.LightGray.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (imageUrl.isNotBlank()) {
                                AsyncImage(
                                    model = imageUrl,
                                    contentDescription = "Preview Box",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Inventory2, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(36.dp))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Belum ada gambar box", fontSize = 12.sp, color = Color.Gray)
                                }
                            }

                            if (isPickingImage) {
                                Surface(
                                    color = Color.Black.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(28.dp))
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { imagePickerLauncher.launch("image/*") },
                                colors = ButtonDefaults.buttonColors(containerColor = ChocoBrown),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Pilih dari Galeri", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { showUrlInput = !showUrlInput },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (showUrlInput) "Tutup URL" else "Link URL", fontSize = 11.sp)
                            }

                            if (imageUrl.isNotBlank()) {
                                IconButton(
                                    onClick = { imageUrl = "" },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Hapus Gambar", tint = Maroon40)
                                }
                            }
                        }

                        if (showUrlInput) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = imageUrl,
                                onValueChange = { imageUrl = it },
                                label = { Text("Link URL Gambar Web") },
                                placeholder = { Text("https://...") },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tipe Box Dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedTypeDropdown,
                    onExpandedChange = { expandedTypeDropdown = !expandedTypeDropdown }
                ) {
                    OutlinedTextField(
                        value = selectedTypeId,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Pilih Tipe Box") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTypeDropdown) },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedTypeDropdown,
                        onDismissRequest = { expandedTypeDropdown = false }
                    ) {
                        types.forEach { pt ->
                            DropdownMenuItem(
                                text = { Text("${pt.id_tipe} (+${formatRupiah(pt.harga_tambahan)})") },
                                onClick = {
                                    selectedTypeId = pt.id_tipe
                                    expandedTypeDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Varian Model Box (misal: Box Pink Gold)") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Deskripsi Varian / Ukuran / Hiasan") },
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        onDismiss()
                    }) {
                        Text("Batal")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank() && selectedTypeId.isNotBlank()) {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                                onSave(selectedTypeId.trim(), name.trim(), imageUrl.trim(), description.trim())
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ChocoBrown),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Simpan Varian")
                    }
                }
            }
        }
    }
}

@Composable
fun AdminLoginView(onLogin: (String) -> Unit) {
    var pin by remember { mutableStateOf("") }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Surface(
                    color = Maroon40.copy(alpha = 0.12f),
                    shape = CircleShape,
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = Maroon40,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Akses Khusus Admin",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Maroon40
                )
                Text(
                    text = "Masukkan PIN 4 digit untuk kelola produk\n(Default PIN: 1234)",
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(20.dp))

                // PIN Display Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(vertical = 10.dp)
                ) {
                    for (i in 0 until 4) {
                        val isFilled = i < pin.length
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(if (isFilled) Maroon40 else Color.LightGray)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Numeric Keypad
                val keys = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("C", "0", "OK")
                )

                keys.forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        row.forEach { key ->
                            Surface(
                                onClick = {
                                    when (key) {
                                        "C" -> pin = ""
                                        "OK" -> {
                                            if (pin.length == 4) onLogin(pin)
                                        }
                                        else -> {
                                            if (pin.length < 4) {
                                                pin += key
                                                if (pin.length == 4) onLogin(pin)
                                            }
                                        }
                                    }
                                },
                                shape = CircleShape,
                                color = if (key == "OK") Maroon40 else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .size(56.dp)
                                    .testTag("keypad_$key")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = key,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (key == "OK") Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminProductRow(
    product: ProductEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(10.dp)
        ) {
            AsyncImage(
                model = product.img,
                contentDescription = product.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp
                )
                Text(
                    text = "${formatRupiah(product.price)} • ${product.cat}",
                    fontSize = 12.sp,
                    color = GoldWarm,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    if (product.bestseller) StatusBadge("Favorit", GoldWarm, Color.White)
                    if (product.isPromo) StatusBadge("Promo", WarningOrange, Color.White)
                    if (product.isNew) StatusBadge("Baru", SuccessGreen, Color.White)
                    if (product.isOutOfStock) StatusBadge("Habis", OutOfStockRed, Color.White)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.testTag("btn_edit_product_${product.id}")
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Maroon40)
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("btn_delete_product_${product.id}")
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFC62828))
                }
            }
        }
    }
}

@Composable
fun ProductEditDialog(
    product: ProductEntity?,
    existingCategories: List<String> = listOf("Hantaran", "Kue Basah", "Kue Kering", "Roti & Pastry", "Snack Box", "Tart & Cake"),
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        price: Long,
        category: String,
        imageUrl: String,
        desc: String,
        bestseller: Boolean,
        isNew: Boolean,
        isPromo: Boolean
    ) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    var name by remember { mutableStateOf(product?.name ?: "") }
    var priceText by remember { mutableStateOf(product?.price?.toString() ?: "") }
    var category by remember { mutableStateOf(product?.cat ?: existingCategories.firstOrNull() ?: "Hantaran") }
    var customCategory by remember { mutableStateOf("") }
    var isCustomCategory by remember { mutableStateOf(product != null && product.cat !in existingCategories && product.cat.isNotBlank()) }
    var imageUrl by remember { mutableStateOf(product?.img ?: "https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/logo.png") }
    var desc by remember { mutableStateOf(product?.desc ?: "") }
    var bestseller by remember { mutableStateOf(product?.bestseller ?: false) }
    var isNew by remember { mutableStateOf(product?.isNew ?: false) }
    var isPromo by remember { mutableStateOf(product?.isPromo ?: false) }

    var isPickingImage by remember { mutableStateOf(false) }
    var showUrlInput by remember { mutableStateOf(imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch {
                isPickingImage = true
                val result = AppStorageHelper.saveImageFromUri(context, it, "product")
                result.onSuccess { path ->
                    imageUrl = path
                }
                isPickingImage = false
            }
        }
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
                .fillMaxWidth(0.92f)
                .padding(vertical = 12.dp)
                .imePadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Text(
                    text = if (product == null) "Tambah Produk Baru" else "Edit Produk",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Maroon40
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Image Preview & Local / Web Picker
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.LightGray.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (imageUrl.isNotBlank()) {
                                AsyncImage(
                                    model = imageUrl,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Image, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(36.dp))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Belum ada gambar", fontSize = 12.sp, color = Color.Gray)
                                }
                            }

                            if (isPickingImage) {
                                Surface(
                                    color = Color.Black.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(28.dp))
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { imagePickerLauncher.launch("image/*") },
                                colors = ButtonDefaults.buttonColors(containerColor = Maroon40),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Pilih dari Galeri", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { showUrlInput = !showUrlInput },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (showUrlInput) "Tutup URL" else "Link URL", fontSize = 11.sp)
                            }

                            if (imageUrl.isNotBlank()) {
                                IconButton(
                                    onClick = { imageUrl = "" },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Hapus Gambar", tint = Maroon40)
                                }
                            }
                        }

                        if (showUrlInput) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = imageUrl,
                                onValueChange = { imageUrl = it },
                                label = { Text("Link URL Gambar Web") },
                                placeholder = { Text("https://...") },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Produk *") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Price
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("Harga (Rp) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Category Section (Dynamic chips + Custom input option)
                Text(
                    text = "Kategori Produk *",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Maroon40
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Existing categories chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(existingCategories) { catItem ->
                        val isSelected = !isCustomCategory && category.equals(catItem, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                isCustomCategory = false
                                category = catItem
                            },
                            label = { Text(catItem, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Maroon40,
                                selectedLabelColor = Color.White
                            )
                        )
                    }

                    item {
                        FilterChip(
                            selected = isCustomCategory,
                            onClick = {
                                isCustomCategory = true
                                if (customCategory.isBlank() && category.isNotBlank() && category !in existingCategories) {
                                    customCategory = category
                                }
                            },
                            label = { Text("+ Kategori Lain", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Maroon40,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                if (isCustomCategory) {
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = customCategory,
                        onValueChange = {
                            customCategory = it
                            category = it
                        },
                        label = { Text("Ketik Nama Kategori Baru") },
                        placeholder = { Text("misal: Minuman, Pastry Special") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Description
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Deskripsi Produk") },
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Badges toggles (Favorit, Promo, Baru - bukan kategori)
                Text(
                    text = "Label Badge (Opsional):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Maroon40
                )
                Text(
                    text = "Tanda visual di kartu katalog (bukan kategori)",
                    fontSize = 10.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { bestseller = !bestseller }
                    ) {
                        Checkbox(checked = bestseller, onCheckedChange = { bestseller = it })
                        Text("Favorit", fontSize = 12.sp)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { isPromo = !isPromo }
                    ) {
                        Checkbox(checked = isPromo, onCheckedChange = { isPromo = it })
                        Text("Promo", fontSize = 12.sp)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { isNew = !isNew }
                    ) {
                        Checkbox(checked = isNew, onCheckedChange = { isNew = it })
                        Text("Baru", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Batal")
                    }

                    Button(
                        onClick = {
                            val price = priceText.toLongOrNull() ?: 0L
                            val finalCat = if (isCustomCategory) customCategory.trim() else category.trim()
                            if (name.isNotBlank() && price > 0 && finalCat.isNotBlank()) {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                                onSave(name.trim(), price, finalCat, imageUrl.trim(), desc.trim(), bestseller, isNew, isPromo)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Maroon40),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Simpan")
                    }
                }
            }
        }
    }
}

@Composable
fun ChangePinDialog(
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    var oldPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = {
            keyboardController?.hide()
            focusManager.clearFocus()
            onDismiss()
        },
        title = { Text("Ganti PIN Admin") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.imePadding()
            ) {
                OutlinedTextField(
                    value = oldPin,
                    onValueChange = { if (it.length <= 4) oldPin = it },
                    label = { Text("PIN Lama") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = newPin,
                    onValueChange = { if (it.length <= 4) newPin = it },
                    label = { Text("PIN Baru (4 Digit)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (oldPin.isNotBlank() && newPin.length == 4) {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        onSave(oldPin, newPin)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ChocoBrown)
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                keyboardController?.hide()
                focusManager.clearFocus()
                onDismiss()
            }) {
                Text("Batal")
            }
        }
    )
}

@Composable
fun BackupRestoreDialog(
    viewModel: PosViewModel,
    onDismiss: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Export, 1 = Import
    val exportJsonText = remember { viewModel.exportCatalogToJson() }
    var importJsonText by remember { mutableStateOf("") }
    var replaceAll by remember { mutableStateOf(true) }

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
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(10.dp),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 12.dp)
                .imePadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CloudSync, contentDescription = null, tint = ChocoBrown)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Master Data (Katalog & Box)",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = ChocoBrown
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
                    text = "Khusus master katalog, varian box, dan profil toko. Tidak mencakup riwayat transaksi/laporan.",
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
                        text = { Text("Ekspor Data JSON", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Impor / Pulihkan", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (selectedTab == 0) {
                    // Export Mode
                    Text(
                        text = "Data Katalog & Box siap disalin / dibagikan:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                    ) {
                        Text(
                            text = exportJsonText,
                            fontSize = 10.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
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
                                viewModel.saveCatalogBackupJson()
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ChocoBrown),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("btn_save_catalog_json")
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Simpan", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                        }

                        FilledTonalButton(
                            onClick = {
                                viewModel.shareCatalogBackupJson()
                            },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = ChocoBrown.copy(alpha = 0.12f),
                                contentColor = ChocoBrown
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("btn_share_catalog_json")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Share", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    // Import Mode
                    Text(
                        text = "Tempelkan data JSON hasil backup di bawah ini:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = importJsonText,
                        onValueChange = { importJsonText = it },
                        placeholder = { Text("{\"version\":1, \"products\":[...], \"packaging_types\":[...]}", fontSize = 11.sp) },
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
                            Text("Ganti seluruh data saat ini (Restore penuh)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("Jika tidak dicentang, data baru akan digabungkan", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            if (importJsonText.isNotBlank()) {
                                viewModel.importCatalogFromJson(importJsonText, replaceAll)
                                onDismiss()
                            } else {
                                viewModel.showMessage("Silakan tempel teks JSON terlebih dahulu")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ChocoBrown),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                    ) {
                        Icon(Icons.Default.FileUpload, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Impor & Terapkan Data", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminStoreSettingsSection(
    viewModel: PosViewModel,
    storeProfile: com.example.data.model.StoreProfile,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    var name by remember(storeProfile) { mutableStateOf(storeProfile.name) }
    var tagline by remember(storeProfile) { mutableStateOf(storeProfile.tagline) }
    var address by remember(storeProfile) { mutableStateOf(storeProfile.address) }
    var whatsapp by remember(storeProfile) { mutableStateOf(storeProfile.whatsapp) }
    var instagram by remember(storeProfile) { mutableStateOf(storeProfile.instagram) }
    var tiktok by remember(storeProfile) { mutableStateOf(storeProfile.tiktok) }
    var receiptGreeting by remember(storeProfile) { mutableStateOf(storeProfile.receiptGreeting) }
    var showReceiptGreeting by remember(storeProfile) { mutableStateOf(storeProfile.showReceiptGreeting) }
    var showSocialMedia by remember(storeProfile) { mutableStateOf(storeProfile.showSocialMedia) }
    var showNotesOnReceipt by remember(storeProfile) { mutableStateOf(storeProfile.showNotesOnReceipt) }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp)
    ) {
        // Section 1: Profil Toko & Kontak
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Store,
                        contentDescription = null,
                        tint = ChocoBrown,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Profil Toko & Kontak",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = ChocoBrown
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Toko") },
                    placeholder = { Text("Contoh: DJANDES") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_store_name")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = tagline,
                    onValueChange = { tagline = it },
                    label = { Text("Slogan / Tagline") },
                    placeholder = { Text("Contoh: SWEET & SAVOURY") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_store_tagline")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Alamat Lengkap Toko") },
                    placeholder = { Text("Contoh: Jl. Anggrek RT 004/RW 013, Desa Tegalrejo...") },
                    minLines = 2,
                    maxLines = 3,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_store_address")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = whatsapp,
                    onValueChange = { whatsapp = it },
                    label = { Text("Nomor WhatsApp (Tampil di Nota & CS)") },
                    placeholder = { Text("0813-3456-7890") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = ChocoBrown) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_store_whatsapp")
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = instagram,
                        onValueChange = { instagram = it },
                        label = { Text("Instagram") },
                        placeholder = { Text("djandes.official") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = tiktok,
                        onValueChange = { tiktok = it },
                        label = { Text("TikTok") },
                        placeholder = { Text("djandes_blitar") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section 2: Kustomisasi Footer & Ucapan Struk Nota
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.ReceiptLong,
                        contentDescription = null,
                        tint = ChocoBrown,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Kustomisasi Struk Nota",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = ChocoBrown
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Toggle 1: Tampilkan Ucapan
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showReceiptGreeting = !showReceiptGreeting }
                        .padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Tampilkan Ucapan di Bawah Struk",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Menampilkan kalimat penutup terima kasih di bagian bawah nota cetak",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = showReceiptGreeting,
                        onCheckedChange = { showReceiptGreeting = it },
                        modifier = Modifier.testTag("switch_show_greeting")
                    )
                }

                if (showReceiptGreeting) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = receiptGreeting,
                        onValueChange = { receiptGreeting = it },
                        label = { Text("Teks Ucapan Bawah Struk") },
                        placeholder = { Text("Terima kasih atas pesanan Anda!\nSimpan nota ini saat pengambilan.") },
                        minLines = 2,
                        maxLines = 4,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_receipt_greeting")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(12.dp))

                // Toggle 2: Tampilkan Media Sosial
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showSocialMedia = !showSocialMedia }
                        .padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Tampilkan Akun Sosmed di Struk",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Menampilkan Instagram & TikTok toko pada footer nota",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = showSocialMedia,
                        onCheckedChange = { showSocialMedia = it },
                        modifier = Modifier.testTag("switch_show_social")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(12.dp))

                // Toggle 3: Tampilkan/Sembunyikan Catatan di Struk & Nota
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showNotesOnReceipt = !showNotesOnReceipt }
                        .padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Tampilkan Catatan di Struk / Nota",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Jika dinonaktifkan, catatan khusus pesanan akan disembunyikan dari struk printer thermal dan nota gambar",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = showNotesOnReceipt,
                        onCheckedChange = { showNotesOnReceipt = it },
                        modifier = Modifier.testTag("switch_show_notes_receipt")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Save Settings Button
        Button(
            onClick = {
                keyboardController?.hide()
                focusManager.clearFocus()
                val updatedProfile = storeProfile.copy(
                    name = if (name.isNotBlank()) name.trim() else storeProfile.name,
                    tagline = tagline.trim(),
                    address = address.trim(),
                    whatsapp = whatsapp.trim(),
                    instagram = instagram.trim(),
                    tiktok = tiktok.trim(),
                    receiptGreeting = receiptGreeting.trim(),
                    showReceiptGreeting = showReceiptGreeting,
                    showSocialMedia = showSocialMedia,
                    showNotesOnReceipt = showNotesOnReceipt
                )
                viewModel.updateStoreProfile(updatedProfile)
            },
            colors = ButtonDefaults.buttonColors(containerColor = ChocoBrown),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("btn_save_store_settings")
        ) {
            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Simpan Pengaturan Toko & Struk",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
