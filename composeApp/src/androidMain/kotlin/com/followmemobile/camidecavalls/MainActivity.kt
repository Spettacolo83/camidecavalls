package com.followmemobile.camidecavalls

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.followmemobile.camidecavalls.domain.service.LocalNotificationManager
import com.followmemobile.camidecavalls.presentation.main.PoiNavigationManager
import org.koin.compose.KoinContext
import org.koin.java.KoinJavaComponent.getKoin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Hide system navigation bar for full-screen experience
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.hide(WindowInsetsCompat.Type.navigationBars())
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        setContent {
            KoinContext {
                App()
            }
        }

        // Handle POI intent on cold start
        handlePoiIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handlePoiIntent(intent)
    }

    private fun handlePoiIntent(intent: Intent?) {
        val poiId = intent?.getIntExtra(LocalNotificationManager.EXTRA_POI_ID, 0) ?: 0
        if (poiId > 0) {
            // Remove the extra to prevent re-trigger on config change
            intent?.removeExtra(LocalNotificationManager.EXTRA_POI_ID)
            try {
                val poiNavigationManager = getKoin().get<PoiNavigationManager>()
                poiNavigationManager.navigateToPoi(poiId)
            } catch (e: Exception) {
                // Koin may not be initialized yet on cold start; will be handled by compose
                println("PoiNavigationManager not yet available: ${e.message}")
            }
        }
    }
}
