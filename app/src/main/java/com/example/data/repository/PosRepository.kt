package com.example.data.repository

import com.example.data.local.OrderDao
import com.example.data.local.PackagingDao
import com.example.data.local.ProductDao
import com.example.data.local.SeedData
import com.example.data.model.OrderEntity
import com.example.data.model.PackagingTypeEntity
import com.example.data.model.PackagingVariantEntity
import com.example.data.model.ProductEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class PosRepository(
    private val productDao: ProductDao,
    private val packagingDao: PackagingDao,
    private val orderDao: OrderDao
) {
    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProducts()
    val allPackagingTypes: Flow<List<PackagingTypeEntity>> = packagingDao.getAllTypes()
    val allPackagingVariants: Flow<List<PackagingVariantEntity>> = packagingDao.getAllVariants()
    val allOrders: Flow<List<OrderEntity>> = orderDao.getAllOrders()

    fun getVariantsByType(typeId: String): Flow<List<PackagingVariantEntity>> {
        return packagingDao.getVariantsByType(typeId)
    }

    suspend fun insertProduct(product: ProductEntity) = withContext(Dispatchers.IO) {
        productDao.insertProduct(product)
    }

    suspend fun updateProduct(product: ProductEntity) = withContext(Dispatchers.IO) {
        productDao.updateProduct(product)
    }

    suspend fun deleteProduct(product: ProductEntity) = withContext(Dispatchers.IO) {
        productDao.deleteProduct(product)
    }

    suspend fun deleteProductById(id: Long) = withContext(Dispatchers.IO) {
        productDao.deleteProductById(id)
    }

    suspend fun insertPackagingType(type: PackagingTypeEntity) = withContext(Dispatchers.IO) {
        packagingDao.insertType(type)
    }

    suspend fun updatePackagingType(type: PackagingTypeEntity) = withContext(Dispatchers.IO) {
        packagingDao.updateType(type)
    }

    suspend fun deletePackagingType(type: PackagingTypeEntity) = withContext(Dispatchers.IO) {
        packagingDao.deleteType(type)
        packagingDao.deleteVariantsByType(type.id_tipe)
    }

    suspend fun insertPackagingVariant(variant: PackagingVariantEntity) = withContext(Dispatchers.IO) {
        packagingDao.insertVariant(variant)
    }

    suspend fun updatePackagingVariant(variant: PackagingVariantEntity) = withContext(Dispatchers.IO) {
        packagingDao.updateVariant(variant)
    }

    suspend fun deletePackagingVariant(variant: PackagingVariantEntity) = withContext(Dispatchers.IO) {
        packagingDao.deleteVariant(variant)
    }

    suspend fun insertOrder(order: OrderEntity): Long = withContext(Dispatchers.IO) {
        orderDao.insertOrder(order)
    }

    suspend fun insertOrders(orders: List<OrderEntity>) = withContext(Dispatchers.IO) {
        orderDao.insertAll(orders)
    }

    suspend fun updateOrder(order: OrderEntity) = withContext(Dispatchers.IO) {
        orderDao.updateOrder(order)
    }

    suspend fun deleteOrder(order: OrderEntity) = withContext(Dispatchers.IO) {
        orderDao.deleteOrder(order)
    }

    suspend fun deleteOrderById(id: Long) = withContext(Dispatchers.IO) {
        orderDao.deleteOrderById(id)
    }

    suspend fun restoreAllData(
        products: List<ProductEntity>,
        types: List<PackagingTypeEntity>,
        variants: List<PackagingVariantEntity>,
        orders: List<OrderEntity> = emptyList()
    ) = withContext(Dispatchers.IO) {
        productDao.deleteAllProducts()
        packagingDao.deleteAllVariants()
        packagingDao.deleteAllTypes()
        if (orders.isNotEmpty()) {
            orderDao.deleteAllOrders()
        }

        if (types.isNotEmpty()) packagingDao.insertTypes(types)
        if (variants.isNotEmpty()) packagingDao.insertVariants(variants)
        if (products.isNotEmpty()) productDao.insertAll(products)
        if (orders.isNotEmpty()) orderDao.insertAll(orders)
    }

    suspend fun mergeData(
        products: List<ProductEntity>,
        types: List<PackagingTypeEntity>,
        variants: List<PackagingVariantEntity>,
        orders: List<OrderEntity> = emptyList()
    ) = withContext(Dispatchers.IO) {
        if (types.isNotEmpty()) packagingDao.insertTypes(types)
        if (variants.isNotEmpty()) packagingDao.insertVariants(variants)
        if (products.isNotEmpty()) productDao.insertAll(products)
        if (orders.isNotEmpty()) orderDao.insertAll(orders)
    }

    suspend fun ensureDataSeeded() = withContext(Dispatchers.IO) {
        if (productDao.getProductCount() == 0) {
            packagingDao.insertTypes(SeedData.initialPackagingTypes)
            packagingDao.insertVariants(SeedData.initialPackagingVariants)
            productDao.insertAll(SeedData.initialProducts)
        }
    }
}
