package dev.anonymous.hurriya.domain.usecase.staff

import dev.anonymous.hurriya.domain.repository.StaffRepository

class FetchStaffRoleUseCase(
    private val staffRepository: StaffRepository
) {
    suspend operator fun invoke(uid: String): Result<String> {
        return staffRepository.fetchStaffRole(uid)
    }
}