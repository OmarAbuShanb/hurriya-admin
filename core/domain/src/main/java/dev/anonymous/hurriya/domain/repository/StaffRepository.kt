package dev.anonymous.hurriya.domain.repository

import dev.anonymous.hurriya.domain.models.Staff
import kotlinx.coroutines.flow.Flow

interface StaffRepository {
    fun listenStaffListSorted(): Flow<List<Staff>>
    suspend fun fetchStaffRole(uid: String): Result<String>
    suspend fun updateRole(uid: String, newRole: String): Result<Unit>
    suspend fun deleteStaff(uid: String): Result<Unit>
}
