package com.example.util

import com.example.data.model.OrderEntity
import com.example.data.model.PackagingTypeEntity
import com.example.data.model.PackagingVariantEntity
import com.example.data.model.ProductEntity
import com.example.data.model.StoreProfile
import org.json.JSONArray
import org.json.JSONObject

data class CatalogBackupData(
    val version: Int = 1,
    val exportedAt: Long = System.currentTimeMillis(),
    val storeProfile: StoreProfile? = null,
    val products: List<ProductEntity> = emptyList(),
    val packagingTypes: List<PackagingTypeEntity> = emptyList(),
    val packagingVariants: List<PackagingVariantEntity> = emptyList(),
    val orders: List<OrderEntity> = emptyList()
)

object DataBackupHelper {

    /**
     * Export Admin Master Data:
     * Includes Store Branding & Info (Djandes, Sweet & Savoury, Logo, WA, Address),
     * Products Catalog, and Packaging Boxes & Variants.
     * EXCLUDES sales orders / transaction reports.
     */
    fun exportCatalogToJson(
        products: List<ProductEntity>,
        packagingTypes: List<PackagingTypeEntity>,
        packagingVariants: List<PackagingVariantEntity>,
        storeProfile: StoreProfile = StoreProfile()
    ): String {
        val root = JSONObject()
        root.put("app", "Djandes POS & Master Catalog")
        root.put("type", "MASTER_CATALOG_AND_STORE")
        root.put("version", 1)
        root.put("exportedAt", System.currentTimeMillis())

        // Store Profile / Branding
        val storeObj = JSONObject()
        storeObj.put("name", storeProfile.name)
        storeObj.put("tagline", storeProfile.tagline)
        storeObj.put("description", storeProfile.description)
        storeObj.put("logo", storeProfile.logo)
        storeObj.put("whatsapp", storeProfile.whatsapp)
        storeObj.put("instagram", storeProfile.instagram)
        storeObj.put("tiktok", storeProfile.tiktok)
        storeObj.put("address", storeProfile.address)
        storeObj.put("receiptGreeting", storeProfile.receiptGreeting)
        storeObj.put("showReceiptGreeting", storeProfile.showReceiptGreeting)
        storeObj.put("showSocialMedia", storeProfile.showSocialMedia)
        storeObj.put("showNotesOnReceipt", storeProfile.showNotesOnReceipt)
        root.put("storeProfile", storeObj)

        // Products
        val productsArr = JSONArray()
        for (p in products) {
            val pObj = JSONObject()
            pObj.put("id", p.id)
            pObj.put("name", p.name)
            pObj.put("price", p.price)
            pObj.put("cat", p.cat)
            pObj.put("img", p.img)
            pObj.put("desc", p.desc)
            pObj.put("bestseller", p.bestseller)
            pObj.put("isNew", p.isNew)
            pObj.put("isPromo", p.isPromo)
            pObj.put("isOutOfStock", p.isOutOfStock)
            pObj.put("imagesJson", p.imagesJson)
            productsArr.put(pObj)
        }
        root.put("products", productsArr)

        // Packaging Types
        val typesArr = JSONArray()
        for (t in packagingTypes) {
            val tObj = JSONObject()
            tObj.put("id_tipe", t.id_tipe)
            tObj.put("harga_tambahan", t.harga_tambahan)
            typesArr.put(tObj)
        }
        root.put("packagingTypes", typesArr)

        // Packaging Variants
        val varsArr = JSONArray()
        for (v in packagingVariants) {
            val vObj = JSONObject()
            vObj.put("id_varian", v.id_varian)
            vObj.put("id_tipe", v.id_tipe)
            vObj.put("name", v.name)
            vObj.put("img", v.img)
            vObj.put("imagesJson", v.imagesJson)
            vObj.put("desc", v.desc)
            vObj.put("featuresJson", v.featuresJson)
            varsArr.put(vObj)
        }
        root.put("packagingVariants", varsArr)

        return root.toString(2)
    }

