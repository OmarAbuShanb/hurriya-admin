package dev.anonymous.hurriya.admin.domain.repository

import dev.anonymous.hurriya.admin.domain.models.Staff
import kotlinx.coroutines.flow.Flow

interface StaffRepository {
    fun listenStaffListSorted(): Flow<List<Staff>>
    suspend fun fetchStaffRole(uid: String): Result<String>
    suspend fun updateRole(uid: String, newRole: String): Result<Unit>
    suspend fun deleteStaff(uid: String): Result<Unit>
}
