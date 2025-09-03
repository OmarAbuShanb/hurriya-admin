package dev.anonymous.hurriya.admin.domain.usecase

import dev.anonymous.hurriya.admin.domain.presence.PresenceManager
import javax.inject.Inject

class TrackPresenceUseCase @Inject constructor(
    private val presenceManager: PresenceManager
) {
    operator fun invoke() {
        presenceManager.setupOnDisconnect()
        presenceManager.setOnline()
    }
}