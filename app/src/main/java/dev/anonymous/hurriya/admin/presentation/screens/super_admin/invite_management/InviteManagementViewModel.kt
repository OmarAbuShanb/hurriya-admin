package dev.anonymous.hurriya.admin.presentation.screens.super_admin.invite_management

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.anonymous.hurriya.admin.core.handlers.ExceptionHandler
import dev.anonymous.hurriya.admin.core.utils.ResultState
import dev.anonymous.hurriya.admin.domain.models.Invitation
import dev.anonymous.hurriya.admin.domain.usecase.invitation.DeleteInvitationUseCase
import dev.anonymous.hurriya.admin.domain.usecase.invitation.GenerateInvitationUseCase
import dev.anonymous.hurriya.admin.domain.usecase.invitation.GetInvitationsUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InviteManagementViewModel @Inject constructor(
    getInvitationsUseCase: GetInvitationsUseCase,
    private val deleteInvitationUseCase: DeleteInvitationUseCase,
    private val generateInvitationUseCase: GenerateInvitationUseCase,
) : ViewModel() {
    val invitationFlow = getInvitationsUseCase().cachedIn(viewModelScope)

    private var deleteInviteJob: Job? = null
    private val _deleteInviteState = MutableStateFlow<ResultState<Unit>>(ResultState.Idle)
    val deleteInviteState: StateFlow<ResultState<Unit>> = _deleteInviteState

    private var generateInviteJob: Job? = null
    private val _generateInviteState = MutableStateFlow<ResultState<Invitation>>(ResultState.Idle)
    val generateInviteState: StateFlow<ResultState<Invitation>> = _generateInviteState

    fun deleteInvitation(id: String) {
        deleteInviteJob?.cancel()
        _deleteInviteState.value = ResultState.Loading

        deleteInviteJob = viewModelScope.launch {
            deleteInvitationUseCase(id).onSuccess {
                _deleteInviteState.value = ResultState.Success(Unit)
                _deleteInviteState.value = ResultState.Idle
            }.onFailure {
                _deleteInviteState.value = ResultState.Error(ExceptionHandler.handle(it))
            }
        }
    }

    fun generateInvite(hint: String, role: String) {
        generateInviteJob?.cancel()
        _generateInviteState.value = ResultState.Loading

        generateInviteJob = viewModelScope.launch {
            generateInvitationUseCase(hint, role).onSuccess { newInvitation ->
                _generateInviteState.value = ResultState.Success(newInvitation)
                _generateInviteState.value = ResultState.Idle
            }.onFailure {
                _generateInviteState.value = ResultState.Error(ExceptionHandler.handle(it))
            }
        }
    }
}