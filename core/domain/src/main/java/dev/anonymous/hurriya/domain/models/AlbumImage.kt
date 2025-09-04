package dev.anonymous.hurriya.domain.models

import android.net.Uri
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp
import dev.anonymous.hurriya.domain.models.base.FirestoreModel

data class AlbumImage(
    @get:Exclude @set:Exclude
    override var id: String? = null,

    var imageUrl: String? = null,

    @get:Exclude @set:Exclude
    var imageUri: Uri? = null,

    @ServerTimestamp
    @Transient
    var lastUpdate: Timestamp? = null
) : FirestoreModel {
    constructor(
        imageUrl: String?,
        lastUpdate: Timestamp? = null
    ) : this(null, imageUrl, null, lastUpdate)

    constructor(id: String?) : this(id, null, null, null)

    constructor(id: String?, imageUri: Uri?) : this(id, null, imageUri, null)
}
