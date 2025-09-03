package dev.anonymous.hurriya.admin.domain.usecase.invitation

import dev.anonymous.hurriya.admin.domain.models.Invitation
import dev.anonymous.hurriya.admin.domain.repository.InvitationRepository
import javax.inject.Inject

class GenerateInvitationUseCase @Inject constructor(
    private val repo: InvitationRepository
) {
    suspend operator fun invoke(hint: String, role: String): Result<Invitation> {
        return repo.generateInvitation(hint,role)
    }
}