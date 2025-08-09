package dev.anonymous.hurriya.admin.presentation.screens.auth.invite_registration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.anonymous.hurriya.admin.core.handlers.ExceptionHandler
import dev.anonymous.hurriya.admin.core.utils.ResultState
import dev.anonymous.hurriya.admin.data.local.datastore.UserPreferences
import dev.anonymous.hurriya.admin.domain.usecase.auth.LoginUseCase
import dev.anonymous.hurriya.admin.domain.usecase.auth.RegisterWithInviteUseCase
import dev.anonymous.hurriya.admin.presentation.validation.ValidationConstants
import dev.anonymous.hurriya.admin.presentation.validation.ValidationError
import dev.anonymous.hurriya.admin.presentation.validation.ValidationResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InviteRegisterViewModel @Inject constructor(
    private val registerWithInviteUseCase: RegisterWithInviteUseCase,
    private val loginUseCase: LoginUseCase,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private var registerJob: Job? = null
    private val _registerState = MutableStateFlow<ResultState<Unit>>(ResultState.Idle)
    val registerState: StateFlow<ResultState<Unit>> get() = _registerState

    fun validateCredentials(
        name: String,
        email: String,
        password: String,
        inviteCode: String
    ): ValidationResult {
        if (name.isBlank() || email.isBlank() || password.isBlank() || inviteCode.isBlank()) {
            return ValidationResult.Invalid(ValidationError.EMPTY_FIELDS)
        }

        if (ValidationConstants.EMAIL_REGEX.matches(email).not()) {
            return ValidationResult.Invalid(ValidationError.INVALID_EMAIL)
        }

        return ValidationResult.Valid
    }

    fun registerWithInvite(email: String, password: String, name: String, inviteCode: String) {
        registerJob?.cancel()
        registerJob = viewModelScope.launch {
            _registerState.value = ResultState.Loading

            val registerResult = registerWithInviteUseCase(email, password, name, inviteCode)
            registerResult.onSuccess { role ->
                userPreferences.setStaffRole(role)
                performLogin(email, password)
            }.onFailure {
                _registerState.value = ResultState.Error(ExceptionHandler.handle(it))
                _registerState.value = ResultState.Idle
            }
        }
    }

    private suspend fun performLogin(email: String, password: String) {
        val loginResult = loginUseCase(email, password)
        loginResult.onSuccess {
            _registerState.value = ResultState.Success(Unit)
            _registerState.value = ResultState.Idle
        }.onFailure {
            _registerState.value = ResultState.Error(ExceptionHandler.handle(it))
            _registerState.value = ResultState.Idle
        }
    }
}
