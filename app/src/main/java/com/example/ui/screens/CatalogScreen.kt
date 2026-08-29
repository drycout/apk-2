package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.ProductEntity
import com.example.ui.components.StatusBadge
import com.example.ui.components.formatRupiah
import com.example.ui.theme.*
import com.example.viewmodel.PosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    viewModel: PosViewModel,
    onNavigateToCart: () -> Unit
) {
    val products by viewModel.allProducts.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val isPrinterConnected by viewModel.isPrinterConnected.collectAsState()
    val storeProfile by viewModel.storeProfile.collectAsState()

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    // Distinct product categories from database + badge filters (only shown if active)
    val dynamicCategories = remember(products) {
        val cats = products.map { it.cat.trim() }.filter { it.isNotBlank() }.distinct()
        val baseCats = if (cats.isEmpty()) listOf("Hantaran", "Kue Basah", "Kue Kering") else cats
        val extraBadges = mutableListOf<String>()
        if (products.any { it.bestseller }) {
            extraBadges.add("⭐ Favorit")
        }
        if (products.any { it.isPromo }) {
            extraBadges.add("🏷️ Promo")
        }
        if (products.any { it.isNew }) {
            extraBadges.add("✨ Baru")
        }
        listOf("Semua") + baseCats + extraBadges
    }

    // Reset filter if previously selected badge category is no longer present
    LaunchedEffect(dynamicCategories, selectedCategory) {
        val isCategoryValid = when (selectedCategory) {
            "Semua" -> true
            "⭐ Favorit", "Favorit" -> dynamicCategories.contains("⭐ Favorit")
            "🏷️ Promo", "Promo" -> dynamicCategories.contains("🏷️ Promo")
            "✨ Baru", "Baru" -> dynamicCategories.contains("✨ Baru")
            else -> dynamicCategories.contains(selectedCategory)
        }
        if (!isCategoryValid) {
            viewModel.setSelectedCategory("Semua")
        }
    }

    // Filter products
    val filteredProducts = remember(products, searchQuery, selectedCategory) {
        products.filter { product ->
            val matchesSearch = product.name.contains(searchQuery, ignoreCase = true) ||
                    product.desc.contains(searchQuery, ignoreCase = true)
            val matchesCategory = when (selectedCategory) {
                "Semua" -> true
                "⭐ Favorit", "Favorit" -> product.bestseller
                "🏷️ Promo", "Promo" -> product.isPromo
                "✨ Baru", "Baru" -> product.isNew
                else -> product.cat.equals(selectedCategory, ignoreCase = true)
            }
            matchesSearch && matchesCategory
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        // Precise Centered Header: Logo -> Name -> Address
        Surface(
            color = ChocoBrown,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    // Logo centered with circular frame
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        shadowElevation = 4.dp,
                        modifier = Modifier.size(52.dp)
                    ) {
                        AsyncImage(
                            model = storeProfile.logo,
                            contentDescription = "Logo Djandes",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp)
                                .clip(CircleShape)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Store Name below Logo
                    Text(
                        text = storeProfile.name.uppercase(),
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = storeProfile.tagline.uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = Gold80,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Full Address centered below Name
                    Text(
                        text = storeProfile.address,
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.92f),
                        textAlign = TextAlign.Center,
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
        }

        // Search Bar & Filter Row
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Cari jajanan, parcel, hantaran...", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = ChocoBrown) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = {
                            viewModel.setSearchQuery("")
                            keyboardController?.hide()
                            focusManager.clearFocus()
                        }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                }),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ChocoBrown,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_bar_catalog")
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Category Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(dynamicCategories) { cat ->
                    val isSelected = selectedCategory == cat || (selectedCategory == "Favorit" && cat == "⭐ Favorit") || (selectedCategory == "Promo" && cat == "🏷️ Promo") || (selectedCategory == "Baru" && cat == "✨ Baru")
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            viewModel.setSelectedCategory(cat)
                        },
                        label = { Text(cat, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ChocoBrown,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("chip_category_$cat")
                    )
                }
            }
        }

        // Products Grid
        if (filteredProducts.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.SearchOff,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tidak ada produk yang cocok",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(filteredProducts, key = { it.id }) { product ->
                    val isInCart = cartItems.any { it.productId == product.id }
                    ProductCardItem(
                        product = product,
                        isInCart = isInCart,
                        onCardClick = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            viewModel.showProductDetail(product)
                        },
                        onAddToCart = { viewModel.addToCart(product) },
                        onRemoveFromCart = { viewModel.removeCartItem(product.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun ProductCardItem(
    product: ProductEntity,
    isInCart: Boolean,
    onCardClick: () -> Unit,
    onAddToCart: () -> Unit,
    onRemoveFromCart: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
            .testTag("product_card_${product.id}")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Thumbnail with Badges & detail trigger
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                AsyncImage(
                    model = product.img,
                    contentDescription = product.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Top badges: Bestseller / Promo / New / Fallback Category
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (product.bestseller) {
                        StatusBadge("Favorit", GoldWarm, Color.White)
                    }
                    if (product.isPromo) {
                        StatusBadge("Promo", WarningOrange, Color.White)
                    }
                    if (product.isNew) {
                        StatusBadge("Baru", SuccessGreen, Color.White)
                    }
                    // Fallback Category Badge if no special badges
                    if (!product.bestseller && !product.isPromo && !product.isNew) {
                        StatusBadge(
                            text = if (product.cat.isNotBlank()) product.cat else "Hantaran",
                            backgroundColor = ChocoMedium,
                            textColor = Color.White
                        )
                    }
                }

                if (product.isOutOfStock) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "STOK HABIS",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Info Section
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = product.name,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = formatRupiah(product.price),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = GoldWarm
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Toggle Button: "+ Keranjang" vs "Hapus"
                if (isInCart) {
                    Button(
                        onClick = onRemoveFromCart,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OutOfStockRed,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(34.dp)
                            .testTag("btn_remove_cart_${product.id}")
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Hapus",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Hapus",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Button(
                        onClick = onAddToCart,
                        enabled = !product.isOutOfStock,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ChocoBrown,
                            disabledContainerColor = Color.LightGray
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(34.dp)
                            .testTag("btn_quick_add_${product.id}")
                    ) {
                        Icon(
                            Icons.Default.AddShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "+ Keranjang",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

