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
import com.example.data.model.PackagingTypeEntity
import com.example.data.model.PackagingVariantEntity
import com.example.ui.theme.ChocoBrown
import com.example.ui.theme.GoldWarm
import com.example.ui.theme.Maroon40

@Composable
fun BoxDetailDialog(
    variant: PackagingVariantEntity,
    packagingType: PackagingTypeEntity? = null,
    onDismiss: () -> Unit,
    onSelectVariant: ((PackagingVariantEntity) -> Unit)? = null,
    onImageClick: (String) -> Unit
) {
    val features = remember(variant) { variant.getFeatureList() }

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
                // Header Image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp)
                        .clickable { onImageClick(variant.img) }
                ) {
                    AsyncImage(
                        model = variant.img,
                        contentDescription = variant.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Zoom indicator badge
                    Surface(
                        color = Color.Black.copy(alpha = 0.55f),
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
                            Text("Klik untuk Perbesar", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Close Button
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            .testTag("btn_close_box_detail")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Color.White)
                    }

                    // Type Badge
                    Surface(
                        color = Maroon40,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Tipe: ${variant.id_tipe}",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Info Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Varian Box: ${variant.name}",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = Maroon40
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    val extraFee = packagingType?.harga_tambahan ?: when {
                        variant.id_tipe.contains("Artificial", ignoreCase = true) -> 15000L
                        variant.id_tipe.contains("Mika", ignoreCase = true) -> 5000L
                        else -> 0L
                    }

                    Text(
                        text = if (extraFee > 0) "+${formatRupiah(extraFee)} / pesanan (${variant.id_tipe})" else "Gratis Kemasan Standard",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (extraFee > 0) GoldWarm else Color(0xFF2E7D32)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (variant.desc.isNotBlank()) {
                        Text(
                            text = "Deskripsi Kemasan:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = variant.desc,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    if (features.isNotEmpty()) {
                        Text(
                            text = "Keunggulan & Detail Box:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        features.forEach { feat ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = GoldWarm,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = feat,
                                    fontSize = 12.5.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Action Button
                    if (onSelectVariant != null) {
                        Button(
                            onClick = {
                                onSelectVariant(variant)
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Maroon40),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("btn_select_box_variant")
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Pilih Varian Box Ini",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                        ) {
                            Text("Tutup", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
