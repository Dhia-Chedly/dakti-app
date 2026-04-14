package com.dakti.app.data.repository

import com.dakti.app.domain.model.Match
import com.dakti.app.domain.repository.MatchRepository
import com.dakti.app.util.Resource
import javax.inject.Inject

class MatchRepositoryImpl @Inject constructor() : MatchRepository {
    override suspend fun getMyMatches(): Resource<List<Match>> {
        return Resource.Success(
            listOf(
                Match(id = "match-1", title = "Friday Night 5v5", status = "Open"),
                Match(id = "match-2", title = "Sunday Padel Doubles", status = "Confirmed")
            )
        )
    }

    override suspend fun createMatch(title: String): Resource<Match> {
        return Resource.Success(
            Match(
                id = "match-new",
                title = title,
                status = "Draft"
            )
        )
    }

    override suspend fun getMatchDetails(matchId: String): Resource<Match> {
        return Resource.Success(
            Match(
                id = matchId,
                title = "Placeholder Match",
                status = "Planning"
            )
        )
    }
}
