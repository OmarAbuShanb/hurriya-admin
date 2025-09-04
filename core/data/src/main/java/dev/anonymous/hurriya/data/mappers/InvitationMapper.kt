package dev.anonymous.hurriya.data.mappers

import com.google.firebase.firestore.FieldValue
import dev.anonymous.hurriya.data.local.entities.InvitationEntity
import dev.anonymous.hurriya.domain.models.Invitation
import java.util.Date

fun InvitationEntity.toDomain(): Invitation {
    return Invitation(
        id = id,
        code = code,
        hint = hint,
        role = role,
        used = used,
        usedBy = usedBy,
        createdAt = Date(createdAt)
    )
}

fun Invitation.toEntity(): InvitationEntity {
    return InvitationEntity(
        id = id,
        code = code,
        hint = hint,
        role = role,
        used = used,
        usedBy = usedBy,
        createdAt = createdAt!!.time,
    )
}

fun Invitation.toFirebaseMap(): Map<String, Any?> {
    return mapOf(
        "id" to id,
        "code" to code,
        "hint" to hint,
        "role" to role,
        "used" to used,
        "usedBy" to usedBy,
        "createdAt" to FieldValue.serverTimestamp()
    )
}
