package dev.anonymous.hurriya.admin.domain.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp
import dev.anonymous.hurriya.admin.domain.models.base.FirestoreModel
import java.io.Serializable

data class Statistic(
    @get:Exclude @set:Exclude
    override var id: String?,
    var title: String?,
    var iconUrl: String?,
    var number: Int?,
    var pdfUrl: String?,
    // transient => Parcelable encountered IOException writing serializable object
    @ServerTimestamp
    @Transient
    var timestamp: Timestamp? = null,
) : Serializable, FirestoreModel {
    constructor(
        title: String?,
        iconUrl: String?,
        number: Int?,
        pdfUrl: String?,
        timestamp: Timestamp?
    ) : this(null, title, iconUrl, number, pdfUrl, timestamp)
}