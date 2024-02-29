package me.matsumo.fanbox.core.ui

import me.matsumo.fanbox.core.ui.extensition.Platform
import me.matsumo.fanbox.core.ui.extensition.currentPlatform

val appName: String
    get() {
        if (currentPlatform == Platform.Android) {
            return "FANBOX Viewer"
        } else {
            return "PixiView"
        }
    }
