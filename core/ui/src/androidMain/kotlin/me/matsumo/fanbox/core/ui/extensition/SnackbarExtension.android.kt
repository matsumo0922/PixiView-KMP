package me.matsumo.fanbox.core.ui.extensition

import android.content.Context
import android.widget.Toast
import androidx.compose.material3.SnackbarHostState
import dev.icerock.moko.resources.StringResource

class ToastExtensionImpl(
    private val context: Context,
) : ToastExtension {

    private var toast: Toast? = null

    override suspend fun show(snackbarHostState: SnackbarHostState, message: StringResource, isSnackbar: Boolean, isOverride: Boolean) {
        show(snackbarHostState, message.getString(context), isSnackbar, isOverride)
    }

    override suspend fun show(snackbarHostState: SnackbarHostState, message: String, isSnackbar: Boolean, isOverride: Boolean) {
        if (isSnackbar) {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(message)
        } else {
            if (isOverride) toast?.cancel()

            Toast.makeText(context, message, Toast.LENGTH_SHORT).also {
                it.show()
                toast = it
            }
        }
    }
}
