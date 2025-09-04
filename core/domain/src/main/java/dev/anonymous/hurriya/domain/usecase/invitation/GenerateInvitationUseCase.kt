package dev.anonymous.hurriya.domain.usecase.invitation

import dev.anonymous.hurriya.domain.models.Invitation
import dev.anonymous.hurriya.domain.repository.InvitationRepository

class GenerateInvitationUseCase(
    private val repo: InvitationRepository
) {
    suspend operator fun invoke(hint: String, role: String): Result<Invitation> {
        return repo.generateInvitation(hint,role)
    }
}