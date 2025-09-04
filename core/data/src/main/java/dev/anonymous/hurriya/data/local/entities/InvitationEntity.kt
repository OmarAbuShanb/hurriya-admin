package dev.anonymous.hurriya.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "invitations")
data class InvitationEntity(
    @PrimaryKey val id: String,
    val code: String,
    val hint: String,
    val role: String,
    val used: Boolean,
    val usedBy: String?,
    val createdAt: Long
)