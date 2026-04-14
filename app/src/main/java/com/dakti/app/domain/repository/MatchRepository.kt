package com.dakti.app.domain.repository

import com.dakti.app.domain.model.Match
import com.dakti.app.util.Resource

interface MatchRepository {
    suspend fun getMyMatches(): Resource<List<Match>>
    suspend fun createMatch(title: String): Resource<Match>
    suspend fun getMatchDetails(matchId: String): Resource<Match>
}
