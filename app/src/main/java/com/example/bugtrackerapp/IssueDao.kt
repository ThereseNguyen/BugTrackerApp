package com.example.bugtrackerapp

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface IssueDao {

    @Insert
    suspend fun insertIssue(issue: Issue)

    @Update
    suspend fun updateIssue(issue: Issue)

    @Delete
    suspend fun deleteIssue(issue: Issue)

    @Query("SELECT * FROM issues ORDER BY creationDate DESC")
    suspend fun getAllIssues(): List<Issue>

    @Query("SELECT * FROM issues WHERE synced = 0")
    suspend fun getUnsyncedIssues(): List<Issue>

    @Query("UPDATE issues SET synced = 1 WHERE id = :issueId")
    suspend fun markAsSynced(issueId: String)
}