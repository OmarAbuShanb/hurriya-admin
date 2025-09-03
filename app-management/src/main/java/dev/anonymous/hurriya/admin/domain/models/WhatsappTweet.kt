package dev.anonymous.hurriya.admin.domain.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import dev.anonymous.hurriya.admin.domain.models.base.FirestoreModel

data class WhatsappTweet(
    @get:Exclude @set:Exclude
    override var id: String?,
    var message: String?,
    var lastUpdate: Timestamp?,
) : FirestoreModel {
    constructor(message: String?, lastUpdate: Timestamp?) : this(null, message, lastUpdate)
}