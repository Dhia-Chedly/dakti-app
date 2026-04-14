@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.dakti.app.ui.screens.invitations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun InvitationsScreen(
    invitations: List<String>,
    onAcceptAll: () -> Unit,
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
                Button(onClick = onAcceptAll) {
                    Text(text = "Accept All (Placeholder)")
                }
            }
            items(invitations) { invitation ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = invitation,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            item {
                Column {
                    TextButton(onClick = onBackToHome) {
                        Text(text = "Back to Home")
                    }
                }
            }
        }
    }
}
