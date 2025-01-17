package me.matsumo.fanbox.core.ui.extensition

import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.staticCompositionLocalOf
import org.jetbrains.compose.resources.StringResource

val LocalSnackbarHostState = staticCompositionLocalOf { SnackbarHostState() }

interface ToastExtension {
    suspend fun show(
        snackbarHostState: SnackbarHostState,
        message: StringResource,
        label: StringResource? = null,
        callback: (SnackbarResult) -> Unit = {},
        isSnackbar: Boolean = label != null,
        isOverride: Boolean = true
    )

    suspend fun show(
        snackbarHostState: SnackbarHostState,
        message: String,
        label: String? = null,
        callback: (SnackbarResult) -> Unit = {},
        isSnackbar: Boolean = label != null,
        isOverride: Boolean = true
    )
}
