package dev.anonymous.hurriya.admin.presentation.validation

sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val error: ValidationError) : ValidationResult()
}
