package dev.anonymous.hurriya.admin.domain.mappers

import com.google.firebase.Timestamp
import dev.anonymous.hurriya.admin.data.local.entities.InvitationEntity
import dev.anonymous.hurriya.admin.domain.models.Invitation
import java.util.Date

fun InvitationEntity.toDomain(): Invitation {
    return Invitation(
        id = id,
        code = code,
        hint = hint,
        role = role,
        used = used,
        usedBy = usedBy,
        createdAt = Timestamp(Date(createdAt))
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
        createdAt = createdAt.toDate().time,
    )
}
