package dev.anonymous.hurriya.domain.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp
import dev.anonymous.hurriya.domain.models.base.FirestoreModel
import java.io.Serializable

data class Video(
    @get:Exclude @set:Exclude
    override var id: String? = null,
    var videoUrl: String? = null,
    var videoImageUrl: String? = null,
    var title: String? = null,

    @ServerTimestamp
    @Transient
    val timestamp: Timestamp? = null
) : Serializable, FirestoreModel {
    constructor(
        videoUrl: String?,
        videoImageUrl: String?,
        title: String?,
        timestamp: Timestamp?
    ) : this(null, videoUrl, videoImageUrl, title, timestamp)
}
