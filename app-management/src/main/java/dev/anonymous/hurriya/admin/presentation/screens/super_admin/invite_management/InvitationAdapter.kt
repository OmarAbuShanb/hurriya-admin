package dev.anonymous.hurriya.admin.presentation.screens.super_admin.invite_management

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import dev.anonymous.hurriya.admin.R
import dev.anonymous.hurriya.admin.databinding.ItemInvitationBinding
import dev.anonymous.hurriya.admin.domain.models.Invitation
import java.util.concurrent.TimeUnit

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
        private val context = binding.root.context

        fun bind(invitation: Invitation) = with(binding) {
            tvHint.text = invitation.hint
            tvRole.text = invitation.role

            val statusInfo = getInvitationStatus(invitation)
            tvStatus.text = context.getString(statusInfo.labelResId)

            val bg = tvStatus.background.mutate() as GradientDrawable
            bg.setStroke(2, statusInfo.strokeColor)

            btnCopyInvitationCode.setOnClickListener {
                listener.onCopyInvitationCode(invitation.code)
            }

            btnDeleteInvitation.setOnClickListener {
                listener.onDeleteInvitation(invitation.id)
            }
        }

        private fun getInvitationStatus(invitation: Invitation): StatusInfo {
            return when {
                invitation.used -> StatusInfo.Used
                NOW_TIME - invitation.createdAt.toDate().time > ONE_DAY_IN_MILLIS -> StatusInfo.Expired
                else -> StatusInfo.Pending
            }
        }
    }

    interface InvitationActionListener {
        fun onCopyInvitationCode(code: String)
        fun onDeleteInvitation(invitationId: String)
    }

    sealed class StatusInfo(val labelResId: Int, val strokeColor: Int) {
        object Used : StatusInfo(R.string.status_used, R.color.invitation_used)
        object Expired : StatusInfo(R.string.status_expired, R.color.invitation_expired)
        object Pending : StatusInfo(R.string.status_pending, R.color.invitation_pending)
    }

    companion object {
        private val NOW_TIME = System.currentTimeMillis()
        private val ONE_DAY_IN_MILLIS = TimeUnit.DAYS.toMillis(1)

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