package me.matsumo.fanbox

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
