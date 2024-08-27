package com.example.myapplication.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface RecordDao {
    @Insert
    suspend fun insert(record: Record)

    @Query("SELECT * FROM records")
    suspend fun getAllRecords(): List<Record>

    @Query("SELECT COUNT(*) FROM records WHERE title = :title AND year = :year")
    suspend fun recordExists(title: String, year: String): Int

    @Delete
    suspend fun delete(record: Record)
}
