package me.matsumo.fanbox.core.model

import androidx.compose.runtime.Stable

@Stable
sealed interface DownloadState {
    data object None : DownloadState

    data class Downloading(
        val items: FanboxDownloadItems,
        val progress: Float,
    ) : DownloadState
}
