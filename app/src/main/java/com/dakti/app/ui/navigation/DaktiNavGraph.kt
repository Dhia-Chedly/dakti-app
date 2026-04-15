package com.dakti.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.dakti.app.ui.screens.invitations.InvitationsScreen
import com.dakti.app.ui.screens.matches.CreateMatchScreen
import com.dakti.app.ui.screens.matches.MatchDetailsScreen
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
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute in AppRoute.bottomNavRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                DaktiBottomNavigation(
                    currentRoute = currentRoute,
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
                        message = state.welcomeMessage,
                        onBrowseVenues = { navController.navigate(AppRoute.Venues.route) },
                        onMyReservations = { navController.navigate(AppRoute.MyReservations.route) },
                        onMyMatches = { navController.navigate(AppRoute.MyMatches.route) },
                        onInvitations = { navController.navigate(AppRoute.Invitations.route) }
                    )
                }

                composable(AppRoute.Venues.route) {
                    val viewModel: VenueViewModel = hiltViewModel()
                    val state by viewModel.uiState.collectAsStateWithLifecycle()

                    VenueListScreen(
                        venues = state.venues,
                        onVenueClick = {
                            viewModel.selectVenue("venue-1")
                            navController.navigate(AppRoute.VenueDetails.create("venue-1"))
                        }
                    )
                }

                composable(
                    route = AppRoute.VenueDetails.route,
                    arguments = listOf(navArgument("venueId") { type = NavType.StringType })
                ) { entry ->
                    val venueId = entry.arguments?.getString("venueId").orEmpty()
                    val reservationViewModel: ReservationViewModel = hiltViewModel()

                    VenueDetailsScreen(
                        venueId = venueId,
                        onReserveClick = {
                            reservationViewModel.confirmReservationForVenue(venueId)
                            navController.navigate(AppRoute.ReservationConfirmation.route)
                        },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(AppRoute.ReservationConfirmation.route) {
                    val viewModel: ReservationViewModel = hiltViewModel()
                    val state by viewModel.uiState.collectAsStateWithLifecycle()

                    ReservationConfirmationScreen(
                        message = state.confirmationMessage,
                        onMyReservationsClick = { navController.navigate(AppRoute.MyReservations.route) },
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
                        reservations = state.reservations,
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
                        matches = state.matches,
                        onCreateMatch = { navController.navigate(AppRoute.CreateMatch.route) },
                        onMatchClick = { navController.navigate(AppRoute.MatchDetails.create("match-1")) }
                    )
                }

                composable(AppRoute.CreateMatch.route) {
                    val viewModel: MatchViewModel = hiltViewModel()

                    CreateMatchScreen(
                        onCreateClick = {
                            viewModel.createDemoMatch()
                            navController.navigate(AppRoute.MatchDetails.create("match-new"))
                        },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(
                    route = AppRoute.MatchDetails.route,
                    arguments = listOf(navArgument("matchId") { type = NavType.StringType })
                ) { entry ->
                    val matchId = entry.arguments?.getString("matchId").orEmpty()
                    MatchDetailsScreen(
                        matchId = matchId,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(AppRoute.Invitations.route) {
                    val viewModel: InvitationViewModel = hiltViewModel()
                    val state by viewModel.uiState.collectAsStateWithLifecycle()

                    InvitationsScreen(
                        invitations = state.invitations,
                        onAcceptAll = { viewModel.acceptAllPlaceholders() },
                        onBackToHome = { navController.navigate(AppRoute.Home.route) }
                    )
                }

                composable(AppRoute.Assistant.route) {
                    val viewModel: AssistantViewModel = hiltViewModel()
                    val state by viewModel.uiState.collectAsStateWithLifecycle()

                    AssistantScreen(
                        lastResponse = state.lastResponse,
                        onAskSuggestion = { viewModel.askSuggestion() },
                        onGoToInvitations = { navController.navigate(AppRoute.Invitations.route) }
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
            }
        }
    }
}

@Composable
private fun DaktiBottomNavigation(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationBar {
        daktiBottomNavItems.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
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
