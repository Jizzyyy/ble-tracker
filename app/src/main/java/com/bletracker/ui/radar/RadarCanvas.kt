package com.bletracker.ui.radar

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bletracker.domain.model.SignalZone
import com.bletracker.ui.components.getColor
import com.bletracker.ui.theme.PrimaryBlue
import com.bletracker.ui.theme.SurfaceBorderLight
import com.bletracker.ui.theme.ZoneLost
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RadarCanvas(
    zone: SignalZone,
    estimatedDistance: Double,
    isLost: Boolean,
    modifier: Modifier = Modifier
) {
    val activeColor = if (isLost) ZoneLost else zone.getColor()

    val infiniteTransition = rememberInfiniteTransition(label = "radarAnimation")

    // Continuous 360-degree sweep angle
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweepAngle"
    )

    // Pulsing blip halo
    val pulseProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseHalo"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxRadius = (size.minDimension / 2f) * 0.92f

            // Concentric zone rings (5 rings: 1m, 3m, 10m, 20m, >20m)
            val ringFractions = floatArrayOf(0.22f, 0.42f, 0.62f, 0.82f, 1.0f)

            // Background ambient grid (Light Mode clean slate lines)
            ringFractions.forEach { fraction ->
                drawCircle(
                    color = Color(0xFFCBD5E1),
                    radius = maxRadius * fraction,
                    center = center,
                    style = Stroke(
                        width = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                    )
                )
            }

            // Crosshair axes
            drawLine(
                color = SurfaceBorderLight,
                start = Offset(center.x - maxRadius, center.y),
                end = Offset(center.x + maxRadius, center.y),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                color = SurfaceBorderLight,
                start = Offset(center.x, center.y - maxRadius),
                end = Offset(center.x, center.y + maxRadius),
                strokeWidth = 1.dp.toPx()
            )

            // Radar sweep beam (semi-transparent sector)
            if (!isLost) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            Color.Transparent,
                            PrimaryBlue.copy(alpha = 0.03f),
                            PrimaryBlue.copy(alpha = 0.16f)
                        ),
                        center = center
                    ),
                    startAngle = sweepAngle - 45f,
                    sweepAngle = 45f,
                    useCenter = true,
                    topLeft = Offset(center.x - maxRadius, center.y - maxRadius),
                    size = androidx.compose.ui.geometry.Size(maxRadius * 2f, maxRadius * 2f)
                )
            }

            // Target blip position mapping
            val targetFraction = when {
                isLost -> 0.96f
                estimatedDistance < 1.0 -> 0.16f + (estimatedDistance.toFloat().coerceIn(0f, 1f) * 0.06f)
                estimatedDistance <= 3.0 -> 0.22f + (((estimatedDistance.toFloat() - 1f) / 2f) * 0.20f)
                estimatedDistance <= 10.0 -> 0.42f + (((estimatedDistance.toFloat() - 3f) / 7f) * 0.20f)
                estimatedDistance <= 20.0 -> 0.62f + (((estimatedDistance.toFloat() - 10f) / 10f) * 0.20f)
                else -> 0.85f + (((estimatedDistance.toFloat() - 20f).coerceAtMost(20f) / 20f) * 0.12f)
            }

            // Target angle (fixed 45-degree angle in upper right quadrant for clear display)
            val angleRad = Math.toRadians(315.0)
            val blipRadius = maxRadius * targetFraction
            val blipCenter = Offset(
                x = center.x + (blipRadius * cos(angleRad)).toFloat(),
                y = center.y + (blipRadius * sin(angleRad)).toFloat()
            )

            if (!isLost) {
                // Expanding pulse halo
                val haloRadius = 10.dp.toPx() + (pulseProgress * 22.dp.toPx())
                val haloAlpha = (1f - pulseProgress).coerceIn(0f, 1f) * 0.35f
                drawCircle(
                    color = activeColor.copy(alpha = haloAlpha),
                    radius = haloRadius,
                    center = blipCenter
                )

                // Outer blip ring
                drawCircle(
                    color = activeColor,
                    radius = 9.dp.toPx(),
                    center = blipCenter,
                    style = Stroke(width = 2.5.dp.toPx())
                )

                // Inner core dot
                drawCircle(
                    color = activeColor,
                    radius = 5.dp.toPx(),
                    center = blipCenter
                )
            } else {
                // Lost marker: dim hollow ring
                drawCircle(
                    color = ZoneLost.copy(alpha = 0.6f),
                    radius = 8.dp.toPx(),
                    center = blipCenter,
                    style = Stroke(width = 2.dp.toPx())
                )
            }

            // Center radar station dot (User)
            drawCircle(
                color = PrimaryBlue,
                radius = 4.dp.toPx(),
                center = center
            )
            drawCircle(
                color = PrimaryBlue.copy(alpha = 0.2f),
                radius = 8.dp.toPx(),
                center = center
            )
        }

        // Distance indicators around center
        Text(
            text = "ANDA",
            color = PrimaryBlue,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 22.dp)
        )
    }
}
