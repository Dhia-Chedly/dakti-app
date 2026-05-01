package com.dakti.app.ai.service

import com.dakti.app.data.remote.supabase.SupabaseRemoteDataSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseEdgeAiAssistantService @Inject constructor(
    private val supabaseRemoteDataSource: SupabaseRemoteDataSource
) : AiAssistantService {

    private val demoFallbackService = DemoAiAssistantService()

    override suspend fun generateReply(request: AiAssistantRequest): AiAssistantResponse {
        return runCatching {
            val response = supabaseRemoteDataSource.invokeFunction(
                functionName = "gemini-assistant",
                payload = mapOf(
                    "prompt" to request.userMessage,
                    "context" to mapOf(
                        "compiledPrompt" to request.compiledPrompt,
                        "conversation" to request.conversation
                    )
                )
            )

            val text = response.get("assistantText")?.asString
                ?.takeIf { value -> value.isNotBlank() }
                ?: "Assistant is available but returned an empty response."

            AiAssistantResponse(
                rawText = text,
                providerLabel = response.get("provider")?.asString ?: "Supabase Gemini",
                usedFallback = response.get("provider")?.asString != "gemini"
            )
        }.getOrElse {
            runCatching {
                demoFallbackService.generateReply(request).copy(
                    providerLabel = "Dakti Demo Assistant (fallback)",
                    usedFallback = true
                )
            }.getOrElse {
                AiAssistantResponse(
                    rawText = "Assistant is temporarily unavailable. You can still continue with manual planning.",
                    providerLabel = "Supabase Gemini",
                    usedFallback = true
                )
            }
        }
    }
}
