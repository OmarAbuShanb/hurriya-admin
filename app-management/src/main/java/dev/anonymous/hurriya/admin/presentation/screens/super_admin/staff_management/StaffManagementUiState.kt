package dev.anonymous.hurriya.admin.presentation.screens.super_admin.staff_management

import dev.anonymous.hurriya.admin.domain.models.Staff

data class StaffManagementUiState(
    val isSuperAdmin: Boolean = false,
    val staffList: List<Staff> = emptyList(),
    val isLoading: Boolean = false,
    val isUpdatingRole: Boolean = false,
    val isDeletingStaff: Boolean = false,
    val errorMessage: String? = null
)
