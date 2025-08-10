package dev.anonymous.hurriya.admin.presentation.components

import dev.anonymous.hurriya.admin.R

enum class StaffRole(val value: String, val displayResId: Int) {
    SUPER_ADMIN("superadmin", R.string.role_owner),
    ADMIN("admin", R.string.role_admin),
    EDITOR("editor", R.string.role_editor),
}
