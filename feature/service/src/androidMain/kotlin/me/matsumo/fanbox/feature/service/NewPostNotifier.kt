package me.matsumo.fanbox.feature.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import coil3.SingletonImageLoader
import coil3.network.NetworkHeaders
import coil3.network.httpHeaders
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.toBitmap
import io.github.aakira.napier.Napier
import me.matsumo.fanbox.core.common.util.suspendRunCatching
import me.matsumo.fanbox.core.logs.category.NotificationsLog
import me.matsumo.fanbox.core.logs.logger.send
import me.matsumo.fanbox.core.model.NewPostNotificationContent
import me.matsumo.fanbox.core.model.NotificationConfigs
import me.matsumo.fanbox.core.model.fanbox.CreatorId
import me.matsumo.fanbox.core.model.fanbox.PostId
import me.matsumo.fanbox.core.resources.Res
import me.matsumo.fanbox.core.resources.notify_new_post_channel_description
import me.matsumo.fanbox.core.resources.notify_new_post_channel_name
import me.matsumo.fanbox.core.resources.notify_new_post_hidden_message
import me.matsumo.fanbox.core.resources.notify_new_post_multiple_title
import org.jetbrains.compose.resources.getString

/** 新着投稿の通知を表示する。通知のタップは、アプリの起動 Activity へ [EXTRA_NEW_POST_COUNT] 付きで届く。 */
internal class NewPostNotifier(private val context: Context) {

    private val manager = NotificationManagerCompat.from(context)
    private val notifyConfig = NotificationConfigs.newPost

    /** 表示する通知を組み立てる。サムネイルの取得と、通知チャンネルの作成を含む。 */
    suspend fun build(content: NewPostNotificationContent): Notification {
        createChannel()

        return buildNotification(content)
    }

    /** [build] で組み立てた通知を、表示できる状態なら表示する。権限がないとき、通知チャンネルが無効のときは何もしない。 */
    // 権限: 直前に areNotificationsEnabled で、Android 13 以上の通知の権限を含めて確かめている
    @SuppressLint("MissingPermission")
    fun show(notification: Notification, postCount: Int) {
        if (!canNotify()) return

        manager.notify(notifyConfig.notifyId, notification)
        NotificationsLog.newPostNotificationShown(postCount).send()
    }

    /** 通知を表示できるか。通知の権限があり、新着投稿の通知チャンネルが無効にされていなければ true を返す。チャンネルがまだなければ、有効として扱う。 */
    fun canNotify(): Boolean {
        val channel = manager.getNotificationChannel(notifyConfig.channelId)
        val isChannelBlocked = channel?.importance == NotificationManager.IMPORTANCE_NONE

        return manager.areNotificationsEnabled() && !isChannelBlocked
    }

    private suspend fun buildNotification(content: NewPostNotificationContent) = NotificationCompat.Builder(context, notifyConfig.channelId)
        .setSmallIcon(R.drawable.vec_app_icon_foreground)
        .setAutoCancel(true)
        .setCategory(NotificationCompat.CATEGORY_SOCIAL)
        .setContentIntent(contentIntent(content))
        .apply { applyContent(content) }
        .build()

    private suspend fun NotificationCompat.Builder.applyContent(content: NewPostNotificationContent) {
        when (content) {
            is NewPostNotificationContent.Single -> {
                setContentTitle(content.creatorName)
                setContentText(content.title)
                content.thumbnailUrl?.let { loadThumbnail(it) }?.let { applyThumbnail(it) }
            }

            is NewPostNotificationContent.SingleHidden -> {
                setContentTitle(getString(Res.string.notify_new_post_hidden_message, content.creatorName))
            }

            is NewPostNotificationContent.Multiple -> {
                setContentTitle(getString(Res.string.notify_new_post_multiple_title, content.postCount))
                setContentText(content.creatorNames.joinToString(", "))
            }
        }
    }

    private fun NotificationCompat.Builder.applyThumbnail(thumbnail: Bitmap) {
        setLargeIcon(thumbnail)
        setStyle(
            NotificationCompat.BigPictureStyle()
                .bigPicture(thumbnail)
                .bigLargeIcon(null as Bitmap?),
        )
    }

    // サムネイルを取得できなくても、サムネイルなしで通知する。
    private suspend fun loadThumbnail(url: String): Bitmap? {
        val request = ImageRequest.Builder(context)
            .data(url)
            .httpHeaders(
                NetworkHeaders.Builder()
                    .set("origin", "https://www.fanbox.cc")
                    .set("referer", "https://www.fanbox.cc")
                    .build(),
            )
            .allowHardware(false)
            .build()

        return suspendRunCatching { SingletonImageLoader.get(context).execute(request) }
            .onFailure { failure -> Napier.w(failure) { "Failed to load the new post thumbnail." } }
            .getOrNull()
            ?.let { result -> (result as? SuccessResult)?.image?.toBitmap() }
    }

    private fun contentIntent(content: NewPostNotificationContent): PendingIntent {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val postUrl = when (content) {
            is NewPostNotificationContent.Single -> postUrlOf(content.creatorId, content.postId)
            is NewPostNotificationContent.SingleHidden -> postUrlOf(content.creatorId, content.postId)
            is NewPostNotificationContent.Multiple -> null
        }

        // 起動 Activity を明示し、起動中なら onNewIntent で受け取らせる。暗黙の起動では、既存の画面が前面に出るだけで intent が届かない。
        val intent = Intent(if (postUrl != null) Intent.ACTION_VIEW else Intent.ACTION_MAIN)
            .setComponent(launchIntent?.component)
            .setData(postUrl?.toUri())
            .putExtra(EXTRA_NEW_POST_COUNT, content.postCount)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)

        return PendingIntent.getActivity(
            context,
            notifyConfig.notifyId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    // 投稿詳細の URL はクリエイター ID を含むため、クリエイター ID がないときはホームを開く。
    private fun postUrlOf(creatorId: CreatorId?, postId: PostId): String? = creatorId?.let {
        "https://www.fanbox.cc/@${it.value}/posts/${postId.value}"
    }

    private suspend fun createChannel() {
        if (manager.getNotificationChannel(notifyConfig.channelId) != null) return

        val channel = NotificationChannel(
            notifyConfig.channelId,
            getString(Res.string.notify_new_post_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = getString(Res.string.notify_new_post_channel_description)
        }

        manager.createNotificationChannel(channel)
    }
}

/** 新着投稿の通知から開いた intent に付く、通知にまとめた新着投稿の件数。 */
const val EXTRA_NEW_POST_COUNT = "me.matsumo.fanbox.extra.NEW_POST_COUNT"

/** 新着投稿の通知から開いた intent なら、通知にまとめた件数を返し、同じ intent で二度数えないよう取り除く。 */
fun Intent.consumeNewPostNotificationTap(): Int? {
    if (!hasExtra(EXTRA_NEW_POST_COUNT)) return null

    val postCount = getIntExtra(EXTRA_NEW_POST_COUNT, 0)
    removeExtra(EXTRA_NEW_POST_COUNT)

    return postCount
}
