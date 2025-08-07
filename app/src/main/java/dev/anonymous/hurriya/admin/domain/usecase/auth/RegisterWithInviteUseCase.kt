package dev.anonymous.hurriya.admin.domain.usecase.auth

import dev.anonymous.hurriya.admin.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterWithInviteUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(
        email: String,
        password: String,
        name: String,
        inviteCode: String
    ): Result<String> {
        return authRepository.registerWithInvite(email, password, name, inviteCode)
    }
}