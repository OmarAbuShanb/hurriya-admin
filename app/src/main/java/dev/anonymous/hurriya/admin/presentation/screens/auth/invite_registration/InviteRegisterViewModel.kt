package dev.anonymous.hurriya.admin.presentation.screens.auth.invite_registration

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.anonymous.hurriya.admin.R
import dev.anonymous.hurriya.admin.core.handlers.ExceptionHandler
import dev.anonymous.hurriya.admin.core.utils.ResultState
import dev.anonymous.hurriya.admin.data.local.datastore.UserPreferences
import dev.anonymous.hurriya.admin.domain.usecase.auth.LoginUseCase
import dev.anonymous.hurriya.admin.domain.usecase.auth.RegisterWithInviteUseCase
import dev.anonymous.hurriya.admin.presentation.validation.RegisterData
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
    val registerState: StateFlow<ResultState<Unit>> = _registerState

    private val _registerValidationState =
        MutableStateFlow<ValidationResult<RegisterData>>(ValidationResult.Idle)
    val registerValidationState: StateFlow<ValidationResult<RegisterData>> =
        _registerValidationState

    fun validate(name: String, email: String, password: String, inviteCode: String) {
        if (name.isBlank() || email.isBlank() || password.isBlank() || inviteCode.isBlank()) {
            _registerValidationState.value = ValidationResult.Invalid(R.string.error_all_fields_required)
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _registerValidationState.value = ValidationResult.Invalid(R.string.error_invalid_email)
            return
        }

        _registerValidationState.value = ValidationResult.Valid(
            RegisterData(name, email, password, inviteCode)
        )
    }


    fun registerWithInvite(email: String, password: String, name: String, inviteCode: String) {
        registerJob?.cancel()
        registerJob = viewModelScope.launch {
            _registerState.value = ResultState.Loading

            val registerResult = registerWithInviteUseCase(email, password, name, inviteCode)
            registerResult.onSuccess { role ->
                userPreferences.setStaffRole(role)
                login(email, password)
            }.onFailure {
                _registerState.value = ResultState.Error(ExceptionHandler.handle(it))
            }
        }
    }

    private suspend fun login(email: String, password: String) {
        val loginResult = loginUseCase(email, password)
        loginResult.onSuccess {
            _registerState.value = ResultState.Success(Unit)
        }.onFailure {
            _registerState.value = ResultState.Error(ExceptionHandler.handle(it))
        }
    }
}
