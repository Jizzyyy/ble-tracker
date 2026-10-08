package com.bletracker.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bletracker.domain.model.BleDevice
import com.bletracker.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class HistoryUiState(
    val devices: List<BleDevice> = emptyList(),
    val totalCount: Int = 0,
    val searchQuery: String = "",
    val isLoading: Boolean = false
)

class HistoryViewModel(
    private val historyRepository: HistoryRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")

    val uiState: StateFlow<HistoryUiState> = combine(
        historyRepository.getHistoryDevices(),
        _searchQuery
    ) { allDevices, rawQuery ->
        val query = rawQuery.trim()
        val queryNoColon = query.replace(":", "")

        val filtered = allDevices.filter { device ->
            query.isBlank() ||
                device.displayName.contains(query, ignoreCase = true) ||
                device.address.contains(query, ignoreCase = true) ||
                (queryNoColon.isNotBlank() && device.address.replace(":", "").contains(queryNoColon, ignoreCase = true))
        }

        HistoryUiState(
            devices = filtered,
            totalCount = allDevices.size,
            searchQuery = rawQuery,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HistoryUiState(isLoading = true)
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            historyRepository.clearHistory()
        }
    }

    fun formatLastSeen(timestamp: Long): String {
        val diffMillis = System.currentTimeMillis() - timestamp
        val diffSeconds = diffMillis / 1000
        val diffMinutes = diffSeconds / 60
        val diffHours = diffMinutes / 60
        val diffDays = diffHours / 24

        return when {
            diffSeconds < 10 -> "Baru saja"
            diffSeconds < 60 -> "$diffSeconds detik lalu"
            diffMinutes < 60 -> "$diffMinutes menit lalu"
            diffHours < 24 -> "$diffHours jam lalu"
            diffDays == 1L -> "Kemarin"
            else -> {
                val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
                sdf.format(Date(timestamp))
            }
        }
    }
}
