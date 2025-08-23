package me.matsumo.fanbox.core.model

expect val currentPlatform: Platform

enum class Platform {
    Android,
    IOS,
}
