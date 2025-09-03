package dev.anonymous.hurriya.admin.domain.repository

import androidx.paging.PagingData
import dev.anonymous.hurriya.admin.domain.models.Invitation
import kotlinx.coroutines.flow.Flow

interface InvitationRepository {
    fun getInvitations(): Flow<PagingData<Invitation>>
    suspend fun deleteInvitation(id: String): Result<Unit>
    suspend fun generateInvitation(hint: String, role: String): Result<Invitation>
}