package me.matsumo.fanbox.core.ui.extensition

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.staticCompositionLocalOf
import dev.icerock.moko.resources.StringResource

val LocalSnackbarHostState = staticCompositionLocalOf { SnackbarHostState() }

interface ToastExtension {
    suspend fun showToast(
        snackbarHostState: SnackbarHostState,
        message: StringResource,
        isSnackbar: Boolean = false,
        isOverride: Boolean = true,
    )

    suspend fun showToast(
        snackbarHostState: SnackbarHostState,
        message: String,
        isSnackbar: Boolean = false,
        isOverride: Boolean = true,
    )
}
