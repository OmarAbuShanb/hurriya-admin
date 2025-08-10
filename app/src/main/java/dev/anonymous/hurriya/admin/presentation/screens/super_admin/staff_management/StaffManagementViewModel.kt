package dev.anonymous.hurriya.admin.presentation.screens.super_admin.staff_management

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.anonymous.hurriya.admin.core.handlers.ExceptionHandler
import dev.anonymous.hurriya.admin.core.utils.ResultState
import dev.anonymous.hurriya.admin.data.local.datastore.UserPreferences
import dev.anonymous.hurriya.admin.domain.usecase.staff.DeleteStaffUseCase
import dev.anonymous.hurriya.admin.domain.usecase.staff.FetchStaffListUseCase
import dev.anonymous.hurriya.admin.domain.usecase.staff.ListenToPresenceUpdatesUseCase
import dev.anonymous.hurriya.admin.domain.usecase.staff.UpdateStaffRoleUseCase
import dev.anonymous.hurriya.admin.domain.models.StaffItem
import dev.anonymous.hurriya.admin.presentation.components.StaffRole
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StaffManagementViewModel @Inject constructor(
    private val fetchStaffListUseCase: FetchStaffListUseCase,
    private val listenToPresenceUpdatesUseCase: ListenToPresenceUpdatesUseCase,
    private val updateStaffRoleUseCase: UpdateStaffRoleUseCase,
    private val deleteStaffUseCase: DeleteStaffUseCase,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _isSuperAdmin = MutableStateFlow(false)
    val isSuperAdmin: StateFlow<Boolean> = _isSuperAdmin

    private val _staffList = MutableStateFlow<List<StaffItem>>(emptyList())
    val staffList: StateFlow<List<StaffItem>> = _staffList

    private val _presenceUpdates = MutableStateFlow<Map<String, Pair<Boolean?, Long?>>>(emptyMap())

    private val _updateRoleState = MutableStateFlow<ResultState<Unit>>(ResultState.Idle)
    val updateRoleState: StateFlow<ResultState<Unit>> = _updateRoleState

    private val _deleteStaffState = MutableStateFlow<ResultState<Unit>>(ResultState.Idle)
    val deleteStaffState: StateFlow<ResultState<Unit>> = _deleteStaffState

    private var fetchJob: Job? = null
    private var presenceJob: Job? = null
    private var updateRoleJob: Job? = null
    private var deleteStaffJob: Job? = null

    init {
        checkSuperAdmin()
        fetchStaffList()
    }

    private fun checkSuperAdmin() {
        viewModelScope.launch {
            val result = userPreferences.isSuperAdmin().first()
            _isSuperAdmin.value = result
        }
    }

    private fun fetchStaffList() {
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            fetchStaffListUseCase().onSuccess { list ->
                _staffList.value = list
                listenToPresenceUpdates()
            }.onFailure {

            }
        }
    }

    private fun listenToPresenceUpdates() {
        presenceJob?.cancel()
        presenceJob = viewModelScope.launch {
            listenToPresenceUpdatesUseCase().collectLatest { presenceMap ->
                _presenceUpdates.value = presenceMap
                updateStaffPresence(presenceMap)
            }
        }
    }

    private fun updateStaffPresence(presenceMap: Map<String, Pair<Boolean?, Long?>>) {
        val updatedList = _staffList.value.map { staff ->
            val presence = presenceMap[staff.uid]
            staff.copy(
                isOnline = presence?.first,
                lastSeen = presence?.second
            )
        }
        _staffList.value = updatedList
    }

    fun updateRole(uid: String, newStaffRole: StaffRole) {
        updateRoleJob?.cancel()
        _updateRoleState.value = ResultState.Loading

        updateRoleJob = viewModelScope.launch {
            updateStaffRoleUseCase(uid, newStaffRole.value).onSuccess {
                _updateRoleState.value = ResultState.Success(Unit)
                _updateRoleState.value = ResultState.Idle
                updateRoleLocally(uid,newStaffRole.value)
            }.onFailure {
                _updateRoleState.value = ResultState.Error(ExceptionHandler.handle(it))
            }
        }
    }

    fun deleteStaff(uid: String) {
        deleteStaffJob?.cancel()
        _deleteStaffState.value = ResultState.Loading

        deleteStaffJob = viewModelScope.launch {
            deleteStaffUseCase(uid).onSuccess {
                _deleteStaffState.value = ResultState.Success(Unit)
                _deleteStaffState.value = ResultState.Idle
                removeStaffLocally(uid)
            }.onFailure {
                _deleteStaffState.value = ResultState.Error(ExceptionHandler.handle(it))
            }
        }
    }

    fun updateRoleLocally(staffId: String, newRole: String) {
        val updatedList = _staffList.value.toMutableList()
        val index = updatedList.indexOfFirst { it.uid == staffId }
        if (index != -1) {
            val updatedItem = updatedList[index].copy(role = newRole)
            updatedList[index] = updatedItem
            _staffList.value = updatedList
        }
    }

    fun removeStaffLocally(staffId: String) {
        val updatedList = _staffList.value.toMutableList()
        updatedList.removeAll { it.uid == staffId }
        _staffList.value = updatedList
    }
}
