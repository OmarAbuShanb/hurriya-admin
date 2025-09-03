package dev.anonymous.hurriya.admin.domain.usecase.invitation

import androidx.paging.PagingData
import dev.anonymous.hurriya.admin.domain.models.Invitation
import dev.anonymous.hurriya.admin.domain.repository.InvitationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetInvitationsUseCase @Inject constructor(
    private val repo: InvitationRepository
) {
    operator fun invoke(): Flow<PagingData<Invitation>> {
        return repo.getInvitations()
    }
}