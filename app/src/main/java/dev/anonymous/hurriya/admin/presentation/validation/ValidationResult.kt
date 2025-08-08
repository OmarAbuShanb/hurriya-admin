package dev.anonymous.hurriya.admin.presentation.validation

sealed class ValidationResult<out T> {
    object Idle : ValidationResult<Nothing>()
    data class Valid<T>(val data: T) : ValidationResult<T>()
    data class Invalid(val message: Int) : ValidationResult<Nothing>()
}
