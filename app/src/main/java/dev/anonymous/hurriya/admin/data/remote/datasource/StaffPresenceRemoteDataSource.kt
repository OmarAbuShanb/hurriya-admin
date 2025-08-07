package dev.anonymous.hurriya.admin.data.remote.datasource

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import dev.anonymous.hurriya.admin.data.remote.keys.realtime.RealtimeDbPaths
import dev.anonymous.hurriya.admin.data.remote.keys.realtime.StaffPresenceFields
import dev.anonymous.hurriya.admin.domain.presence.PresenceManager
import javax.inject.Inject

class StaffPresenceRemoteDataSource @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseDatabase: FirebaseDatabase
) : PresenceManager {

    private fun getUid(): String? = firebaseAuth.currentUser?.uid

    private fun getPresenceRef(uid: String) =
        firebaseDatabase.getReference(RealtimeDbPaths.STAFF_PRESENCE + uid)

    private fun presenceData(isOnline: Boolean) = mapOf(
        StaffPresenceFields.IS_ONLINE to isOnline,
        StaffPresenceFields.LAST_SEEN to ServerValue.TIMESTAMP
    )

    override fun setOnline() {
        val uid = getUid() ?: return
        getPresenceRef(uid).setValue(presenceData(true))
    }

    override fun setOffline() {
        val uid = getUid() ?: return
        getPresenceRef(uid).setValue(presenceData(false))
    }

    override fun setupOnDisconnect() {
        val uid = getUid() ?: return
        getPresenceRef(uid).onDisconnect().setValue(presenceData(false))
    }
}
