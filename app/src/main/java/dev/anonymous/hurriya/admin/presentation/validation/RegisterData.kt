package dev.anonymous.hurriya.admin.presentation.validation

data class RegisterData(
    val name: String,
    val email: String,
    val password: String,
    val inviteCode: String
)
