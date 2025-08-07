package dev.anonymous.hurriya.admin.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferences @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    companion object {
        private val STAFF_ROLE = stringPreferencesKey("staff_role")
    }

    val staffRole: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[STAFF_ROLE] }

    suspend fun hasStaffRole(): Boolean {
        return staffRole.firstOrNull() in listOf("admin", "editor", "superadmin")
    }

    fun isSuperAdmin() = staffRole.map {
        it.equals("superadmin")
    }

    suspend fun setStaffRole(role: String) {
        context.dataStore.edit { preferences ->
            preferences[STAFF_ROLE] = role
        }
    }

    suspend fun clearStaffRole() {
        context.dataStore.edit { preferences ->
            preferences.remove(STAFF_ROLE)
        }
    }

    suspend fun clearAll() {
        context.dataStore.edit {
            it.clear()
        }
    }
}