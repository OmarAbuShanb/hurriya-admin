package dev.anonymous.hurriya.admin.presentation.screens.auth.login

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import dev.anonymous.hurriya.admin.core.utils.ResultState
import dev.anonymous.hurriya.admin.databinding.FragmentLoginBinding
import dev.anonymous.hurriya.admin.presentation.components.BaseFragment
import dev.anonymous.hurriya.admin.presentation.validation.ValidationResult
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : BaseFragment<FragmentLoginBinding>(FragmentLoginBinding::inflate) {
    private val viewModel: LoginViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
        observeValidationState()
        observeLoginState()
    }

    private fun setupListeners() {
        binding.btnLogin.setOnButtonClickListener {
            val email = binding.etEmail.getText().toString().trim()
            val password = binding.etPassword.getText().toString().trim()
            viewModel.validate(email, password)
        }

        binding.tvInviteRegister.setOnClickListener {
            navigateTo(
                LoginFragmentDirections.actionToInviteRegistrationFragment()
            )
        }
    }

    private fun observeValidationState() {
        lifecycleScope.launch {
            viewModel.validationState.collect { result ->
                when (result) {
                    is ValidationResult.Valid -> {
                        binding.btnLogin.showLoading()

                        val loginData = result.data
                        viewModel.login(loginData.email, loginData.password)
                    }

                    is ValidationResult.Invalid -> {
                        Toast.makeText(
                            requireContext(),
                            requireContext().getString(result.message),
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    ValidationResult.Idle -> {
                        binding.btnLogin.hideLoading()
                    }
                }
            }
        }
    }

    private fun observeLoginState() {
        lifecycleScope.launch {
            viewModel.loginState.collect { state ->
                when (state) {
                    is ResultState.Loading -> {
                        binding.btnLogin.showLoading()
                        binding.tvInviteRegister.isEnabled = false
                    }

                    is ResultState.Success ->
                        navigateTo(
                            LoginFragmentDirections.actionLoginFragmentToDashboardFragment()
                        )

                    is ResultState.Error -> Toast.makeText(
                        requireContext(),
                        state.message,
                        Toast.LENGTH_LONG
                    ).show()

                    ResultState.Idle -> {
                        binding.btnLogin.hideLoading()
                        binding.tvInviteRegister.isEnabled = true
                    }
                }
            }
        }
    }
}
