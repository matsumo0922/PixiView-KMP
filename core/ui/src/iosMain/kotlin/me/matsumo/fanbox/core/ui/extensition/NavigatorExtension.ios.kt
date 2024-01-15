package me.matsumo.fanbox.core.ui.extensition

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSSelectorFromString
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

class NavigatorExtensionImpl: NavigatorExtension {

    override fun navigateToWebPage(url: String) {
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
