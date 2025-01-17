package me.matsumo.fanbox.core.ui.extensition

import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

class ToastExtensionImpl : ToastExtension {

    override suspend fun show(
        snackbarHostState: SnackbarHostState,
        message: StringResource,
        label: StringResource?,
        callback: (SnackbarResult) -> Unit,
        isSnackbar: Boolean,
        isOverride: Boolean
    ) {
        show(
            snackbarHostState = snackbarHostState,
            message = getString(message),
            label = label?.let { getString(it) },
            callback = callback,
            isSnackbar = isSnackbar,
            isOverride = isOverride,
        )
    }

    override suspend fun show(
        snackbarHostState: SnackbarHostState,
        message: String,
        label: String?,
        callback: (SnackbarResult) -> Unit,
        isSnackbar: Boolean,
        isOverride: Boolean
    ) {
        snackbarHostState.currentSnackbarData?.dismiss()
        snackbarHostState.showSnackbar(message, label).run(callback)
    }
}
