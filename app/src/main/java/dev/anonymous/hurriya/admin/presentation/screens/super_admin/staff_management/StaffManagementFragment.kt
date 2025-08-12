package dev.anonymous.hurriya.admin.presentation.screens.super_admin.staff_management

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import dev.anonymous.hurriya.admin.R
import dev.anonymous.hurriya.admin.databinding.FragmentStaffManagementBinding
import dev.anonymous.hurriya.admin.domain.models.Staff
import dev.anonymous.hurriya.admin.presentation.components.BaseFragment
import dev.anonymous.hurriya.admin.presentation.components.StaffRole
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class StaffManagementFragment :
    BaseFragment<FragmentStaffManagementBinding>(FragmentStaffManagementBinding::inflate),
    StaffAdapter.OnStaffActionListener {

    private val viewModel: StaffManagementViewModel by viewModels()
    private lateinit var staffAdapter: StaffAdapter
    private var currentPopupMenu: PopupMenu? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initStaffRecycler()
        observeUiState()
        observeEvents()
    }

    private fun initStaffRecycler() {
        staffAdapter = StaffAdapter(this)
        binding.recyclerStaff.adapter = staffAdapter
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collectLatest { state ->
                    staffAdapter.isSuperAdmin = state.isSuperAdmin
                    binding.progressStaff.isVisible = state.isLoading

                    staffAdapter.submitList(state.staffList)
                }
            }
        }
    }

    private fun observeEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collectLatest { event ->
                    when (event) {
                        is UiEvent.ShowSnackbar ->
                            Snackbar.make(binding.root, event.message, Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    override fun onOptionsClicked(view: View, staff: Staff) {
        showPopupMenu(view, staff)
    }

    private fun showPopupMenu(anchor: View, staff: Staff) {
        currentPopupMenu?.dismiss()
        val popupMenu = PopupMenu(requireContext(), anchor)
        currentPopupMenu = popupMenu
        popupMenu.menuInflater.inflate(R.menu.menu_staff_options, popupMenu.menu)
        popupMenu.setForceShowIcon(true)

        val switchRoleItem = popupMenu.menu.findItem(R.id.action_switch_role)
        switchRoleItem?.title = if (staff.role == StaffRole.ADMIN.value) {
            getString(R.string.change_to_editor)
        } else {
            getString(R.string.change_to_admin)
        }

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_switch_role -> {
                    val newRole = if (staff.role == StaffRole.ADMIN.value) {
                        StaffRole.EDITOR
                    } else {
                        StaffRole.ADMIN
                    }
                    viewModel.updateRole(staff.uid, newRole)
                    true
                }

                R.id.action_delete -> {
                    viewModel.deleteStaff(staff.uid)
                    true
                }

                else -> false
            }
        }

        popupMenu.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        currentPopupMenu?.dismiss()
        currentPopupMenu = null
    }
}