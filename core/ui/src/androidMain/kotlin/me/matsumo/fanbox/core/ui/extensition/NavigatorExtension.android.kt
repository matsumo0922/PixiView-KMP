package me.matsumo.fanbox.core.ui.extensition

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Process
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import io.github.aakira.napier.Napier
import me.matsumo.fanbox.core.logs.category.NavigationLog
import me.matsumo.fanbox.core.logs.logger.send

class NavigatorExtensionImpl(
    private val context: Context,
) : NavigatorExtension {

    override fun navigateToWebPage(url: String, referrer: String) {
        runCatching {
            val intent = CustomTabsIntent.Builder()
                .setShowTitle(true)
                .build()

            intent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.intent.setPackage(getDefaultBrowserPackage(context))

            intent.launchUrl(context, url.toUri())
        }.recoverCatching {
            Napier.e(it) { "Failed to open the web page with custom tabs" }

            context.startActivity(
                Intent(Intent.ACTION_VIEW, url.toUri()).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    setPackage(getDefaultBrowserPackage(context))
                },
            )
        }.onFailure {
            Napier.e(it) { "Failed to open the web page" }
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

    private fun getDefaultBrowserPackage(context: Context): String? {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://www.example.com"))
        val resolveInfo = context.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)

        return resolveInfo?.activityInfo?.packageName
    }
}
