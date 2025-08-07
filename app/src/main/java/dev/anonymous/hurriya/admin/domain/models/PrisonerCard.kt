package dev.anonymous.hurriya.admin.domain.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import dev.anonymous.hurriya.admin.domain.models.base.FirestoreModel
import java.io.Serializable

data class PrisonerCard(
    @get:Exclude @set:Exclude
    override var id: String? = null,
    var imageUrl: String? = null,
    var name: String? = null,
    var dateOfArrest: String? = null,
    var judgment: String? = null,
    var living: String? = null,
    val uploadDate: Timestamp? = null
) : Serializable, FirestoreModel {
    constructor(
        imageUrl: String?,
        name: String?,
        dateOfArrest: String?,
        judgment: String?,
        living: String?,
        uploadDate: Timestamp?
    ) : this(null, imageUrl, name, dateOfArrest, judgment, living, uploadDate)
}
