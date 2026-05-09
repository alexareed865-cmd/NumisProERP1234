package com.numisproerp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.numisproerp.data.entities.CollectionItem
import kotlinx.coroutines.flow.Flow

@Dao
interface CollectionItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: CollectionItem)

    @Update
    suspend fun update(item: CollectionItem)

    @Delete
    suspend fun delete(item: CollectionItem)

    @Query("SELECT * FROM collection_items ORDER BY dateAdded DESC")
    fun getAll(): Flow<List<CollectionItem>>

    @Query("SELECT * FROM collection_items ORDER BY dateAdded DESC")
    suspend fun getAllSync(): List<CollectionItem>

    @Query("SELECT * FROM collection_items WHERE collectionId = :id")
    suspend fun getById(id: String): CollectionItem?

    @Query("SELECT SUM(estimatedValue * quantity) FROM collection_items")
    suspend fun getTotalEstimatedValue(): Double?

    @Query("SELECT COUNT(*) FROM collection_items")
    suspend fun getCount(): Int

    @Query("DELETE FROM collection_items")
    suspend fun deleteAll()
}
