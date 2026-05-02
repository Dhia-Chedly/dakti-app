package com.dakti.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dakti.app.notification.NotificationNavigation
import com.dakti.app.presentation.theme.ThemeViewModel
import com.dakti.app.ui.navigation.AppRoute
import com.dakti.app.ui.navigation.DaktiNavGraph
import com.dakti.app.ui.theme.DaktiTheme
import com.dakti.app.util.AppConstants
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var pendingNotificationRoute: String? by mutableStateOf(null)
    private val themeViewModel: ThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pendingNotificationRoute = NotificationNavigation.extractTargetRoute(intent)

        setContent {
            val themeState by themeViewModel.uiState.collectAsStateWithLifecycle()
            val isSystemDarkTheme = isSystemInDarkTheme()
            val useDarkTheme = ThemeViewModel.resolveDarkTheme(
                mode = themeState.mode,
                isSystemDarkTheme = isSystemDarkTheme
            )

            SideEffect {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        lightScrim = android.graphics.Color.TRANSPARENT,
                        darkScrim = android.graphics.Color.TRANSPARENT,
                        detectDarkMode = { useDarkTheme }
                    ),
                    navigationBarStyle = SystemBarStyle.auto(
                        lightScrim = android.graphics.Color.TRANSPARENT,
                        darkScrim = android.graphics.Color.TRANSPARENT,
                        detectDarkMode = { useDarkTheme }
                    )
                )
            }

            DaktiTheme(darkTheme = useDarkTheme) {
                val shouldForceMainGraph = !pendingNotificationRoute.isNullOrBlank()
                val startDestination = if (AppConstants.SKIP_AUTH_FOR_DEMO || shouldForceMainGraph) {
                    AppRoute.MainGraph.route
                } else {
                    AppRoute.Splash.route
                }

                DaktiNavGraph(
                    startDestination = startDestination,
                    pendingNotificationRoute = pendingNotificationRoute,
                    onNotificationRouteConsumed = {
                        pendingNotificationRoute = null
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingNotificationRoute = NotificationNavigation.extractTargetRoute(intent)
    }
}
