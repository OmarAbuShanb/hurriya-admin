package dev.anonymous.hurriya.domain.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import dev.anonymous.hurriya.domain.models.base.FirestoreModel

data class Poster(
    @get:Exclude @set:Exclude
    override var id: String? = null,
    var imageUrl: String? = null,
    val uploadDate: Timestamp? = null
) : FirestoreModel {
    constructor(imageUrl: String?, uploadDate: Timestamp?) : this(null, imageUrl, uploadDate)
}
