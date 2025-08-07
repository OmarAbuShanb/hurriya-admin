package dev.anonymous.hurriya.admin.domain.usecase.auth

import dev.anonymous.hurriya.admin.domain.repository.AuthRepository
import dev.anonymous.hurriya.admin.domain.usecase.staff.FetchStaffRoleUseCase
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val repository: AuthRepository,
    private val fetchStaffRoleUseCase: FetchStaffRoleUseCase,
) {
    suspend operator fun invoke(email: String, password: String): Result<String> {
        return repository.login(email, password).mapCatching { uid ->
            fetchStaffRoleUseCase(uid).getOrThrow()
        }
    }
}

