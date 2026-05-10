package com.numisproerp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.numisproerp.data.entities.Sale

data class SaleWithProductName(
    val saleId: String,
    val date: Long,
    val catalogId: String,
    val productName: String,
    val quantity: Int,
    val pricePerUnit: Double,
    val totalAmount: Double
)

data class SaleWithDetails(
    val saleId: String,
    val date: Long,
    val totalAmount: Double,
    val productName: String,
    val clientName: String
)

data class SaleWithClientName(
    val saleId: String,
    val date: Long,
    val catalogId: String,
    val productName: String,
    val clientName: String,
    val quantity: Int,
    val pricePerUnit: Double,
    val totalAmount: Double
)

@Dao
interface SaleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sale: Sale)

    @Query("SELECT SUM(totalAmount) FROM sales")
    suspend fun getTotalSum(): Double?

    @Query("SELECT SUM(totalAmount) FROM sales WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getSumByDateRange(startDate: Long, endDate: Long): Double?

    @Query("""
        SELECT s.saleId, s.date, s.totalAmount, 
               pr.name as productName, c.name as clientName
        FROM sales s
        JOIN products pr ON s.catalogId = pr.catalogId
        JOIN clients c ON s.clientId = c.clientId
        ORDER BY s.date DESC
        LIMIT :limit
    """)
    suspend fun getRecentWithDetails(limit: Int): List<SaleWithDetails>

    @Query("""
        SELECT s.saleId, s.date, s.catalogId, s.quantity, s.pricePerUnit, s.totalAmount,
               pr.name as productName
        FROM sales s
        JOIN products pr ON s.catalogId = pr.catalogId
        WHERE s.clientId = :clientId
        ORDER BY s.date DESC
    """)
    suspend fun getSalesByClient(clientId: String): List<SaleWithProductName>

    @Query("""
        SELECT 
            p.catalogId,
            p.name,
            COALESCE((
                SELECT SUM(quantity) FROM purchases WHERE catalogId = p.catalogId
            ), 0) + COALESCE((
                SELECT quantity FROM collection_items WHERE collectionId = p.catalogId
            ), 0) - COALESCE((
                SELECT SUM(quantity) FROM sales WHERE catalogId = p.catalogId
            ), 0) - COALESCE((
                SELECT SUM(quantity) FROM writeoffs WHERE catalogId = p.catalogId
            ), 0) as currentStock,
            COALESCE((
                SELECT AVG(pricePerUnit) FROM purchases WHERE catalogId = p.catalogId
            ), 0.0) as avgPurchasePrice
        FROM products p
        WHERE p.catalogId = :catalogId
    """)
    suspend fun getProductStockInfo(catalogId: String): ProductStockInfo?

    @Query("SELECT * FROM sales ORDER BY date DESC")
    suspend fun getAllSales(): List<Sale>

    @Query("""
        SELECT s.saleId, s.date, s.catalogId, s.quantity, s.pricePerUnit, s.totalAmount,
               pr.name as productName, c.name as clientName
        FROM sales s
        JOIN products pr ON s.catalogId = pr.catalogId
        JOIN clients c ON s.clientId = c.clientId
        ORDER BY s.date DESC
    """)
    suspend fun getAllSalesWithClientName(): List<SaleWithClientName>

    @Query("SELECT SUM(quantity) FROM sales WHERE catalogId = :catalogId")
    suspend fun getTotalQuantitySold(catalogId: String): Int

    @Query("DELETE FROM sales")
    suspend fun deleteAll()
}