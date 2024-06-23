package me.matsumo.fanbox.core.ui.extensition

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.staticCompositionLocalOf
import org.jetbrains.compose.resources.StringResource

val LocalSnackbarHostState = staticCompositionLocalOf { SnackbarHostState() }

interface ToastExtension {
    suspend fun show(
        snackbarHostState: SnackbarHostState,
        message: StringResource,
        isSnackbar: Boolean = false,
        isOverride: Boolean = true,
    )

    suspend fun show(
        snackbarHostState: SnackbarHostState,
        message: String,
        isSnackbar: Boolean = false,
        isOverride: Boolean = true,
    )
}
