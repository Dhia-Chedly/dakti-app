@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.dakti.app.ui.screens.matches

import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dakti.app.ui.components.DaktiPlaceholderContent

@Composable
fun MatchDetailsScreen(
    matchId: String,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text(text = "Match Details") })
        }
    ) { innerPadding ->
        DaktiPlaceholderContent(
            title = "Match: $matchId",
            description = "Placeholder match detail screen for upcoming player and invitation logic.",
            modifier = Modifier.padding(innerPadding)
        ) {
            TextButton(onClick = onBack) {
                Text(text = "Back")
            }
        }
    }
}
