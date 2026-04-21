@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.dakti.app.ui.screens.matches

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dakti.app.presentation.matches.MatchDetailsUi
import com.dakti.app.presentation.matches.MatchReadinessUi
import kotlinx.coroutines.launch

@Composable
fun MatchDetailsScreen(
    isLoading: Boolean,
    details: MatchDetailsUi?,
    isMonitoringLoading: Boolean,
    readiness: MatchReadinessUi?,
    monitoringErrorMessage: String?,
    errorMessage: String?,
    onBack: () -> Unit,
    onInvitePlayers: () -> Unit,
    onRefreshMonitoring: () -> Unit,
    onOpenAssistantSuggestions: () -> Unit,
    onSendInvitationViaWhatsApp: () -> String?,
    onSendReminderViaWhatsApp: () -> String?,
    onSendMonitoringReminderViaWhatsApp: () -> String?,
    onSendMonitoringUpdateViaWhatsApp: () -> String?,
    onSendInvitationViaEmail: () -> String?,
    onSendReminderViaEmail: () -> String?,
    onSendMonitoringReminderViaEmail: () -> String?,
    onSendMonitoringUpdateViaEmail: () -> String?,
    onOpenInMaps: () -> String?,
    onAddToCalendar: () -> String?
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Match Details") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text(text = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isLoading) {
                item {
                    CircularProgressIndicator()
                }
            }

            if (!isLoading && errorMessage != null) {
                item {
                    MatchEmptyState(
                        title = "Match unavailable",
                        message = errorMessage,
                        actionLabel = "Back",
                        onActionClick = onBack
                    )
                }
            }

            if (!isLoading && details != null) {
                item {
                    MatchSummaryCard(details = details)
                }

                if (isMonitoringLoading) {
                    item {
                        CircularProgressIndicator()
                    }
                }

                if (monitoringErrorMessage != null) {
                    item {
                        MatchEmptyState(
                            title = "Monitoring unavailable",
                            message = monitoringErrorMessage,
                            actionLabel = "Refresh readiness",
                            onActionClick = onRefreshMonitoring
                        )
                    }
                }

                readiness?.let { matchReadiness ->
                    item {
                        MatchReadinessCard(readiness = matchReadiness)
                    }
                    item {
                        MatchMonitoringActionsCard(readiness = matchReadiness)
                    }
                    item {
                        OutlinedButton(
                            onClick = onRefreshMonitoring,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Refresh Readiness")
                        }
                    }
                }

                item {
                    OutlinedButton(
                        onClick = onInvitePlayers,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Invite Players")
                    }
                }

                item {
                    OutlinedButton(
                        onClick = {
                            val message = onSendInvitationViaWhatsApp()
                            message?.let { result ->
                                coroutineScope.launch { snackbarHostState.showSnackbar(result) }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Send Invitation (WhatsApp)")
                    }
                }

                item {
                    OutlinedButton(
                        onClick = {
                            val message = onSendReminderViaWhatsApp()
                            message?.let { result ->
                                coroutineScope.launch { snackbarHostState.showSnackbar(result) }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Send Reminder (WhatsApp)")
                    }
                }

                item {
                    OutlinedButton(
                        onClick = {
                            val message = onSendMonitoringReminderViaWhatsApp()
                            message?.let { result ->
                                coroutineScope.launch { snackbarHostState.showSnackbar(result) }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Send Monitoring Reminder (WhatsApp)")
                    }
                }

                item {
                    OutlinedButton(
                        onClick = {
                            val message = onSendMonitoringUpdateViaWhatsApp()
                            message?.let { result ->
                                coroutineScope.launch { snackbarHostState.showSnackbar(result) }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Send Monitoring Update (WhatsApp)")
                    }
                }

                item {
                    OutlinedButton(
                        onClick = {
                            val message = onSendInvitationViaEmail()
                            message?.let { result ->
                                coroutineScope.launch { snackbarHostState.showSnackbar(result) }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Send Invitation (Email)")
                    }
                }

                item {
                    OutlinedButton(
                        onClick = {
                            val message = onSendReminderViaEmail()
                            message?.let { result ->
                                coroutineScope.launch { snackbarHostState.showSnackbar(result) }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Send Reminder (Email)")
                    }
                }

                item {
                    OutlinedButton(
                        onClick = {
                            val message = onSendMonitoringReminderViaEmail()
                            message?.let { result ->
                                coroutineScope.launch { snackbarHostState.showSnackbar(result) }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Send Monitoring Reminder (Email)")
                    }
                }

                item {
                    OutlinedButton(
                        onClick = {
                            val message = onSendMonitoringUpdateViaEmail()
                            message?.let { result ->
                                coroutineScope.launch { snackbarHostState.showSnackbar(result) }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Send Monitoring Update (Email)")
                    }
                }

                item {
                    OutlinedButton(
                        onClick = onOpenAssistantSuggestions,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Open Assistant Suggestions")
                    }
                }

                item {
                    OutlinedButton(
                        onClick = {
                            val message = onOpenInMaps()
                            message?.let { result ->
                                coroutineScope.launch { snackbarHostState.showSnackbar(result) }
                            }
                        },
                        enabled = details.venueAddress.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Open in Maps")
                    }
                }

                item {
                    OutlinedButton(
                        onClick = {
                            val message = onAddToCalendar()
                            message?.let { result ->
                                coroutineScope.launch { snackbarHostState.showSnackbar(result) }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Add to Calendar")
                    }
                }
            }
        }
    }
}
