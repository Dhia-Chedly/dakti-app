package com.dakti.app.ai.suggestion

import com.dakti.app.ai.prompt.PromptBuilder
import javax.inject.Inject

class SuggestionEngine @Inject constructor() {
    fun buildPrompt(topic: String): String {
        return PromptBuilder.buildMatchSuggestionPrompt(topic)
    }
}
