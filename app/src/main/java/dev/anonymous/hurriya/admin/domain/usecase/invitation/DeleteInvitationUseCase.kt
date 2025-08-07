package dev.anonymous.hurriya.admin.domain.usecase.invitation

import dev.anonymous.hurriya.admin.domain.repository.InvitationRepository
import javax.inject.Inject

class DeleteInvitationUseCase @Inject constructor(
    private val repo: InvitationRepository
) {
    suspend operator fun invoke(id: String): Result<Unit> {
        return repo.deleteInvitation(id)
    }
}