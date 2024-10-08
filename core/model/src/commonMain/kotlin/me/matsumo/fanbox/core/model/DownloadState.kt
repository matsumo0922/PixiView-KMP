package me.matsumo.fanbox.core.model

import androidx.compose.runtime.Stable

@Stable
sealed interface DownloadState {
    data object None : DownloadState

    data class Downloading(
        val title: String,
        val progress: Float,
        val remainingItems: Int,
    ) : DownloadState
}