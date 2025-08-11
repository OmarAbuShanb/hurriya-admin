package dev.anonymous.hurriya.admin.domain.models

import com.google.firebase.Timestamp
import com.google.firebase.database.PropertyName

data class Staff(
    val uid: String = "",
    val name: String = "",
    val role: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    @get:PropertyName("isOnline")
    val isOnline: Boolean = false,
    val lastSeen: Long = 0L
) {
    override fun toString(): String {
        return "Staff(uid='$uid', name='$name', role='$role', createdAt=$createdAt, isOnline=$isOnline, lastSeen=$lastSeen)"
    }
}