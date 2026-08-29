package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.repository.PosRepository
import com.example.util.AuthManager
import com.example.util.BluetoothPrinterHelper
import com.example.util.ExportHelper
import com.example.util.ReceiptImageHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class PosViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application, viewModelScope)
    val repository = PosRepository(db.productDao(), db.packagingDao(), db.orderDao())
    val printerHelper = BluetoothPrinterHelper(application)
    val authManager = AuthManager(application)

    val storeProfileManager = com.example.util.StoreProfileManager(application)
    private val _storeProfile = MutableStateFlow(storeProfileManager.getStoreProfile())
    val storeProfile: StateFlow<StoreProfile> = _storeProfile.asStateFlow()

    fun updateStoreProfile(profile: StoreProfile) {
        storeProfileManager.saveStoreProfile(profile)
        _storeProfile.value = profile
        showMessage("Pengaturan toko & struk berhasil disimpan")
    }

    // Products & Catalogs
    val allProducts: StateFlow<List<ProductEntity>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPackagingTypes: StateFlow<List<PackagingTypeEntity>> = repository.allPackagingTypes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPackagingVariants: StateFlow<List<PackagingVariantEntity>> = repository.allPackagingVariants
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allOrders: StateFlow<List<OrderEntity>> = repository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search & Filter
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("Semua")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // Cart State
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    val cartTotalCount = cartItems.map { list -> list.sumOf { it.quantity } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val cartSubtotal = cartItems.map { list -> list.sumOf { it.subtotal } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    // Full Screen Image Viewer State: (imageUrl, title, subtitle)
    private val _fullScreenImageView = MutableStateFlow<Triple<String, String, String>?>(null)
    val fullScreenImageView: StateFlow<Triple<String, String, String>?> = _fullScreenImageView.asStateFlow()

    // Dialog & Flow States
    private val _selectedProductDetail = MutableStateFlow<ProductEntity?>(null)
    val selectedProductDetail: StateFlow<ProductEntity?> = _selectedProductDetail.asStateFlow()

    private val _selectedBoxVariantDetail = MutableStateFlow<PackagingVariantEntity?>(null)
    val selectedBoxVariantDetail: StateFlow<PackagingVariantEntity?> = _selectedBoxVariantDetail.asStateFlow()

    private val _editingOrder = MutableStateFlow<OrderEntity?>(null)
    val editingOrder: StateFlow<OrderEntity?> = _editingOrder.asStateFlow()

    private val _isCheckoutOpen = MutableStateFlow(false)
    val isCheckoutOpen: StateFlow<Boolean> = _isCheckoutOpen.asStateFlow()

    private val _activeReceiptOrder = MutableStateFlow<OrderEntity?>(null)
    val activeReceiptOrder: StateFlow<OrderEntity?> = _activeReceiptOrder.asStateFlow()

    private val _isReceiptOpen = MutableStateFlow(false)
    val isReceiptOpen: StateFlow<Boolean> = _isReceiptOpen.asStateFlow()

    private val _isPrinting = MutableStateFlow(false)
    val isPrinting: StateFlow<Boolean> = _isPrinting.asStateFlow()

    private val _receiptFeedback = MutableStateFlow<String?>(null)
    val receiptFeedback: StateFlow<String?> = _receiptFeedback.asStateFlow()

    private val _isPrinterSettingsOpen = MutableStateFlow(false)
    val isPrinterSettingsOpen: StateFlow<Boolean> = _isPrinterSettingsOpen.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()
    private var snackbarJob: kotlinx.coroutines.Job? = null

    // Checkout Fields
    private val _selectedType = MutableStateFlow<PackagingTypeEntity?>(null)
    val selectedType: StateFlow<PackagingTypeEntity?> = _selectedType.asStateFlow()

    private val _selectedVariant = MutableStateFlow<PackagingVariantEntity?>(null)
    val selectedVariant: StateFlow<PackagingVariantEntity?> = _selectedVariant.asStateFlow()

    private val _customerName = MutableStateFlow("")
    val customerName: StateFlow<String> = _customerName.asStateFlow()

    private val _paymentMethod = MutableStateFlow("Tunai")
    val paymentMethod: StateFlow<String> = _paymentMethod.asStateFlow()

    private val _amountPaid = MutableStateFlow<Long>(0L)
    val amountPaid: StateFlow<Long> = _amountPaid.asStateFlow()

    // Pickup Calendar defaults
    private val calendar = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
    private val _pickupDateString = MutableStateFlow(
        SimpleDateFormat("dd MMMM yyyy", Locale("in", "ID")).format(calendar.time)
    )
    val pickupDateString: StateFlow<String> = _pickupDateString.asStateFlow()

    private val _pickupTimeString = MutableStateFlow("14:00") // 24-hour format default
    val pickupTimeString: StateFlow<String> = _pickupTimeString.asStateFlow()

    private val _orderNotes = MutableStateFlow("")
    val orderNotes: StateFlow<String> = _orderNotes.asStateFlow()

    // Admin Auth State
    private val _isAdminLoggedIn = MutableStateFlow(authManager.isLoggedIn)
    val isAdminLoggedIn: StateFlow<Boolean> = _isAdminLoggedIn.asStateFlow()

    // Bluetooth
    private val _isPrinterConnected = MutableStateFlow(printerHelper.isConnected)
    val isPrinterConnected: StateFlow<Boolean> = _isPrinterConnected.asStateFlow()

    private val _pairedDevices = MutableStateFlow<List<BluetoothPrinterDevice>>(emptyList())
    val pairedDevices: StateFlow<List<BluetoothPrinterDevice>> = _pairedDevices.asStateFlow()

    private val _autoPrint = MutableStateFlow(true)
    val autoPrint: StateFlow<Boolean> = _autoPrint.asStateFlow()

    // Daily report date selection
    private val _selectedReportDate = MutableStateFlow(Calendar.getInstance().timeInMillis)
    val selectedReportDate: StateFlow<Long> = _selectedReportDate.asStateFlow()

    init {
        viewModelScope.launch {
            repository.ensureDataSeeded()
            refreshPairedDevices()

            // Preload images into disk & memory cache
            repository.allProducts.firstOrNull()?.let { products ->
                val urls = mutableListOf<String>()
                products.forEach { p ->
                    if (p.img.isNotBlank()) urls.add(p.img)
                    p.getImageList().forEach { urls.add(it) }
                }
                repository.allPackagingVariants.firstOrNull()?.forEach { v ->
                    if (v.img.isNotBlank()) urls.add(v.img)
                }
                com.example.util.ImagePreloadHelper.preloadImages(application, urls)
            }
        }
    }

    fun openFullImageView(imageUrl: String, title: String = "", subtitle: String = "") {
        _fullScreenImageView.value = Triple(imageUrl, title, subtitle)
    }

    fun closeFullImageView() {
        _fullScreenImageView.value = null
    }

    fun isItemInCart(productId: Long): Boolean {
        return _cartItems.value.any { it.productId == productId }
    }

    /**
     * Calculates additional box packaging fee based on rules:
     * - Parcel items already have artificial decoration included (no extra box fee).
     * - Standard packaging has 0 extra fee.
     * - Artificial packaging: Rp 18.000 * total quantity of non-parcel items in cart.
     */
    fun calculateBoxFee(type: PackagingTypeEntity?): Long {
        if (type == null || type.harga_tambahan <= 0) return 0L
        val productMap = allProducts.value.associateBy { it.id }
        val nonParcelItemCount = _cartItems.value.filter { item ->
            val prod = productMap[item.productId]
            val isParcel = (prod?.cat?.contains("parcel", ignoreCase = true) == true) ||
                    item.productName.contains("parcel", ignoreCase = true)
            !isParcel
        }.sumOf { it.quantity }

        return type.harga_tambahan * nonParcelItemCount
    }

    fun getNonParcelQuantity(): Int {
        val productMap = allProducts.value.associateBy { it.id }
        return _cartItems.value.filter { item ->
            val prod = productMap[item.productId]
            val isParcel = (prod?.cat?.contains("parcel", ignoreCase = true) == true) ||
                    item.productName.contains("parcel", ignoreCase = true)
            !isParcel
        }.sumOf { it.quantity }
    }

    fun getParcelQuantity(): Int {
        val productMap = allProducts.value.associateBy { it.id }
        return _cartItems.value.filter { item ->
            val prod = productMap[item.productId]
            val isParcel = (prod?.cat?.contains("parcel", ignoreCase = true) == true) ||
                    item.productName.contains("parcel", ignoreCase = true)
            isParcel
        }.sumOf { it.quantity }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun showProductDetail(product: ProductEntity) {
        _selectedProductDetail.value = product
    }

    fun hideProductDetail() {
        _selectedProductDetail.value = null
    }

    fun showBoxVariantDetail(variant: PackagingVariantEntity) {
        _selectedBoxVariantDetail.value = variant
    }

    fun hideBoxVariantDetail() {
        _selectedBoxVariantDetail.value = null
    }

    fun openEditOrder(order: OrderEntity) {
        _editingOrder.value = order
    }

    fun closeEditOrder() {
        _editingOrder.value = null
    }

    fun addToCart(product: ProductEntity, quantity: Int = 1) {
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.productId == product.id }
        if (index >= 0) {
            val item = current[index]
            current[index] = item.copy(quantity = item.quantity + quantity)
        } else {
            current.add(
                CartItem(
                    productId = product.id,
                    productName = product.name,
                    price = product.price,
                    quantity = quantity,
                    img = product.img
                )
            )
        }
        _cartItems.value = current
        showMessage("${product.name} ditambahkan ke keranjang")
    }

    fun updateCartItemQuantity(productId: Long, delta: Int) {
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.productId == productId }
        if (index >= 0) {
            val newQty = current[index].quantity + delta
            if (newQty <= 0) {
                current.removeAt(index)
            } else {
                current[index] = current[index].copy(quantity = newQty)
            }
            _cartItems.value = current
        }
    }

    fun removeCartItem(productId: Long) {
        _cartItems.value = _cartItems.value.filter { it.productId != productId }
    }

    fun removeFromCart(productId: Long) {
        removeCartItem(productId)
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }

    // Checkout Flow
    fun openCheckout() {
        if (_cartItems.value.isEmpty()) {
            showMessage("Keranjang masih kosong")
            return
        }
        // Initialize packaging defaults if not set
        if (_selectedType.value == null && allPackagingTypes.value.isNotEmpty()) {
            _selectedType.value = allPackagingTypes.value.first()
            val variants = allPackagingVariants.value.filter { it.id_tipe == _selectedType.value?.id_tipe }
            if (variants.isNotEmpty()) {
                _selectedVariant.value = variants.first()
            }
        }
        // Reset amount paid to total by default (Lunas)
        val boxPrice = calculateBoxFee(_selectedType.value)
        _amountPaid.value = cartSubtotal.value + boxPrice
        _isCheckoutOpen.value = true
    }

    fun closeCheckout() {
        _isCheckoutOpen.value = false
    }

    fun selectPackagingType(type: PackagingTypeEntity) {
        _selectedType.value = type
        // Reset variant or pick first matching
        val variants = allPackagingVariants.value.filter { it.id_tipe == type.id_tipe }
        _selectedVariant.value = variants.firstOrNull()
        // Update default amount paid
        val boxPrice = calculateBoxFee(type)
        _amountPaid.value = cartSubtotal.value + boxPrice
    }

    fun selectPackagingVariant(variant: PackagingVariantEntity) {
        _selectedVariant.value = variant
    }

    fun setCustomerName(name: String) {
        _customerName.value = name
    }

    fun setPaymentMethod(method: String) {
        _paymentMethod.value = method
    }

    fun setAmountPaid(amount: Long) {
        _amountPaid.value = amount
    }

    fun setPickupDate(date: String) {
        _pickupDateString.value = date
    }

    fun setPickupTime(time: String) {
        _pickupTimeString.value = time
    }

    fun setOrderNotes(notes: String) {
        _orderNotes.value = notes
    }

    fun setAutoPrint(enabled: Boolean) {
        _autoPrint.value = enabled
    }

    fun submitCheckout() {
        val name = _customerName.value.trim()
        if (name.isBlank()) {
            showMessage("Wajib mengisi Nama Pelanggan")
            return
        }
        val type = _selectedType.value
        val variant = _selectedVariant.value
        if (type == null || variant == null) {
            showMessage("Pilih Tipe dan Varian Box terlebih dahulu")
            return
        }
        val date = _pickupDateString.value.trim()
        if (date.isBlank()) {
            showMessage("Wajib memilih Tanggal Pengambilan")
            return
        }
        val time = _pickupTimeString.value.trim()
        if (time.isBlank() || !time.contains(":")) {
            showMessage("Wajib mengisi Jam Pengambilan (format 24 jam misal 14:30)")
            return
        }

        val items = _cartItems.value
        if (items.isEmpty()) {
            showMessage("Keranjang kosong")
            return
        }

        viewModelScope.launch {
            val subtotal = items.sumOf { it.subtotal }
            val boxPrice = calculateBoxFee(type)
            val grandTotal = subtotal + boxPrice

            val paid = _amountPaid.value
            val status = when {
                paid == 0L -> "PENDING"
                paid < grandTotal -> "DP"
                else -> "LUNAS"
            }
            val changeOrRem = when {
                paid == 0L -> grandTotal
                paid < grandTotal -> grandTotal - paid
                else -> paid - grandTotal
            }

            val jsonArray = org.json.JSONArray()
            items.forEach { jsonArray.put(it.toJson()) }

            val orderNumber = "DJD-" + SimpleDateFormat("yyMMdd-HHmmss", Locale.getDefault()).format(Date())

            val newOrder = OrderEntity(
                orderNumber = orderNumber,
                customerName = name,
                pickupDate = date,
                pickupTime = time,
                packagingType = type.id_tipe,
                packagingVariant = variant.name,
                packagingPrice = boxPrice,
                itemsJson = jsonArray.toString(),
                subtotal = subtotal,
                total = grandTotal,
                paymentMethod = _paymentMethod.value,
                amountPaid = paid,
                paymentStatus = status,
                changeOrRemaining = changeOrRem,
                notes = _orderNotes.value.trim(),
                initialDeposit = if (status == "DP") paid else 0L,
                settlementPaid = if (status == "LUNAS") paid else 0L
            )

            val orderId = repository.insertOrder(newOrder)
            val savedOrder = newOrder.copy(id = orderId)

            _activeReceiptOrder.value = savedOrder
            _isCheckoutOpen.value = false
            _isReceiptOpen.value = true
            clearCart()
            _customerName.value = ""
            _orderNotes.value = ""

            showMessage("Pesanan $orderNumber ($status) berhasil disimpan!")

            // Auto-print if enabled & connected
            if (_autoPrint.value) {
                if (printerHelper.isConnected) {
                    printerHelper.printReceipt(savedOrder, _storeProfile.value)
                }
            }
        }
    }

    fun updateExistingOrder(order: OrderEntity) {
        viewModelScope.launch {
            repository.updateOrder(order)
            _editingOrder.value = null
            _activeReceiptOrder.value = order
            _isReceiptOpen.value = true
            showMessage("Pelunasan/Nota ${order.orderNumber} berhasil diperbarui")

            // Auto-print updated receipt if printer is connected and autoPrint enabled
            if (_autoPrint.value && printerHelper.isConnected) {
                printerHelper.printReceipt(order, _storeProfile.value)
            }
        }
    }

    fun deleteOrder(order: OrderEntity) {
        viewModelScope.launch {
            repository.deleteOrder(order)
            if (_activeReceiptOrder.value?.id == order.id) {
                _isReceiptOpen.value = false
                _activeReceiptOrder.value = null
            }
            showMessage("Nota ${order.orderNumber} telah dihapus")
        }
    }

    fun loadOrderToCart(order: OrderEntity) {
        val items = order.getCartItems()
        _cartItems.value = items
        _customerName.value = order.customerName
        _orderNotes.value = order.notes
        showMessage("Item pesanan ${order.orderNumber} dimuat ke keranjang")
    }

    fun showReceipt(order: OrderEntity) {
        _activeReceiptOrder.value = order
        _receiptFeedback.value = null
        _isPrinting.value = false
        _isReceiptOpen.value = true
    }

    fun closeReceipt() {
        _isReceiptOpen.value = false
        _receiptFeedback.value = null
        _isPrinting.value = false
    }

    fun clearReceiptFeedback() {
        _receiptFeedback.value = null
    }

    fun openPrinterSettings() {
        refreshPairedDevices()
        _isPrinterSettingsOpen.value = true
    }

    fun closePrinterSettings() {
        _isPrinterSettingsOpen.value = false
    }

    fun printCurrentReceipt() {
        val order = _activeReceiptOrder.value ?: return
        printOrderDirect(order, autoClose = true)
    }

    fun printOrderDirect(order: OrderEntity, autoClose: Boolean = true) {
        viewModelScope.launch {
            if (_isPrinting.value) return@launch
            if (!printerHelper.isConnected) {
                _receiptFeedback.value = "ERR:Printer Bluetooth belum terhubung"
                showMessage("Printer belum terhubung. Silakan pilih printer Bluetooth.")
                openPrinterSettings()
                return@launch
            }
            _isPrinting.value = true
            _receiptFeedback.value = "INFO:Sedang mencetak struk ke printer..."
            val result = printerHelper.printReceipt(order, _storeProfile.value)
            _isPrinting.value = false
            result.onSuccess { msg ->
                _receiptFeedback.value = "OK:Struk berhasil dicetak!"
                showMessage(msg)
                if (autoClose) {
                    kotlinx.coroutines.delay(1200)
                    _isReceiptOpen.value = false
                    _receiptFeedback.value = null
                }
            }.onFailure { err ->
                _receiptFeedback.value = "ERR:${err.message ?: "Gagal mencetak struk"}"
                showMessage(err.message ?: "Gagal mencetak")
            }
        }
    }

    fun saveCurrentReceiptAsImage() {
        val order = _activeReceiptOrder.value ?: return
        viewModelScope.launch {
            _receiptFeedback.value = "INFO:Menyimpan gambar struk..."
            val result = ReceiptImageHelper.saveReceiptImage(
                getApplication(),
                order,
                _storeProfile.value
            )
            result.onSuccess { msg ->
                _receiptFeedback.value = "OK:Gambar struk berhasil disimpan!"
                showMessage(msg)
            }.onFailure { err ->
                _receiptFeedback.value = "ERR:Gagal menyimpan: ${err.message}"
                showMessage("Gagal menyimpan gambar nota: ${err.message}")
            }
        }
    }

    fun shareCurrentReceiptAsImage() {
        val order = _activeReceiptOrder.value ?: return
        viewModelScope.launch {
            _receiptFeedback.value = "INFO:Menyiapkan gambar untuk dibagikan..."
            val result = ReceiptImageHelper.shareReceiptImage(
                getApplication(),
                order,
                _storeProfile.value
            )
            result.onSuccess {
                _receiptFeedback.value = "OK:Membuka aplikasi berbagi..."
                showMessage("Membuka aplikasi untuk membagikan gambar nota...")
            }.onFailure { err ->
                _receiptFeedback.value = "ERR:Gagal membagikan: ${err.message}"
                showMessage("Gagal membuat gambar nota: ${err.message}")
            }
        }
    }

    // Bluetooth Management
    fun refreshPairedDevices() {
        _pairedDevices.value = printerHelper.getPairedDevices()
        _isPrinterConnected.value = printerHelper.isConnected
    }

    fun connectPrinter(address: String) {
        viewModelScope.launch {
            showMessage("Menghubungkan ke printer...")
            val result = printerHelper.connectToDevice(address)
            result.onSuccess { msg ->
                _isPrinterConnected.value = true
                refreshPairedDevices()
                showMessage(msg)
            }.onFailure { err ->
                _isPrinterConnected.value = false
                showMessage(err.message ?: "Gagal terhubung")
            }
        }
    }

    fun disconnectPrinter() {
        printerHelper.disconnect()
        _isPrinterConnected.value = false
        refreshPairedDevices()
        showMessage("Printer terputus")
    }

    fun testPrint() {
        viewModelScope.launch {
            val result = printerHelper.printTestPage(_storeProfile.value)
            result.onSuccess { showMessage(it) }
                .onFailure { showMessage(it.message ?: "Gagal uji cetak") }
        }
    }

    // Admin Auth
    fun loginAdmin(pin: String): Boolean {
        val success = authManager.verifyPin(pin)
        _isAdminLoggedIn.value = success
        if (success) {
            showMessage("Login Admin berhasil")
        } else {
            showMessage("PIN salah! Default: 1234")
        }
        return success
    }

    fun logoutAdmin() {
        authManager.logout()
        _isAdminLoggedIn.value = false
        showMessage("Logout berhasil")
    }

    // Product Management
    fun addProduct(
        name: String,
        price: Long,
        category: String,
        imageUrl: String,
        desc: String,
        bestseller: Boolean,
        isNew: Boolean,
        isPromo: Boolean
    ) {
        viewModelScope.launch {
            val newProduct = ProductEntity(
                id = System.currentTimeMillis(),
                name = name.trim(),
                price = price,
                cat = if (category.isBlank()) "Hantaran" else category.trim(),
                img = if (imageUrl.isBlank()) "https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/logo.png" else imageUrl.trim(),
                imagesJson = if (imageUrl.isBlank()) "[]" else "[\"${imageUrl.trim()}\"]",
                desc = desc.trim(),
                bestseller = bestseller,
                isNew = isNew,
                isPromo = isPromo,
                isOutOfStock = false
            )
            repository.insertProduct(newProduct)
            showMessage("Produk '${name}' berhasil ditambahkan")
        }
    }

    fun updateProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.updateProduct(product)
            showMessage("Produk '${product.name}' diperbarui")
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.deleteProduct(product)
            showMessage("Produk '${product.name}' dihapus")
        }
    }

    // Packaging Box & Variant Management (CRUD)
    fun addPackagingType(id_tipe: String, harga_tambahan: Long) {
        viewModelScope.launch {
            val trimmedType = id_tipe.trim()
            if (trimmedType.isBlank()) {
                showMessage("Nama tipe box tidak boleh kosong")
                return@launch
            }
            repository.insertPackagingType(
                PackagingTypeEntity(
                    id_tipe = trimmedType,
                    harga_tambahan = harga_tambahan
                )
            )
            showMessage("Tipe Box '$trimmedType' berhasil ditambahkan")
        }
    }

    fun updatePackagingType(type: PackagingTypeEntity) {
        viewModelScope.launch {
            repository.updatePackagingType(type)
            showMessage("Tipe Box '${type.id_tipe}' diperbarui")
        }
    }

    fun deletePackagingType(type: PackagingTypeEntity) {
        viewModelScope.launch {
            repository.deletePackagingType(type)
            showMessage("Tipe Box '${type.id_tipe}' dan seluruh variannya berhasil dihapus")
        }
    }

    fun addPackagingVariant(
        id_tipe: String,
        name: String,
        img: String,
        desc: String,
        imagesJson: String = "[]",
        featuresJson: String = "[]"
    ) {
        viewModelScope.launch {
            val variantId = "var_${System.currentTimeMillis()}"
            val newVariant = PackagingVariantEntity(
                id_varian = variantId,
                id_tipe = id_tipe,
                name = name.trim(),
                img = if (img.isBlank()) "https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/logo.png" else img.trim(),
                imagesJson = imagesJson,
                desc = desc.trim(),
                featuresJson = featuresJson
            )
            repository.insertPackagingVariant(newVariant)
            showMessage("Varian Box '$name' berhasil ditambahkan")
        }
    }

    fun updatePackagingVariant(variant: PackagingVariantEntity) {
        viewModelScope.launch {
            repository.updatePackagingVariant(variant)
            showMessage("Varian Box '${variant.name}' diperbarui")
        }
    }

    fun deletePackagingVariant(variant: PackagingVariantEntity) {
        viewModelScope.launch {
            repository.deletePackagingVariant(variant)
            showMessage("Varian Box '${variant.name}' dihapus")
        }
    }

    // Reports & Analytics
    fun setSelectedReportDate(dateMillis: Long) {
        _selectedReportDate.value = dateMillis
    }

    fun saveReportToCsv(orders: List<OrderEntity>, totalRevenue: Long, dateLabel: String) {
        viewModelScope.launch {
            val result = ExportHelper.saveDailyReportToCsv(
                getApplication(),
                dateLabel,
                orders,
                totalRevenue,
                _storeProfile.value
            )
            result.onSuccess { msg ->
                showMessage(msg)
            }.onFailure {
                showMessage("Gagal simpan CSV: ${it.message}")
            }
        }
    }

    fun shareReportCsv(orders: List<OrderEntity>, totalRevenue: Long, dateLabel: String) {
        viewModelScope.launch {
            val result = ExportHelper.shareDailyReportCsv(
                getApplication(),
                dateLabel,
                orders,
                totalRevenue,
                _storeProfile.value
            )
            result.onSuccess {
                showMessage("Membuka aplikasi untuk membagikan laporan Excel/CSV...")
            }.onFailure {
                showMessage("Gagal bagikan CSV: ${it.message}")
            }
        }
    }

    fun exportReportToCsv(orders: List<OrderEntity>, totalRevenue: Long, dateLabel: String) {
        shareReportCsv(orders, totalRevenue, dateLabel)
    }

    fun saveReportToPdf(orders: List<OrderEntity>, totalRevenue: Long, dateLabel: String) {
        viewModelScope.launch {
            val result = ExportHelper.saveDailyReportToPdf(
                getApplication(),
                dateLabel,
                orders,
                totalRevenue,
                _storeProfile.value
            )
            result.onSuccess { msg ->
                showMessage(msg)
            }.onFailure {
                showMessage("Gagal simpan PDF: ${it.message}")
            }
        }
    }

    fun shareReportPdf(orders: List<OrderEntity>, totalRevenue: Long, dateLabel: String) {
        viewModelScope.launch {
            val result = ExportHelper.shareDailyReportPdf(
                getApplication(),
                dateLabel,
                orders,
                totalRevenue,
                _storeProfile.value
            )
            result.onSuccess {
                showMessage("Membuka aplikasi untuk membagikan laporan PDF...")
            }.onFailure {
                showMessage("Gagal bagikan PDF: ${it.message}")
            }
        }
    }

    fun exportReportToPdf(orders: List<OrderEntity>, totalRevenue: Long, dateLabel: String) {
        shareReportPdf(orders, totalRevenue, dateLabel)
    }

    // Master Data Backup (Catalog & Boxes, excluding orders)
    fun exportCatalogToJson(): String {
        return com.example.util.DataBackupHelper.exportCatalogToJson(
            products = allProducts.value,
            packagingTypes = allPackagingTypes.value,
            packagingVariants = allPackagingVariants.value,
            storeProfile = _storeProfile.value
        )
    }

    fun saveCatalogBackupJson() {
        viewModelScope.launch {
            val json = exportCatalogToJson()
            val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmm", java.util.Locale.getDefault()).format(java.util.Date())
            val fileName = "Backup_Katalog_Djandes_${timestamp}.json"
            val result = com.example.util.DataBackupHelper.saveJsonToStorage(
                getApplication(),
                fileName,
                json
            )
            result.onSuccess { msg ->
                showMessage(msg)
            }.onFailure { err ->
                showMessage("Gagal simpan JSON: ${err.message}")
            }
        }
    }

    fun shareCatalogBackupJson() {
        val json = exportCatalogToJson()
        com.example.util.DataBackupHelper.shareJsonContent(
            getApplication(),
            "Backup Data Katalog Djandes",
            json
        )
    }

    fun saveOrdersBackupJson() {
        viewModelScope.launch {
            val json = exportOrdersToJson()
            val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmm", java.util.Locale.getDefault()).format(java.util.Date())
            val fileName = "Backup_Laporan_Djandes_${timestamp}.json"
            val result = com.example.util.DataBackupHelper.saveJsonToStorage(
                getApplication(),
                fileName,
                json
            )
            result.onSuccess { msg ->
                showMessage(msg)
            }.onFailure { err ->
                showMessage("Gagal simpan JSON: ${err.message}")
            }
        }
    }

    fun shareOrdersBackupJson() {
        val json = exportOrdersToJson()
        com.example.util.DataBackupHelper.shareJsonContent(
            getApplication(),
            "Backup Laporan Nota Djandes",
            json
        )
    }

    fun saveOrdersBackupCsv() {
        viewModelScope.launch {
            val csv = exportOrdersToCsv()
            val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmm", java.util.Locale.getDefault()).format(java.util.Date())
            val fileName = "Backup_Riwayat_Nota_${timestamp}.csv"
            val result = com.example.util.AppStorageHelper.saveToDownloads(
                getApplication(),
                com.example.util.AppStorageHelper.SUBFOLDER_CSV,
                fileName,
                "text/csv"
            ) { os ->
                os.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
                os.write(csv.toByteArray(Charsets.UTF_8))
            }
            result.onSuccess { msg ->
                showMessage(msg)
            }.onFailure { err ->
                showMessage("Gagal simpan CSV: ${err.message}")
            }
        }
    }

    fun shareOrdersBackupCsv() {
        val csv = exportOrdersToCsv()
        com.example.util.AppStorageHelper.shareText(
            getApplication(),
            csv,
            "Backup Riwayat Nota CSV Djandes"
        )
    }

    fun importCatalogFromJson(jsonStr: String, replaceAll: Boolean = true) {
        viewModelScope.launch {
            val parseResult = com.example.util.DataBackupHelper.parseFromJson(jsonStr)
            parseResult.onSuccess { data ->
                if (replaceAll) {
                    repository.restoreAllData(data.products, data.packagingTypes, data.packagingVariants, emptyList())
                    showMessage("Master data berhasil dipulihkan: ${data.products.size} produk, ${data.packagingTypes.size} tipe box")
                } else {
                    repository.mergeData(data.products, data.packagingTypes, data.packagingVariants, emptyList())
                    showMessage("Master data berhasil digabungkan: ${data.products.size} produk")
                }
                // Update store profile if present in JSON
                data.storeProfile?.let { updateStoreProfile(it) }
                // Preload newly imported images
                val urls = data.products.map { it.img } + data.packagingVariants.map { it.img }
                com.example.util.ImagePreloadHelper.preloadImages(getApplication(), urls)
            }.onFailure { err ->
                showMessage(err.message ?: "Gagal memproses data JSON")
            }
        }
    }

    // Reports / Orders Data Backup & Import (CSV & JSON)
    fun exportOrdersToCsv(): String {
        return ExportHelper.generateOrdersCsvText(allOrders.value, _storeProfile.value)
    }

    fun importOrdersFromCsv(csvStr: String, replaceAll: Boolean = false) {
        viewModelScope.launch {
            val parseResult = ExportHelper.parseOrdersFromCsv(csvStr)
            parseResult.onSuccess { orders ->
                if (orders.isEmpty()) {
                    showMessage("Tidak ditemukan data nota dalam teks CSV")
                    return@launch
                }
                if (replaceAll) {
                    repository.restoreAllData(allProducts.value, allPackagingTypes.value, allPackagingVariants.value, orders)
                    showMessage("Berhasil memulihkan ${orders.size} riwayat nota laporan dari CSV")
                } else {
                    repository.mergeData(emptyList(), emptyList(), emptyList(), orders)
                    showMessage("Berhasil menambahkan ${orders.size} riwayat nota laporan dari CSV")
                }
            }.onFailure { err ->
                showMessage(err.message ?: "Gagal memproses file/teks CSV laporan")
            }
        }
    }

    fun exportOrdersToJson(): String {
        return com.example.util.DataBackupHelper.exportOrdersToJson(allOrders.value)
    }

    fun importOrdersFromJson(jsonStr: String, replaceAll: Boolean = false) {
        viewModelScope.launch {
            val parseResult = com.example.util.DataBackupHelper.parseFromJson(jsonStr)
            parseResult.onSuccess { data ->
                if (data.orders.isEmpty()) {
                    showMessage("Tidak ditemukan data nota/laporan dalam file JSON")
                    return@launch
                }
                if (replaceAll) {
                    repository.restoreAllData(allProducts.value, allPackagingTypes.value, allPackagingVariants.value, data.orders)
                    showMessage("Berhasil memulihkan ${data.orders.size} riwayat nota laporan")
                } else {
                    repository.mergeData(emptyList(), emptyList(), emptyList(), data.orders)
                    showMessage("Berhasil menambahkan ${data.orders.size} riwayat nota laporan")
                }
            }.onFailure { err ->
                showMessage(err.message ?: "Gagal memproses data JSON laporan")
            }
        }
    }

    fun showMessage(msg: String) {
        snackbarJob?.cancel()
        _snackbarMessage.value = msg
        snackbarJob = viewModelScope.launch {
            kotlinx.coroutines.delay(1800)
            if (_snackbarMessage.value == msg) {
                _snackbarMessage.value = null
            }
        }
    }

    fun clearSnackbar() {
        snackbarJob?.cancel()
        _snackbarMessage.value = null
    }
}
