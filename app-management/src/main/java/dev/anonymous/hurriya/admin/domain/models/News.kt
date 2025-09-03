package dev.anonymous.hurriya.admin.domain.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp
import dev.anonymous.hurriya.admin.domain.models.base.FirestoreModel
import java.io.Serializable

data class News(
    @get:Exclude @set:Exclude
    override var id: String? = null,
    var imageUrl: String? = null,
    var title: String? = null,
    var details: String? = null,

    @ServerTimestamp
    @Transient
    var timestamp: Timestamp? = null
) : Serializable, FirestoreModel {
    constructor(imageUrl: String?, title: String?, details: String?) : this(
        null,
        imageUrl,
        title,
        details,
        null
    )
}
