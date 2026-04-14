package com.dakti.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dakti.app.domain.model.AISuggestionType

@Entity(
    tableName = "ai_requests",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["userId", "createdAt"])
    ]
)
data class AIRequestEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val promptText: String,
    val contextType: String?,
    val createdAt: Long
)

@Entity(
    tableName = "ai_suggestions",
    foreignKeys = [
        ForeignKey(
            entity = AIRequestEntity::class,
            parentColumns = ["id"],
            childColumns = ["requestId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["requestId", "type"]),
        Index(value = ["createdAt"])
    ]
)
data class AISuggestionEntity(
    @PrimaryKey
    val id: String,
    val requestId: String,
    val type: AISuggestionType,
    val suggestionText: String,
    val confidenceScore: Double?,
    val createdAt: Long
)
