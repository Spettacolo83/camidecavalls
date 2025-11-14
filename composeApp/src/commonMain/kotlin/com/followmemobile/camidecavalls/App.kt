package com.followmemobile.camidecavalls

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.navigator.Navigator
import com.followmemobile.camidecavalls.presentation.about.AboutScreen
import com.followmemobile.camidecavalls.domain.usecase.tracking.TrackingManager
import com.followmemobile.camidecavalls.domain.usecase.tracking.TrackingState
import com.followmemobile.camidecavalls.ui.theme.AppTypography
import org.koin.compose.koinInject
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import camidecavalls.composeapp.generated.resources.Res
import camidecavalls.composeapp.generated.resources.tracking_recording_badge

@Composable
fun App() {
    KoinInitializer {
        MaterialTheme(
            typography = AppTypography()
        ) {
            val trackingManager: TrackingManager = koinInject()
            val trackingState by trackingManager.trackingState.collectAsState()

            Box(modifier = Modifier.fillMaxSize()) {
                Navigator(AboutScreen())

                if (trackingState is TrackingState.Recording) {
                    RecordingBadge(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 56.dp, end = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun RecordingBadge(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "recording-badge")
    val alpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "recording-alpha"
    )

    Surface(
        modifier = modifier.graphicsLayer { this.alpha = alpha },
        color = MaterialTheme.colorScheme.error,
        contentColor = MaterialTheme.colorScheme.onError,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = stringResource(Res.string.tracking_recording_badge),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Platform-specific Koin initialization wrapper.
 * On Android: Koin is already initialized in Application class, so this is just a passthrough.
 * On iOS: Koin is initialized here using KoinApplication.
 */
@Composable
expect fun KoinInitializer(content: @Composable () -> Unit)
