package dev.anonymous.hurriya.domain.usecase.staff

import dev.anonymous.hurriya.domain.repository.StaffRepository

class DeleteStaffUseCase(
    private val repository: StaffRepository
) {
    suspend operator fun invoke(uid: String): Result<Unit> {
        return repository.deleteStaff(uid)
    }
}
