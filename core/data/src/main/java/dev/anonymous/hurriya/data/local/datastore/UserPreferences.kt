package dev.anonymous.hurriya.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.anonymous.hurriya.admin.presentation.components.StaffRole
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

    val staffRole: Flow<StaffRole?> = context.dataStore.data
        .map { preferences ->
            preferences[STAFF_ROLE]?.let { value ->
                StaffRole.entries.firstOrNull { it.value == value }
            }
        }

    suspend fun isStaffRoleSet(): Boolean {
        return staffRole.firstOrNull() != null
    }

    fun isSuperAdmin() = staffRole.map {
        it?.equals(StaffRole.SUPER_ADMIN) ?: false
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