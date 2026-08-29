package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val price: Long,
    val cat: String,
    val img: String,
    val imagesJson: String = "[]",
    val desc: String = "",
    val bestseller: Boolean = false,
    val isNew: Boolean = false,
    val isPromo: Boolean = false,
    val isOutOfStock: Boolean = false
) {
    fun getImageList(): List<String> {
        val list = mutableListOf<String>()
        if (img.isNotBlank()) list.add(img)
        try {
            val jsonArray = JSONArray(imagesJson)
            for (i in 0 until jsonArray.length()) {
                val url = jsonArray.optString(i)
                if (url.isNotBlank() && !list.contains(url)) {
                    list.add(url)
                }
            }
        } catch (e: Exception) {
            // ignore
        }
        return if (list.isEmpty()) listOf("https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/logo.png") else list
    }
}

@Entity(tableName = "packaging_types")
data class PackagingTypeEntity(
    @PrimaryKey val id_tipe: String,
    val harga_tambahan: Long
)

@Entity(tableName = "packaging_variants")
data class PackagingVariantEntity(
    @PrimaryKey val id_varian: String,
    val id_tipe: String,
    val name: String,
    val img: String,
    val imagesJson: String = "[]",
    val desc: String = "",
    val featuresJson: String = "[]"
) {
    fun getFeatureList(): List<String> {
        val list = mutableListOf<String>()
        try {
            val jsonArray = JSONArray(featuresJson)
            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.optString(i)
                if (item.isNotBlank()) list.add(item)
            }
        } catch (e: Exception) {
            // ignore
        }
        return list
    }
}

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderNumber: String,
    val customerName: String,
    val pickupDate: String, // e.g. "26/08/2026" or "26 Agu 2026"
    val pickupTime: String, // e.g. "14:30" (24-hour format)
    val packagingType: String,
    val packagingVariant: String,
    val packagingPrice: Long,
    val itemsJson: String,
    val subtotal: Long,
    val total: Long,
    val paymentMethod: String = "Tunai",
    val amountPaid: Long = 0L,
    val paymentStatus: String = "LUNAS", // "LUNAS", "DP", "PENDING"
    val changeOrRemaining: Long = 0L,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val status: String = "Selesai",
    val initialDeposit: Long = 0L, // DP Lama / Awal
    val settlementPaid: Long = 0L  // Pelunasan terakhir
) {
    fun getCartItems(): List<CartItem> {
        val items = mutableListOf<CartItem>()
        try {
            val jsonArray = JSONArray(itemsJson)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                items.add(
                    CartItem(
                        productId = obj.optLong("productId"),
                        productName = obj.optString("productName"),
                        price = obj.optLong("price"),
                        quantity = obj.optInt("quantity"),
                        img = obj.optString("img")
                    )
                )
            }
        } catch (e: Exception) {
            // ignore
        }
        return items
    }

    fun getActualCashReceived(): Long {
        return when (paymentStatus) {
            "LUNAS" -> if (amountPaid > 0L) minOf(total, amountPaid) else total
            "DP" -> if (amountPaid > 0L) minOf(total, amountPaid) else (if (initialDeposit > 0L) minOf(total, initialDeposit) else 0L)
            else -> if (amountPaid > 0L) minOf(total, amountPaid) else 0L
        }
    }

    fun getOutstandingRemaining(): Long {
        return when (paymentStatus) {
            "LUNAS" -> 0L
            else -> maxOf(0L, total - getActualCashReceived())
        }
    }
}

data class CartItem(
    val productId: Long,
    val productName: String,
    val price: Long,
    var quantity: Int,
    val img: String
) {
    val subtotal: Long get() = price * quantity

    fun toJson(): JSONObject {
        val obj = JSONObject()
        obj.put("productId", productId)
        obj.put("productName", productName)
        obj.put("price", price)
        obj.put("quantity", quantity)
        obj.put("img", img)
        return obj
    }
}

data class StoreProfile(
    val name: String = "Djandes",
    val tagline: String = "Sweet & Savoury",
    val description: String = "DJANDES adalah home made kue basah lokal yang menyajikan berbagai macam kue tradisional dan modern dengan cita rasa autentik dan kualitas terbaik.",
    val logo: String = "https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/logo.png",
    val whatsapp: String = "6285812006225",
    val instagram: String = "djandes15",
    val tiktok: String = "djandes15",
    val address: String = "Jl. Anggrek RT 004 / RW 013, Tegalrejo - Sawentar, Kanigoro - Blitar",
    val receiptGreeting: String = "Terima kasih atas pesanan Anda. Simpan nota ini saat pengambilan kue.",
    val showReceiptGreeting: Boolean = true,
    val showSocialMedia: Boolean = true,
    val showNotesOnReceipt: Boolean = true
)

data class MonthlySalesStat(
    val monthYear: String, // e.g. "Agu 2026"
    val totalRevenue: Long,
    val totalOrders: Int
)

data class DailySalesSummary(
    val dateString: String,
    val totalRevenue: Long,
    val totalOrders: Int,
    val totalItemsSold: Int,
    val orders: List<OrderEntity>
)

data class BluetoothPrinterDevice(
    val name: String,
    val address: String,
    val isConnected: Boolean = false
)
