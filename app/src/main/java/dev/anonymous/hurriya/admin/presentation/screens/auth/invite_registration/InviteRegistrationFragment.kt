package dev.anonymous.hurriya.admin.presentation.screens.auth.invite_registration

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import dev.anonymous.hurriya.admin.core.utils.ResultState
import dev.anonymous.hurriya.admin.databinding.FragmentInviteRegistrationBinding
import dev.anonymous.hurriya.admin.presentation.components.BaseFragment
import dev.anonymous.hurriya.admin.presentation.validation.ValidationResult
import kotlinx.coroutines.launch

@AndroidEntryPoint
class InviteRegistrationFragment :
    BaseFragment<FragmentInviteRegistrationBinding>(FragmentInviteRegistrationBinding::inflate) {

    private val viewModel: InviteRegisterViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRegisterButton()
        observeValidationResult()
        observeRegisterState()
    }

    private fun setupRegisterButton() {
        binding.btnRegisterWithInvite.setOnButtonClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val inviteCode = binding.etInviteCode.text.toString().trim()

            viewModel.validate(name, email, password, inviteCode)
        }
    }

    private fun observeValidationResult() {
        lifecycleScope.launch {
            viewModel.registerValidationState.collect { result ->
                when (result) {
                    is ValidationResult.Valid -> {
                        val data = result.data
                        viewModel.registerWithInvite(
                            data.email,
                            data.password,
                            data.name,
                            data.inviteCode
                        )
                    }
                    is ValidationResult.Invalid -> Toast.makeText(
                        requireContext(),
                        requireContext().getString(result.message),
                        Toast.LENGTH_SHORT
                    ).show()

                    ValidationResult.Idle -> TODO()
                }
            }
        }
    }

    private fun observeRegisterState() {
        lifecycleScope.launch {
            viewModel.registerState.collect { state ->
                when (state) {
                    is ResultState.Loading -> binding.btnRegisterWithInvite.showLoading()
                    is ResultState.Success -> {
                        binding.btnRegisterWithInvite.hideLoading()
                        navigateTo(
                            InviteRegistrationFragmentDirections.actionToDashboardFragment()
                        )
                    }
                    is ResultState.Error -> {
                        binding.btnRegisterWithInvite.hideLoading()
                        Toast.makeText(
                            requireContext(),
                            state.message,
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    ResultState.Idle -> TODO()
                }
            }
        }
    }
}
