package com.bletracker.ui.theme

import androidx.compose.ui.graphics.Color

val BackgroundDark = Color(0xFF0A0E17)
val SurfaceDark = Color(0xFF131A29)
val SurfaceVariantDark = Color(0xFF1E283D)
val SurfaceBorderDark = Color(0xFF2B3854)

val PrimaryCyan = Color(0xFF00E5FF)
val PrimaryCyanDark = Color(0xFF004953)
val AccentPurple = Color(0xFF8B5CF6)

val TextPrimary = Color(0xFFF8FAFC)
val TextSecondary = Color(0xFF94A3B8)
val TextTertiary = Color(0xFF64748B)

// Signal Zones
val ZoneImmediate = Color(0xFF10B981) // < 1m (-10 to -30 dBm)
val ZoneNear = Color(0xFF22C55E)      // 1-3m (-30 to -50 dBm)
val ZoneMid = Color(0xFFEAB308)       // 3-10m (-50 to -70 dBm)
val ZoneWeak = Color(0xFFF97316)      // 10-20m (-70 to -80 dBm)
val ZoneVeryWeak = Color(0xFFEF4444)  // > 20m (-80 to -90 dBm)
val ZoneLost = Color(0xFF64748B)      // Lost (< -90 dBm)
