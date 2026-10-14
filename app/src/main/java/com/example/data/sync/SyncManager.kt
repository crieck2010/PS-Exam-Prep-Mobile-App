package com.example.data.sync

import com.example.data.local.AppDatabase
import com.example.data.network.NetworkMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class SyncState {
    SYNCED,
    SYNCING,
    OFFLINE_CACHED,
    ERROR
}

data class SyncInfo(
    val state: SyncState = SyncState.SYNCED,
    val unsyncedCount: Int = 0,
    val lastSyncFormatted: String = "Just now",
    val isOnline: Boolean = true
)

class SyncManager(
    private val database: AppDatabase,
    private val networkMonitor: NetworkMonitor,
    private val coroutineScope: CoroutineScope
) {
    private val _syncInfo = MutableStateFlow(SyncInfo())
    val syncInfo: StateFlow<SyncInfo> = _syncInfo.asStateFlow()

    init {
        // Observe network state and automatically sync queued data when connection is restored
        coroutineScope.launch {
            networkMonitor.isOnline.collect { online ->
                _syncInfo.value = _syncInfo.value.copy(
                    isOnline = online,
                    state = if (online) {
                        if (_syncInfo.value.unsyncedCount > 0) SyncState.SYNCING else SyncState.SYNCED
                    } else {
                        SyncState.OFFLINE_CACHED
                    }
                )

                if (online) {
                    syncQueuedData()
                }
            }
        }
    }

    fun triggerSync() {
        coroutineScope.launch {
            syncQueuedData()
        }
    }

    suspend fun syncQueuedData() {
        val isOnline = networkMonitor.isCurrentlyConnected()
        if (!isOnline) {
            checkUnsyncedCounts(isOnline = false)
            return
        }

        try {
            _syncInfo.value = _syncInfo.value.copy(state = SyncState.SYNCING)

            val unsyncedQuizzes = database.quizDao().getUnsyncedQuizzes()
            val unsyncedProgress = database.questionProgressDao().getUnsyncedProgress()
            val unsyncedStats = database.userStatsDao().getUnsyncedStats()

            val totalToSync = unsyncedQuizzes.size + unsyncedProgress.size + unsyncedStats.size

            if (totalToSync > 0) {
                // Simulate cloud synchronization delay
                delay(800L)
                val now = System.currentTimeMillis()

                if (unsyncedQuizzes.isNotEmpty()) {
                    database.quizDao().markQuizzesSynced(unsyncedQuizzes.map { it.id }, now)
                }

                if (unsyncedProgress.isNotEmpty()) {
                    database.questionProgressDao().markProgressSynced(unsyncedProgress.map { it.questionId }, now)
                }

                if (unsyncedStats.isNotEmpty()) {
                    database.userStatsDao().markStatsSynced("local_user", now)
                }
            }

            val timeStr = SimpleDateFormat("h:mm a", Locale.US).format(Date())
            _syncInfo.value = SyncInfo(
                state = SyncState.SYNCED,
                unsyncedCount = 0,
                lastSyncFormatted = timeStr,
                isOnline = true
            )
        } catch (e: Exception) {
            _syncInfo.value = _syncInfo.value.copy(
                state = SyncState.ERROR
            )
        }
    }

    suspend fun checkUnsyncedCounts(isOnline: Boolean) {
        val unsyncedQuizzes = database.quizDao().getUnsyncedQuizzes()
        val unsyncedProgress = database.questionProgressDao().getUnsyncedProgress()
        val count = unsyncedQuizzes.size + unsyncedProgress.size
        _syncInfo.value = _syncInfo.value.copy(
            unsyncedCount = count,
            state = if (isOnline) {
                if (count > 0) SyncState.SYNCING else SyncState.SYNCED
            } else {
                SyncState.OFFLINE_CACHED
            },
            isOnline = isOnline
        )
    }
}
