package com.dakti.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.dakti.app.data.local.entity.MatchEntity
import com.dakti.app.data.local.entity.MatchWithContextRelation
import com.dakti.app.data.local.entity.MatchWithInvitationsRelation
import kotlinx.coroutines.flow.Flow

@Dao
interface MatchDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMatch(match: MatchEntity)

    @Update
    suspend fun updateMatch(match: MatchEntity)

    @Query("SELECT * FROM matches WHERE id = :matchId LIMIT 1")
    suspend fun getMatchById(matchId: String): MatchEntity?

    @Query("SELECT * FROM matches WHERE organizerId = :organizerId ORDER BY scheduledStartTime ASC")
    suspend fun getMatchesByOrganizer(organizerId: String): List<MatchEntity>

    @Query("SELECT * FROM matches WHERE organizerId = :organizerId ORDER BY scheduledStartTime ASC")
    fun observeMatchesByOrganizer(organizerId: String): Flow<List<MatchEntity>>

    @Transaction
    @Query("SELECT * FROM matches WHERE organizerId = :organizerId ORDER BY scheduledStartTime ASC")
    suspend fun getMatchesWithContextByOrganizer(
        organizerId: String
    ): List<MatchWithContextRelation>

    @Transaction
    @Query("SELECT * FROM matches WHERE id = :matchId LIMIT 1")
    suspend fun getMatchWithContextById(matchId: String): MatchWithContextRelation?

    @Transaction
    @Query("SELECT * FROM matches WHERE id IN (:matchIds)")
    suspend fun getMatchesWithContextByIds(matchIds: List<String>): List<MatchWithContextRelation>

    @Transaction
    @Query("SELECT * FROM matches WHERE id = :matchId LIMIT 1")
    fun observeMatchWithInvitations(matchId: String): Flow<MatchWithInvitationsRelation?>

    @Query("DELETE FROM matches WHERE id = :matchId")
    suspend fun deleteMatch(matchId: String)
}
