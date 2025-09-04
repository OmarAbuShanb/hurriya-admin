package dev.anonymous.hurriya.data.remote.datasource

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.functions.FirebaseFunctions
import dev.anonymous.hurriya.admin.core.exceptions.InvalidResponseException
import dev.anonymous.hurriya.data.remote.keys.firestore.FirestoreCollections
import dev.anonymous.hurriya.data.remote.keys.firestore.InvitationFirestoreFields
import dev.anonymous.hurriya.data.remote.keys.functions.CloudFunctionNames
import dev.anonymous.hurriya.data.remote.keys.functions.InvitationFunctionFields
import dev.anonymous.hurriya.domain.models.Invitation
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class InvitationRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val functions: FirebaseFunctions
) {
    suspend fun fetchInvitations(limit: Long, lastCreatedAt: Timestamp?): List<Invitation> {
        var query = firestore.collection(FirestoreCollections.INVITATIONS)
            .orderBy(InvitationFirestoreFields.CREATED_AT, Query.Direction.DESCENDING)
            .limit(limit)

        if (lastCreatedAt != null) {
            query = query.startAfter(lastCreatedAt)
        }

        val snapshot = query.get().await()

        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(Invitation::class.java)?.copy(id = doc.id)
        }
    }

    suspend fun deleteInvitation(id: String) {
        firestore.collection(FirestoreCollections.INVITATIONS)
            .document(id)
            .delete()
            .await()
    }

    suspend fun generateInvitation(hint: String, role: String): Result<String> {
        val data = hashMapOf(
            InvitationFunctionFields.Request.HINT to hint,
            InvitationFunctionFields.Request.ROLE to role
        )
        val result = functions
            .getHttpsCallable(CloudFunctionNames.GENERATE_INVITE)
            .call(data)
            .await()

        val map = result.data as? Map<*, *>
        val code = map?.get(InvitationFunctionFields.Response.CODE) as? String

        return if (code != null) {
            Result.success(code)
        } else {
            Result.failure(InvalidResponseException())
        }
    }
}