package dev.anonymous.hurriya.admin.presentation.screens.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.anonymous.hurriya.admin.core.handlers.ExceptionHandler
import dev.anonymous.hurriya.admin.core.utils.ResultState
import dev.anonymous.hurriya.admin.data.local.datastore.UserPreferences
import dev.anonymous.hurriya.admin.domain.usecase.auth.LoginUseCase
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

    fun login(email: String, password: String) {
        loginJob?.cancel()
        _loginState.value = ResultState.Loading

        loginJob = viewModelScope.launch {
            loginUseCase(email, password)
                .onSuccess { role ->
                    userPreferences.setStaffRole(role)
                    _loginState.value = ResultState.Success(Unit)
                    _loginState.value = ResultState.Idle
                }
                .onFailure {
                    _loginState.value = ResultState.Error(ExceptionHandler.handle(it))
                }
        }
    }
}
