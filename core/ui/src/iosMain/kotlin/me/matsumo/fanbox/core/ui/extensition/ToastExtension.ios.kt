package me.matsumo.fanbox.core.ui.extensition

import androidx.compose.material3.SnackbarHostState
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

class ToastExtensionImpl : ToastExtension {

    override suspend fun show(snackbarHostState: SnackbarHostState, message: StringResource, isSnackbar: Boolean, isOverride: Boolean) {
        snackbarHostState.currentSnackbarData?.dismiss()
        snackbarHostState.showSnackbar(getString(message))
    }

    override suspend fun show(snackbarHostState: SnackbarHostState, message: String, isSnackbar: Boolean, isOverride: Boolean) {
        snackbarHostState.currentSnackbarData?.dismiss()
        snackbarHostState.showSnackbar(message)
    }
}
