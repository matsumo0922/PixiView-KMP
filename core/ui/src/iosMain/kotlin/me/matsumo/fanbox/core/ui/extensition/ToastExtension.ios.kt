package me.matsumo.fanbox.core.ui.extensition

import androidx.compose.material3.SnackbarHostState
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.desc.desc

class ToastExtensionImpl : ToastExtension {

    override suspend fun show(snackbarHostState: SnackbarHostState, message: StringResource, isSnackbar: Boolean, isOverride: Boolean) {
        snackbarHostState.currentSnackbarData?.dismiss()
        snackbarHostState.showSnackbar(message.desc().localized())
    }

    override suspend fun show(snackbarHostState: SnackbarHostState, message: String, isSnackbar: Boolean, isOverride: Boolean) {
        snackbarHostState.currentSnackbarData?.dismiss()
        snackbarHostState.showSnackbar(message)
    }
}
