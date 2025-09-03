package dev.anonymous.hurriya.admin.domain.usecase.auth

import dev.anonymous.hurriya.admin.data.local.cleaner.LocalUserDataCleaner
import dev.anonymous.hurriya.admin.domain.repository.AuthRepository
import dev.anonymous.hurriya.admin.domain.presence.PresenceManager
import javax.inject.Inject

class SignOutUseCase @Inject constructor(
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