package dev.anonymous.hurriya.admin.presentation.screens.super_admin.staff_management

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dev.anonymous.hurriya.admin.R
import dev.anonymous.hurriya.admin.databinding.ItemStaffMemberBinding
import dev.anonymous.hurriya.admin.domain.models.Staff
import dev.anonymous.hurriya.admin.presentation.components.StaffRole
import dev.anonymous.hurriya.admin.presentation.utils.PresenceFormatter

class StaffAdapter(
    private val listener: OnStaffActionListener,
    private val isSuperAdmin: Boolean,
) : ListAdapter<Staff, StaffAdapter.StaffViewHolder>(DiffCallback) {

    interface OnStaffActionListener {
        fun onOptionsClicked(view: View, staff: Staff)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StaffViewHolder {
        val binding = ItemStaffMemberBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return StaffViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StaffViewHolder, position: Int) {
        holder.bind(getItem(position))

    }

    inner class StaffViewHolder(private val binding: ItemStaffMemberBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val context: Context = binding.root.context

        fun bind(item: Staff) = with(binding) {
            tvName.text = item.name
            tvRole.text = getRoleDisplayName(context, item.role)
            tvStatus.text = PresenceFormatter.getLastSeenText(item.lastSeen, item.isOnline)

            dotStatus.setBackgroundResource(
                if (item.isOnline) {
                    R.drawable.green_dot
                } else {
                    R.drawable.gray_dot
                }
            )

            val shouldShowOptions = isSuperAdmin && item.role != StaffRole.SUPER_ADMIN.value
            btnOptions.visibility = if (shouldShowOptions) View.VISIBLE else View.GONE
            btnOptions.setOnClickListener(
                if (shouldShowOptions) { v ->
                    listener.onOptionsClicked(v, item)
                } else null
            )
        }

        private fun getRoleDisplayName(context: Context, role: String) =
            StaffRole.entries.firstOrNull { it.value == role }?.let {
                context.getString(it.displayResId)
            } ?: role
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Staff>() {
        override fun areItemsTheSame(oldItem: Staff, newItem: Staff) = oldItem.uid == newItem.uid
        override fun areContentsTheSame(oldItem: Staff, newItem: Staff) =
            oldItem.name == newItem.name &&
                    oldItem.role == newItem.role &&
                    oldItem.isOnline == newItem.isOnline &&
                    oldItem.lastSeen == newItem.lastSeen
    }
}