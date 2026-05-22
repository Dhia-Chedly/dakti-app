package com.dakti.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material.icons.outlined.Stadium
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val isCenterProminent: Boolean = false
)

val daktiBottomNavItems = listOf(
    BottomNavItem(route = AppRoute.Home.route, label = "Home", icon = Icons.Outlined.Home),
    BottomNavItem(route = AppRoute.Venues.route, label = "Venues", icon = Icons.Outlined.Stadium),
    BottomNavItem(
        route = AppRoute.Assistant.route,
        label = "AI Assistant",
        icon = Icons.Outlined.SmartToy,
        isCenterProminent = true
    ),
    BottomNavItem(route = AppRoute.Matches.route, label = "My Matches", icon = Icons.Outlined.SportsSoccer),
    BottomNavItem(route = AppRoute.Profile.route, label = "Profile", icon = Icons.Outlined.AccountCircle)
)

