package me.matsumo.fanbox.core.ui.extensition

import platform.Foundation.NSURL
import platform.UIKit.UIApplication

class NavigatorExtensionImpl: NavigatorExtension {

    override fun navigateToWebPage(url: String) {
        UIApplication.sharedApplication.openURL(NSURL(string = url))
    }
}
