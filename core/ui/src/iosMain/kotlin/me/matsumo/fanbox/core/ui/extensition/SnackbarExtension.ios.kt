package me.matsumo.fanbox.core.ui.extensition

import androidx.compose.material3.SnackbarHostState
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.desc.desc

class SnackbarExtensionImpl : SnackbarExtension {

    override suspend fun showSnackbar(snackbarHostState: SnackbarHostState, message: StringResource, isOverride: Boolean) {
        snackbarHostState.currentSnackbarData?.dismiss()
        snackbarHostState.showSnackbar(message.desc().localized())
    }
}
