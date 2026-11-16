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
import me.matsumo.fanbox.core.model.NewPostNotificationContent
import me.matsumo.fanbox.core.model.NotificationConfigs
import me.matsumo.fanbox.core.model.fanbox.CreatorId
import me.matsumo.fanbox.core.model.fanbox.PostId
import me.matsumo.fanbox.core.resources.Res
import me.matsumo.fanbox.core.resources.notify_new_post_channel_description
import me.matsumo.fanbox.core.resources.notify_new_post_channel_name
import me.matsumo.fanbox.core.resources.notify_new_post_multiple_title
import me.matsumo.fanbox.core.resources.notify_new_post_title
import org.jetbrains.compose.resources.getString
import kotlin.time.ExperimentalTime

/**
 * 新着投稿の通知を、投稿ごとに表示する。投稿ごとの通知は、グループの親の通知の下に OS がまとめる。
 *
 * 通知のタップは、アプリの起動 Activity へ [EXTRA_NEW_POST_COUNT] 付きで届く。投稿ごとの通知は投稿詳細を、グループの親の通知はアプリを開く。
 */
@OptIn(ExperimentalTime::class)
internal class NewPostNotifier(private val context: Context) {

    private val manager = NotificationManagerCompat.from(context)
    private val notifyConfig = NotificationConfigs.newPost

    /** 新着投稿 1 件の通知を組み立てる。サムネイルの取得と、通知チャンネルの作成を含む。[isSilent] が true なら、音と振動なしで表示する。 */
    suspend fun build(content: NewPostNotificationContent, isSilent: Boolean): Notification {
        createChannel()

        val contentIntent = contentIntent(
            postUrl = postUrlOf(content.creatorId, content.postId),
            postCount = 1,
            requestCode = content.postId.value.hashCode(),
        )

        return NotificationCompat.Builder(context, notifyConfig.channelId)
            .applyCommon(contentIntent)
            .setContentTitle(getString(Res.string.notify_new_post_title, content.creatorName))
            .setWhen(content.publishedAt.toEpochMilliseconds())
            .setShowWhen(true)
            .setSilent(isSilent)
            .apply { if (content is NewPostNotificationContent.Visible) applyVisibleContent(content) }
            .build()
    }

    /** [build] で組み立てた [postId] の通知を表示し、表示したら true を返す。権限がないとき、通知チャンネルが無効のときは表示しない。 */
    // 権限: 直前に areNotificationsEnabled で、Android 13 以上の通知の権限を含めて確かめている
    @SuppressLint("MissingPermission")
    fun show(postId: PostId, notification: Notification): Boolean {
        if (!canNotify()) return false

        // 投稿ごとに tag を分け、同じ投稿をもう一度表示したときは、表示中の通知を置き換える。
        manager.notify(postTagOf(postId), notifyConfig.notifyId, notification)

        return true
    }

    /** 表示中の投稿ごとの通知をまとめる、グループの親の通知を表示する。[shownPostIds] は直前に表示した投稿。 */
    // 権限: 直前に areNotificationsEnabled で、Android 13 以上の通知の権限を含めて確かめている
    @SuppressLint("MissingPermission")
    suspend fun showSummary(shownPostIds: List<PostId>) {
        if (!canNotify()) return

        // 表示の要求は非同期に処理されるため、直前に表示した通知は、表示中の通知の一覧にまだないことがある。
        val postCount = (activePostTags() + shownPostIds.map { postTagOf(it) }).size
        val contentIntent = contentIntent(
            postUrl = null,
            postCount = postCount,
            requestCode = notifyConfig.notifyId,
        )
        val notification = NotificationCompat.Builder(context, notifyConfig.channelId)
            .applyCommon(contentIntent)
            .setContentTitle(getString(Res.string.notify_new_post_multiple_title, postCount))
            .setGroupSummary(true)
            .setSilent(true)
            .build()

        manager.notify(notifyConfig.notifyId, notification)
    }

    /** 通知を表示できるか。通知の権限があり、新着投稿の通知チャンネルが無効にされていなければ true を返す。チャンネルがまだなければ、有効として扱う。 */
    fun canNotify(): Boolean {
        val channel = manager.getNotificationChannel(notifyConfig.channelId)
        val isChannelBlocked = channel?.importance == NotificationManager.IMPORTANCE_NONE

        return manager.areNotificationsEnabled() && !isChannelBlocked
    }

    private fun NotificationCompat.Builder.applyCommon(contentIntent: PendingIntent) = this
        .setSmallIcon(R.drawable.vec_app_icon_foreground)
        .setAutoCancel(true)
        .setCategory(NotificationCompat.CATEGORY_SOCIAL)
        .setGroup("me.matsumo.fanbox.NEW_POST")
        .setContentIntent(contentIntent)

    private suspend fun NotificationCompat.Builder.applyVisibleContent(content: NewPostNotificationContent.Visible) {
        setContentText(content.title)
        content.thumbnailUrl?.let { loadThumbnail(it) }?.let { applyThumbnail(it) }
    }

    private fun activePostTags(): Set<String> = manager.activeNotifications
        .filter { it.id == notifyConfig.notifyId && it.tag?.startsWith(POST_TAG_PREFIX) == true }
        .mapNotNull { it.tag }
        .toSet()

    private fun postTagOf(postId: PostId): String = POST_TAG_PREFIX + postId.value

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

    private fun contentIntent(
        postUrl: String?,
        postCount: Int,
        requestCode: Int,
    ): PendingIntent {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)

        // 起動 Activity を明示し、起動中なら onNewIntent で受け取らせる。暗黙の起動では、既存の画面が前面に出るだけで intent が届かない。
        val intent = Intent(if (postUrl != null) Intent.ACTION_VIEW else Intent.ACTION_MAIN)
            .setComponent(launchIntent?.component)
            .setData(postUrl?.toUri())
            .putExtra(EXTRA_NEW_POST_COUNT, postCount)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)

        return PendingIntent.getActivity(
            context,
            requestCode,
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

    private companion object {
        // 表示中の通知から、投稿ごとの通知を見分けるための tag の接頭辞。
        const val POST_TAG_PREFIX = "new_post:"
    }
}

/** 新着投稿の通知から開いた intent に付く、タップした通知の新着投稿の件数。投稿ごとの通知は 1、グループの親の通知はまとめた件数。 */
const val EXTRA_NEW_POST_COUNT = "me.matsumo.fanbox.extra.NEW_POST_COUNT"

/** 新着投稿の通知から開いた intent なら、タップした通知の新着投稿の件数を返し、同じ intent で二度数えないよう取り除く。 */
fun Intent.consumeNewPostNotificationTap(): Int? {
    if (!hasExtra(EXTRA_NEW_POST_COUNT)) return null

    val postCount = getIntExtra(EXTRA_NEW_POST_COUNT, 0)
    removeExtra(EXTRA_NEW_POST_COUNT)

    return postCount
}
