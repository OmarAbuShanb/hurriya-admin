package dev.anonymous.hurriya.admin.presentation.screens.auth.login

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.anonymous.hurriya.admin.R
import dev.anonymous.hurriya.admin.core.handlers.ExceptionHandler
import dev.anonymous.hurriya.admin.core.utils.ResultState
import dev.anonymous.hurriya.admin.data.local.datastore.UserPreferences
import dev.anonymous.hurriya.admin.domain.usecase.auth.LoginUseCase
import dev.anonymous.hurriya.admin.presentation.validation.LoginData
import dev.anonymous.hurriya.admin.presentation.validation.ValidationResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private var loginJob: Job? = null
    private val _loginState = MutableStateFlow<ResultState<Unit>>(ResultState.Idle)
    val loginState: StateFlow<ResultState<Unit>> = _loginState

    private val _validationState =
        MutableStateFlow<ValidationResult<LoginData>>(ValidationResult.Idle)
    val validationState: StateFlow<ValidationResult<LoginData>> = _validationState

    fun validate(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _validationState.value = ValidationResult.Invalid(R.string.error_all_fields_required)
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _validationState.value = ValidationResult.Invalid(R.string.error_invalid_email)
            return
        }

        _validationState.value = ValidationResult.Valid(
            LoginData(email, password)
        )
    }

    fun login(email: String, password: String) {
        loginJob?.cancel()
        _loginState.value = ResultState.Loading

        loginJob = viewModelScope.launch {
            loginUseCase(email, password)
                .onSuccess { role ->
                    userPreferences.setStaffRole(role)
                    _loginState.value = ResultState.Success(Unit)
                }
                .onFailure {
                    _loginState.value = ResultState.Error(ExceptionHandler.handle(it))
                }
        }
    }
}
