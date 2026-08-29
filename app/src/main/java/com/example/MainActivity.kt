package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.components.*
import com.example.ui.screens.AdminProductScreen
import com.example.ui.screens.CartScreen
import com.example.ui.screens.CatalogScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.theme.DjandesTheme
import com.example.ui.theme.ChocoBrown
import com.example.ui.theme.GoldWarm
import com.example.viewmodel.PosViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

sealed class Screen(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Catalog : Screen("catalog", "Katalog", Icons.Default.Storefront)
    object Cart : Screen("cart", "Keranjang", Icons.Default.ShoppingBag)
    object Orders : Screen("orders", "Nota", Icons.Default.ReceiptLong)
    object Reports : Screen("reports", "Laporan", Icons.Default.Analytics)
    object Admin : Screen("admin", "Admin", Icons.Default.AdminPanelSettings)
}

class MainActivity : ComponentActivity() {

    private val viewModel: PosViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            DjandesTheme {
                DjandesApp(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DjandesApp(viewModel: PosViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Catalog.route

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()

    // Dialog States from ViewModel
    val selectedDetailProduct by viewModel.selectedProductDetail.collectAsState()
    val selectedBoxVariantDetail by viewModel.selectedBoxVariantDetail.collectAsState()
    val editingOrder by viewModel.editingOrder.collectAsState()
    val isCheckoutOpen by viewModel.isCheckoutOpen.collectAsState()
    val activeReceiptOrder by viewModel.activeReceiptOrder.collectAsState()
    val isReceiptOpen by viewModel.isReceiptOpen.collectAsState()
    val fullScreenImage by viewModel.fullScreenImageView.collectAsState()
    val isPrinterSettingsOpen by viewModel.isPrinterSettingsOpen.collectAsState()

    // Bluetooth permission launcher for Android 12+
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.all { it }
        if (granted) {
            viewModel.refreshPairedDevices()
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val hasConnect = ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
            val hasScan = ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
            if (!hasConnect || !hasScan) {
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.BLUETOOTH_SCAN
                    )
                )
            }
        }
    }

    // React to snackbar messages (quick auto-dismiss for snappy UX)
    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
        }
    }

    val screens = listOf(
        Screen.Catalog,
        Screen.Cart,
        Screen.Orders,
        Screen.Reports,
        Screen.Admin
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                screens.forEach { screen ->
                    val isSelected = currentRoute == screen.route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(Screen.Catalog.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (screen == Screen.Cart && cartItems.isNotEmpty()) {
                                        Badge(
                                            containerColor = ChocoBrown,
                                            contentColor = Color.White
                                        ) {
                                            Text("${cartItems.sumOf { it.quantity }}")
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = screen.icon,
                                    contentDescription = screen.label
                                )
                            }
                        },
                        label = {
                            Text(
                                text = screen.label,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = ChocoBrown,
                            indicatorColor = ChocoBrown,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_item_${screen.route}")
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets.systemBars
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Catalog.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(Screen.Catalog.route) {
                CatalogScreen(
                    viewModel = viewModel,
                    onNavigateToCart = {
                        navController.navigate(Screen.Cart.route) {
                            popUpTo(Screen.Catalog.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable(Screen.Cart.route) {
                CartScreen(
                    viewModel = viewModel,
                    onNavigateToCatalog = {
                        navController.navigate(Screen.Catalog.route) {
                            popUpTo(Screen.Catalog.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(Screen.Orders.route) {
                HistoryScreen(
                    viewModel = viewModel,
                    onNavigateToCart = {
                        navController.navigate(Screen.Cart.route) {
                            popUpTo(Screen.Catalog.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable(Screen.Reports.route) {
                ReportsScreen(viewModel = viewModel)
            }
            composable(Screen.Admin.route) {
                AdminProductScreen(viewModel = viewModel)
            }
        }

        // Global Product Detail Dialog (with multi-image thumbnail gallery & image zoom)
        selectedDetailProduct?.let { product ->
            val inCart = cartItems.any { it.productId == product.id }
            ProductDetailDialog(
                product = product,
                isInCart = inCart,
                onDismiss = { viewModel.hideProductDetail() },
                onAddToCart = { prod, qty -> viewModel.addToCart(prod, qty) },
                onRemoveFromCart = { viewModel.removeCartItem(product.id) },
                onImageClick = { imgUrl ->
                    viewModel.openFullImageView(
                        imgUrl,
                        product.name,
                        "${formatRupiah(product.price)} • ${product.cat}"
                    )
                }
            )
        }

        // Global Box Variant Detail Dialog
        selectedBoxVariantDetail?.let { variant ->
            val packagingTypes by viewModel.allPackagingTypes.collectAsState()
            val packagingType = packagingTypes.find { it.id_tipe.equals(variant.id_tipe, ignoreCase = true) }
            BoxDetailDialog(
                variant = variant,
                packagingType = packagingType,
                onDismiss = { viewModel.hideBoxVariantDetail() },
                onImageClick = { imgUrl ->
                    viewModel.openFullImageView(
                        imgUrl,
                        "Box: ${variant.name}",
                        variant.desc
                    )
                }
            )
        }

        // Global Edit Past Order Dialog
        editingOrder?.let { order ->
            EditOrderDialog(
                order = order,
                onDismiss = { viewModel.closeEditOrder() },
                onSave = { updatedOrder ->
                    viewModel.updateExistingOrder(updatedOrder)
                }
            )
        }

        // Global Checkout Bottom Sheet (2 Steps: Box selection -> Schedule & Payment)
        if (isCheckoutOpen) {
            CheckoutBottomSheet(
                viewModel = viewModel,
                onDismiss = { viewModel.closeCheckout() }
            )
        }

        // Global Receipt Preview Dialog
        if (isReceiptOpen && activeReceiptOrder != null) {
            ReceiptDialog(
                order = activeReceiptOrder!!,
                viewModel = viewModel,
                onDismiss = { viewModel.closeReceipt() }
            )
        }

        // Global Full Screen Image Viewer Dialog
        fullScreenImage?.let { (imageUrl, title, subtitle) ->
            FullScreenImageViewerDialog(
                imageUrl = imageUrl,
                title = title,
                subtitle = subtitle,
                onDismiss = { viewModel.closeFullImageView() }
            )
        }

        // Global Bluetooth Printer Settings Dialog
        if (isPrinterSettingsOpen) {
            BluetoothSettingsDialog(
                viewModel = viewModel,
                onDismiss = { viewModel.closePrinterSettings() }
            )
        }
    }
}
