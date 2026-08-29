package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.model.ProductEntity
import com.example.ui.theme.*

@Composable
fun ProductDetailDialog(
    product: ProductEntity,
    isInCart: Boolean = false,
    onDismiss: () -> Unit,
    onAddToCart: (ProductEntity, Int) -> Unit,
    onRemoveFromCart: () -> Unit = {},
    onImageClick: (String) -> Unit = {}
) {
    var quantity by remember { mutableIntStateOf(1) }
    val images = remember(product) { product.getImageList() }
    var selectedImageIndex by remember { mutableIntStateOf(0) }
    val currentImage = images.getOrNull(selectedImageIndex) ?: product.img

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Image Container with Close button, Zoom button & Badges
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clickable { onImageClick(currentImage) }
                ) {
                    AsyncImage(
                        model = currentImage,
                        contentDescription = product.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Zoom indicator overlay
                    Surface(
                        color = Color.Black.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.ZoomIn, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Lihat Gambar Penuh", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Close Button
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            .testTag("btn_close_product_detail")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Color.White)
                    }

                    // Badges
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (product.bestseller) {
                            StatusBadge("Favorit", GoldWarm, Color.White)
                        }
                        if (product.isNew) {
                            StatusBadge("Baru", SuccessGreen, Color.White)
                        }
                        if (product.isPromo) {
                            StatusBadge("Promo", WarningOrange, Color.White)
                        }
                        if (!product.bestseller && !product.isNew && !product.isPromo) {
                            StatusBadge(
                                text = if (product.cat.isNotBlank()) product.cat else "Hantaran",
                                backgroundColor = ChocoMedium,
                                textColor = Color.White
                            )
                        }
                        if (product.isOutOfStock) {
                            StatusBadge("Habis", OutOfStockRed, Color.White)
                        }
                    }
                }

                // Multiple thumbnail strip if more than 1 image
                if (images.size > 1) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        images.forEachIndexed { idx, url ->
                            Surface(
                                onClick = { selectedImageIndex = idx },
                                shape = RoundedCornerShape(8.dp),
                                border = if (selectedImageIndex == idx) ButtonDefaults.outlinedButtonBorder else null,
                                modifier = Modifier.size(54.dp)
                            ) {
                                AsyncImage(
                                    model = url,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }

                // Info Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = product.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = ChocoBrown
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${formatRupiah(product.price)} • Kategori: ${product.cat}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = GoldWarm
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Deskripsi Produk",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (product.desc.isNotBlank()) product.desc else "Kue basah tradisional khas Djandes berkualitas premium, dibuat dengan bahan higienis dan cita rasa istimewa.",
                        fontSize = 12.5.sp,
                        lineHeight = 17.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Quantity Counter
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "Jumlah Pesanan:",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilledIconButton(
                                onClick = { if (quantity > 1) quantity-- },
                                enabled = quantity > 1 && !product.isOutOfStock,
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = ChocoBrown.copy(alpha = 0.15f),
                                    contentColor = ChocoBrown
                                ),
                                modifier = Modifier.size(36.dp).testTag("btn_detail_qty_minus")
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Kurang")
                            }

                            Text(
                                text = "$quantity",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )

                            FilledIconButton(
                                onClick = { quantity++ },
                                enabled = !product.isOutOfStock,
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = ChocoBrown,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier.size(36.dp).testTag("btn_detail_qty_plus")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Tambah")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Subtotal preview & Add Button
                    Button(
                        onClick = {
                            onAddToCart(product, quantity)
                            onDismiss()
                        },
                        enabled = !product.isOutOfStock,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ChocoBrown),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_add_to_cart_detail")
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (product.isOutOfStock) "Stok Habis" else "Tambah ke Keranjang • ${formatRupiah(product.price * quantity)}",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (isInCart) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = {
                                onRemoveFromCart()
                                onDismiss()
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = OutOfStockRed),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("btn_detail_remove_cart")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Hapus dari Keranjang", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

