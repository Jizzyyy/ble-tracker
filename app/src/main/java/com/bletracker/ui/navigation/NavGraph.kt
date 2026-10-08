package com.bletracker.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Radar
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Scanner : Screen("scanner", "Pemindai", Icons.Default.Radar)
    data object History : Screen("history", "Riwayat", Icons.Default.History)
    data object Radar : Screen("radar/{address}", "Radar", Icons.Default.Radar) {
        fun createRoute(address: String): String = "radar/$address"
    }
}

val bottomNavItems = listOf(
    Screen.Scanner,
    Screen.History
)
