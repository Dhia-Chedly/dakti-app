package com.dakti.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.dakti.app.ui.navigation.AppRoute
import com.dakti.app.ui.navigation.DaktiNavGraph
import com.dakti.app.ui.theme.DaktiTheme
import com.dakti.app.util.AppConstants
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            DaktiTheme {
                val startDestination = if (AppConstants.SKIP_AUTH_FOR_DEMO) {
                    AppRoute.MainGraph.route
                } else {
                    AppRoute.Splash.route
                }

                DaktiNavGraph(startDestination = startDestination)
            }
        }
    }
}
