package com.dakti.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dakti.app.domain.model.InvitationResponseStatus
import com.dakti.app.domain.model.InvitationWithContext
import com.dakti.app.domain.model.MatchMonitoringResult
import com.dakti.app.domain.model.MatchWithContext
import com.dakti.app.domain.repository.AuthRepository
import com.dakti.app.domain.usecase.EvaluateMyMatchReadinessUseCase
import com.dakti.app.domain.usecase.GetMyMatchesUseCase
import com.dakti.app.domain.usecase.GetPlayerInvitationsUseCase
import com.dakti.app.domain.usecase.RespondToInvitationUseCase
import com.dakti.app.domain.usecase.SearchVenuesUseCase
import com.dakti.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class HomeQuickActionType {
    BOOK_VENUE,
    CREATE_MATCH,
    INVITE_PLAYERS,
    ASK_AI
}

data class HomeHeaderUi(
    val greeting: String = "Hello, Champ!",
    val avatarUrl: String? = null,
    val notificationCount: Int = 0
)

data class HomeNextMatchUi(
    val matchId: String? = null,
    val sectionLabel: String = "NEXT MATCH",
    val dateTimeLabel: String = "No scheduled match",
    val venueLabel: String = "Book a venue and create a match to get started.",
    val readinessLabel: String = "0/0 Ready",
    val readinessProgress: Float = 0f,
    val hasMatch: Boolean = false,
    val remainingSpots: Int = 0
)

data class HomeQuickActionUi(
    val type: HomeQuickActionType,
    val title: String,
    val isPrimary: Boolean = false
)

data class HomeInsightBannerUi(
    val message: String = "No upcoming match yet. Create one and start inviting players.",
    val ctaLabel: String = "Create Match"
)

data class HomeInvitationPreviewUi(
    val invitationId: String,
    val matchId: String,
    val title: String,
    val scheduledLabel: String,
    val canRespond: Boolean,
    val isResponding: Boolean
)

data class HomeRecommendedVenueUi(
    val id: String,
    val name: String,
    val address: String,
    val sportType: String,
    val imageUrl: String?,
    val distanceLabel: String?,
    val ratingLabel: String?,
    val priceLabel: String?
)

