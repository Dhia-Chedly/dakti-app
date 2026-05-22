package com.dakti.app.data.remote.supabase

import com.dakti.app.data.local.session.SessionLocalDataSource
import com.dakti.app.data.remote.supabase.api.SupabaseApiService
import com.dakti.app.data.remote.supabase.model.AiRequestRowDto
import com.dakti.app.data.remote.supabase.model.AiSuggestionRowDto
import com.dakti.app.data.remote.supabase.model.InvitationRowDto
import com.dakti.app.data.remote.supabase.model.MatchRowDto
import com.dakti.app.data.remote.supabase.model.ProfileRowDto
import com.dakti.app.data.remote.supabase.model.ReservationRowDto
import com.dakti.app.data.remote.supabase.model.SupabaseAuthSignInRequest
import com.dakti.app.data.remote.supabase.model.SupabaseAuthSignUpRequest
import com.dakti.app.data.remote.supabase.model.SupabaseAuthSignUpResponse
import com.dakti.app.data.remote.supabase.model.SupabaseAuthUserResponse
import com.dakti.app.data.remote.supabase.model.SupabaseRefreshTokenRequest
import com.dakti.app.data.remote.supabase.model.SupabaseSessionResponse
import com.dakti.app.data.remote.supabase.model.TimeSlotRowDto
import com.dakti.app.data.remote.supabase.model.VenueRowDto
import com.dakti.app.util.AppConstants
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.HttpException

