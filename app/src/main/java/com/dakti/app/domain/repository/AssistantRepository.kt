package com.dakti.app.domain.repository

import com.dakti.app.util.Resource

interface AssistantRepository {
    suspend fun askAssistant(prompt: String): Resource<String>
}
