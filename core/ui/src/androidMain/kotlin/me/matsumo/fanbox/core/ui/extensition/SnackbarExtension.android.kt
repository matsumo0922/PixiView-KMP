package me.matsumo.fanbox.core.ui.extensition

import android.content.Context
import androidx.compose.material3.SnackbarHostState
import dev.icerock.moko.resources.StringResource

class SnackbarExtensionImpl(
    private val context: Context,
) : SnackbarExtension {

    override suspend fun showSnackbar(snackbarHostState: SnackbarHostState, message: StringResource, isOverride: Boolean) {
        snackbarHostState.currentSnackbarData?.dismiss()
        snackbarHostState.showSnackbar(message.getString(context))
    }

    override suspend fun showSnackbar(snackbarHostState: SnackbarHostState, message: String, isOverride: Boolean) {
        snackbarHostState.currentSnackbarData?.dismiss()
        snackbarHostState.showSnackbar(message)
    }
}
