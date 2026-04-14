package com.dakti.app.data.repository

import com.dakti.app.ai.service.AiAssistantService
import com.dakti.app.data.local.dao.AssistantDao
import com.dakti.app.data.mapper.toDomain
import com.dakti.app.data.mapper.toEntity
import com.dakti.app.domain.model.AIRequest
import com.dakti.app.domain.model.AISuggestion
import com.dakti.app.domain.repository.AssistantRepository
import com.dakti.app.util.Resource
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AssistantRepositoryImpl @Inject constructor(
    private val aiAssistantService: AiAssistantService,
    private val assistantDao: AssistantDao
) : AssistantRepository {

    override suspend fun askAssistant(prompt: String): Resource<String> {
        val reply = aiAssistantService.generateReply(prompt)
        return Resource.Success(reply)
    }

    override suspend fun createRequest(request: AIRequest): Resource<AIRequest> {
        assistantDao.upsertAIRequest(request.toEntity())
        return Resource.Success(request)
    }

    override suspend fun saveSuggestions(
        requestId: String,
        suggestions: List<AISuggestion>
    ): Resource<Unit> {
        val entities = suggestions
            .map { suggestion -> suggestion.copy(requestId = requestId).toEntity() }
        assistantDao.upsertSuggestions(entities)
        return Resource.Success(Unit)
    }

    override fun observeSuggestions(requestId: String): Flow<List<AISuggestion>> =
        assistantDao.observeSuggestionsByRequest(requestId)
            .map { entities -> entities.map { entity -> entity.toDomain() } }
}
