package com.numisproerp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.numisproerp.data.entities.Writeoff

data class WriteoffWithProductName(
    val writeoffId: String,
    val date: Long,
    val catalogId: String,
    val productName: String,
    val quantity: Int,
    val pricePerUnit: Double,
    val totalAmount: Double,
    val reason: String,
    val comment: String
)

@Dao
interface WriteoffDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(writeoff: Writeoff)

    @Query("SELECT * FROM writeoffs ORDER BY date DESC")
    suspend fun getAll(): List<Writeoff>

    @Query("SELECT SUM(totalAmount) FROM writeoffs")
    suspend fun getTotalSum(): Double?

    @Query("SELECT SUM(totalAmount) FROM writeoffs WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getSumByDateRange(startDate: Long, endDate: Long): Double?

    @Query("SELECT SUM(quantity) FROM writeoffs WHERE catalogId = :catalogId")
    suspend fun getTotalQuantityByProduct(catalogId: String): Int

    @Query("""
        SELECT w.writeoffId, w.date, w.catalogId, w.quantity, w.pricePerUnit, w.totalAmount,
               w.reason, w.comment, p.name as productName
        FROM writeoffs w
        JOIN products p ON w.catalogId = p.catalogId
        ORDER BY w.date DESC
    """)
    suspend fun getAllWithProductName(): List<WriteoffWithProductName>

    @Query("DELETE FROM writeoffs")
    suspend fun deleteAll()
}
