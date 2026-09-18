package com.example.bugtrackerapp

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Bug::class],
    version = 2,
    exportSchema = false
)
abstract class BugDatabase : RoomDatabase() {

    abstract fun bugDao(): BugDao
}