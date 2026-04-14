@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.dakti.app.ui.screens.matches

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
fun CreateMatchScreen(
    onCreateClick: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text(text = "Create Match") })
        }
    ) { innerPadding ->
        DaktiPlaceholderContent(
            title = "Create a Match",
            description = "Placeholder match creation flow. Real form fields will be added in later phases.",
            modifier = Modifier.padding(innerPadding)
        ) {
            Button(onClick = onCreateClick) {
                Text(text = "Create Demo Match")
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onBack) {
                Text(text = "Back")
            }
        }
    }
}
