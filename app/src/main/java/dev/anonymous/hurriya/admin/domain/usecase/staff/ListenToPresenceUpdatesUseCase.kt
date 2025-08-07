package dev.anonymous.hurriya.admin.domain.usecase.staff

import dev.anonymous.hurriya.admin.domain.repository.StaffRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ListenToPresenceUpdatesUseCase @Inject constructor(
    private val repository: StaffRepository
) {
    operator fun invoke(): Flow<Map<String, Pair<Boolean?, Long?>>> {
        return repository.listenToPresenceUpdates()
    }
}
