@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.dakti.app.ui.screens.reservations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import com.dakti.app.ui.components.DaktiHeroScaffold
import com.dakti.app.ui.components.DaktiGlassTopBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dakti.app.presentation.reservations.ReservationDraftUi
import com.dakti.app.ui.components.AppLoadingState
import com.dakti.app.ui.components.SectionHeader

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
    onHomeClick: () -> Unit
) {
    DaktiHeroScaffold(
        topBar = {
            DaktiGlassTopBar(
                title = "Reservation Confirmation",
                onBack = onBack
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SectionHeader(
                    title = "Reservation Review",
                    subtitle = "Confirm venue and slot details before creating your booking."
                )
            }

            if (isDraftLoading) {
                item {
                    AppLoadingState(message = "Preparing reservation summary...")
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
                        onHomeClick = onHomeClick
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

            if (!isDraftLoading && draft == null && errorMessage == null && successMessage == null) {
                item {
                    ReservationEmptyState(
                        message = "Reservation details are not ready yet.",
                        actionLabel = "Go Back",
                        onActionClick = onBack
                    )
                }
            }
        }
    }
}

