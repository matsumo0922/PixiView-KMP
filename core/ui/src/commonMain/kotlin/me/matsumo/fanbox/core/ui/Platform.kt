package me.matsumo.fanbox.core.ui

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform