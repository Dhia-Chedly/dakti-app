package com.dakti.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dakti.app.presentation.assistant.AssistantViewModel
import com.dakti.app.presentation.auth.AuthStatus
import com.dakti.app.presentation.auth.AuthViewModel
import com.dakti.app.presentation.home.HomeViewModel
import com.dakti.app.presentation.invitations.InvitationViewModel
import com.dakti.app.presentation.integration.ExternalActionViewModel
import com.dakti.app.presentation.matches.MatchViewModel
import com.dakti.app.presentation.profile.ProfileViewModel
import com.dakti.app.presentation.reservations.ReservationViewModel
import com.dakti.app.presentation.venues.VenueViewModel
import com.dakti.app.ui.screens.assistant.AssistantScreen
import com.dakti.app.ui.screens.auth.LoginScreen
import com.dakti.app.ui.screens.auth.RegisterScreen
import com.dakti.app.ui.screens.auth.SplashScreen
import com.dakti.app.ui.screens.auth.WelcomeScreen
import com.dakti.app.ui.screens.home.HomeScreen
import com.dakti.app.ui.screens.invitations.InvitePlayersScreen
import com.dakti.app.ui.screens.invitations.InvitationsScreen
import com.dakti.app.ui.screens.matches.CreateMatchScreen
import com.dakti.app.ui.screens.matches.MatchDetailsScreen
import com.dakti.app.ui.screens.matches.MatchesScreen
import com.dakti.app.ui.screens.matches.MyMatchesScreen
import com.dakti.app.ui.screens.profile.ProfileScreen
import com.dakti.app.ui.screens.reservations.MyReservationsScreen
import com.dakti.app.ui.screens.reservations.ReservationConfirmationScreen
import com.dakti.app.ui.screens.venues.VenueDetailsScreen
import com.dakti.app.ui.screens.venues.VenueListScreen

