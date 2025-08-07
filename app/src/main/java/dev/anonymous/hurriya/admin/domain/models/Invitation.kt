package dev.anonymous.hurriya.admin.domain.models

import com.google.firebase.Timestamp
import java.io.Serializable

data class Invitation(
    val id: String = "",
    val code: String = "",
    val hint: String = "",
    val role: String = "",
    val used: Boolean = false,
    val usedBy: String? = null,
    val createdAt: Timestamp = Timestamp.Companion.now()
) : Serializable