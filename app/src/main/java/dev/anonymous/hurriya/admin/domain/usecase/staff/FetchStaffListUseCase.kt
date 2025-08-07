package dev.anonymous.hurriya.admin.domain.usecase.staff

import dev.anonymous.hurriya.admin.domain.repository.StaffRepository
import dev.anonymous.hurriya.admin.domain.models.StaffItem
import javax.inject.Inject

class FetchStaffListUseCase @Inject constructor(
    private val repository: StaffRepository
) {
    suspend operator fun invoke(): Result<List<StaffItem>> {
        return repository.fetchStaffList()
    }
}
