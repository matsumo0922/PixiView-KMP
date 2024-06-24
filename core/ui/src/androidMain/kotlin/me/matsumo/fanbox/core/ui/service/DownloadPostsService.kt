package me.matsumo.fanbox.core.ui.service

import android.app.Service
import android.content.Intent
import android.os.IBinder

class DownloadPostsService: Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }
}