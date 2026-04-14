package com.dakti.app.ai.prompt

object PromptBuilder {
    fun buildMatchSuggestionPrompt(context: String): String {
        return "Suggest a sports match plan for: $context"
    }
}
