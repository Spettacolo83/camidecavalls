package com.followmemobile.camidecavalls.presentation.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.min
import kotlin.random.Random
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

private val GradientTop = Color(0xFF00C8FF)
private val GradientBottom = Color(0xFF004CFF)

// How much of the screen width Menorca should cover
private const val MENORCA_WIDTH_FRACTION = 0.88f

// Shrink routes slightly so they sit within the silhouette outline
private const val ROUTE_SCALE_ADJUST = 0.97f
// Tiny offset to fine-tune after scale (in SVG coordinate units)
private const val ROUTE_SHIFT_X = 3f
private const val ROUTE_SHIFT_Y = -3f

// Logo and text max opacity (slightly transparent for visual softness)
private const val LOGO_TEXT_MAX_ALPHA = 0.85f

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    // Randomly choose logo variant at each launch
    val useHorseVariant = remember { Random.nextBoolean() }

    // Animation progress values
    val menorcaScale = remember { Animatable(0f) }
    val routeProgress = remember { Animatable(0f) }
    val crossFade = remember { Animatable(0f) }
    val logoScale = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    val screenAlpha = remember { Animatable(1f) }

    // Pre-parse accurate Menorca silhouette (phase 1+2, from menorca_silhouette.svg)
    val menorcaMapPath = remember {
        PathParser().parsePathString(MenorcaSilhouetteData.MENORCA_MAP_PATH).toPath()
    }
    val menorcaMapBounds = remember { menorcaMapPath.getBounds() }

    // Pre-parse logo paths (phase 3) — menorca variant (500x500 viewport)
    val logoMenorcaHorseshoePath = remember {
        PathParser().parsePathString(MenorcaSilhouetteData.HORSESHOE_PATH).toPath()
    }
    val logoMenorcaSilhouettePath = remember {
        PathParser().parsePathString(MenorcaSilhouetteData.MENORCA_ICON_PATH).toPath()
    }

    // Pre-parse logo paths (phase 3) — horse variant (188.176x188.176 viewport)
    val logoHorseIconPath = remember {
        PathParser().parsePathString(MenorcaSilhouetteData.HORSE_ICON_PATH).toPath()
    }

    // Pre-build route colors
    val routeColors = remember {
        (0 until 20).map { index ->
            val hex = com.followmemobile.camidecavalls.presentation.map.RouteColorPalette.colorForIndex(index)
            Color(("FF" + hex.removePrefix("#")).toLong(16))
        }
    }

    LaunchedEffect(Unit) {
        // Step 1: Menorca silhouette scales in (0 → 1.0s)
        menorcaScale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        )
        // Step 2: Route tracing (1.0 → 3.0s)
        routeProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2000, easing = FastOutSlowInEasing)
        )
        // Step 3: Cross-fade to logo + logo scale in parallel (3.0 → 3.8s)
        coroutineScope {
            launch {
                crossFade.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
                )
            }
            launch {
                logoScale.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
                )
            }
        }
        // Step 4: Text fade-in (3.8 → 4.5s)
        textAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing)
        )
        // Step 5: Pause then fade out
        kotlinx.coroutines.delay(200)
        screenAlpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 300)
        )
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(GradientTop, GradientBottom))),
        contentAlignment = Alignment.Center
    ) {
        // Phase 1+2: Menorca silhouette + route tracing
        // Always composed, visibility controlled by graphicsLayer alpha
        val mapAlpha = 1f - crossFade.value
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = mapAlpha }
        ) {
            val canvasW = size.width
            val canvasH = size.height

            // Compute the Menorca drawing area from the silhouette's actual bounds
            val silAspect = menorcaMapBounds.width / menorcaMapBounds.height
            val drawWidth = canvasW * MENORCA_WIDTH_FRACTION
            val drawHeight = drawWidth / silAspect
            val drawLeft = (canvasW - drawWidth) / 2f
            val drawTop = (canvasH - drawHeight) / 2f

            // Draw silhouette: fit SVG path bounds into the drawing area
            val silScale = drawWidth / menorcaMapBounds.width
            val silOffsetX = drawLeft - menorcaMapBounds.left * silScale
            val silOffsetY = drawTop - menorcaMapBounds.top * silScale

            scale(menorcaScale.value, pivot = Offset(canvasW / 2f, canvasH / 2f)) {
                translate(silOffsetX, silOffsetY) {
                    scale(silScale, pivot = Offset.Zero) {
                        drawPath(
                            path = menorcaMapPath,
                            color = Color.White.copy(alpha = 0.18f),
                            style = Fill
                        )
                    }
                }
            }

            // Draw route tracing using the same transform as the silhouette
            // with slight scale-down centered on the silhouette center
            if (routeProgress.value > 0f) {
                val silCenterX = (menorcaMapBounds.left + menorcaMapBounds.right) / 2f
                val silCenterY = (menorcaMapBounds.top + menorcaMapBounds.bottom) / 2f
                drawRouteTracing(
                    routeColors = routeColors,
                    progress = routeProgress.value,
                    alpha = 1f,
                    svgScale = silScale,
                    svgOffsetX = silOffsetX,
                    svgOffsetY = silOffsetY,
                    silCenterX = silCenterX,
                    silCenterY = silCenterY
                )
            }
        }

        // Phase 3+4: Logo + text
        // Always composed to avoid cold-start delay on iOS
        val logoAlpha = crossFade.value * screenAlpha.value * LOGO_TEXT_MAX_ALPHA

        // Logo Canvas (60% of screen width)
        Canvas(
            modifier = Modifier
                .fillMaxSize(0.6f)
                .graphicsLayer { alpha = crossFade.value * screenAlpha.value }
        ) {
            val canvasW = size.width
            val canvasH = size.height
            val pivotCenter = Offset(canvasW / 2f, canvasH / 2f)

            if (useHorseVariant) {
                // Horse variant: 188.176x188.176 viewport
                val vp = MenorcaSilhouetteData.HORSE_ICON_VIEWPORT_SIZE
                val fitScale = min(canvasW / vp, canvasH / vp)
                val offsetX = (canvasW - vp * fitScale) / 2f
                val offsetY = (canvasH - vp * fitScale) / 2f

                scale(logoScale.value, pivot = pivotCenter) {
                    translate(offsetX, offsetY) {
                        scale(fitScale, pivot = Offset.Zero) {
                            drawPath(
                                path = logoHorseIconPath,
                                color = Color.White.copy(alpha = LOGO_TEXT_MAX_ALPHA),
                                style = Fill
                            )
                        }
                    }
                }
            } else {
                // Menorca variant: 500x500 viewport
                val vw = MenorcaSilhouetteData.ICON_VIEWPORT_WIDTH
                val vh = MenorcaSilhouetteData.ICON_VIEWPORT_HEIGHT
                val fitScale = min(canvasW / vw, canvasH / vh)
                val offsetX = (canvasW - vw * fitScale) / 2f
                val offsetY = (canvasH - vh * fitScale) / 2f

                scale(logoScale.value, pivot = pivotCenter) {
                    translate(offsetX, offsetY) {
                        scale(fitScale, pivot = Offset.Zero) {
                            drawPath(
                                path = logoMenorcaHorseshoePath,
                                color = Color.White.copy(alpha = LOGO_TEXT_MAX_ALPHA),
                                style = Fill
                            )
                            drawPath(
                                path = logoMenorcaSilhouettePath,
                                color = Color.White.copy(alpha = LOGO_TEXT_MAX_ALPHA),
                                style = Fill
                            )
                        }
                    }
                }
            }
        }

        // Text below logo
        Column(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = textAlpha.value * screenAlpha.value },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(0.72f))
            Text(
                text = "Cam\u00ED de Cavalls",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White.copy(alpha = LOGO_TEXT_MAX_ALPHA),
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(0.28f))
        }
    }
}

