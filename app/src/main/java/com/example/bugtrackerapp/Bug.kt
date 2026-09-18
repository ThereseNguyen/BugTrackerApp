package com.example.bugtrackerapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bugs")
data class Bug(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val title: String,

    val description: String,

    val status: String,

    val priority: String,

    val creationDate: Long = System.currentTimeMillis(),

    val synced: Boolean = false
)