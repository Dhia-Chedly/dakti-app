@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.dakti.app.ui.screens.reservations

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dakti.app.ui.components.DaktiPlaceholderContent

@Composable
fun ReservationConfirmationScreen(
    message: String,
    onMyReservationsClick: () -> Unit,
    onHomeClick: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text(text = "Reservation Confirmation") })
        }
    ) { innerPadding ->
        DaktiPlaceholderContent(
            title = "Reservation status",
            description = message,
            modifier = Modifier.padding(innerPadding)
        ) {
            Button(onClick = onMyReservationsClick) {
                Text(text = "Go to My Reservations")
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onHomeClick) {
                Text(text = "Back to Home")
            }
        }
    }
}
