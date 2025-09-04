package dev.anonymous.hurriya.domain.repository

import dev.anonymous.hurriya.domain.models.Invitation
import kotlinx.coroutines.flow.Flow

interface InvitationRepository {
    fun getInvitations(): Flow<List<Invitation>>
    suspend fun deleteInvitation(id: String): Result<Unit>
    suspend fun generateInvitation(hint: String, role: String): Result<Invitation>
}