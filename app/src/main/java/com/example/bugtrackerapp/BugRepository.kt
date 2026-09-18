package com.example.bugtrackerapp

class BugRepository(
    private val bugDao: BugDao
) {

    suspend fun getAllBugs(): List<Bug> {
        return bugDao.getAllBugs()
    }

    suspend fun getUnsyncedBugs(): List<Bug> {
        return bugDao.getUnsyncedBugs()
    }

    suspend fun addBug(bug: Bug) {
        bugDao.insertBug(bug)
    }

    suspend fun updateBug(bug: Bug) {
        bugDao.updateBug(bug)
    }

    suspend fun deleteBug(bug: Bug) {
        bugDao.deleteBug(bug)
    }

    suspend fun markBugAsSynced(bugId: Int) {
        bugDao.markAsSynced(bugId)
    }
    suspend fun syncUnsyncedBugs(): SyncResult {
        return try {
            val unsyncedBugs = bugDao.getUnsyncedBugs()

            if (unsyncedBugs.isEmpty()) {
                return SyncResult.Success
            }

            // Network synchronization will be connected here later.
            SyncResult.NetworkError

        } catch (e: Exception) {
            SyncResult.Error(
                e.message ?: "Unknown synchronization error"
            )
        }
    }
}