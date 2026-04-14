package com.dakti.app.domain.repository

import com.dakti.app.domain.model.AIRequest
import com.dakti.app.domain.model.AISuggestion
import com.dakti.app.util.Resource
import kotlinx.coroutines.flow.Flow

interface AssistantRepository {
    suspend fun askAssistant(prompt: String): Resource<String>

    suspend fun createRequest(request: AIRequest): Resource<AIRequest>
    suspend fun saveSuggestions(
        requestId: String,
        suggestions: List<AISuggestion>
    ): Resource<Unit>
    fun observeSuggestions(requestId: String): Flow<List<AISuggestion>>
}
