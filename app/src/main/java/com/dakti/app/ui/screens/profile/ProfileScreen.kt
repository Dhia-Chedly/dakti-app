@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.dakti.app.ui.screens.profile

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
fun ProfileScreen(
    displayName: String,
    onLogout: () -> Unit,
    onMyReservations: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text(text = "Profile") })
        }
    ) { innerPadding ->
        DaktiPlaceholderContent(
            title = "Player: $displayName",
            description = "Profile settings and account management are placeholders in Phase 1.",
            modifier = Modifier.padding(innerPadding)
        ) {
            Button(onClick = onMyReservations) {
                Text(text = "My Reservations")
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onLogout) {
                Text(text = "Logout")
            }
        }
    }
}
