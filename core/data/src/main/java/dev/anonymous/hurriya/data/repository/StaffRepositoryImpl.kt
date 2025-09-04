package dev.anonymous.hurriya.data.repository

import dev.anonymous.hurriya.data.remote.datasource.StaffRemoteDataSource
import dev.anonymous.hurriya.domain.models.Staff
import dev.anonymous.hurriya.domain.repository.StaffRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class StaffRepositoryImpl @Inject constructor(
    private val remoteDataSource: StaffRemoteDataSource
) : StaffRepository {

    override fun listenStaffListSorted(): Flow<List<Staff>> {
        return remoteDataSource.listenStaffListSorted()
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
