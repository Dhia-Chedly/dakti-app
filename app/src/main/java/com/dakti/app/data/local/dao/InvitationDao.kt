package com.dakti.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dakti.app.data.local.entity.InvitationEntity
import com.dakti.app.domain.model.InvitationResponseStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface InvitationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertInvitation(invitation: InvitationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertInvitations(invitations: List<InvitationEntity>)

    @Query("SELECT * FROM invitations WHERE id = :invitationId LIMIT 1")
    suspend fun getInvitationById(invitationId: String): InvitationEntity?

    @Query("SELECT * FROM invitations WHERE playerId = :playerId ORDER BY sentAt DESC")
    suspend fun getInvitationsByPlayer(playerId: String): List<InvitationEntity>

    @Query("SELECT * FROM invitations WHERE playerId = :playerId ORDER BY sentAt DESC")
    fun observeInvitationsByPlayer(playerId: String): Flow<List<InvitationEntity>>

    @Query("SELECT * FROM invitations WHERE matchId = :matchId ORDER BY sentAt DESC")
    suspend fun getInvitationsByMatch(matchId: String): List<InvitationEntity>

    @Query("SELECT * FROM invitations WHERE matchId = :matchId ORDER BY sentAt DESC")
    fun observeInvitationsByMatch(matchId: String): Flow<List<InvitationEntity>>

    @Query("SELECT COUNT(*) FROM invitations WHERE matchId = :matchId AND status = 'PENDING'")
    suspend fun countPendingInvitationsForMatch(matchId: String): Int

    @Query("SELECT * FROM invitations WHERE matchId = :matchId AND playerId IN (:playerIds)")
    suspend fun getInvitationsByMatchAndPlayers(
        matchId: String,
        playerIds: List<String>
    ): List<InvitationEntity>

    @Query("UPDATE invitations SET status = :status, respondedAt = :respondedAt WHERE id = :invitationId")
    suspend fun updateInvitationStatus(
        invitationId: String,
        status: InvitationResponseStatus,
        respondedAt: Long?
    )

    @Query("DELETE FROM invitations WHERE id = :invitationId")
    suspend fun deleteInvitation(invitationId: String)
}
