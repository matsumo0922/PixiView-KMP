package me.matsumo.fanbox.core.ui.extensition

import android.content.Context
import android.content.Intent
import android.os.Process
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.core.net.toUri
import me.matsumo.fanbox.core.logs.category.NavigationLog
import me.matsumo.fanbox.core.logs.logger.send

class NavigatorExtensionImpl(
    private val context: Context,
) : NavigatorExtension {

    override fun navigateToWebPage(url: String, referrer: String) {
        runCatching {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, url.toUri()).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                },
            )
        }.onFailure {
            Toast.makeText(context, "Failed to open the web page", Toast.LENGTH_SHORT).show()
        }.also {
            NavigationLog.openUrl(
                url = url,
                referer = referrer,
                isSuccess = it.isSuccess,
            ).send()
        }
    }

    override fun killApp() {
        Process.killProcess(Process.myPid())
    }
}

@Composable
actual fun BackHandler(isEnable: Boolean, onBack: () -> Unit) {
    androidx.activity.compose.BackHandler(isEnable, onBack)
}
