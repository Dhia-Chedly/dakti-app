@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.dakti.app.ui.screens.reservations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dakti.app.presentation.reservations.ReservationDraftUi

@Composable
fun ReservationConfirmationScreen(
    isDraftLoading: Boolean,
    draft: ReservationDraftUi?,
    isSubmitting: Boolean,
    successMessage: String?,
    errorMessage: String?,
    onConfirm: () -> Unit,
    onBack: () -> Unit,
    onMyReservationsClick: () -> Unit,
    onHomeClick: () -> Unit,
    onCreateMatchPlaceholder: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Reservation Confirmation") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text(text = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Review your selected venue slot and confirm to create reservation.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (isDraftLoading) {
                item {
                    CircularProgressIndicator()
                }
            }

            if (!isDraftLoading && draft != null) {
                item {
                    ReservationSummaryCard(draft = draft)
                }
            }

            if (errorMessage != null) {
                item {
                    ReservationEmptyState(
                        message = errorMessage,
                        actionLabel = "Go Back",
                        onActionClick = onBack
                    )
                }
            }

            if (successMessage != null) {
                item {
                    ReservationSuccessContent(
                        message = successMessage,
                        onMyReservationsClick = onMyReservationsClick,
                        onHomeClick = onHomeClick,
                        onCreateMatchPlaceholder = onCreateMatchPlaceholder
                    )
                }
            }

            if (!isDraftLoading && draft != null && successMessage == null) {
                item {
                    Button(
                        onClick = onConfirm,
                        enabled = draft.isSlotAvailable && !isSubmitting,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isSubmitting) {
                                "Confirming..."
                            } else {
                                "Confirm Reservation"
                            }
                        )
                    }
                }
            }
        }
    }
}
