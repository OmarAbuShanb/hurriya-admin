package dev.anonymous.hurriya.admin.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import dev.anonymous.hurriya.admin.data.local.dao.InvitationDao
import dev.anonymous.hurriya.admin.data.local.entities.InvitationEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Database(
    entities = [InvitationEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun invitationDao(): InvitationDao

    suspend fun clearAllData() {
        withContext(Dispatchers.IO) {
            clearAllTables()
        }
    }
}

