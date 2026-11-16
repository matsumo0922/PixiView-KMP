package me.matsumo.fanbox.feature.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.app.NotificationCompat
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

    private val notifyConfig = NotificationConfigs.newPost
    private val group = NewContentNotificationGroup(
        context = context,
        config = notifyConfig,
        groupKey = "me.matsumo.fanbox.NEW_POST",
        tagPrefix = "new_post:",
    )

    /** 新着投稿 1 件の通知を組み立てる。サムネイルの取得と、通知チャンネルの作成を含む。[isSilent] が true なら、音と振動なしで表示する。 */
    suspend fun build(content: NewPostNotificationContent, isSilent: Boolean): Notification {
        group.createChannel(Res.string.notify_new_post_channel_name, Res.string.notify_new_post_channel_description)

        val contentIntent = contentIntent(
            postUrl = postUrlOf(content.creatorId, content.postId),
            postCount = 1,
            requestCode = content.postId.value.hashCode(),
        )

        return group.builder(contentIntent, isSilent)
            .setContentTitle(getString(Res.string.notify_new_post_title, content.creatorName))
            .setWhen(content.publishedAt.toEpochMilliseconds())
            .setShowWhen(true)
            .apply { if (content is NewPostNotificationContent.Visible) applyVisibleContent(content) }
            .build()
    }

    /** [build] で組み立てた [postId] の通知を表示し、表示したら true を返す。権限がないとき、通知チャンネルが無効のときは表示しない。 */
    fun show(postId: PostId, notification: Notification): Boolean = group.show(postId.value, notification)

    /** 表示中の投稿ごとの通知をまとめる、グループの親の通知を表示する。[shownPostIds] は直前に表示した投稿。 */
    suspend fun showSummary(shownPostIds: List<PostId>) {
        group.showSummary(
            shownItemIds = shownPostIds.map { it.value },
            title = Res.string.notify_new_post_multiple_title,
            contentIntentOf = { postCount ->
                contentIntent(
                    postUrl = null,
                    postCount = postCount,
                    requestCode = notifyConfig.notifyId,
                )
            },
        )
    }

    /** 通知を表示できるか。通知の権限があり、新着投稿の通知チャンネルが無効にされていなければ true を返す。チャンネルがまだなければ、有効として扱う。 */
    fun canNotify(): Boolean = group.canNotify()

    private suspend fun NotificationCompat.Builder.applyVisibleContent(content: NewPostNotificationContent.Visible) {
        setContentText(content.title)
        content.thumbnailUrl?.let { loadThumbnail(it) }?.let { applyThumbnail(it) }
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

    private fun contentIntent(
        postUrl: String?,
        postCount: Int,
        requestCode: Int,
    ): PendingIntent = group.launchIntent(requestCode) {
        if (postUrl != null) {
            action = Intent.ACTION_VIEW
            data = postUrl.toUri()
        }
        putExtra(EXTRA_NEW_POST_COUNT, postCount)
    }

    // 投稿詳細の URL はクリエイター ID を含むため、クリエイター ID がないときはホームを開く。
    private fun postUrlOf(creatorId: CreatorId?, postId: PostId): String? = creatorId?.let {
        "https://www.fanbox.cc/@${it.value}/posts/${postId.value}"
    }
}

/** 新着投稿の通知から開いた intent に付く、タップした通知の新着投稿の件数。投稿ごとの通知は 1、グループの親の通知はまとめた件数。 */
const val EXTRA_NEW_POST_COUNT = "me.matsumo.fanbox.extra.NEW_POST_COUNT"

/** 新着投稿の通知から開いた intent なら、タップした通知の新着投稿の件数を返し、同じ intent で二度数えないよう取り除く。 */
fun Intent.consumeNewPostNotificationTap(): Int? = consumeIntExtra(EXTRA_NEW_POST_COUNT)
