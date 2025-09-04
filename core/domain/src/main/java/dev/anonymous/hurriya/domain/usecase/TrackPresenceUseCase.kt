package dev.anonymous.hurriya.domain.usecase

import dev.anonymous.hurriya.domain.presence.PresenceManager

class TrackPresenceUseCase(
    private val presenceManager: PresenceManager
) {
    operator fun invoke() {
        presenceManager.setupOnDisconnect()
        presenceManager.setOnline()
    }
}