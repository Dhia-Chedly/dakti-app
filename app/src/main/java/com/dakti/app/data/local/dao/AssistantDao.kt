package com.dakti.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dakti.app.data.local.entity.AIRequestEntity
import com.dakti.app.data.local.entity.AISuggestionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AssistantDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAIRequest(request: AIRequestEntity)

    @Query("SELECT * FROM ai_requests WHERE id = :requestId LIMIT 1")
    suspend fun getRequestById(requestId: String): AIRequestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSuggestion(suggestion: AISuggestionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSuggestions(suggestions: List<AISuggestionEntity>)

    @Query("SELECT * FROM ai_suggestions WHERE requestId = :requestId ORDER BY createdAt DESC")
    fun observeSuggestionsByRequest(requestId: String): Flow<List<AISuggestionEntity>>
}
