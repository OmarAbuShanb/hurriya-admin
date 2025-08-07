package dev.anonymous.hurriya.admin.presentation.screens.super_admin.staff_management

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dev.anonymous.hurriya.admin.R
import dev.anonymous.hurriya.admin.databinding.ItemStaffMemberBinding
import dev.anonymous.hurriya.admin.domain.models.StaffItem
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
        fun bind(item: StaffItem) {
            binding.tvName.text = item.name

            binding.tvRole.text = when (item.role) {
                "superadmin" -> "مالك"
                "admin" -> "مشرف"
                else -> "محرر"
            }

            binding.tvStatus.text = PresenceFormatter.getLastSeenText(
                item.lastSeen,
                item.isOnline
            )

            binding.dotStatus.setBackgroundResource(
                if (item.isOnline != null && item.isOnline)
                    R.drawable.green_dot
                else
                    R.drawable.gray_dot
            )

            binding.btnOptions.visibility = if (isSuperAdmin && item.role != "superadmin") {
                binding.btnOptions.setOnClickListener { v ->
                    listener.onOptionsClicked(v, item)
                }
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<StaffItem>() {
        override fun areItemsTheSame(oldItem: StaffItem, newItem: StaffItem) =
            oldItem.uid == newItem.uid

        override fun areContentsTheSame(oldItem: StaffItem, newItem: StaffItem) = oldItem == newItem
    }
}