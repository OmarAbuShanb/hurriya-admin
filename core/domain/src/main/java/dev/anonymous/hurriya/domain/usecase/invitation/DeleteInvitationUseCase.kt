package dev.anonymous.hurriya.domain.usecase.invitation

import dev.anonymous.hurriya.domain.repository.InvitationRepository

class DeleteInvitationUseCase(
    private val repo: InvitationRepository
) {
    suspend operator fun invoke(id: String): Result<Unit> {
        return repo.deleteInvitation(id)
    }
}