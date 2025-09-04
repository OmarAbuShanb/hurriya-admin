package dev.anonymous.hurriya.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.anonymous.hurriya.data.local.entities.InvitationEntity

@Dao
interface InvitationDao {
    @Query("SELECT * FROM invitations ORDER BY createdAt DESC")
    fun getAllInvitations(): PagingSource<Int, InvitationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(invitation: InvitationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(invitations: List<InvitationEntity>)

    @Query("DELETE FROM invitations")
    suspend fun clearAll()

    @Query("DELETE FROM invitations WHERE id = :invitationId")
    suspend fun deleteById(invitationId: String)
}