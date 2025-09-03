package dev.anonymous.hurriya.admin.data.local.cleaner

import android.content.Context
import dev.anonymous.hurriya.admin.data.local.database.AppDatabase
import dev.anonymous.hurriya.admin.data.local.datastore.UserPreferences
import javax.inject.Inject

class LocalUserDataCleaner @Inject constructor(
    private val appDatabase: AppDatabase,
    private val userPreferences: UserPreferences,
    private val context: Context,
) {

    suspend fun clearAll() {
        appDatabase.clearAllData()
        userPreferences.clearAll()
        context.cacheDir.deleteRecursively()
    }
}
