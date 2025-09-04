package dev.anonymous.hurriya.domain.usecase.auth

import dev.anonymous.hurriya.domain.repository.AuthRepository

class RegisterWithInviteUseCase(
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