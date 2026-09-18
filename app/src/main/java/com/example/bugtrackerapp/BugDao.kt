package com.example.bugtrackerapp

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface BugDao {

    @Insert
    suspend fun insertBug(bug: Bug)

    @Update
    suspend fun updateBug(bug: Bug)

    @Delete
    suspend fun deleteBug(bug: Bug)

    @Query("SELECT * FROM bugs ORDER BY id DESC")
    suspend fun getAllBugs(): List<Bug>

    @Query("SELECT * FROM bugs WHERE synced = 0 ORDER BY id DESC")
    suspend fun getUnsyncedBugs(): List<Bug>

    @Query("UPDATE bugs SET synced = 1 WHERE id = :bugId")
    suspend fun markAsSynced(bugId: Int)
}