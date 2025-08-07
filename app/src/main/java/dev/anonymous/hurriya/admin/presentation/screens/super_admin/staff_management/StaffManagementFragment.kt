package dev.anonymous.hurriya.admin.presentation.screens.super_admin.staff_management

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import dev.anonymous.hurriya.admin.R
import dev.anonymous.hurriya.admin.databinding.FragmentStaffManagementBinding
import dev.anonymous.hurriya.admin.domain.models.StaffItem
import dev.anonymous.hurriya.admin.presentation.components.BaseFragment
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

        ovs()
    }

    fun ovs() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isSuperAdmin.collectLatest { isSuper ->
                    staffAdapter = StaffAdapter(this@StaffManagementFragment, isSuper)
                    binding.recyclerStaff.adapter = staffAdapter

                    viewModel.staffList.collectLatest { list ->
                        staffAdapter.submitList(list)
                    }
                }
            }
        }
    }

    override fun onOptionsClicked(view: View, staff: StaffItem) {
        showPopupMenu(view, staff)
    }

    private fun showPopupMenu(anchor: View, staff: StaffItem) {
        currentPopupMenu?.dismiss()
        val popupMenu = PopupMenu(requireContext(), anchor)
        currentPopupMenu = popupMenu
        popupMenu.menuInflater.inflate(R.menu.menu_staff_options, popupMenu.menu)
        popupMenu.setForceShowIcon(true)

        val switchRoleItem = popupMenu.menu.findItem(R.id.action_switch_role)
        switchRoleItem?.title = if (staff.role == "admin") {
            "تغيير إلى محرر"
        } else {
            "تغيير إلى مشرف"
        }

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_switch_role -> {
                    val newRole = if (staff.role == "admin") "editor" else "admin"
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