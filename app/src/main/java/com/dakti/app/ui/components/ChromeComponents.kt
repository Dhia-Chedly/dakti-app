@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.dakti.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dakti.app.ui.theme.DaktiThemeTokens

data class DaktiGlassBottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val isCenterProminent: Boolean = false
)

@Composable
fun DaktiGlassContainer(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
    content: @Composable () -> Unit
) {
    val chrome = DaktiThemeTokens.chrome
    val chromeMetrics = DaktiThemeTokens.chromeMetrics
    val elevations = DaktiThemeTokens.elevations

    Surface(
        modifier = modifier
            .padding(
                horizontal = chromeMetrics.horizontalMargin,
                vertical = chromeMetrics.verticalInset
            )
            .shadow(
                elevation = elevations.medium,
                shape = RoundedCornerShape(chromeMetrics.cornerRadius),
                clip = false,
                ambientColor = chrome.shadowAmbient,
                spotColor = chrome.shadowSpot
            ),
        shape = RoundedCornerShape(chromeMetrics.cornerRadius),
        color = chrome.container,
        border = BorderStroke(1.dp, chrome.border),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding)
        ) {
            content()
        }
    }
}

@Composable
fun DaktiGlassTopBar(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val chrome = DaktiThemeTokens.chrome
    DaktiGlassTopBar(
        modifier = modifier,
        navigationIcon = onBack?.let { backAction ->
            {
                IconButton(onClick = backAction) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = chrome.content
                    )
                }
            }
        },
        titleContent = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = chrome.content,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        actions = actions
    )
}

@Composable
fun DaktiGlassTopBar(
    modifier: Modifier = Modifier,
    navigationIcon: (@Composable () -> Unit)? = null,
    titleContent: @Composable () -> Unit,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val chrome = DaktiThemeTokens.chrome

    DaktiGlassContainer(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
    ) {
        TopAppBar(
            title = titleContent,
            navigationIcon = {
                navigationIcon?.invoke()
            },
            actions = actions,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                scrolledContainerColor = Color.Transparent,
                navigationIconContentColor = chrome.content,
                titleContentColor = chrome.content,
                actionIconContentColor = chrome.content
            )
        )
    }
}

@Composable
fun DaktiGlassBottomNav(
    items: List<DaktiGlassBottomNavItem>,
    selectedRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val chrome = DaktiThemeTokens.chrome

    DaktiGlassContainer(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items.forEach { item ->
                val selected = selectedRoute == item.route
                val containerColor = if (selected) {
                    chrome.selectedPill
                } else if (item.isCenterProminent) {
                    chrome.container.copy(alpha = 0.95f)
                } else {
                    Color.Transparent
                }
                val contentColor = if (selected) {
                    chrome.selectedContent
                } else {
                    chrome.content.copy(alpha = 0.88f)
                }
                val itemShape = if (item.isCenterProminent) {
                    RoundedCornerShape(999.dp)
                } else {
                    CircleShape
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(color = containerColor, shape = itemShape)
                        .clickable { onNavigate(item.route) }
                        .padding(
                            horizontal = if (item.isCenterProminent) 8.dp else 6.dp,
                            vertical = if (item.isCenterProminent) 10.dp else 8.dp
                        ),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = contentColor,
                        modifier = Modifier.size(if (item.isCenterProminent) 24.dp else 20.dp)
                    )
                    Text(
                        text = item.label,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelMedium,
                        color = contentColor,
                        fontWeight = if (item.isCenterProminent) FontWeight.SemiBold else FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
