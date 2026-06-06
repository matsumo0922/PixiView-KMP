package me.matsumo.fanbox.feature.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.model.DownloadState
import me.matsumo.fanbox.core.model.NotificationConfigs
import me.matsumo.fanbox.core.repository.DownloadPostsRepository
import me.matsumo.fanbox.core.resources.Res
import me.matsumo.fanbox.core.resources.notify_download_channel_description
import me.matsumo.fanbox.core.resources.notify_download_channel_name
import me.matsumo.fanbox.core.resources.post_detail_downloading
import org.jetbrains.compose.resources.getString
import org.koin.android.ext.android.inject

class DownloadPostService : Service() {

    private val downloadPostsRepository by inject<DownloadPostsRepository>()
    private val downloadState = downloadPostsRepository.downloadState
    private val reservingPosts = downloadPostsRepository.reservingPosts

    private val manager by lazy { baseContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }
    private val notifyConfig = NotificationConfigs.download

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var isForeground = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()

        scope.launch {
            createNotifyChannel()

            downloadState.collectLatest {
                if (it is DownloadState.Downloading) {
                    setForegroundService(
                        isForeground = true,
                        title = getString(Res.string.post_detail_downloading, reservingPosts.value.size),
                        message = it.items.title,
                        subMessage = "${(it.progress * 100).toInt()} %",
                        progress = it.progress,
                    )
                } else if (reservingPosts.value.isEmpty()) {
                    delay(3000)
                    setForegroundService(false)
                }
            }
        }
    }

    private fun setForegroundService(
        isForeground: Boolean,
        title: String = "",
        message: String = "",
        subMessage: String = "",
        progress: Float = 1f,
    ) {
        if (isForeground) {
            if (!this.isForeground) {
                Napier.d { "DownloadPostService start" }

                runCatching {
                    startForeground(notifyConfig.notifyId, createNotify(baseContext, title, message, subMessage, progress))
                }.onFailure {
                    Napier.e(it) { "Failed to start foreground service. $title, $message" }
                }
            } else {
                manager.notify(notifyConfig.notifyId, createNotify(baseContext, title, message, subMessage, progress))
            }
        } else {
            Napier.d { "DownloadPostService stop" }
            stopForeground(STOP_FOREGROUND_REMOVE)
        }

        this.isForeground = isForeground
    }

    private fun createNotify(context: Context, title: String, message: String, subMessage: String, progress: Float): Notification {
        return NotificationCompat.Builder(context, notifyConfig.channelId)
            .setSmallIcon(R.drawable.vec_app_icon_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setSubText(subMessage)
            .setAutoCancel(false)
            .setColorized(true)
            .setProgress(100, (progress * 100).toInt(), false)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
    }

    private suspend fun createNotifyChannel() {
        if (manager.getNotificationChannel(notifyConfig.channelId) != null) return

        val channelName = getString(Res.string.notify_download_channel_name)
        val channelDescription = getString(Res.string.notify_download_channel_description)

        val channel = NotificationChannel(
            notifyConfig.channelId,
            channelName,
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = channelDescription
        }

        manager.createNotificationChannel(channel)
    }
}
