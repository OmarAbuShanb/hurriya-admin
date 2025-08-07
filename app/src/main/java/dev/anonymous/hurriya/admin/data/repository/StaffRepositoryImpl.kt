package dev.anonymous.hurriya.admin.data.repository

import dev.anonymous.hurriya.admin.data.remote.datasource.StaffRemoteDataSource
import dev.anonymous.hurriya.admin.domain.repository.StaffRepository
import dev.anonymous.hurriya.admin.domain.models.StaffItem
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class StaffRepositoryImpl @Inject constructor(
    private val remoteDataSource: StaffRemoteDataSource
) : StaffRepository {

    override suspend fun fetchStaffList(): Result<List<StaffItem>> {
        return remoteDataSource.fetchStaffList()
    }

    override fun listenToPresenceUpdates(): Flow<Map<String, Pair<Boolean?, Long?>>> {
        return remoteDataSource.listenToPresenceUpdates()
    }

    override suspend fun fetchStaffRole(uid: String): Result<String> {
        return remoteDataSource.fetchUserRole(uid)
    }

    override suspend fun updateRole(uid: String, newRole: String): Result<Unit> {
        return remoteDataSource.updateRole(uid, newRole)
    }

    override suspend fun deleteStaff(uid: String): Result<Unit> {
        return remoteDataSource.deleteStaff(uid)
    }
}
