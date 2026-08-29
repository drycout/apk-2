package com.example.data.local

import androidx.room.*
import com.example.data.model.OrderEntity
import com.example.data.model.PackagingTypeEntity
import com.example.data.model.PackagingVariantEntity
import com.example.data.model.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Long): ProductEntity?

    @Query("SELECT COUNT(*) FROM products")
    suspend fun getProductCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<ProductEntity>)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProductById(id: Long)

    @Query("DELETE FROM products")
    suspend fun deleteAllProducts()
}

@Dao
interface PackagingDao {
    @Query("SELECT * FROM packaging_types")
    fun getAllTypes(): Flow<List<PackagingTypeEntity>>

    @Query("SELECT * FROM packaging_variants")
    fun getAllVariants(): Flow<List<PackagingVariantEntity>>

    @Query("SELECT * FROM packaging_variants WHERE id_tipe = :typeId")
    fun getVariantsByType(typeId: String): Flow<List<PackagingVariantEntity>>

    @Query("SELECT COUNT(*) FROM packaging_types")
    suspend fun getTypeCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertType(type: PackagingTypeEntity)

    @Update
    suspend fun updateType(type: PackagingTypeEntity)

    @Delete
    suspend fun deleteType(type: PackagingTypeEntity)

    @Query("DELETE FROM packaging_types WHERE id_tipe = :id")
    suspend fun deleteTypeById(id: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVariant(variant: PackagingVariantEntity)

    @Update
    suspend fun updateVariant(variant: PackagingVariantEntity)

    @Delete
    suspend fun deleteVariant(variant: PackagingVariantEntity)

    @Query("DELETE FROM packaging_variants WHERE id_varian = :id")
    suspend fun deleteVariantById(id: String)

    @Query("DELETE FROM packaging_variants WHERE id_tipe = :typeId")
    suspend fun deleteVariantsByType(typeId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTypes(types: List<PackagingTypeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVariants(variants: List<PackagingVariantEntity>)

    @Query("DELETE FROM packaging_types")
    suspend fun deleteAllTypes()

    @Query("DELETE FROM packaging_variants")
    suspend fun deleteAllVariants()
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE id = :id")
    suspend fun getOrderById(id: Long): OrderEntity?

    @Query("SELECT * FROM orders WHERE createdAt >= :startTimestamp AND createdAt <= :endTimestamp ORDER BY createdAt DESC")
    fun getOrdersBetween(startTimestamp: Long, endTimestamp: Long): Flow<List<OrderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(orders: List<OrderEntity>)

    @Update
    suspend fun updateOrder(order: OrderEntity)

    @Delete
    suspend fun deleteOrder(order: OrderEntity)

    @Query("DELETE FROM orders WHERE id = :id")
    suspend fun deleteOrderById(id: Long)

    @Query("DELETE FROM orders")
    suspend fun deleteAllOrders()
}
