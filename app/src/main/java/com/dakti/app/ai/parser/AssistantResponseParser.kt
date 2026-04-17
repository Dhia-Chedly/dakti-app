package com.dakti.app.ai.parser

import com.dakti.app.domain.model.AISuggestionType

data class ParsedAssistantSuggestion(
    val type: AISuggestionType,
    val title: String,
    val description: String?
)

data class ParsedAssistantResponse(
    val replyText: String,
    val suggestions: List<ParsedAssistantSuggestion>
)

object AssistantResponseParser {
    fun parse(rawResponse: String): ParsedAssistantResponse {
        val lines = rawResponse
            .lineSequence()
            .map { line -> line.trim() }
            .filter { line -> line.isNotBlank() }
            .toList()

        val suggestionLines = lines.filter { line -> line.startsWith(SUGGESTION_PREFIX) }
        val responseLines = lines.filterNot { line -> line.startsWith(SUGGESTION_PREFIX) }

        val suggestions = suggestionLines.mapNotNull { line -> parseSuggestionLine(line) }
        val reply = responseLines.joinToString(separator = "\n").trim().ifBlank {
            "I can help with planning your venue, match setup, invitations, and scheduling."
        }

        return ParsedAssistantResponse(
            replyText = reply,
            suggestions = suggestions
        )
    }

    private fun parseSuggestionLine(line: String): ParsedAssistantSuggestion? {
        val parts = line.split("|")
        if (parts.size < 4) {
            return null
        }

        val type = parts[1].trim().toSuggestionTypeOrNull() ?: AISuggestionType.GENERAL
        val title = parts[2].trim()
        val description = parts.subList(3, parts.size)
            .joinToString(separator = "|")
            .trim()
            .takeIf { value -> value.isNotBlank() }

        if (title.isBlank()) {
            return null
        }

        return ParsedAssistantSuggestion(
            type = type,
            title = title,
            description = description
        )
    }

    private fun String.toSuggestionTypeOrNull(): AISuggestionType? {
        return runCatching {
            AISuggestionType.valueOf(trim().uppercase())
        }.getOrNull()
    }

    private const val SUGGESTION_PREFIX: String = "SUGGESTION|"
}
