package dev.anonymous.hurriya.admin.data.local.datasource


import androidx.paging.PagingSource
import dev.anonymous.hurriya.admin.data.local.dao.InvitationDao
import dev.anonymous.hurriya.admin.data.local.entities.InvitationEntity
import javax.inject.Inject

class InvitationLocalDataSource @Inject constructor(
    private val dao: InvitationDao
) {
    fun getInvitations(): PagingSource<Int, InvitationEntity> = dao.getAllInvitations()

    suspend fun deleteInvitationById(id: String) = dao.deleteById(id)

    suspend fun insert(invitation: InvitationEntity) = dao.insert(invitation)

    suspend fun insertAll(invitations: List<InvitationEntity>) = dao.insertAll(invitations)

    suspend fun clearAll() = dao.clearAll()
}