package com.example.bugtrackerapp

sealed class SyncResult {

    data object Success : SyncResult()

    data object NetworkError : SyncResult()

    data class Error(
        val message: String
    ) : SyncResult()
}