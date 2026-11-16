package me.matsumo.fanbox.feature.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import me.matsumo.fanbox.core.model.NotificationConfigs
import me.matsumo.fanbox.core.model.fanbox.NewsLetter
import me.matsumo.fanbox.core.model.fanbox.NewsLetterId
import me.matsumo.fanbox.core.resources.Res
import me.matsumo.fanbox.core.resources.notify_new_message_channel_description
import me.matsumo.fanbox.core.resources.notify_new_message_channel_name
import me.matsumo.fanbox.core.resources.notify_new_message_multiple_title
import me.matsumo.fanbox.core.resources.notify_new_message_title
import org.jetbrains.compose.resources.getString
import kotlin.time.ExperimentalTime

/**
 * 新着メッセージの通知を、メッセージごとに表示する。メッセージごとの通知は、新着投稿とは別のグループの親の通知の下に OS がまとめる。
 *
 * 通知のタップは、アプリの起動 Activity へ [EXTRA_NEW_MESSAGE_COUNT] と [EXTRA_OPEN_LIBRARY_MESSAGE] 付きで届き、ライブラリの「メッセージ」タブを開く。
 * メッセージごとの通知も、グループの親の通知も、同じ遷移先にする。
 */
@OptIn(ExperimentalTime::class)
internal class NewMessageNotifier(context: Context) {

    private val notifyConfig = NotificationConfigs.newMessage
    private val group = NewContentNotificationGroup(
        context = context,
        config = notifyConfig,
        groupKey = "me.matsumo.fanbox.NEW_MESSAGE",
        tagPrefix = "new_message:",
    )

    /** 新着メッセージ 1 件の通知を組み立てる。通知チャンネルの作成を含む。[isSilent] が true なら、音と振動なしで表示する。 */
    suspend fun build(message: NewsLetter, isSilent: Boolean): Notification {
        group.createChannel(Res.string.notify_new_message_channel_name, Res.string.notify_new_message_channel_description)

        val contentIntent = contentIntent(
            messageCount = 1,
            requestCode = message.id.value.hashCode(),
        )
        val creatorName = message.creator.user?.name.orEmpty()

        // 展開したときに本文の全体を読めるよう、BigTextStyle で表示する。
        return group.builder(contentIntent, isSilent)
            .setContentTitle(getString(Res.string.notify_new_message_title, creatorName))
            .setContentText(message.body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message.body))
            .setWhen(message.createdAt.toEpochMilliseconds())
            .setShowWhen(true)
            .build()
    }

    /** [build] で組み立てた [messageId] の通知を表示し、表示したら true を返す。権限がないとき、通知チャンネルが無効のときは表示しない。 */
    fun show(messageId: NewsLetterId, notification: Notification): Boolean = group.show(messageId.value, notification)

    /** 表示中のメッセージごとの通知をまとめる、グループの親の通知を表示する。[shownMessageIds] は直前に表示したメッセージ。 */
    suspend fun showSummary(shownMessageIds: List<NewsLetterId>) {
        group.showSummary(
            shownItemIds = shownMessageIds.map { it.value },
            title = Res.string.notify_new_message_multiple_title,
            contentIntentOf = { messageCount -> contentIntent(messageCount, notifyConfig.notifyId) },
        )
    }

    /** 通知を表示できるか。通知の権限があり、新着メッセージの通知チャンネルが無効にされていなければ true を返す。チャンネルがまだなければ、有効として扱う。 */
    fun canNotify(): Boolean = group.canNotify()

    private fun contentIntent(messageCount: Int, requestCode: Int): PendingIntent = group.launchIntent(requestCode) {
        putExtra(EXTRA_NEW_MESSAGE_COUNT, messageCount)
        putExtra(EXTRA_OPEN_LIBRARY_MESSAGE, true)
    }
}

/** 新着メッセージの通知から開いた intent に付く、タップした通知の新着メッセージの件数。メッセージごとの通知は 1、グループの親の通知はまとめた件数。 */
const val EXTRA_NEW_MESSAGE_COUNT = "me.matsumo.fanbox.extra.NEW_MESSAGE_COUNT"

/** 新着メッセージの通知から開いた intent に付く、ライブラリの「メッセージ」タブを開くことを表す値。 */
const val EXTRA_OPEN_LIBRARY_MESSAGE = "me.matsumo.fanbox.extra.OPEN_LIBRARY_MESSAGE"

/** 新着メッセージの通知から開いた intent なら、タップした通知の新着メッセージの件数を返し、同じ intent で二度数えないよう取り除く。 */
fun Intent.consumeNewMessageNotificationTap(): Int? = consumeIntExtra(EXTRA_NEW_MESSAGE_COUNT)