@Composable
fun DaktiNavGraph(startDestination: String) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val showBottomBar = currentDestination
        ?.hierarchy
        ?.any { destination -> destination.route in AppRoute.bottomNavRoutes } == true

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                DaktiBottomNavigation(
                    currentDestinationRoute = currentDestination?.route,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(AppRoute.MainGraph.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(AppRoute.Splash.route) {
                val viewModel: AuthViewModel = hiltViewModel()
                val state by viewModel.uiState.collectAsStateWithLifecycle()

                LaunchedEffect(state.authStatus) {
                    when (state.authStatus) {
                        AuthStatus.Loading -> Unit
                        is AuthStatus.Authenticated -> {
                            navController.navigate(AppRoute.MainGraph.route) {
                                popUpTo(AppRoute.Splash.route) { inclusive = true }
                            }
                        }

                        AuthStatus.Unauthenticated,
                        is AuthStatus.Error -> {
                            navController.navigate(AppRoute.AuthGraph.route) {
                                popUpTo(AppRoute.Splash.route) { inclusive = true }
                            }
                        }
                    }
                }

                SplashScreen()
            }

            navigation(
                startDestination = AppRoute.Welcome.route,
                route = AppRoute.AuthGraph.route
            ) {
                composable(AppRoute.Welcome.route) {
                    WelcomeScreen(
                        onLoginClick = { navController.navigate(AppRoute.Login.route) },
                        onRegisterClick = { navController.navigate(AppRoute.Register.route) }
                    )
                }

                composable(AppRoute.Login.route) {
                    val viewModel: AuthViewModel = hiltViewModel()
                    val state by viewModel.uiState.collectAsStateWithLifecycle()

                    LaunchedEffect(state.authStatus) {
                        if (state.authStatus is AuthStatus.Authenticated) {
                            navController.navigate(AppRoute.MainGraph.route) {
                                popUpTo(AppRoute.AuthGraph.route) { inclusive = true }
                            }
                        }
                    }

                    LoginScreen(
                        formState = state.loginForm,
                        isLoading = state.isLoginLoading,
                        feedbackMessage = state.feedbackMessage,
                        onEmailChanged = viewModel::onLoginEmailChanged,
                        onPasswordChanged = viewModel::onLoginPasswordChanged,
                        onLoginClick = viewModel::submitLogin,
                        onGoToRegister = { navController.navigate(AppRoute.Register.route) },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(AppRoute.Register.route) {
                    val viewModel: AuthViewModel = hiltViewModel()
                    val state by viewModel.uiState.collectAsStateWithLifecycle()

                    LaunchedEffect(state.authStatus) {
                        if (state.authStatus is AuthStatus.Authenticated) {
                            navController.navigate(AppRoute.MainGraph.route) {
                                popUpTo(AppRoute.AuthGraph.route) { inclusive = true }
                            }
                        }
                    }

                    RegisterScreen(
                        formState = state.registerForm,
                        isLoading = state.isRegisterLoading,
                        feedbackMessage = state.feedbackMessage,
                        onNameChanged = viewModel::onRegisterNameChanged,
                        onEmailChanged = viewModel::onRegisterEmailChanged,
                        onPhoneChanged = viewModel::onRegisterPhoneChanged,
                        onPasswordChanged = viewModel::onRegisterPasswordChanged,
                        onConfirmPasswordChanged = viewModel::onRegisterConfirmPasswordChanged,
                        onRegisterClick = viewModel::submitRegistration,
                        onGoToLogin = { navController.navigate(AppRoute.Login.route) },
                        onBack = { navController.popBackStack() }
                    )
                }
            }

            navigation(
                startDestination = AppRoute.Home.route,
                route = AppRoute.MainGraph.route
            ) {
                composable(AppRoute.Home.route) {
                    val viewModel: HomeViewModel = hiltViewModel()
                    val state by viewModel.uiState.collectAsStateWithLifecycle()

                    HomeScreen(
                        uiState = state,
                        onBrowseVenues = { navController.navigate(AppRoute.Venues.route) },
                        onCreateMatch = { navController.navigate(AppRoute.CreateMatch.create(null)) },
                        onMyReservations = { navController.navigate(AppRoute.MyReservations.route) },
                        onMyMatches = { navController.navigate(AppRoute.MyMatches.route) },
                        onOpenAssistant = { navController.navigate(AppRoute.Assistant.route) }
                    )
                }

                composable(AppRoute.Venues.route) {
                    val viewModel: VenueViewModel = hiltViewModel()
                    val state by viewModel.uiState.collectAsStateWithLifecycle()

                    VenueListScreen(
                        searchQuery = state.searchQuery,
                        sportFilters = state.sportFilters,
                        selectedSportFilter = state.selectedSportFilter,
                        isLoading = state.isLoading,
                        venues = state.filteredVenues,
                        errorMessage = state.errorMessage,
                        onSearchQueryChanged = viewModel::onSearchQueryChanged,
                        onSportFilterSelected = viewModel::onSportFilterSelected,
                        onRetry = viewModel::refreshVenues,
                        onVenueClick = { venueId -> navController.navigate(AppRoute.VenueDetails.create(venueId)) }
                    )
                }

                composable(AppRoute.Matches.route) {
                    val viewModel: MatchViewModel = hiltViewModel()
                    val state by viewModel.uiState.collectAsStateWithLifecycle()

                    MatchesScreen(
                        isLoading = state.isLoading,
                        errorMessage = state.errorMessage,
                        openMatchesCount = state.openMatchesCount,
                        matchesPreview = state.matches,
                        onCreateMatch = { navController.navigate(AppRoute.CreateMatch.create(null)) },
                        onOpenMyMatches = { navController.navigate(AppRoute.MyMatches.route) },
                        onOpenInvitations = { navController.navigate(AppRoute.Invitations.route) },
                        onRefresh = viewModel::refreshMatchesModule,
                        onOpenMatchDetails = { matchId ->
                            navController.navigate(AppRoute.MatchDetails.create(matchId))
                        }
                    )
                }

                composable(AppRoute.Assistant.route) {
                    val viewModel: AssistantViewModel = hiltViewModel()
                    val externalActions: ExternalActionViewModel = hiltViewModel()
                    val context = LocalContext.current
                    val state by viewModel.uiState.collectAsStateWithLifecycle()

                    AssistantScreen(
                        uiState = state,
                        onInputChanged = viewModel::onInputChanged,
                        onSendMessage = viewModel::sendCurrentMessage,
                        onPromptSelected = viewModel::sendSuggestedPrompt,
                        onQuickActionSelected = viewModel::sendQuickAction,
                        onUseVenueSuggestion = viewModel::useVenueSuggestion,
                        onConfirmAction = viewModel::confirmPendingAction,
                        onCancelAction = viewModel::cancelPendingAction,
                        onSendGeneratedViaWhatsApp = { generated ->
                            val payload = viewModel.buildSharePayload(generated)
                            if (payload == null) {
                                "Generated text is empty."
                            } else {
                                externalActions.launchWhatsApp(context, payload)
                            }
                        },
                        onSendGeneratedViaEmail = { generated ->
                            val payload = viewModel.buildEmailPayload(generated)
                            if (payload == null) {
                                "Generated text is empty."
                            } else {
                                externalActions.launchEmail(context, payload)
                            }
                        },
                        onRetry = viewModel::retryLastFailedMessage,
                        onDismissError = viewModel::clearError,
                        onDismissActionResult = viewModel::clearActionResultMessage
                    )
                }

                composable(AppRoute.Profile.route) {
                    val profileViewModel: ProfileViewModel = hiltViewModel()
                    val state by profileViewModel.uiState.collectAsStateWithLifecycle()

                    LaunchedEffect(state.isLoggedOut) {
                        if (state.isLoggedOut) {
                            navController.navigate(AppRoute.AuthGraph.route) {
                                popUpTo(AppRoute.MainGraph.route) { inclusive = true }
                            }
                            profileViewModel.onLogoutHandled()
                        }
                    }

                    ProfileScreen(
                        uiState = state,
                        onDisplayNameChanged = profileViewModel::onDisplayNameChanged,
                        onPhoneNumberChanged = profileViewModel::onPhoneNumberChanged,
                        onAvatarUrlChanged = profileViewModel::onAvatarUrlChanged,
                        onStartEditing = profileViewModel::startEditing,
                        onCancelEditing = profileViewModel::cancelEditing,
                        onSaveProfile = profileViewModel::saveProfile,
                        onLogout = profileViewModel::logout
                    )
                }

                composable(
                    route = AppRoute.VenueDetails.route,
                    arguments = listOf(navArgument("venueId") { type = NavType.StringType })
                ) { entry ->
                    val venueId = entry.arguments?.getString("venueId").orEmpty()
                    val venueViewModel: VenueViewModel = hiltViewModel()
                    val externalActions: ExternalActionViewModel = hiltViewModel()
                    val context = LocalContext.current
                    val state by venueViewModel.uiState.collectAsStateWithLifecycle()

                    LaunchedEffect(venueId) {
                        venueViewModel.loadVenueDetails(venueId)
                    }

                    VenueDetailsScreen(
                        isLoading = state.isDetailsLoading,
                        venueDetails = state.selectedVenueDetails,
                        selectedSlotId = state.selectedSlotId,
                        errorMessage = state.detailsErrorMessage,
                        onSlotSelected = venueViewModel::selectTimeSlot,
                        onContinueToReservation = { slotId ->
                            navController.navigate(AppRoute.ReservationConfirmation.create(venueId, slotId))
                        },
                        onOpenInMaps = {
                            val payload = venueViewModel.buildSelectedVenueLocationPayload()
                            if (payload == null) {
                                "Venue location is unavailable."
                            } else {
                                externalActions.launchMaps(context, payload)
                            }
                        },
                        onCallVenue = {
                            val payload = venueViewModel.buildSelectedVenueDialerPayload()
                            if (payload == null) {
                                "Venue phone number is unavailable."
                            } else {
                                externalActions.launchDialer(context, payload)
                            }
                        },
                        onRetry = { venueViewModel.loadVenueDetails(venueId) },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(
                    route = AppRoute.ReservationConfirmation.route,
                    arguments = listOf(
                        navArgument("venueId") { type = NavType.StringType },
                        navArgument("timeSlotId") { type = NavType.StringType }
                    )
                ) { entry ->
                    val venueId = entry.arguments?.getString("venueId").orEmpty()
                    val timeSlotId = entry.arguments?.getString("timeSlotId").orEmpty()
                    val viewModel: ReservationViewModel = hiltViewModel()
                    val state by viewModel.uiState.collectAsStateWithLifecycle()

                    LaunchedEffect(venueId, timeSlotId) {
                        viewModel.loadReservationDraft(venueId = venueId, timeSlotId = timeSlotId)
                    }

                    ReservationConfirmationScreen(
                        isDraftLoading = state.isDraftLoading,
                        draft = state.draft,
                        isSubmitting = state.isCreatingReservation,
                        successMessage = state.reservationCreatedMessage,
                        errorMessage = state.confirmationErrorMessage,
                        onConfirm = viewModel::confirmReservation,
                        onBack = { navController.popBackStack() },
                        onMyReservationsClick = { navController.navigate(AppRoute.MyReservations.route) },
                        onCreateMatchPlaceholder = {
                            navController.navigate(
                                AppRoute.CreateMatch.create(state.latestCreatedReservationId)
                            )
                        },
                        onHomeClick = {
                            navController.navigate(AppRoute.Home.route) {
                                popUpTo(AppRoute.Home.route) { inclusive = false }
                            }
                        }
                    )
                }

                composable(AppRoute.MyReservations.route) {
                    val viewModel: ReservationViewModel = hiltViewModel()
                    val state by viewModel.uiState.collectAsStateWithLifecycle()

                    MyReservationsScreen(
                        isLoading = state.isHistoryLoading,
                        reservations = state.myReservations,
                        errorMessage = state.historyErrorMessage,
                        onRefresh = viewModel::loadMyReservations,
                        onCreateMatchFromReservation = { reservationId ->
                            navController.navigate(AppRoute.CreateMatch.create(reservationId))
                        },
                        onBackToHome = {
                            navController.navigate(AppRoute.Home.route) {
                                popUpTo(AppRoute.Home.route) { inclusive = false }
                            }
                        }
                    )
                }

                composable(AppRoute.MyMatches.route) {
                    val viewModel: MatchViewModel = hiltViewModel()
                    val state by viewModel.uiState.collectAsStateWithLifecycle()

                    MyMatchesScreen(
                        isLoading = state.isLoading,
                        matches = state.matches,
                        errorMessage = state.errorMessage,
                        onRefresh = viewModel::refreshMatchesModule,
                        onCreateMatch = { navController.navigate(AppRoute.CreateMatch.create(null)) },
                        onMatchClick = { matchId ->
                            navController.navigate(AppRoute.MatchDetails.create(matchId))
                        }
                    )
                }

                composable(
                    route = AppRoute.CreateMatch.route,
                    arguments = listOf(
                        navArgument(AppRoute.CreateMatch.RESERVATION_ID_ARG) {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        }
                    )
                ) { entry ->
                    val viewModel: MatchViewModel = hiltViewModel()
                    val state by viewModel.uiState.collectAsStateWithLifecycle()
                    val reservationId = entry.arguments?.getString(AppRoute.CreateMatch.RESERVATION_ID_ARG)

                    LaunchedEffect(reservationId) {
                        viewModel.onCreateScreenOpened(reservationId)
                    }

                    CreateMatchScreen(
                        formState = state.formState,
                        reservationContexts = state.reservationContexts,
                        venueOptions = state.venueOptions,
                        isSubmitting = state.isCreatingMatch,
                        isCreateEnabled = state.isCreateEnabled,
                        successMessage = state.createSuccessMessage,
                        errorMessage = state.createErrorMessage,
                        onReservationSelected = viewModel::onReservationContextSelected,
                        onVenueSelected = viewModel::onVenueSelected,
                        onSportTypeChanged = viewModel::onSportTypeChanged,
                        onScheduledAtChanged = viewModel::onScheduledAtChanged,
                        onRequiredPlayersChanged = viewModel::onRequiredPlayersChanged,
                        onDescriptionChanged = viewModel::onDescriptionChanged,
                        onCreateClick = viewModel::createMatch,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(
                    route = AppRoute.MatchDetails.route,
                    arguments = listOf(navArgument("matchId") { type = NavType.StringType })
                ) { entry ->
                    val matchId = entry.arguments?.getString("matchId").orEmpty()
                    val viewModel: MatchViewModel = hiltViewModel()
                    val externalActions: ExternalActionViewModel = hiltViewModel()
                    val context = LocalContext.current
                    val state by viewModel.uiState.collectAsStateWithLifecycle()
                    val lifecycleOwner = LocalLifecycleOwner.current

                    LaunchedEffect(matchId) {
                        viewModel.loadMatchDetails(matchId)
                    }

                    DisposableEffect(lifecycleOwner, matchId) {
                        val observer = LifecycleEventObserver { _, event ->
                            if (event == Lifecycle.Event.ON_RESUME) {
                                viewModel.loadMatchDetails(matchId)
                            }
                        }
                        lifecycleOwner.lifecycle.addObserver(observer)
                        onDispose {
                            lifecycleOwner.lifecycle.removeObserver(observer)
                        }
                    }

                    MatchDetailsScreen(
                        isLoading = state.isDetailsLoading,
                        details = state.selectedMatchDetails,
                        errorMessage = state.detailsErrorMessage,
                        onBack = { navController.popBackStack() },
                        onInvitePlayers = { navController.navigate(AppRoute.InvitePlayers.create(matchId)) },
                        onSendInvitationViaWhatsApp = {
                            val payload = viewModel.buildInvitationSharePayloadForSelectedMatch()
                            if (payload == null) {
                                "Match details are unavailable."
                            } else {
                                externalActions.launchWhatsApp(context, payload)
                            }
                        },
                        onSendReminderViaWhatsApp = {
                            val payload = viewModel.buildReminderSharePayloadForSelectedMatch()
                            if (payload == null) {
                                "Match details are unavailable."
                            } else {
                                externalActions.launchWhatsApp(context, payload)
                            }
                        },
                        onSendInvitationViaEmail = {
                            val payload = viewModel.buildInvitationEmailPayloadForSelectedMatch()
                            if (payload == null) {
                                "Match details are unavailable."
                            } else {
                                externalActions.launchEmail(context, payload)
                            }
                        },
                        onSendReminderViaEmail = {
                            val payload = viewModel.buildReminderEmailPayloadForSelectedMatch()
                            if (payload == null) {
                                "Match details are unavailable."
                            } else {
                                externalActions.launchEmail(context, payload)
                            }
                        },
                        onOpenInMaps = {
                            val payload = viewModel.buildSelectedMatchVenueLocationPayload()
                            if (payload == null) {
                                "Venue location is unavailable."
                            } else {
                                externalActions.launchMaps(context, payload)
                            }
                        },
                        onAddToCalendar = {
                            val payload = viewModel.buildSelectedMatchCalendarPayload()
                            if (payload == null) {
                                "Match schedule is unavailable."
                            } else {
                                externalActions.launchCalendar(context, payload)
                            }
                        }
                    )
                }

                composable(AppRoute.Invitations.route) {
                    val viewModel: InvitationViewModel = hiltViewModel()
                    val state by viewModel.uiState.collectAsStateWithLifecycle()

                    LaunchedEffect(Unit) {
                        viewModel.loadPlayerInvitations()
                    }

                    InvitationsScreen(
                        isLoading = state.isLoading,
                        invitations = state.invitations,
                        errorMessage = state.errorMessage,
                        actionMessage = state.actionMessage,
                        onAccept = { invitationId ->
                            viewModel.respondToInvitation(invitationId = invitationId, accept = true)
                        },
                        onDecline = { invitationId ->
                            viewModel.respondToInvitation(invitationId = invitationId, accept = false)
                        },
                        onRefresh = viewModel::loadPlayerInvitations,
                        onBackToHome = { navController.navigate(AppRoute.Home.route) }
                    )
                }

                composable(
                    route = AppRoute.InvitePlayers.route,
                    arguments = listOf(navArgument("matchId") { type = NavType.StringType })
                ) { entry ->
                    val matchId = entry.arguments?.getString("matchId").orEmpty()
                    val viewModel: InvitationViewModel = hiltViewModel()
                    val state by viewModel.uiState.collectAsStateWithLifecycle()

                    LaunchedEffect(matchId) {
                        viewModel.loadInvitePlayers(matchId)
                    }

                    InvitePlayersScreen(
                        uiState = state.invitePlayers,
                        onMessageChanged = viewModel::onInviteMessageChanged,
                        onTogglePlayer = viewModel::togglePlayerSelection,
                        onSendInvitations = viewModel::sendInvitations,
                        onRefresh = { viewModel.loadInvitePlayers(matchId) },
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

@Composable
private fun DaktiBottomNavigation(
    currentDestinationRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationBar {
        daktiBottomNavItems.forEach { item ->
            NavigationBarItem(
                selected = currentDestinationRoute == item.route,
                onClick = { onNavigate(item.route) },
                icon = {
                    Icon(imageVector = item.icon, contentDescription = item.label)
                },
                label = {
                    Text(
                        text = item.label,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            )
        }
    }
}

