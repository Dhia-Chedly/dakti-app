package com.dakti.app.domain.repository

import com.dakti.app.domain.model.Match
import com.dakti.app.domain.model.MatchWithInvitations
import com.dakti.app.util.Resource
import kotlinx.coroutines.flow.Flow

interface MatchRepository {
    suspend fun getMyMatches(): Resource<List<Match>>
    suspend fun createMatch(title: String): Resource<Match>
    suspend fun getMatchDetails(matchId: String): Resource<Match>

    fun observeMatchesByOrganizer(organizerId: String): Flow<List<Match>>
    fun observeMatchWithInvitations(matchId: String): Flow<MatchWithInvitations?>
    suspend fun saveMatch(match: Match): Resource<Match>
}
