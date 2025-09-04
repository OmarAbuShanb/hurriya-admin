package dev.anonymous.hurriya.domain.usecase.auth

import dev.anonymous.hurriya.admin.data.local.cleaner.LocalUserDataCleaner
import dev.anonymous.hurriya.domain.repository.AuthRepository
import dev.anonymous.hurriya.domain.presence.PresenceManager

class SignOutUseCase(
    private val authRepository: AuthRepository,
    private val localUserDataCleaner: LocalUserDataCleaner,
    private val presenceManager: PresenceManager
) {
    suspend operator fun invoke() {
        presenceManager.setOffline()
        localUserDataCleaner.clearAll()
        authRepository.signOut()
    }
}