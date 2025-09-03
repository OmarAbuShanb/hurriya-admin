package dev.anonymous.hurriya.admin.presentation.screens.super_admin.staff_management

sealed interface UiEvent {
    data class ShowSnackbar(val message: String) : UiEvent
}