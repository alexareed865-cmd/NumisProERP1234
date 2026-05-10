package com.numisproerp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.numisproerp.data.entities.Note
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: Note)

    @Update
    suspend fun update(note: Note)

    @Delete
    suspend fun delete(note: Note)

    @Query("SELECT * FROM notes ORDER BY isCompleted ASC, CASE WHEN reminderDate IS NOT NULL THEN reminderDate ELSE 9999999999999 END ASC, createdAt DESC")
    fun getAll(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE noteId = :id")
    suspend fun getById(id: String): Note?

    @Query("SELECT * FROM notes WHERE reminderDate IS NOT NULL AND reminderDate <= :timestamp AND isCompleted = 0")
    suspend fun getDueReminders(timestamp: Long): List<Note>

    @Query("SELECT * FROM notes WHERE reminderDate IS NOT NULL AND reminderDate > :timestamp AND isCompleted = 0")
    suspend fun getFutureReminders(timestamp: Long): List<Note>

    @Query("SELECT COUNT(*) FROM notes WHERE isCompleted = 0")
    suspend fun getActiveCount(): Int

    @Query("DELETE FROM notes")
    suspend fun deleteAll()
}
