package me.matsumo.fanbox.core.ui.extensition

interface NavigatorExtension {
    fun navigateToWebPage(url: String, referrer: String)
    fun killApp()
}
