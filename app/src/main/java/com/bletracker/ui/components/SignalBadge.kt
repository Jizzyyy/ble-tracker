package com.bletracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.bletracker.ui.theme.ZoneImmediateBg
import com.bletracker.ui.theme.ZoneLost
import com.bletracker.ui.theme.ZoneLostBg
import com.bletracker.ui.theme.ZoneMid
import com.bletracker.ui.theme.ZoneMidBg
import com.bletracker.ui.theme.ZoneNear
import com.bletracker.ui.theme.ZoneNearBg
import com.bletracker.ui.theme.ZoneVeryWeak
import com.bletracker.ui.theme.ZoneVeryWeakBg
import com.bletracker.ui.theme.ZoneWeak
import com.bletracker.ui.theme.ZoneWeakBg

fun SignalZone.getColor(): Color = when (this) {
    SignalZone.IMMEDIATE -> ZoneImmediate
    SignalZone.NEAR -> ZoneNear
    SignalZone.MID -> ZoneMid
    SignalZone.WEAK -> ZoneWeak
    SignalZone.VERY_WEAK -> ZoneVeryWeak
    SignalZone.LOST -> ZoneLost
}

fun SignalZone.getBgColor(): Color = when (this) {
    SignalZone.IMMEDIATE -> ZoneImmediateBg
    SignalZone.NEAR -> ZoneNearBg
    SignalZone.MID -> ZoneMidBg
    SignalZone.WEAK -> ZoneWeakBg
    SignalZone.VERY_WEAK -> ZoneVeryWeakBg
    SignalZone.LOST -> ZoneLostBg
}

@Composable
fun SignalBadge(
    zone: SignalZone,
    modifier: Modifier = Modifier
) {
    val zoneColor = zone.getColor()
    val bgColor = zone.getBgColor()

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .border(1.dp, zoneColor.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
            .padding(horizontal = 9.dp, vertical = 4.dp),
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
            text = zone.badgeLabel,
            color = zoneColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
