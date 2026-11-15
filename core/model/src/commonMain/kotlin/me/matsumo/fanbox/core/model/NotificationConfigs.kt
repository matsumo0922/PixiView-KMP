package me.matsumo.fanbox.core.model

object NotificationConfigs {
    val download = NotificationConfig(
        serviceId = 1,
        notifyId = 1,
        channelId = "postDownload",
    )
    val firebase = NotificationConfig(
        serviceId = 2,
        notifyId = 2,
        channelId = "firebase",
    )
    val newPost = NotificationConfig(
        serviceId = 3,
        notifyId = 3,
        channelId = "newPost",
    )
}

data class NotificationConfig(
    val serviceId: Int,
    val notifyId: Int,
    val channelId: String,
)
