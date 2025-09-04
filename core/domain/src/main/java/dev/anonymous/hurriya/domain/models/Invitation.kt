package dev.anonymous.hurriya.domain.models

import java.io.Serializable
import java.util.Date

data class Invitation(
    val id: String = "",
    val code: String = "",
    val hint: String = "",
    val role: String = "",
    val used: Boolean = false,
    val usedBy: String? = null,
    val createdAt: Date? = null
) : Serializable