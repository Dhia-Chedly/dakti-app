package com.dakti.app.ai.service

interface AiAssistantService {
    suspend fun generateReply(prompt: String): String
}

class PlaceholderAiAssistantService : AiAssistantService {
    override suspend fun generateReply(prompt: String): String {
        return "Placeholder AI response for: $prompt"
    }
}
