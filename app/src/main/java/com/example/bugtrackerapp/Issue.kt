package com.example.bugtrackerapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "issues")
data class Issue(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val priority: String,
    val status: String,
    val creationDate: Long,
    val synced: Boolean = false
)