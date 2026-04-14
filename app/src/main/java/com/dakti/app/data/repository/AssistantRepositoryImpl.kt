package com.dakti.app.data.repository

import com.dakti.app.ai.service.AiAssistantService
import com.dakti.app.domain.repository.AssistantRepository
import com.dakti.app.util.Resource
import javax.inject.Inject

class AssistantRepositoryImpl @Inject constructor(
    private val aiAssistantService: AiAssistantService
) : AssistantRepository {
    override suspend fun askAssistant(prompt: String): Resource<String> {
        val reply = aiAssistantService.generateReply(prompt)
        return Resource.Success(reply)
    }
}
