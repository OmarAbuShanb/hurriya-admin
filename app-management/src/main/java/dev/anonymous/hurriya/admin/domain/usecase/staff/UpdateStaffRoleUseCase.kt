package dev.anonymous.hurriya.admin.domain.usecase.staff

import dev.anonymous.hurriya.admin.domain.repository.StaffRepository
import javax.inject.Inject

class UpdateStaffRoleUseCase @Inject constructor(
    private val repository: StaffRepository
) {
    suspend operator fun invoke(uid: String, newRole: String): Result<Unit> {
        return repository.updateRole(uid, newRole)
    }
}
