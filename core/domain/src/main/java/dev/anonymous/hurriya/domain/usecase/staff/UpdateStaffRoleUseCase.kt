package dev.anonymous.hurriya.domain.usecase.staff

import dev.anonymous.hurriya.domain.repository.StaffRepository

class UpdateStaffRoleUseCase(
    private val repository: StaffRepository
) {
    suspend operator fun invoke(uid: String, newRole: String): Result<Unit> {
        return repository.updateRole(uid, newRole)
    }
}
