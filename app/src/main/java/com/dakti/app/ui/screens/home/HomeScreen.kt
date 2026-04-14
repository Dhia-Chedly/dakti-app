@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.dakti.app.ui.screens.home

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dakti.app.ui.components.DaktiPlaceholderContent

@Composable
fun HomeScreen(
    message: String,
    onBrowseVenues: () -> Unit,
    onMyReservations: () -> Unit,
    onMyMatches: () -> Unit,
    onInvitations: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text(text = "Home") })
        }
    ) { innerPadding ->
        DaktiPlaceholderContent(
            title = "Dakti Home",
            description = message,
            modifier = Modifier.padding(innerPadding)
        ) {
            Button(onClick = onBrowseVenues) {
                Text(text = "Browse Venues")
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(onClick = onMyReservations) {
                Text(text = "My Reservations")
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(onClick = onMyMatches) {
                Text(text = "My Matches")
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(onClick = onInvitations) {
                Text(text = "Invitations")
            }
        }
    }
}
