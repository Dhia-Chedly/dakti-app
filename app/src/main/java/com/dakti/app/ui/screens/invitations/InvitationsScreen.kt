@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.dakti.app.ui.screens.invitations

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
import com.dakti.app.presentation.invitations.InvitationItemUi

@Composable
fun InvitationsScreen(
    isLoading: Boolean,
    invitations: List<InvitationItemUi>,
    errorMessage: String?,
    actionMessage: String?,
    onAccept: (String) -> Unit,
    onDecline: (String) -> Unit,
    onRefresh: () -> Unit,
    onBackToHome: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text(text = "Invitations") })
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Review incoming match invites and confirm your participation.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            actionMessage?.takeIf { message -> message.isNotBlank() }?.let { message ->
                item {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (isLoading) {
                item { CircularProgressIndicator() }
            }

            if (!isLoading && errorMessage != null) {
                item {
                    InvitationsEmptyState(
                        title = "Could not load invitations",
                        message = errorMessage
                    )
                }
            }

            if (!isLoading && errorMessage == null && invitations.isEmpty()) {
                item {
                    InvitationsEmptyState(
                        title = "No invitations yet",
                        message = "When organizers invite you to a match, invitations will appear here."
                    )
                }
            }

            if (!isLoading && invitations.isNotEmpty()) {
                items(invitations, key = { item -> item.invitationId }) { invitation ->
                    InvitationCard(
                        invitation = invitation,
                        onAccept = onAccept,
                        onDecline = onDecline
                    )
                }
            }

            item {
                TextButton(
                    onClick = onRefresh,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Refresh")
                }
            }

            item {
                TextButton(
                    onClick = onBackToHome,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Back to Home")
                }
            }
        }
    }
}

