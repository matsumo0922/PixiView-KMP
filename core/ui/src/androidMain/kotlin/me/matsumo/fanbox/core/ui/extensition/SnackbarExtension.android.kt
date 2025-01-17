package me.matsumo.fanbox.core.ui.extensition

import android.content.Context
import android.widget.Toast
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

class ToastExtensionImpl(
    private val context: Context,
) : ToastExtension {

    private var toast: Toast? = null

    override suspend fun show(
        snackbarHostState: SnackbarHostState,
        message: StringResource,
        label: StringResource?,
        callback: (SnackbarResult) -> Unit,
        isSnackbar: Boolean,
        isOverride: Boolean,
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
        isOverride: Boolean,
    ) {
        if (isSnackbar) {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(message, label).run(callback)
        } else {
            if (isOverride) toast?.cancel()

            Toast.makeText(context, message, Toast.LENGTH_SHORT).also {
                it.show()
                toast = it
            }
        }
    }
}