    /**
     * Export Orders / Sales Transactions for Reports
     */
    fun exportOrdersToJson(orders: List<OrderEntity>): String {
        val root = JSONObject()
        root.put("app", "Djandes POS & Orders")
        root.put("type", "ORDERS_AND_TRANSACTIONS")
        root.put("version", 1)
        root.put("exportedAt", System.currentTimeMillis())

        val ordersArr = JSONArray()
        for (o in orders) {
            val oObj = JSONObject()
            oObj.put("id", o.id)
            oObj.put("orderNumber", o.orderNumber)
            oObj.put("customerName", o.customerName)
            oObj.put("pickupDate", o.pickupDate)
            oObj.put("pickupTime", o.pickupTime)
            oObj.put("packagingType", o.packagingType)
            oObj.put("packagingVariant", o.packagingVariant)
            oObj.put("packagingPrice", o.packagingPrice)
            oObj.put("itemsJson", o.itemsJson)
            oObj.put("subtotal", o.subtotal)
            oObj.put("total", o.total)
            oObj.put("paymentMethod", o.paymentMethod)
            oObj.put("amountPaid", o.amountPaid)
            oObj.put("paymentStatus", o.paymentStatus)
            oObj.put("changeOrRemaining", o.changeOrRemaining)
            oObj.put("notes", o.notes)
            oObj.put("createdAt", o.createdAt)
            oObj.put("status", o.status)
            oObj.put("initialDeposit", o.initialDeposit)
            oObj.put("settlementPaid", o.settlementPaid)
            ordersArr.put(oObj)
        }
        root.put("orders", ordersArr)

        return root.toString(2)
    }

    /**
     * Saves JSON backup content into internal storage Downloads/DP/JSON/
     */
    suspend fun saveJsonToStorage(
        context: android.content.Context,
        fileName: String,
        jsonContent: String
    ): Result<String> {
        return AppStorageHelper.saveToDownloads(
            context = context,
            subFolder = AppStorageHelper.SUBFOLDER_JSON,
            fileName = fileName,
            mimeType = "application/json"
        ) { os ->
            os.write(jsonContent.toByteArray(Charsets.UTF_8))
        }
    }

    /**
     * Shares JSON text directly
     */
    fun shareJsonContent(
        context: android.content.Context,
        title: String,
        jsonContent: String
    ) {
        AppStorageHelper.shareText(context, jsonContent, title)
    }

