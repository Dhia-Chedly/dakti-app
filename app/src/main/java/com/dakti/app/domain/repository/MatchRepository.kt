package com.dakti.app.domain.repository

import com.dakti.app.domain.model.Match
import com.dakti.app.domain.model.MatchCreatePayload
import com.dakti.app.domain.model.MatchReservationContext
import com.dakti.app.domain.model.MatchStatus
import com.dakti.app.domain.model.MatchWithContext
import com.dakti.app.domain.model.MatchWithInvitations
import com.dakti.app.util.Resource
import kotlinx.coroutines.flow.Flow

interface MatchRepository {
    suspend fun getMyMatches(): Resource<List<MatchWithContext>>
    suspend fun createMatch(payload: MatchCreatePayload): Resource<MatchWithContext>
    suspend fun getMatchDetails(matchId: String): Resource<MatchWithContext>
    suspend fun getReservationContextsForCurrentOrganizer(): Resource<List<MatchReservationContext>>
    suspend fun updateMatchStatus(matchId: String, status: MatchStatus): Resource<Unit>

    fun observeMatchesByOrganizer(organizerId: String): Flow<List<Match>>
    fun observeMatchWithInvitations(matchId: String): Flow<MatchWithInvitations?>
    suspend fun saveMatch(match: Match): Resource<Match>
}
