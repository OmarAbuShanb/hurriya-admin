package dev.anonymous.hurriya.admin.data.remote.datasource

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
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
import dev.anonymous.hurriya.admin.domain.models.Staff
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class StaffRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val realtimeDb: FirebaseDatabase,
    private val functions: FirebaseFunctions
) {

    fun listenStaffListSorted(): Flow<List<Staff>> = callbackFlow {
        val ref = realtimeDb.getReference(RealtimeDbPaths.STAFF)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { child ->
                    child.key?.let {
                        child.getValue(Staff::class.java)?.copy(uid = it)
                    }
                }
                val sorted = list.sortedWith(
                    compareByDescending<Staff> { it.isOnline }
                        .thenByDescending { it.lastSeen }
                )
                trySend(sorted)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        ref.addValueEventListener(listener)
        awaitClose {
            ref.removeEventListener(listener)
        }
    }.distinctUntilChanged()

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
