package com.bletracker.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bletracker.ui.theme.SurfaceBorderLight
import com.bletracker.ui.theme.SurfaceLight

fun Modifier.shimmerEffect(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmerTransition")
    val translateAnim by transition.animateFloat(
        initialValue = -500f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    val shimmerColors = listOf(
        Color(0xFFE2E8F0),
        Color(0xFFF8FAFC),
        Color(0xFFE2E8F0)
    )

    background(
        brush = Brush.linearGradient(
            colors = shimmerColors,
            start = Offset(translateAnim, 0f),
            end = Offset(translateAnim + 300f, 0f)
        )
    )
}

@Composable
fun DeviceCardSkeleton(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceLight)
            .border(1.dp, SurfaceBorderLight, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circle icon placeholder
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .shimmerEffect()
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    // Device name placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.55f)
                            .height(16.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .shimmerEffect()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    // MAC placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.35f)
                            .height(12.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .shimmerEffect()
                    )
                }

                // Badge placeholder
                Box(
                    modifier = Modifier
                        .width(76.dp)
                        .height(24.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .shimmerEffect()
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Metrics row placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .shimmerEffect()
            )
        }
    }
}

@Composable
fun ScannerSkeletonList(
    count: Int = 4,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        repeat(count) {
            DeviceCardSkeleton()
        }
    }
}