data class HomeUiState(
    val isLoading: Boolean = true,
    val header: HomeHeaderUi = HomeHeaderUi(),
    val nextMatch: HomeNextMatchUi = HomeNextMatchUi(),
    val quickActions: List<HomeQuickActionUi> = defaultHomeQuickActions(),
    val insightBanner: HomeInsightBannerUi = HomeInsightBannerUi(),
    val upcomingInvitations: List<HomeInvitationPreviewUi> = emptyList(),
    val isInvitationsLoading: Boolean = true,
    val invitationsMessage: String? = null,
    val recommendedVenues: List<HomeRecommendedVenueUi> = emptyList(),
    val isVenuesLoading: Boolean = true,
    val errorMessage: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val getMyMatchesUseCase: GetMyMatchesUseCase,
    private val evaluateMyMatchReadinessUseCase: EvaluateMyMatchReadinessUseCase,
    private val getPlayerInvitationsUseCase: GetPlayerInvitationsUseCase,
    private val respondToInvitationUseCase: RespondToInvitationUseCase,
    private val searchVenuesUseCase: SearchVenuesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeAuthenticatedUser()
        refreshHomeData()
    }

    fun refreshHomeData() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    isInvitationsLoading = true,
                    isVenuesLoading = true,
                    errorMessage = null
                )
            }

            val matchesResult = getMyMatchesUseCase()
            val readinessResult = evaluateMyMatchReadinessUseCase()
            val invitationsResult = getPlayerInvitationsUseCase()
            val venuesResult = searchVenuesUseCase(query = "", sportType = null)

            val nextMatch = buildNextMatchUi(matchesResult, readinessResult)
            val invitations = buildInvitationPreview(invitationsResult)
            val venues = buildRecommendedVenuePreview(venuesResult)

            val errors = buildList {
                if (matchesResult is Resource.Error) add(matchesResult.message)
                if (readinessResult is Resource.Error) add(readinessResult.message)
                if (invitationsResult is Resource.Error) add(invitationsResult.message)
                if (venuesResult is Resource.Error) add(venuesResult.message)
            }

            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    nextMatch = nextMatch,
                    insightBanner = buildInsightBanner(nextMatch),
                    upcomingInvitations = invitations,
                    isInvitationsLoading = false,
                    invitationsMessage = if (invitations.isEmpty()) {
                        "No pending invitations at the moment."
                    } else {
                        null
                    },
                    recommendedVenues = venues,
                    isVenuesLoading = false,
                    errorMessage = errors.firstOrNull()
                )
            }
        }
    }

    fun acceptInvitation(invitationId: String) {
        respondToInvitation(invitationId = invitationId, status = InvitationResponseStatus.ACCEPTED)
    }

    fun declineInvitation(invitationId: String) {
        respondToInvitation(invitationId = invitationId, status = InvitationResponseStatus.DECLINED)
    }

    private fun observeAuthenticatedUser() {
        viewModelScope.launch {
            authRepository.observeAuthenticatedUser().collect { user ->
                _uiState.update { state ->
                    state.copy(
                        header = state.header.copy(
                            greeting = user?.displayName.toGreeting(),
                            avatarUrl = user?.avatarUrl
                        )
                    )
                }
            }
        }
    }

    private fun respondToInvitation(
        invitationId: String,
        status: InvitationResponseStatus
    ) {
        if (_uiState.value.upcomingInvitations.none { item -> item.invitationId == invitationId }) {
            return
        }

        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    upcomingInvitations = state.upcomingInvitations.map { item ->
                        if (item.invitationId == invitationId) {
                            item.copy(isResponding = true)
                        } else {
                            item
                        }
                    },
                    invitationsMessage = null
                )
            }

            when (val result = respondToInvitationUseCase(invitationId = invitationId, status = status)) {
                is Resource.Success -> {
                    refreshInvitationsSection(
                        successMessage = if (status == InvitationResponseStatus.ACCEPTED) {
                            "Invitation accepted."
                        } else {
                            "Invitation declined."
                        }
                    )
                }

                is Resource.Error -> {
                    _uiState.update { state ->
                        state.copy(
                            upcomingInvitations = state.upcomingInvitations.map { item ->
                                if (item.invitationId == invitationId) {
                                    item.copy(isResponding = false)
                                } else {
                                    item
                                }
                            },
                            invitationsMessage = result.message
                        )
                    }
                }

                Resource.Loading -> Unit
            }
        }
    }

    private suspend fun refreshInvitationsSection(successMessage: String? = null) {
        _uiState.update { state ->
            state.copy(isInvitationsLoading = true)
        }

        when (val result = getPlayerInvitationsUseCase()) {
            is Resource.Success -> {
                val invitations = buildInvitationPreview(result)
                _uiState.update { state ->
                    state.copy(
                        upcomingInvitations = invitations,
                        isInvitationsLoading = false,
                        invitationsMessage = successMessage ?: if (invitations.isEmpty()) {
                            "No pending invitations at the moment."
                        } else {
                            null
                        }
                    )
                }
            }

            is Resource.Error -> {
                _uiState.update { state ->
                    state.copy(
                        isInvitationsLoading = false,
                        invitationsMessage = result.message
                    )
                }
            }

            Resource.Loading -> Unit
        }
    }

    private fun buildNextMatchUi(
        matchesResult: Resource<List<MatchWithContext>>,
        readinessResult: Resource<List<MatchMonitoringResult>>
    ): HomeNextMatchUi {
        val matches = (matchesResult as? Resource.Success)?.data.orEmpty()
        val nextMatch = matches
            .filter { item -> item.match.scheduledStartTime.isAfter(Instant.now()) }
            .minByOrNull { item -> item.match.scheduledStartTime }
            ?: return HomeNextMatchUi()

        val readinessLookup = (readinessResult as? Resource.Success)
            ?.data
            ?.associateBy { item -> item.matchId }
            .orEmpty()

        val readiness = readinessLookup[nextMatch.match.id]
        val requiredPlayers = (readiness?.requiredPlayers ?: nextMatch.match.requiredPlayers).coerceAtLeast(1)
        val confirmedPlayers = (readiness?.confirmedPlayersCount ?: nextMatch.confirmedPlayersCount)
            .coerceAtLeast(0)
            .coerceAtMost(requiredPlayers)
        val progress = (confirmedPlayers.toFloat() / requiredPlayers.toFloat()).coerceIn(0f, 1f)

        return HomeNextMatchUi(
            matchId = nextMatch.match.id,
            dateTimeLabel = nextMatch.match.scheduledStartTime.toNextMatchLabel(),
            venueLabel = nextMatch.venueName,
            readinessLabel = "$confirmedPlayers/$requiredPlayers Ready",
            readinessProgress = progress,
            hasMatch = true,
            remainingSpots = (requiredPlayers - confirmedPlayers).coerceAtLeast(0)
        )
    }

    private fun buildInsightBanner(nextMatch: HomeNextMatchUi): HomeInsightBannerUi {
        if (!nextMatch.hasMatch) {
            return HomeInsightBannerUi(
                message = "No upcoming match yet. Create one and start inviting players.",
                ctaLabel = "Create Match"
            )
        }

        return if (nextMatch.remainingSpots > 0) {
            HomeInsightBannerUi(
                message = "You may need ${nextMatch.remainingSpots} more players for the next match to complete the squad.",
                ctaLabel = "Find Players"
            )
        } else {
            HomeInsightBannerUi(
                message = "Squad readiness looks strong for your next match.",
                ctaLabel = "View Match"
            )
        }
    }

    private fun buildInvitationPreview(
        invitationsResult: Resource<List<InvitationWithContext>>
    ): List<HomeInvitationPreviewUi> {
        val invitations = (invitationsResult as? Resource.Success)
            ?.data
            .orEmpty()

        return invitations
            .filter { item -> item.status == InvitationResponseStatus.PENDING }
            .sortedBy { item -> item.scheduledStartTime }
            .take(MAX_HOME_INVITATIONS)
            .map { item ->
                HomeInvitationPreviewUi(
                    invitationId = item.invitationId,
                    matchId = item.matchId,
                    title = item.matchTitle,
                    scheduledLabel = item.scheduledStartTime.toInvitationScheduleLabel(),
                    canRespond = true,
                    isResponding = false
                )
            }
    }

    private fun buildRecommendedVenuePreview(
        venuesResult: Resource<List<com.dakti.app.domain.model.VenueWithTimeSlots>>
    ): List<HomeRecommendedVenueUi> {
        val venues = (venuesResult as? Resource.Success)?.data.orEmpty()

        return venues
            .take(MAX_HOME_VENUES)
            .map { item ->
                HomeRecommendedVenueUi(
                    id = item.venue.id,
                    name = item.venue.name,
                    address = item.venue.address,
                    sportType = item.venue.sportType,
                    imageUrl = item.venue.imageUrl,
                    distanceLabel = null,
                    ratingLabel = null,
                    priceLabel = "${item.venue.currency} ${item.venue.pricePerHour.roundToInt()}/hr"
                )
            }
    }

    private fun String?.toGreeting(): String {
        val firstName = this
            ?.trim()
            ?.split("\\s+".toRegex())
            ?.firstOrNull()
            .orEmpty()

        return if (firstName.isBlank()) {
            "Hello, Champ!"
        } else {
            "Hello, $firstName!"
        }
    }

    private fun Instant.toNextMatchLabel(): String {
        return atZone(ZoneId.systemDefault()).format(nextMatchFormatter)
    }

    private fun Instant.toInvitationScheduleLabel(): String {
        val zoneId = ZoneId.systemDefault()
        val dateTime = atZone(zoneId)
        val date = dateTime.toLocalDate()
        val today = LocalDate.now(zoneId)

        val dayLabel = when (date) {
            today -> "Today"
            today.plusDays(1) -> "Tomorrow"
            else -> dateTime.format(invitationDayFormatter)
        }

        return "$dayLabel, ${dateTime.format(timeFormatter)}"
    }

    private companion object {
        private const val MAX_HOME_INVITATIONS: Int = 2
        private const val MAX_HOME_VENUES: Int = 5

        private val nextMatchFormatter: DateTimeFormatter =
            DateTimeFormatter.ofPattern("EEEE h:mm a", Locale.getDefault())
        private val invitationDayFormatter: DateTimeFormatter =
            DateTimeFormatter.ofPattern("EEE", Locale.getDefault())
        private val timeFormatter: DateTimeFormatter =
            DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
    }
}

private fun defaultHomeQuickActions(): List<HomeQuickActionUi> = listOf(
    HomeQuickActionUi(
        type = HomeQuickActionType.BOOK_VENUE,
        title = "Book Venue"
    ),
    HomeQuickActionUi(
        type = HomeQuickActionType.CREATE_MATCH,
        title = "Create Match"
    ),
    HomeQuickActionUi(
        type = HomeQuickActionType.INVITE_PLAYERS,
        title = "Invite Players"
    ),
    HomeQuickActionUi(
        type = HomeQuickActionType.ASK_AI,
        title = "Ask AI",
        isPrimary = true
    )
)
