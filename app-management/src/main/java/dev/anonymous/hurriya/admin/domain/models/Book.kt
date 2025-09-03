package dev.anonymous.hurriya.admin.domain.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import dev.anonymous.hurriya.admin.domain.models.base.FirestoreModel
import java.io.Serializable

data class Book(
    @get:Exclude @set:Exclude
    override var id: String? = null,
    var imageUrl: String? = null,
    var name: String? = null,
    var author: String? = null,
    var pdfUrl: String? = null,
    val uploadDate: Timestamp? = null
) : Serializable, FirestoreModel {
    constructor(
        imageUrl: String?,
        name: String?,
        author: String?,
        pdfUrl: String?,
        uploadDate: Timestamp?
    ) : this(null, imageUrl, name, author, pdfUrl, uploadDate)
}
