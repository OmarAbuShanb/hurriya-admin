package dev.anonymous.hurriya.admin.domain.usecase.staff

import dev.anonymous.hurriya.admin.domain.repository.StaffRepository
import javax.inject.Inject

class FetchStaffRoleUseCase @Inject constructor(
    private val staffRepository: StaffRepository
) {
    suspend operator fun invoke(uid: String): Result<String> {
        return staffRepository.fetchStaffRole(uid)
    }
}