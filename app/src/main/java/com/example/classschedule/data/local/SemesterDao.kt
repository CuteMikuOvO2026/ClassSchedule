package com.example.classschedule.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SemesterDao {

    @Query("SELECT * FROM semesters ORDER BY id LIMIT 1")
    fun observeFirst(): Flow<SemesterEntity?>

    @Query("SELECT * FROM semesters WHERE id = :id")
    suspend fun getById(id: Long): SemesterEntity?

    @Query("SELECT * FROM semesters ORDER BY id LIMIT 1")
    suspend fun getFirst(): SemesterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(semester: SemesterEntity): Long
}
