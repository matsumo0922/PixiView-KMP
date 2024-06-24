package me.matsumo.fanbox.core.ui.extensition

import androidx.compose.runtime.Composable
import kotlinx.cinterop.ExperimentalForeignApi
import me.matsumo.fanbox.core.logs.category.NavigationLog
import me.matsumo.fanbox.core.logs.logger.send
import platform.Foundation.NSSelectorFromString
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

class NavigatorExtensionImpl: NavigatorExtension {

    override fun navigateToWebPage(url: String, referrer: String) {
        NavigationLog.openUrl(
            url = url,
            referer = referrer,
            isSuccess = true
        ).send()

        UIApplication.sharedApplication.openURL(NSURL(string = url))
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun killApp() {
        UIApplication.sharedApplication.performSelector(
            aSelector = NSSelectorFromString("terminateWithSuccess"),
            withObject = null
        )
    }
}

@Composable
actual fun BackHandler(isEnable: Boolean, onBack: () -> Unit) {
    // Not supported on iOS
}