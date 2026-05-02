@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.dakti.app.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dakti.app.presentation.profile.ProfileUiState
import com.dakti.app.ui.components.AppInlineMessage
import com.dakti.app.ui.components.AppLoadingState
import com.dakti.app.ui.components.DaktiHeroScaffold
import com.dakti.app.ui.components.DaktiGlassTopBar
import com.dakti.app.ui.components.SectionHeader
import com.dakti.app.util.AppThemeMode

@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    onDisplayNameChanged: (String) -> Unit,
    onPhoneNumberChanged: (String) -> Unit,
    onAvatarUrlChanged: (String) -> Unit,
    onStartEditing: () -> Unit,
    onCancelEditing: () -> Unit,
    onSaveProfile: () -> Unit,
    onThemeModeSelected: (AppThemeMode) -> Unit,
    onLogout: () -> Unit
) {
    DaktiHeroScaffold(
        topBar = {
            DaktiGlassTopBar(title = "Profile")
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                AppLoadingState(message = "Loading profile...")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .size(86.dp)
                        .align(Alignment.CenterHorizontally),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = uiState.displayName.trim().take(1).ifEmpty { "U" }.uppercase(),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Text(
                    text = uiState.displayName,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "${uiState.roleLabel} account",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                SectionHeader(
                    title = "Profile Details",
                    subtitle = "Keep your contact and display information up to date."
                )

                ProfileSectionCard {
                    OutlinedTextField(
                        value = uiState.email,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = uiState.displayName,
                        onValueChange = onDisplayNameChanged,
                        readOnly = !uiState.isEditing,
                        label = { Text("Full name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = uiState.phoneNumber,
                        onValueChange = onPhoneNumberChanged,
                        readOnly = !uiState.isEditing,
                        label = { Text("Phone number") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = uiState.avatarUrl,
                        onValueChange = onAvatarUrlChanged,
                        readOnly = !uiState.isEditing,
                        label = { Text("Avatar URL (optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                SectionHeader(
                    title = "Appearance",
                    subtitle = "Choose how Dakti should look on this device."
                )

                ProfileSectionCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppThemeMode.entries.forEach { mode ->
                            FilterChip(
                                selected = uiState.themeMode == mode,
                                onClick = { onThemeModeSelected(mode) },
                                label = { Text(text = mode.label()) }
                            )
                        }
                    }
                }

                uiState.errorMessage?.let { message ->
                    AppInlineMessage(
                        message = message,
                        isError = true
                    )
                }

                uiState.statusMessage?.let { message ->
                    AppInlineMessage(
                        message = message,
                        isError = false
                    )
                }

                if (uiState.isEditing) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TextButton(
                            onClick = onCancelEditing,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "Cancel")
                        }

                        Button(
                            onClick = onSaveProfile,
                            enabled = !uiState.isSaving,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = if (uiState.isSaving) "Saving..." else "Save")
                        }
                    }
                } else {
                    Button(
                        onClick = onStartEditing,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Edit Profile")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Logout")
                }
            }
        }
    }
}

@Composable
private fun ProfileSectionCard(
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.92f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = content
        )
    }
}

private fun AppThemeMode.label(): String {
    return when (this) {
        AppThemeMode.LIGHT -> "Light"
        AppThemeMode.DARK -> "Dark"
        AppThemeMode.SYSTEM -> "System"
    }
}

