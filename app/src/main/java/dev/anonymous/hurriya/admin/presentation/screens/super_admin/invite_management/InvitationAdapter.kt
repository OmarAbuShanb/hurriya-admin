package dev.anonymous.hurriya.admin.presentation.screens.super_admin.invite_management

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import dev.anonymous.hurriya.admin.databinding.ItemInvitationBinding
import dev.anonymous.hurriya.admin.domain.models.Invitation

class InvitationAdapter(
    private val listener: InvitationActionListener
) : PagingDataAdapter<Invitation, InvitationAdapter.InvitationViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InvitationViewHolder {
        val binding = ItemInvitationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return InvitationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InvitationViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    inner class InvitationViewHolder(
        private val binding: ItemInvitationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(invitation: Invitation) = with(binding) {
            tvHint.text = invitation.hint
            tvRole.text = invitation.role

            val statusInfo = getInvitationStatus(invitation)
            tvStatus.text = statusInfo.label

            val bg = tvStatus.background.mutate() as GradientDrawable
            bg.setStroke(2, statusInfo.strokeColor)

            btnCopyInvitationCode.setOnClickListener {
                listener.onCopyInvitationCode(invitation.code)
            }

            btnDeleteInvitation.setOnClickListener {
                listener.onDeleteInvitation(invitation.id)
            }
        }

        fun getInvitationStatus(invitation: Invitation): StatusInfo {
            val now = System.currentTimeMillis()

            return when {
                invitation.used -> StatusInfo("مستخدمة", "#4CAF50".toColorInt())
                now - invitation.createdAt.toDate().time > 86_400_000 -> StatusInfo(
                    "منتهية",
                    "#F44336".toColorInt()
                )

                else -> StatusInfo("معلقة", "#2196F3".toColorInt())
            }
        }
    }

    data class StatusInfo(
        val label: String,
        val strokeColor: Int
    )

    interface InvitationActionListener {
        fun onCopyInvitationCode(code: String)
        fun onDeleteInvitation(invitationId: String)
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Invitation>() {
            override fun areItemsTheSame(oldItem: Invitation, newItem: Invitation): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Invitation, newItem: Invitation): Boolean {
                return oldItem == newItem
            }
        }
    }
}