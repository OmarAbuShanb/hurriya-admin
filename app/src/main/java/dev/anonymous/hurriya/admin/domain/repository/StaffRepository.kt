package dev.anonymous.hurriya.admin.domain.repository

import dev.anonymous.hurriya.admin.domain.models.StaffItem
import kotlinx.coroutines.flow.Flow

interface StaffRepository {
    suspend fun fetchStaffList(): Result<List<StaffItem>>
    fun listenToPresenceUpdates(): Flow<Map<String, Pair<Boolean?, Long?>>>
    suspend fun fetchStaffRole(uid: String): Result<String>
    suspend fun updateRole(uid: String, newRole: String): Result<Unit>
    suspend fun deleteStaff(uid: String): Result<Unit>
}
