package com.dakti.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.dakti.app.notification.NotificationNavigation
import com.dakti.app.ui.navigation.AppRoute
import com.dakti.app.ui.navigation.DaktiNavGraph
import com.dakti.app.ui.theme.DaktiTheme
import com.dakti.app.util.AppConstants
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var pendingNotificationRoute: String? by mutableStateOf(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                lightScrim = android.graphics.Color.TRANSPARENT,
                darkScrim = android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.auto(
                lightScrim = android.graphics.Color.TRANSPARENT,
                darkScrim = android.graphics.Color.TRANSPARENT
            )
        )
        pendingNotificationRoute = NotificationNavigation.extractTargetRoute(intent)

        setContent {
            DaktiTheme {
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
