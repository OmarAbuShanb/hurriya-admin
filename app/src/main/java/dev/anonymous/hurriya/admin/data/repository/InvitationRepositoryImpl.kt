package dev.anonymous.hurriya.admin.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import dev.anonymous.hurriya.admin.core.exceptions.NoInternetException
import dev.anonymous.hurriya.admin.core.utils.NetworkChecker
import dev.anonymous.hurriya.admin.data.local.datasource.InvitationLocalDataSource
import dev.anonymous.hurriya.admin.data.remote.datasource.InvitationRemoteDataSource
import dev.anonymous.hurriya.admin.data.remote.mediator.InvitationRemoteMediator
import dev.anonymous.hurriya.admin.domain.models.Invitation
import dev.anonymous.hurriya.admin.domain.mappers.toDomain
import dev.anonymous.hurriya.admin.domain.mappers.toEntity
import dev.anonymous.hurriya.admin.domain.repository.InvitationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class InvitationRepositoryImpl @Inject constructor(
    private val remoteDataSource: InvitationRemoteDataSource,
    private val localDataSource: InvitationLocalDataSource,
    private val networkChecker: NetworkChecker,
) : InvitationRepository {

    @OptIn(ExperimentalPagingApi::class)
    override fun getInvitations(): Flow<PagingData<Invitation>> {
        return Pager(
            config = PagingConfig(initialLoadSize = 10, pageSize = 10),
            remoteMediator = InvitationRemoteMediator(
                remoteDataSource,
                localDataSource,
                networkChecker
            ),
            pagingSourceFactory = {
                localDataSource.getInvitations()
            }
        ).flow.map { pagingData ->
            pagingData.map { it.toDomain() }
        }
    }

    override suspend fun generateInvitation(hint: String, role: String): Result<Invitation> {
        if (!networkChecker.isOnline()) {
            return Result.failure(NoInternetException())
        }

        return try {
            val code = remoteDataSource.generateInvitation(hint, role).getOrThrow()
            val newInvitation = Invitation(code, code, hint, role)
            localDataSource.insert(newInvitation.toEntity())
            Result.success(newInvitation)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteInvitation(id: String): Result<Unit> {
        return if (networkChecker.isOnline()) {
            try {
                remoteDataSource.deleteInvitation(id)
                localDataSource.deleteInvitationById(id)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(Exception(e.message))
            }
        } else {
            Result.failure(Exception("No internet connection"))
        }
    }
}
