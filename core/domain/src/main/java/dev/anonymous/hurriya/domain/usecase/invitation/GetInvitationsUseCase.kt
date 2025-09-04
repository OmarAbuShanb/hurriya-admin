package dev.anonymous.hurriya.domain.usecase.invitation

import dev.anonymous.hurriya.domain.models.Invitation
import dev.anonymous.hurriya.domain.repository.InvitationRepository
import kotlinx.coroutines.flow.Flow

class GetInvitationsUseCase(
    private val repo: InvitationRepository
) {
    operator fun invoke(): Flow<List<Invitation>> {
        return repo.getInvitations()
    }
}