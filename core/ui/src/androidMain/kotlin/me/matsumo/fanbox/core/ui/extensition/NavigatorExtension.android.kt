package me.matsumo.fanbox.core.ui.extensition

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri

class NavigatorExtensionImpl(
    private val context: Context,
): NavigatorExtension {

    override fun navigateToWebPage(url: String) {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, url.toUri()).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    }
}
