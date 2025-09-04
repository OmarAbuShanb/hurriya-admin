package dev.anonymous.hurriya.domain.usecase.auth

import dev.anonymous.hurriya.domain.repository.AuthRepository
import dev.anonymous.hurriya.domain.usecase.staff.FetchStaffRoleUseCase

class LoginUseCase(
    private val repository: AuthRepository,
    private val fetchStaffRoleUseCase: FetchStaffRoleUseCase,
) {
    suspend operator fun invoke(email: String, password: String): Result<String> {
        return repository.login(email, password).mapCatching { uid ->
            fetchStaffRoleUseCase(uid).getOrThrow()
        }
    }
}