private fun DrawScope.drawRouteTracing(
    routeColors: List<Color>,
    progress: Float,
    alpha: Float,
    svgScale: Float,
    svgOffsetX: Float,
    svgOffsetY: Float,
    silCenterX: Float,
    silCenterY: Float
) {
    val routes = RoutePathsData.routes
    val routeCount = routes.size
    val segmentSize = 1f / routeCount
    val strokeWidth = 3.dp.toPx()

    for (i in routes.indices) {
        val routeStart = i * segmentSize
        val routeLocalProgress = ((progress - routeStart) / segmentSize).coerceIn(0f, 1f)
        if (routeLocalProgress <= 0f) continue

        val points = routes[i]
        if (points.size < 2) continue

        // Route points are in SVG coordinates — scale toward silhouette center + tiny shift
        val canvasPath = Path().apply {
            val ax = silCenterX + (points[0].x - silCenterX) * ROUTE_SCALE_ADJUST + ROUTE_SHIFT_X
            val ay = silCenterY + (points[0].y - silCenterY) * ROUTE_SCALE_ADJUST + ROUTE_SHIFT_Y
            moveTo(
                svgOffsetX + ax * svgScale,
                svgOffsetY + ay * svgScale
            )
            for (j in 1 until points.size) {
                val px = silCenterX + (points[j].x - silCenterX) * ROUTE_SCALE_ADJUST + ROUTE_SHIFT_X
                val py = silCenterY + (points[j].y - silCenterY) * ROUTE_SCALE_ADJUST + ROUTE_SHIFT_Y
                lineTo(
                    svgOffsetX + px * svgScale,
                    svgOffsetY + py * svgScale
                )
            }
        }

        val color = routeColors[i].copy(alpha = alpha)
        val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round)

        if (routeLocalProgress >= 1f) {
            drawPath(path = canvasPath, color = color, style = stroke)
        } else {
            val measure = PathMeasure()
            measure.setPath(canvasPath, false)
            val destPath = Path()
            measure.getSegment(0f, measure.length * routeLocalProgress, destPath, true)
            drawPath(path = destPath, color = color, style = stroke)
        }
    }
}