@Singleton
class SupabaseRemoteDataSource @Inject constructor(
    private val apiService: SupabaseApiService,
    private val gson: Gson,
    private val sessionLocalDataSource: SessionLocalDataSource
) {

    private val refreshMutex = Mutex()

    suspend fun signIn(email: String, password: String): SupabaseSessionResponse {
        ensureConfigured()
        return apiService.signInWithPassword(
            apiKey = AppConstants.SUPABASE_ANON_KEY,
            request = SupabaseAuthSignInRequest(email = email, password = password)
        )
    }

    suspend fun signUp(
        email: String,
        password: String,
        metadata: Map<String, Any?>
    ): SupabaseAuthSignUpResponse {
        ensureConfigured()
        return apiService.signUp(
            apiKey = AppConstants.SUPABASE_ANON_KEY,
            request = SupabaseAuthSignUpRequest(
                email = email,
                password = password,
                data = metadata
            )
        )
    }

    suspend fun getCurrentUser(accessToken: String): SupabaseAuthUserResponse {
        ensureConfigured()
        return apiService.getCurrentUser(
            apiKey = AppConstants.SUPABASE_ANON_KEY,
            authorization = bearer(accessToken)
        )
    }

    suspend fun getProfile(userId: String): ProfileRowDto? {
        val rows = selectProfiles(filters = mapOf("id" to "eq.$userId"))
        return rows.firstOrNull()
    }

    suspend fun selectProfiles(
        filters: Map<String, String> = emptyMap()
    ): List<ProfileRowDto> {
        val query = linkedMapOf(
            "select" to "id,email,full_name,phone,role,avatar_url,preferred_sport,availability_note,created_at,updated_at"
        )
        query.putAll(filters)
        return selectRows(
            table = "profiles",
            query = query,
            type = object : TypeToken<List<ProfileRowDto>>() {}.type
        )
    }

    suspend fun upsertProfile(payload: Map<String, Any?>): ProfileRowDto? {
        val rows = insertRows<ProfileRowDto>(
            table = "profiles",
            payload = listOf(payload),
            extraHeaders = mapOf("Prefer" to "resolution=merge-duplicates,return=representation"),
            type = object : TypeToken<List<ProfileRowDto>>() {}.type
        )
        return rows.firstOrNull()
    }

    suspend fun updateProfile(userId: String, payload: Map<String, Any?>): ProfileRowDto? {
        val rows = updateRows<ProfileRowDto>(
            table = "profiles",
            filters = mapOf("id" to "eq.$userId"),
            payload = payload,
            type = object : TypeToken<List<ProfileRowDto>>() {}.type
        )
        return rows.firstOrNull()
    }

    suspend fun getVenues(
        queryText: String,
        sportType: String?
    ): List<VenueRowDto> {
        val baseFilters = linkedMapOf<String, String>()
        if (sportType != null && sportType.isNotBlank()) {
            baseFilters["sport_type"] = "eq.${sportType.trim()}"
        }

        val safeQuery = queryText.toSafeSearchValue()
        if (safeQuery.isNotBlank()) {
            val likePattern = wildcardPattern(safeQuery)
            baseFilters["or"] = "(name.ilike.$likePattern,address.ilike.$likePattern)"
        }

        val modernQuery = linkedMapOf(
            // Read all available columns from the current Supabase schema.
            "select" to "*",
            "order" to "created_at.desc"
        ).apply { putAll(baseFilters) }

        return try {
            selectRows(
                table = "venues",
                query = modernQuery,
                type = object : TypeToken<List<VenueRowDto>>() {}.type
            )
        } catch (error: HttpException) {
            if (error.code() != 400) {
                throw error
            }

            val compatibleQuery = linkedMapOf(
                "select" to "*",
                "order" to "name.asc"
            ).apply { putAll(baseFilters) }
            selectRows(
                table = "venues",
                query = compatibleQuery,
                type = object : TypeToken<List<VenueRowDto>>() {}.type
            )
        }
    }

    suspend fun getVenueById(venueId: String): VenueRowDto? {
        val rows = selectRows<VenueRowDto>(
            table = "venues",
            query = mapOf(
                "select" to "*",
                "id" to "eq.$venueId",
                "limit" to "1"
            ),
            type = object : TypeToken<List<VenueRowDto>>() {}.type
        )
        return rows.firstOrNull()
    }

    suspend fun upsertVenue(payload: Map<String, Any?>): VenueRowDto? {
        val rows = insertRows<VenueRowDto>(
            table = "venues",
            payload = listOf(payload),
            extraHeaders = mapOf("Prefer" to "resolution=merge-duplicates,return=representation"),
            type = object : TypeToken<List<VenueRowDto>>() {}.type
        )
        return rows.firstOrNull()
    }

    suspend fun getTimeSlotById(slotId: String): TimeSlotRowDto? {
        val rows = selectTimeSlots(
            filters = mapOf(
                "id" to "eq.$slotId",
                "limit" to "1"
            )
        )
        return rows.firstOrNull()
    }

    suspend fun selectTimeSlots(
        filters: Map<String, String> = emptyMap()
    ): List<TimeSlotRowDto> {
        val query = linkedMapOf(
            "select" to "id,venue_id,start_time,end_time,is_available,created_at",
            "order" to "start_time.asc"
        )
        query.putAll(filters)
        return selectRows(
            table = "time_slots",
            query = query,
            type = object : TypeToken<List<TimeSlotRowDto>>() {}.type
        )
    }

    suspend fun upsertTimeSlots(payload: List<Map<String, Any?>>): List<TimeSlotRowDto> {
        return insertRows(
            table = "time_slots",
            payload = payload,
            extraHeaders = mapOf("Prefer" to "resolution=merge-duplicates,return=representation"),
            type = object : TypeToken<List<TimeSlotRowDto>>() {}.type
        )
    }

    suspend fun updateTimeSlot(slotId: String, payload: Map<String, Any?>): TimeSlotRowDto? {
        val rows = updateRows<TimeSlotRowDto>(
            table = "time_slots",
            filters = mapOf("id" to "eq.$slotId"),
            payload = payload,
            type = object : TypeToken<List<TimeSlotRowDto>>() {}.type
        )
        return rows.firstOrNull()
    }

    suspend fun createReservation(payload: Map<String, Any?>): ReservationRowDto? {
        val rows = insertRows<ReservationRowDto>(
            table = "reservations",
            payload = listOf(payload),
            type = object : TypeToken<List<ReservationRowDto>>() {}.type
        )
        return rows.firstOrNull()
    }

    suspend fun getReservationById(reservationId: String): ReservationRowDto? {
        val rows = selectRows<ReservationRowDto>(
            table = "reservations",
            query = mapOf(
                "select" to "id,organizer_id,venue_id,time_slot_id,status,notes,total_price,currency,created_at,updated_at",
                "id" to "eq.$reservationId",
                "limit" to "1"
            ),
            type = object : TypeToken<List<ReservationRowDto>>() {}.type
        )
        return rows.firstOrNull()
    }

    suspend fun getReservationsByOrganizer(organizerId: String): List<ReservationRowDto> {
        return selectRows(
            table = "reservations",
            query = mapOf(
                "select" to "id,organizer_id,venue_id,time_slot_id,status,notes,total_price,currency,created_at,updated_at",
                "organizer_id" to "eq.$organizerId",
                "order" to "created_at.desc"
            ),
            type = object : TypeToken<List<ReservationRowDto>>() {}.type
        )
    }

    suspend fun updateReservation(
        reservationId: String,
        payload: Map<String, Any?>
    ): ReservationRowDto? {
        val rows = updateRows<ReservationRowDto>(
            table = "reservations",
            filters = mapOf("id" to "eq.$reservationId"),
            payload = payload,
            type = object : TypeToken<List<ReservationRowDto>>() {}.type
        )
        return rows.firstOrNull()
    }

    suspend fun createMatch(payload: Map<String, Any?>): MatchRowDto? {
        val rows = insertRows<MatchRowDto>(
            table = "matches",
            payload = listOf(payload),
            type = object : TypeToken<List<MatchRowDto>>() {}.type
        )
        return rows.firstOrNull()
    }

    suspend fun getMatchById(matchId: String): MatchRowDto? {
        val rows = selectRows<MatchRowDto>(
            table = "matches",
            query = mapOf(
                "select" to "*",
                "id" to "eq.$matchId",
                "limit" to "1"
            ),
            type = object : TypeToken<List<MatchRowDto>>() {}.type
        )
        return rows.firstOrNull()
    }

    suspend fun getMatchesByOrganizer(organizerId: String): List<MatchRowDto> {
        return selectRows(
            table = "matches",
            query = mapOf(
                "select" to "*",
                "organizer_id" to "eq.$organizerId",
                "order" to "match_time.asc"
            ),
            type = object : TypeToken<List<MatchRowDto>>() {}.type
        )
    }

    suspend fun updateMatch(matchId: String, payload: Map<String, Any?>): MatchRowDto? {
        val rows = updateRows<MatchRowDto>(
            table = "matches",
            filters = mapOf("id" to "eq.$matchId"),
            payload = payload,
            type = object : TypeToken<List<MatchRowDto>>() {}.type
        )
        return rows.firstOrNull()
    }

    suspend fun getInvitationsByPlayer(playerId: String): List<InvitationRowDto> {
        return selectRows(
            table = "invitations",
            query = mapOf(
                "select" to "id,match_id,player_id,sender_id,message_text,response_status,sent_at,responded_at",
                "player_id" to "eq.$playerId",
                "order" to "sent_at.desc"
            ),
            type = object : TypeToken<List<InvitationRowDto>>() {}.type
        )
    }

    suspend fun getInvitationsByMatch(matchId: String): List<InvitationRowDto> {
        return selectRows(
            table = "invitations",
            query = mapOf(
                "select" to "id,match_id,player_id,sender_id,message_text,response_status,sent_at,responded_at",
                "match_id" to "eq.$matchId",
                "order" to "sent_at.desc"
            ),
            type = object : TypeToken<List<InvitationRowDto>>() {}.type
        )
    }

    suspend fun getInvitationById(invitationId: String): InvitationRowDto? {
        val rows = selectRows<InvitationRowDto>(
            table = "invitations",
            query = mapOf(
                "select" to "id,match_id,player_id,sender_id,message_text,response_status,sent_at,responded_at",
                "id" to "eq.$invitationId",
                "limit" to "1"
            ),
            type = object : TypeToken<List<InvitationRowDto>>() {}.type
        )
        return rows.firstOrNull()
    }

    suspend fun getInvitationsByMatchAndPlayers(
        matchId: String,
        playerIds: List<String>
    ): List<InvitationRowDto> {
        if (playerIds.isEmpty()) {
            return emptyList()
        }
        return selectRows(
            table = "invitations",
            query = mapOf(
                "select" to "id,match_id,player_id,sender_id,message_text,response_status,sent_at,responded_at",
                "match_id" to "eq.$matchId",
                "player_id" to inFilter(playerIds)
            ),
            type = object : TypeToken<List<InvitationRowDto>>() {}.type
        )
    }

    suspend fun createInvitations(payload: List<Map<String, Any?>>): List<InvitationRowDto> {
        if (payload.isEmpty()) {
            return emptyList()
        }
        return insertRows(
            table = "invitations",
            payload = payload,
            type = object : TypeToken<List<InvitationRowDto>>() {}.type
        )
    }

    suspend fun updateInvitation(
        invitationId: String,
        payload: Map<String, Any?>
    ): InvitationRowDto? {
        val rows = updateRows<InvitationRowDto>(
            table = "invitations",
            filters = mapOf("id" to "eq.$invitationId"),
            payload = payload,
            type = object : TypeToken<List<InvitationRowDto>>() {}.type
        )
        return rows.firstOrNull()
    }

    suspend fun createAiRequest(payload: Map<String, Any?>): AiRequestRowDto? {
        val rows = insertRows<AiRequestRowDto>(
            table = "ai_requests",
            payload = listOf(payload),
            type = object : TypeToken<List<AiRequestRowDto>>() {}.type
        )
        return rows.firstOrNull()
    }

    suspend fun createAiSuggestions(payload: List<Map<String, Any?>>): List<AiSuggestionRowDto> {
        return insertRows(
            table = "ai_suggestions",
            payload = payload,
            type = object : TypeToken<List<AiSuggestionRowDto>>() {}.type
        )
    }

    suspend fun invokeFunction(
        functionName: String,
        payload: Map<String, Any?>
    ): JsonObject {
        ensureConfigured()
        val jsonPayload = gson.toJsonTree(payload).asJsonObject

        if (functionName == GEMINI_ASSISTANT_FUNCTION_NAME) {
            return apiService.invokeFunction(
                functionName = functionName,
                headers = anonEdgeFunctionHeaders(),
                payload = jsonPayload
            )
        }

        return withAuthRetry {
            apiService.invokeFunction(
                functionName = functionName,
                headers = baseHeaders(requireRepresentation = false),
                payload = jsonPayload
            )
        }
    }

    suspend fun currentAccessToken(): String? = sessionLocalDataSource.accessToken.first()

    private suspend fun <T> selectRows(
        table: String,
        query: Map<String, String>,
        type: Type
    ): List<T> {
        ensureConfigured()
        val json = withAuthRetry {
            apiService.selectRows(
                table = table,
                headers = baseHeaders(requireRepresentation = false),
                query = query
            )
        }
        return gson.fromJson(json, type)
    }

    private suspend fun <T> insertRows(
        table: String,
        payload: Any,
        type: Type,
        extraHeaders: Map<String, String> = emptyMap()
    ): List<T> {
        ensureConfigured()
        val json = withAuthRetry {
            val headers = baseHeaders(requireRepresentation = true) + extraHeaders
            apiService.insertRows(
                table = table,
                headers = headers,
                payload = gson.toJsonTree(payload)
            )
        }
        return gson.fromJson(json, type)
    }

    private suspend fun <T> updateRows(
        table: String,
        filters: Map<String, String>,
        payload: Map<String, Any?>,
        type: Type
    ): List<T> {
        ensureConfigured()
        val json = withAuthRetry {
            apiService.updateRows(
                table = table,
                headers = baseHeaders(requireRepresentation = true),
                filters = filters,
                payload = gson.toJsonTree(payload).asJsonObject
            )
        }
        return gson.fromJson(json, type)
    }

    private suspend fun <T> withAuthRetry(block: suspend () -> T): T {
        return try {
            block()
        } catch (httpException: HttpException) {
            if (httpException.code() == 401 && refreshSessionIfPossible()) {
                block()
            } else {
                throw httpException
            }
        }
    }

    private suspend fun refreshSessionIfPossible(): Boolean {
        val refreshToken = sessionLocalDataSource.refreshToken.first()
            ?.takeIf { value -> value.isNotBlank() }
            ?: return false

        return refreshMutex.withLock {
            val latestRefreshToken = sessionLocalDataSource.refreshToken.first()
                ?.takeIf { value -> value.isNotBlank() }
                ?: return@withLock false

            if (latestRefreshToken != refreshToken) {
                return@withLock true
            }

            val refreshed = runCatching {
                apiService.refreshSession(
                    apiKey = AppConstants.SUPABASE_ANON_KEY,
                    request = SupabaseRefreshTokenRequest(refreshToken = latestRefreshToken)
                )
            }.getOrNull() ?: return@withLock false

            sessionLocalDataSource.setSession(
                userId = refreshed.user.id,
                accessToken = refreshed.accessToken,
                refreshToken = refreshed.refreshToken
            )
            true
        }
    }

    private suspend fun baseHeaders(
        requireRepresentation: Boolean
    ): Map<String, String> {
        val token = sessionLocalDataSource.accessToken.first()?.takeIf { it.isNotBlank() }
        val bearerToken = token ?: AppConstants.SUPABASE_ANON_KEY

        return buildMap {
            put("apikey", AppConstants.SUPABASE_ANON_KEY)
            put("Authorization", bearer(bearerToken))
            if (requireRepresentation) {
                put("Prefer", "return=representation")
            }
        }
    }

    private fun anonEdgeFunctionHeaders(): Map<String, String> {
        return mapOf(
            "apikey" to AppConstants.SUPABASE_ANON_KEY,
            "Authorization" to bearer(AppConstants.SUPABASE_ANON_KEY)
        )
    }

    private fun ensureConfigured() {
        if (!AppConstants.IS_SUPABASE_CONFIGURED) {
            throw IllegalStateException("Supabase is not configured. Add SUPABASE_URL and SUPABASE_ANON_KEY.")
        }
    }

    private fun bearer(token: String): String = "Bearer $token"

    private fun String.toSafeSearchValue(): String {
        // PostgREST reserves comma/dot/colon/parentheses in filter grammar.
        return trim()
            .replace(",", " ")
            .replace(".", " ")
            .replace(":", " ")
            .replace("(", " ")
            .replace(")", " ")
            .replace("'", " ")
            .replace("\"", " ")
            .replace("\\", " ")
            .replace("%", " ")
            .replace(Regex("\\s+"), " ")
    }

    private fun wildcardPattern(value: String): String = "*$value*"

    private fun inFilter(ids: List<String>): String = "in.(${ids.joinToString(",")})"

    private companion object {
        private const val GEMINI_ASSISTANT_FUNCTION_NAME: String = "gemini-assistant"
    }
}
