package dev.anonymous.hurriya.domain.usecase.staff

import dev.anonymous.hurriya.domain.models.Staff
import dev.anonymous.hurriya.domain.repository.StaffRepository
import kotlinx.coroutines.flow.Flow

class ListenToPresenceUpdatesUseCase(
    private val repository: StaffRepository
) {
    operator fun invoke(): Flow<List<Staff>> {
        return repository.listenStaffListSorted()
    }
}
