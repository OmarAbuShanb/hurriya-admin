package dev.anonymous.hurriya.admin.domain.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp
import dev.anonymous.hurriya.admin.domain.models.base.FirestoreModel
import java.io.Serializable

data class Album(
    @get:Exclude @set:Exclude
    override var id: String? = null,

    var imageUrl: String? = null,
    var title: String? = null,

    @ServerTimestamp
    @Transient
    var timestamp: Timestamp? = null
) : Serializable, FirestoreModel {
    constructor(imageUrl: String?, title: String?, timestamp: Timestamp?)
            : this(null, imageUrl, title, timestamp)
}
