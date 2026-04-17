package com.dakti.app.ui.navigation

sealed class AppRoute(val route: String) {
    data object Splash : AppRoute("splash")

    data object AuthGraph : AppRoute("auth_graph")
    data object Welcome : AppRoute("welcome")
    data object Login : AppRoute("login")
    data object Register : AppRoute("register")

    data object MainGraph : AppRoute("main_graph")
    data object Home : AppRoute("home")
    data object Venues : AppRoute("venues")
    data object Matches : AppRoute("matches")
    data object Assistant : AppRoute("assistant")
    data object Profile : AppRoute("profile")

    data object VenueDetails : AppRoute("venue_details/{venueId}") {
        fun create(venueId: String): String = "venue_details/$venueId"
    }

    data object ReservationConfirmation : AppRoute("reservation_confirmation/{venueId}/{timeSlotId}") {
        fun create(venueId: String, timeSlotId: String): String =
            "reservation_confirmation/$venueId/$timeSlotId"
    }
    data object MyReservations : AppRoute("my_reservations")

    data object CreateMatch : AppRoute("create_match")
    data object MatchDetails : AppRoute("match_details/{matchId}") {
        fun create(matchId: String): String = "match_details/$matchId"
    }
    data object MyMatches : AppRoute("my_matches")

    data object Invitations : AppRoute("invitations")

    companion object {
        val bottomNavRoutes = setOf(
            Home.route,
            Venues.route,
            Matches.route,
            Assistant.route,
            Profile.route
        )
    }
}

