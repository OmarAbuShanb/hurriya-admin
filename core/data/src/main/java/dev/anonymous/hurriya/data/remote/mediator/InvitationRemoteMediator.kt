package dev.anonymous.hurriya.data.remote.mediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.google.firebase.Timestamp
import dev.anonymous.hurriya.admin.core.utils.NetworkChecker
import dev.anonymous.hurriya.data.local.datasource.InvitationLocalDataSource
import dev.anonymous.hurriya.data.local.entities.InvitationEntity
import dev.anonymous.hurriya.data.remote.datasource.InvitationRemoteDataSource
import dev.anonymous.hurriya.data.mappers.toEntity

@OptIn(ExperimentalPagingApi::class)
class InvitationRemoteMediator(
    private val remoteDataSource: InvitationRemoteDataSource,
    private val localDataSource: InvitationLocalDataSource,
    private val networkChecker: NetworkChecker
) : RemoteMediator<Int, InvitationEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, InvitationEntity>
    ): MediatorResult {

        if (!networkChecker.isOnline()) {
            return MediatorResult.Success(endOfPaginationReached = true)
        }

        return try {
            val loadSize = state.config.pageSize.toLong()
            val lastCreatedAt = when (loadType) {
                LoadType.REFRESH -> null
                else -> {
                    val millis = state.lastItemOrNull()?.createdAt
                    millis?.let { Timestamp(it / 1000, 0) }
                }
            }

            val fetchedInvitations = remoteDataSource
                .fetchInvitations(loadSize, lastCreatedAt)
                .map { it.toEntity() }

            if (loadType == LoadType.REFRESH) {
                localDataSource.clearAll()
            }

            localDataSource.insertAll(fetchedInvitations)
            MediatorResult.Success(endOfPaginationReached = fetchedInvitations.isEmpty())
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }
}
