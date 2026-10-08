package com.bletracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bletracker.domain.model.SignalZone
import com.bletracker.ui.theme.ZoneImmediate
import com.bletracker.ui.theme.ZoneLost
import com.bletracker.ui.theme.ZoneMid
import com.bletracker.ui.theme.ZoneNear
import com.bletracker.ui.theme.ZoneVeryWeak
import com.bletracker.ui.theme.ZoneWeak

fun SignalZone.getColor(): Color = when (this) {
    SignalZone.IMMEDIATE -> ZoneImmediate
    SignalZone.NEAR -> ZoneNear
    SignalZone.MID -> ZoneMid
    SignalZone.WEAK -> ZoneWeak
    SignalZone.VERY_WEAK -> ZoneVeryWeak
    SignalZone.LOST -> ZoneLost
}

@Composable
fun SignalBadge(
    zone: SignalZone,
    modifier: Modifier = Modifier
) {
    val zoneColor = zone.getColor()

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(zoneColor.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(zoneColor)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = zone.title,
            color = zoneColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
