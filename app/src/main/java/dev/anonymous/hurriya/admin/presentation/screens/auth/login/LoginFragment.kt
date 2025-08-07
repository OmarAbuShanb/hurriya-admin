package dev.anonymous.hurriya.admin.presentation.screens.auth.login

import android.content.res.Configuration
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import dev.anonymous.hurriya.admin.core.utils.ResultState
import dev.anonymous.hurriya.admin.databinding.FragmentLoginBinding
import dev.anonymous.hurriya.admin.presentation.components.BaseFragment
import dev.anonymous.hurriya.admin.utils.UtilsGeneral
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : BaseFragment<FragmentLoginBinding>(FragmentLoginBinding::inflate) {
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("LoginDebug", "Fragment hash: ${this.hashCode()}")
        Log.d("LoginDebug", "ViewModel hash: ${viewModel.hashCode()}")

    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Log.d("LoginDebug", "Configuration changed: $newConfig")
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
    }

    private fun init() {
        binding.btnLogin.setOnButtonClickListener {
            performLogin()
        }

        binding.tvInviteRegister.setOnClickListener {
            binding.btnLogin.hideLoading()
            navigateTo(
                LoginFragmentDirections.actionLoginFragmentToInviteRegistrationFragment()
            )
        }

        observeLoginState()
    }

    private fun checkData(email: String, password: String): Boolean {
        return !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)
    }

    private fun performLogin() {
        val email = binding.etEmail.getText().toString().trim()
        val password = binding.etPassword.getText().toString().trim()

        if (checkData(email, password)) {
            viewModel.login(email, password)
        }
    }

    private fun observeLoginState() {
        lifecycleScope.launch {
            viewModel.loginState.collect { state ->
                when (state) {
                    is ResultState.Loading -> {
                        binding.btnLogin.showLoading()
                    }

                    is ResultState.Success -> {
                        dismissLoadingDialog()
                        Toast.makeText(requireContext(), "تم تسجيل الدخول بنجاح", Toast.LENGTH_LONG)
                            .show()
                        navigateToHomeScreen()
                    }

                    is ResultState.Error -> {
                        binding.btnLogin.hideLoading()
                        UtilsGeneral.instance?.showSnackBar(binding.getRoot(), state.message)
                    }

                    ResultState.Idle -> {

                    }
                }
            }
        }
    }

    private fun navigateToHomeScreen() {
        navigateTo(
            LoginFragmentDirections.actionLoginFragmentToDashboardFragment()
        )
    }
}