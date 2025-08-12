package dev.anonymous.hurriya.admin.presentation.screens.super_admin.staff_management

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.anonymous.hurriya.admin.core.handlers.ExceptionHandler
import dev.anonymous.hurriya.admin.data.local.datastore.UserPreferences
import dev.anonymous.hurriya.admin.domain.usecase.staff.DeleteStaffUseCase
import dev.anonymous.hurriya.admin.domain.usecase.staff.ListenToPresenceUpdatesUseCase
import dev.anonymous.hurriya.admin.domain.usecase.staff.UpdateStaffRoleUseCase
import dev.anonymous.hurriya.admin.presentation.components.StaffRole
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StaffManagementViewModel @Inject constructor(
    private val listenToPresenceUpdatesUseCase: ListenToPresenceUpdatesUseCase,
    private val updateStaffRoleUseCase: UpdateStaffRoleUseCase,
    private val deleteStaffUseCase: DeleteStaffUseCase,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(StaffManagementUiState())
    val uiState: StateFlow<StaffManagementUiState> = _uiState

    private val _events = MutableSharedFlow<UiEvent>()
    val events: SharedFlow<UiEvent> = _events

    private var presenceJob: Job? = null
    private var updateStaffRoleJob: Job? = null
    private var deleteStaffJob: Job? = null

    init {
        checkSuperAdmin()
        listenStaffListSorted()
    }

    private fun checkSuperAdmin() {
        viewModelScope.launch {
            val result = userPreferences.isSuperAdmin().first()
            _uiState.update {
                it.copy(isSuperAdmin = result)
            }
        }
    }

    private fun listenStaffListSorted() {
        presenceJob?.cancel()
        presenceJob = viewModelScope.launch {
            listenToPresenceUpdatesUseCase()
                .onStart {
                    _uiState.update {
                        it.copy(isLoading = true)
                    }
                }
                .catch {
                    _uiState.update { state ->
                        state.copy(isLoading = false)
                    }
                    _events.emit(UiEvent.ShowSnackbar(ExceptionHandler.handle(it)))
                }
                .collectLatest { staffList ->
                    _uiState.update {
                        it.copy(
                            staffList = staffList,
                            isLoading = false
                        )
                    }
                }
        }
    }

    fun updateRole(uid: String, newStaffRole: StaffRole) {
        updateStaffRoleJob?.cancel()
        _uiState.update { it.copy(isUpdatingRole = true) }

        val before = _uiState.value.staffList.firstOrNull { it.uid == uid }

        updateRoleLocally(uid, newStaffRole.value)

        updateStaffRoleJob = viewModelScope.launch {
            updateStaffRoleUseCase(uid, newStaffRole.value)
                .onSuccess {
                    _uiState.update { it.copy(isUpdatingRole = false) }
                }
                .onFailure {
                    if (before != null) {
                        updateRoleLocally(uid, before.role)
                    }
                    _uiState.update { state ->
                        state.copy(isUpdatingRole = false)
                    }
                    _events.emit(UiEvent.ShowSnackbar(ExceptionHandler.handle(it)))
                }
        }
    }

    fun deleteStaff(uid: String) {
        deleteStaffJob?.cancel()
        _uiState.update {
            it.copy(isDeletingStaff = true)
        }

        val beforeList = _uiState.value.staffList

        removeStaffLocally(uid)

        deleteStaffJob = viewModelScope.launch {
            deleteStaffUseCase(uid)
                .onSuccess {
                    _uiState.update {
                        it.copy(isDeletingStaff = false)
                    }
                }
                .onFailure {
                    _uiState.update { state ->
                        state.copy(
                            staffList = beforeList,
                            isDeletingStaff = false
                        )
                    }
                    _events.emit(UiEvent.ShowSnackbar(ExceptionHandler.handle(it)))
                }
        }
    }

    private fun updateRoleLocally(staffId: String, newRole: String) {
        _uiState.update {
            val updatedList = it.staffList.map { staff ->
                if (staff.uid == staffId) staff.copy(role = newRole) else staff
            }
            it.copy(staffList = updatedList)
        }
    }

    private fun removeStaffLocally(staffId: String) {
        _uiState.update {
            val updatedList = it.staffList.filter { staff -> staff.uid != staffId }
            it.copy(staffList = updatedList)
        }
    }
}

