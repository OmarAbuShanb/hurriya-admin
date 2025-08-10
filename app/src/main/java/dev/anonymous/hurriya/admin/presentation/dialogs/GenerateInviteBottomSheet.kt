package dev.anonymous.hurriya.admin.presentation.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import dev.anonymous.hurriya.admin.R
import dev.anonymous.hurriya.admin.core.utils.ResultState
import dev.anonymous.hurriya.admin.databinding.BottomSheetGenerateInviteBinding
import dev.anonymous.hurriya.admin.presentation.components.StaffRole
import dev.anonymous.hurriya.admin.presentation.screens.super_admin.invite_management.InviteManagementViewModel
import kotlinx.coroutines.launch

@AndroidEntryPoint
class GenerateInviteBottomSheet() : BottomSheetDialogFragment() {
    private lateinit var binding: BottomSheetGenerateInviteBinding
    private val viewModel: InviteManagementViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = BottomSheetGenerateInviteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()

        lifecycleScope.launch {
            viewModel.generateInviteState.collect { state ->
                if (state is ResultState.Success) {
                    Toast.makeText(
                        requireContext(),
                        "تم توليد الرمز: ${state.data.code}",
                        Toast.LENGTH_SHORT
                    ).show()
                    dismiss()
                } else if (state is ResultState.Error) {
                    Toast.makeText(requireContext(), "خطأ: ${state.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun setupListeners() {
        binding.btnGenerateInvite.setOnClickListener {
            val hint = binding.etHint.text.toString().trim()
            val selectedRole = when (binding.roleRadioGroup.checkedRadioButtonId) {
                R.id.rbEditor -> StaffRole.EDITOR
                R.id.rbAdmin -> StaffRole.ADMIN
                else -> StaffRole.EDITOR
            }
            viewModel.generateInvite(hint, selectedRole)
        }
    }
}
