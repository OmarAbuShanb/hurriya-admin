package dev.anonymous.hurriya.admin.data.remote.datasource

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import dev.anonymous.hurriya.admin.core.exceptions.UserNotFoundException
import dev.anonymous.hurriya.admin.data.remote.keys.firestore.FirestoreCollections
import dev.anonymous.hurriya.admin.data.remote.keys.firestore.StaffFirestoreFields
import dev.anonymous.hurriya.admin.data.remote.keys.functions.CloudFunctionNames
import dev.anonymous.hurriya.admin.data.remote.keys.functions.StaffFunctionFields
import dev.anonymous.hurriya.admin.data.remote.keys.realtime.RealtimeDbPaths
import dev.anonymous.hurriya.admin.data.remote.keys.realtime.StaffPresenceFields
import dev.anonymous.hurriya.admin.domain.models.StaffItem
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class StaffRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val realtimeDb: FirebaseDatabase,
    private val functions: FirebaseFunctions
) {
    suspend fun fetchStaffList(): Result<List<StaffItem>> {
        return try {
            val snapshot = firestore
                .collection(FirestoreCollections.STAFF)
                .get()
                .await()

            val list = snapshot.mapNotNull { doc ->
                doc.toObject(StaffItem::class.java).copy(uid = doc.id)
            }
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun listenToPresenceUpdates(): Flow<Map<String, Pair<Boolean?, Long?>>> = callbackFlow {
        val ref: DatabaseReference = realtimeDb.getReference(RealtimeDbPaths.STAFF_PRESENCE)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val presenceMap = snapshot.children.associate { staffSnapshot ->
                    val uid = staffSnapshot.key!!

                    val isOnline = staffSnapshot.child(StaffPresenceFields.IS_ONLINE)
                        .getValue(Boolean::class.java)

                    val lastSeen = staffSnapshot.child(StaffPresenceFields.LAST_SEEN)
                        .getValue(Long::class.java)

                    uid to (isOnline to lastSeen)
                }
                trySend(presenceMap)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        ref.addValueEventListener(listener)
        awaitClose {
            ref.removeEventListener(listener)
        }
    }

    suspend fun fetchUserRole(uid: String): Result<String> {
        return try {
            val document = firestore.collection(FirestoreCollections.STAFF)
                .document(uid)
                .get()
                .await()

            if (!document.exists())
                return Result.failure(UserNotFoundException())

            val role = document.getString(StaffFirestoreFields.ROLE)
                ?: return Result.failure(UserNotFoundException())

            Result.success(role)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateRole(targetUid: String, newRole: String): Result<Unit> {
        return try {
            val data = mapOf(
                StaffFunctionFields.Request.UID to targetUid,
                StaffFunctionFields.Request.ROLE to newRole
            )
            functions
                .getHttpsCallable(CloudFunctionNames.UPDATE_STAFF_ROLE)
                .call(data)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteStaff(uid: String): Result<Unit> {
        return try {
            val data = mapOf(StaffFunctionFields.Request.UID to uid)
            functions
                .getHttpsCallable(CloudFunctionNames.DELETE_STAFF)
                .call(data)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
