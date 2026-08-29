package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.CartItem
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.PosViewModel

@Composable
fun CartScreen(
    viewModel: PosViewModel,
    onNavigateToCatalog: () -> Unit = {}
) {
    val cartItems by viewModel.cartItems.collectAsState()
    val subtotal by viewModel.cartSubtotal.collectAsState()

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
                color = ChocoBrown.copy(alpha = 0.12f),
                shape = CircleShape,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.ShoppingBag,
                        contentDescription = null,
                        tint = ChocoBrown,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Keranjang Belanja",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = ChocoBrown
                )
                Text(
                    text = "Daftar pesanan jajanan siap diproses",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (cartItems.isEmpty()) {
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
                        color = ChocoBrown.copy(alpha = 0.08f),
                        shape = CircleShape,
                        modifier = Modifier.size(76.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.RemoveShoppingCart,
                                contentDescription = null,
                                tint = ChocoBrown,
                                modifier = Modifier.size(38.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Keranjang Belanja Kosong",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ChocoBrown
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Belum ada item pesanan yang dipilih. Silakan buka menu Katalog untuk menambahkan jajanan pilihan Anda.",
                        fontSize = 12.5.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Button(
                        onClick = onNavigateToCatalog,
                        colors = ButtonDefaults.buttonColors(containerColor = ChocoBrown),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Buka Katalog Jajanan", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            val nonParcelCount = remember(cartItems) { viewModel.getNonParcelQuantity() }
            val parcelCount = remember(cartItems) { viewModel.getParcelQuantity() }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Daftar Item (${cartItems.size} Jenis)",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = ChocoBrown
                    )
                    TextButton(
                        onClick = { viewModel.clearCart() },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = Color(0xFFC62828), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Kosongkan", color = Color(0xFFC62828), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(cartItems, key = { it.productId }) { item ->
                        CartItemRow(
                            item = item,
                            onAdd = { viewModel.updateCartItemQuantity(item.productId, 1) },
                            onMinus = { viewModel.updateCartItemQuantity(item.productId, -1) },
                            onRemove = { viewModel.removeFromCart(item.productId) },
                            onImageClick = {
                                viewModel.openFullImageView(item.img, item.productName, formatRupiah(item.price))
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Summary & Checkout Button Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(3.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Total Produk Kue:", fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                if (parcelCount > 0) "$nonParcelCount item kue + $parcelCount parcel" else "$nonParcelCount item kue",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Subtotal Produk:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(formatRupiah(subtotal), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = ChocoBrown)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "*Pilihan kemasan/box dan jadwal pengambilan ditentukan di langkah berikutnya.",
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { viewModel.openCheckout() },
                            colors = ButtonDefaults.buttonColors(containerColor = ChocoBrown),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("btn_cart_checkout")
                        ) {
                            Icon(Icons.Default.Inbox, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Pilih Box & Jadwal Pengambilan",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
