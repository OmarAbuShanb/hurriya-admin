package dev.anonymous.hurriya.admin.presentation.screens.super_admin.invite_management

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import dagger.hilt.android.AndroidEntryPoint
import dev.anonymous.hurriya.admin.databinding.FragmentInviteManagementBinding
import dev.anonymous.hurriya.admin.domain.models.Invitation
import dev.anonymous.hurriya.admin.presentation.components.BaseFragment
import dev.anonymous.hurriya.admin.presentation.utils.ClipboardUtils
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class InviteManagementFragment :
    BaseFragment<FragmentInviteManagementBinding>(FragmentInviteManagementBinding::inflate),
    InvitationAdapter.InvitationActionListener {
    private val viewModel: InviteManagementViewModel by activityViewModels()
    private val invitationAdapter by lazy { InvitationAdapter(this) }

    private val generateInviteResultKey = "generate_invite_result"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.floatAddInvite.setOnClickListener {
            navigateTo(
                InviteManagementFragmentDirections.actionToGenerateInviteBottomSheet(
                    generateInviteResultKey
                )
            )
        }
        setupRecyclerView()
        collectInvites()
        observeGenerateInviteSheetResults()
    }

    private fun setupRecyclerView() {
        binding.recyclerInvites.apply {
            adapter = invitationAdapter
            setHasFixedSize(true)
        }
    }

    private fun collectInvites() {
        lifecycleScope.launch {
            viewModel.invitationFlow.collectLatest {
                binding.progressInvites.visibility = View.GONE
                invitationAdapter.submitData(it)
            }
        }

        lifecycleScope.launch {
            invitationAdapter.loadStateFlow.collectLatest { loadStates ->
                val mediatorError = loadStates.mediator?.refresh as? LoadState.Error
                mediatorError?.let {
                    Toast.makeText(
                        requireContext(),
                        "فشل في جلب البيانات: ${it.error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun observeGenerateInviteSheetResults() {
        val savedStateHandle = findNavController().currentBackStackEntry?.savedStateHandle ?: return

        savedStateHandle.getLiveData<Invitation>(generateInviteResultKey)
            .observe(viewLifecycleOwner) { newInvitation ->
                savedStateHandle.remove<String>(generateInviteResultKey)
            }
    }

    override fun onCopyInvitationCode(code: String) {
        ClipboardUtils.copyToClipboard(requireContext(), "كود الدعوة", code)
    }

    override fun onDeleteInvitation(invitationId: String) {
        viewModel.deleteInvitation(invitationId)
    }
}