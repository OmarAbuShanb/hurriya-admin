package dev.anonymous.hurriya.admin.presentation.screens.auth.invite_registration

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import dev.anonymous.hurriya.admin.R
import dev.anonymous.hurriya.admin.core.utils.ResultState
import dev.anonymous.hurriya.admin.databinding.FragmentInviteRegistrationBinding
import dev.anonymous.hurriya.admin.presentation.components.BaseFragment
import dev.anonymous.hurriya.admin.presentation.validation.ValidationError
import dev.anonymous.hurriya.admin.presentation.validation.ValidationResult
import kotlinx.coroutines.launch

@AndroidEntryPoint
class InviteRegistrationFragment :
    BaseFragment<FragmentInviteRegistrationBinding>(FragmentInviteRegistrationBinding::inflate) {

    private val viewModel: InviteRegisterViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
        observeRegisterState()
    }

    private fun setupListeners() {
        binding.btnRegisterWithInvite.setOnButtonClickListener {
            onRegisterWithInviteClicked()
        }
    }

    fun onRegisterWithInviteClicked() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val inviteCode = binding.etInviteCode.text.toString().trim()

        when (val result = viewModel.validateCredentials(name, email, password, inviteCode)) {
            is ValidationResult.Valid -> viewModel.registerWithInvite(
                email,
                password,
                name,
                inviteCode
            )

            is ValidationResult.Invalid -> showValidationError(result.error)
        }
    }

    private fun observeRegisterState() {
        lifecycleScope.launch {
            viewModel.registerState.collect { state ->
                when (state) {
                    is ResultState.Loading -> showLoadingState()
                    is ResultState.Success -> navigateToDashboard()
                    is ResultState.Error -> showError(state.message)
                    ResultState.Idle -> showIdleState()
                }
            }
        }
    }

    private fun showValidationError(error: ValidationError) {
        val messageResId = when (error) {
            ValidationError.EMPTY_FIELDS -> R.string.error_all_fields_required
            ValidationError.INVALID_EMAIL -> R.string.error_invalid_email
        }
        showError(getString(messageResId))
    }

    private fun showLoadingState() {
        binding.btnRegisterWithInvite.showLoading()
        binding.etName.isEnabled = false
        binding.etEmail.isEnabled = false
        binding.etPassword.isEnabled = false
        binding.etInviteCode.isEnabled = false
    }

    private fun showIdleState() {
        binding.btnRegisterWithInvite.hideLoading()
        binding.etName.isEnabled = true
        binding.etEmail.isEnabled = true
        binding.etPassword.isEnabled = true
        binding.etInviteCode.isEnabled = true
    }

    private fun navigateToDashboard() {
        navigateTo(InviteRegistrationFragmentDirections.actionToDashboardFragment())
    }

    private fun showError(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show()
    }
}
