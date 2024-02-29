package me.matsumo.fanbox.core.ui.extensition

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.staticCompositionLocalOf
import dev.icerock.moko.resources.StringResource

val LocalSnackbarHostState = staticCompositionLocalOf { SnackbarHostState() }

interface SnackbarExtension {
    suspend fun showSnackbar(
        snackbarHostState: SnackbarHostState,
        message: StringResource,
        isOverride: Boolean = true,
    )

    suspend fun showSnackbar(
        snackbarHostState: SnackbarHostState,
        message: String,
        isOverride: Boolean = true,
    )
}
