package me.matsumo.fanbox.core.ui.extensition

import android.content.Context
import android.content.Intent
import android.os.Process
import android.widget.Toast
import androidx.core.net.toUri

class NavigatorExtensionImpl(
    private val context: Context,
): NavigatorExtension {

    override fun navigateToWebPage(url: String) {
        runCatching {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, url.toUri()).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            )
        }.onFailure {
            Toast.makeText(context, "Failed to open the web page", Toast.LENGTH_SHORT).show()
        }
    }

    override fun killApp() {
        Process.killProcess(Process.myPid())
    }
}
