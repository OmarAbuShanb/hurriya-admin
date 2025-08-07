package dev.anonymous.hurriya.admin.domain.models

import com.google.firebase.Timestamp

data class StaffItem(
    val uid: String = "",
    val name: String = "",
    val role: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val isOnline: Boolean? = null,
    var lastSeen: Long? = null
)