    fun parseFromJson(jsonStr: String): Result<CatalogBackupData> {
        return try {
            val root = JSONObject(jsonStr)
            val productsList = mutableListOf<ProductEntity>()
            val typesList = mutableListOf<PackagingTypeEntity>()
            val varsList = mutableListOf<PackagingVariantEntity>()
            val ordersList = mutableListOf<OrderEntity>()
            var storeProfile: StoreProfile? = null

            if (root.has("storeProfile")) {
                val s = root.getJSONObject("storeProfile")
                storeProfile = StoreProfile(
                    name = s.optString("name", "Djandes"),
                    tagline = s.optString("tagline", "Sweet & Savoury"),
                    description = s.optString("description", ""),
                    logo = s.optString("logo", "https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/logo.png"),
                    whatsapp = s.optString("whatsapp", "6285812006225"),
                    instagram = s.optString("instagram", "djandes15"),
                    tiktok = s.optString("tiktok", "djandes15"),
                    address = s.optString("address", ""),
                    receiptGreeting = s.optString("receiptGreeting", "Terima kasih atas pesanan Anda. Simpan nota ini saat pengambilan kue."),
                    showReceiptGreeting = s.optBoolean("showReceiptGreeting", true),
                    showSocialMedia = s.optBoolean("showSocialMedia", true),
                    showNotesOnReceipt = s.optBoolean("showNotesOnReceipt", true)
                )
            }

            if (root.has("products")) {
                val pArr = root.getJSONArray("products")
                for (i in 0 until pArr.length()) {
                    val p = pArr.getJSONObject(i)
                    productsList.add(
                        ProductEntity(
                            id = if (p.has("id")) p.getLong("id") else 0L,
                            name = p.optString("name", "Produk"),
                            price = p.optLong("price", 0L),
                            cat = p.optString("cat", "Hantaran"),
                            img = p.optString("img", ""),
                            desc = p.optString("desc", ""),
                            bestseller = p.optBoolean("bestseller", false),
                            isNew = p.optBoolean("isNew", false),
                            isPromo = p.optBoolean("isPromo", false),
                            isOutOfStock = p.optBoolean("isOutOfStock", false),
                            imagesJson = p.optString("imagesJson", "[]")
                        )
                    )
                }
            }

            if (root.has("packagingTypes")) {
                val tArr = root.getJSONArray("packagingTypes")
                for (i in 0 until tArr.length()) {
                    val t = tArr.getJSONObject(i)
                    typesList.add(
                        PackagingTypeEntity(
                            id_tipe = t.optString("id_tipe", "Standard"),
                            harga_tambahan = t.optLong("harga_tambahan", 0L)
                        )
                    )
                }
            }

            if (root.has("packagingVariants")) {
                val vArr = root.getJSONArray("packagingVariants")
                for (i in 0 until vArr.length()) {
                    val v = vArr.getJSONObject(i)
                    varsList.add(
                        PackagingVariantEntity(
                            id_varian = v.optString("id_varian", "var_$i"),
                            id_tipe = v.optString("id_tipe", "Standard"),
                            name = v.optString("name", v.optString("nama_varian", "Standard")),
                            img = v.optString("img", ""),
                            imagesJson = v.optString("imagesJson", "[]"),
                            desc = v.optString("desc", v.optString("deskripsi", "")),
                            featuresJson = v.optString("featuresJson", "[]")
                        )
                    )
                }
            }

            if (root.has("orders")) {
                val oArr = root.getJSONArray("orders")
                for (i in 0 until oArr.length()) {
                    val o = oArr.getJSONObject(i)
                    ordersList.add(
                        OrderEntity(
                            id = if (o.has("id")) o.getLong("id") else 0L,
                            orderNumber = o.optString("orderNumber", "DJD-000"),
                            customerName = o.optString("customerName", "Pelanggan"),
                            pickupDate = o.optString("pickupDate", ""),
                            pickupTime = o.optString("pickupTime", ""),
                            packagingType = o.optString("packagingType", "Standard"),
                            packagingVariant = o.optString("packagingVariant", "Standard"),
                            packagingPrice = o.optLong("packagingPrice", 0L),
                            itemsJson = o.optString("itemsJson", "[]"),
                            subtotal = o.optLong("subtotal", 0L),
                            total = o.optLong("total", 0L),
                            paymentMethod = o.optString("paymentMethod", "Tunai"),
                            amountPaid = o.optLong("amountPaid", 0L),
                            paymentStatus = o.optString("paymentStatus", "LUNAS"),
                            changeOrRemaining = o.optLong("changeOrRemaining", 0L),
                            notes = o.optString("notes", ""),
                            createdAt = o.optLong("createdAt", System.currentTimeMillis()),
                            status = o.optString("status", "Selesai"),
                            initialDeposit = o.optLong("initialDeposit", 0L),
                            settlementPaid = o.optLong("settlementPaid", 0L)
                        )
                    )
                }
            }

            Result.success(
                CatalogBackupData(
                    version = root.optInt("version", 1),
                    exportedAt = root.optLong("exportedAt", System.currentTimeMillis()),
                    storeProfile = storeProfile,
                    products = productsList,
                    packagingTypes = typesList,
                    packagingVariants = varsList,
                    orders = ordersList
                )
            )
        } catch (e: Exception) {
            Result.failure(Exception("Format JSON data tidak valid: ${e.localizedMessage}"))
        }
    }
}
