package com.example.bugtrackerapp

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Issue::class],
    version = 1,
    exportSchema = false
)
abstract class IssueDatabase : RoomDatabase() {

    abstract fun issueDao(): IssueDao
}