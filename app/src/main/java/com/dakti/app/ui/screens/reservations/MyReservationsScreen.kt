@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.dakti.app.ui.screens.reservations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dakti.app.presentation.reservations.ReservationHistoryItemUi

@Composable
fun MyReservationsScreen(
    isLoading: Boolean,
    reservations: List<ReservationHistoryItemUi>,
    errorMessage: String?,
    onRefresh: () -> Unit,
    onCreateMatchFromReservation: (String) -> Unit,
    onBackToHome: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text(text = "My Reservations") })
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Your latest reservation history appears here.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (isLoading) {
                item {
                    CircularProgressIndicator()
                }
            }

            if (!isLoading && errorMessage != null) {
                item {
                    ReservationEmptyState(
                        message = errorMessage,
                        actionLabel = "Try Again",
                        onActionClick = onRefresh
                    )
                }
            }

            if (!isLoading && errorMessage == null && reservations.isEmpty()) {
                item {
                    ReservationEmptyState(
                        message = "No reservations yet. Reserve a slot from the Venues section.",
                        actionLabel = "Refresh",
                        onActionClick = onRefresh
                    )
                }
            }

            if (!isLoading && reservations.isNotEmpty()) {
                items(reservations, key = { reservation -> reservation.id }) { reservation ->
                    ReservationCard(reservation = reservation)
                    TextButton(
                        onClick = { onCreateMatchFromReservation(reservation.id) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Create Match from this Reservation")
                    }
                }
            }

            item {
                TextButton(onClick = onBackToHome) {
                    Text(text = "Back to Home")
                }
            }
        }
    }
}
