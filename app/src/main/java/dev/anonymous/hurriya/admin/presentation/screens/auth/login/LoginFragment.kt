package dev.anonymous.hurriya.admin.presentation.screens.auth.login

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import dev.anonymous.hurriya.admin.R
import dev.anonymous.hurriya.admin.core.utils.ResultState
import dev.anonymous.hurriya.admin.databinding.FragmentLoginBinding
import dev.anonymous.hurriya.admin.presentation.components.BaseFragment
import dev.anonymous.hurriya.admin.presentation.validation.ValidationError
import dev.anonymous.hurriya.admin.presentation.validation.ValidationResult
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : BaseFragment<FragmentLoginBinding>(FragmentLoginBinding::inflate) {
    private val viewModel: LoginViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
        observeLoginState()
    }

    private fun setupListeners() {
        binding.btnLogin.setOnButtonClickListener {
            onLoginClicked()
        }

        binding.tvInviteRegister.setOnClickListener {
            navigateTo(LoginFragmentDirections.actionToInviteRegistrationFragment())
        }
    }

    private fun onLoginClicked() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        when (val result = viewModel.validateCredentials(email, password)) {
            is ValidationResult.Valid -> viewModel.login(email, password)
            is ValidationResult.Invalid -> showValidationError(result.error)
        }
    }

    private fun showValidationError(error: ValidationError) {
        val messageResId = when (error) {
            ValidationError.EMPTY_FIELDS -> R.string.error_all_fields_required
            ValidationError.INVALID_EMAIL -> R.string.error_invalid_email
        }
        showError(getString(messageResId))
    }

    private fun observeLoginState() {
        lifecycleScope.launch {
            viewModel.loginState.collect { state ->
                when (state) {
                    is ResultState.Loading -> showLoadingState()
                    is ResultState.Success -> navigateToDashboard()
                    is ResultState.Error -> showError(state.message)
                    ResultState.Idle -> showIdleState()
                }
            }
        }
    }

    private fun showLoadingState() {
        binding.btnLogin.showLoading()
        binding.tvInviteRegister.isEnabled = false
        binding.etEmail.isEnabled = false
        binding.etPassword.isEnabled = false
    }

    private fun showIdleState() {
        binding.btnLogin.hideLoading()
        binding.tvInviteRegister.isEnabled = true
        binding.etEmail.isEnabled = true
        binding.etPassword.isEnabled = true
    }

    private fun navigateToDashboard() {
        navigateTo(LoginFragmentDirections.actionLoginFragmentToDashboardFragment())
    }

    private fun showError(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show()
    }
}

