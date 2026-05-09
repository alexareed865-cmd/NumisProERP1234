package com.numisproerp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.numisproerp.data.entities.Product
import kotlinx.coroutines.flow.Flow

data class ProductWithStock(
    val catalogId: String,
    val name: String,
    val series: String,
    val category: String,
    val quality: String,
    val material: String,
    val nominal: String,
    val photoPath: String,
    val totalPurchased: Int,
    val totalSold: Int,
    val currentStock: Int,
    val avgPurchasePrice: Double
)

data class ProductForSelection(
    val catalogId: String,
    val name: String,
    val series: String,
    val category: String
)

data class ProductInStock(
    val catalogId: String,
    val name: String,
    val series: String,
    val category: String,
    val material: String,
    val currentStock: Int,
    val avgPurchasePrice: Double
)

@Dao
interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: Product)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<Product>)

    @Query("SELECT * FROM products ORDER BY name")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products ORDER BY name")
    suspend fun getAllProductsSync(): List<Product>

    @Query("SELECT catalogId, name, series, category FROM products ORDER BY name")
    suspend fun getProductsForSelection(): List<ProductForSelection>

    @Query("SELECT * FROM products WHERE catalogId = :catalogId")
    suspend fun getProductById(catalogId: String): Product?

    /**
     * Залишок = SUM(purchases) − SUM(sales) − SUM(writeoffs).
     * Списання повинні зменшувати залишок та виключатися з доступного для продажу.
     */
    @Query("""
        SELECT 
            p.catalogId,
            p.name,
            p.series,
            p.category,
            p.quality,
            p.material,
            p.nominal,
            p.photoPath,
            COALESCE((
                SELECT SUM(quantity) 
                FROM purchases 
                WHERE catalogId = p.catalogId
            ), 0) as totalPurchased,
            COALESCE((
                SELECT SUM(quantity) 
                FROM sales 
                WHERE catalogId = p.catalogId
            ), 0) as totalSold,
            COALESCE((
                SELECT SUM(quantity) 
                FROM purchases 
                WHERE catalogId = p.catalogId
            ), 0) - COALESCE((
                SELECT SUM(quantity) 
                FROM sales 
                WHERE catalogId = p.catalogId
            ), 0) - COALESCE((
                SELECT SUM(quantity)
                FROM writeoffs
                WHERE catalogId = p.catalogId
            ), 0) as currentStock,
            COALESCE((
                SELECT SUM(totalAmount) / SUM(quantity)
                FROM purchases 
                WHERE catalogId = p.catalogId AND quantity > 0
            ), 0.0) as avgPurchasePrice
        FROM products p
        WHERE p.name LIKE '%' || :searchQuery || '%' 
           OR p.series LIKE '%' || :searchQuery || '%'
        ORDER BY p.name
    """)
    fun getProductsWithStock(searchQuery: String = ""): Flow<List<ProductWithStock>>

    @Query("""
        SELECT 
            p.catalogId,
            p.name,
            p.series,
            p.category,
            p.quality,
            p.material,
            p.nominal,
            p.photoPath,
            COALESCE((
                SELECT SUM(quantity) 
                FROM purchases 
                WHERE catalogId = p.catalogId
            ), 0) as totalPurchased,
            COALESCE((
                SELECT SUM(quantity) 
                FROM sales 
                WHERE catalogId = p.catalogId
            ), 0) as totalSold,
            COALESCE((
                SELECT SUM(quantity) 
                FROM purchases 
                WHERE catalogId = p.catalogId
            ), 0) - COALESCE((
                SELECT SUM(quantity) 
                FROM sales 
                WHERE catalogId = p.catalogId
            ), 0) - COALESCE((
                SELECT SUM(quantity)
                FROM writeoffs
                WHERE catalogId = p.catalogId
            ), 0) as currentStock,
            COALESCE((
                SELECT SUM(totalAmount) / SUM(quantity)
                FROM purchases 
                WHERE catalogId = p.catalogId AND quantity > 0
            ), 0.0) as avgPurchasePrice
        FROM products p
        WHERE p.category = :category
        ORDER BY p.name
    """)
    fun getProductsWithStockByCategory(category: String): Flow<List<ProductWithStock>>

    @Query("""
        SELECT 
            p.catalogId,
            p.name,
            p.series,
            p.category,
            p.material,
            COALESCE((
                SELECT SUM(quantity) FROM purchases WHERE catalogId = p.catalogId
            ), 0) - COALESCE((
                SELECT SUM(quantity) FROM sales WHERE catalogId = p.catalogId
            ), 0) - COALESCE((
                SELECT SUM(quantity) FROM writeoffs WHERE catalogId = p.catalogId
            ), 0) as currentStock,
            COALESCE((
                SELECT SUM(totalAmount) / SUM(quantity)
                FROM purchases 
                WHERE catalogId = p.catalogId AND quantity > 0
            ), 0.0) as avgPurchasePrice
        FROM products p
        WHERE COALESCE((
            SELECT SUM(quantity) FROM purchases WHERE catalogId = p.catalogId
        ), 0) - COALESCE((
            SELECT SUM(quantity) FROM sales WHERE catalogId = p.catalogId
        ), 0) - COALESCE((
            SELECT SUM(quantity) FROM writeoffs WHERE catalogId = p.catalogId
        ), 0) > 0
        ORDER BY p.name
    """)
    fun getProductsInStock(): Flow<List<ProductInStock>>

    @Query("SELECT DISTINCT category FROM products WHERE category IS NOT NULL AND category != ''")
    suspend fun getDistinctCategories(): List<String>

    @Query("DELETE FROM products")
    suspend fun deleteAll()
}
