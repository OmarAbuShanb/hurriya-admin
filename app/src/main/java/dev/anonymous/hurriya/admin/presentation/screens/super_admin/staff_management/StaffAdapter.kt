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
import dev.anonymous.hurriya.admin.domain.models.StaffItem
import dev.anonymous.hurriya.admin.presentation.components.StaffRole
import dev.anonymous.hurriya.admin.presentation.utils.PresenceFormatter

class StaffAdapter(
    private val listener: OnStaffActionListener,
    private val isSuperAdmin: Boolean,
) : ListAdapter<StaffItem, StaffAdapter.StaffViewHolder>(DiffCallback) {

    interface OnStaffActionListener {
        fun onOptionsClicked(view: View, staff: StaffItem)
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

        fun bind(item: StaffItem) = with(binding) {
            tvName.text = item.name
            tvRole.text = getRoleDisplayName(context, item.role)
            tvStatus.text = PresenceFormatter.getLastSeenText(item.lastSeen, item.isOnline)

            dotStatus.setBackgroundResource(
                if (item.isOnline == true) {
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

    companion object DiffCallback : DiffUtil.ItemCallback<StaffItem>() {
        override fun areItemsTheSame(oldItem: StaffItem, newItem: StaffItem) =
            oldItem.uid == newItem.uid

        override fun areContentsTheSame(oldItem: StaffItem, newItem: StaffItem) = oldItem == newItem
    }
}