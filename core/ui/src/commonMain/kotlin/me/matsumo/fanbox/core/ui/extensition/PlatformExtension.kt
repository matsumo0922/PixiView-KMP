package me.matsumo.fanbox.core.ui.extensition

enum class Platform {
    Android,
    Desktop,
    IOS,
    Web,
}

expect val currentPlatform: Platform
