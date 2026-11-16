package me.matsumo.fanbox.feature.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import me.matsumo.fanbox.core.model.NotificationConfig
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

/**
 * 新着の通知を 1 件ずつ表示し、グループの親の通知の下に OS がまとめる。
 *
 * 新着投稿と新着メッセージは、それぞれ別の [config]（通知チャンネル、通知 ID）、[groupKey]、[tagPrefix] で使う。
 * 1 件ごとの通知は「[tagPrefix] + 対象の ID」を tag にし、同じ対象をもう一度表示したときは表示中の通知を置き換える。
 */
internal class NewContentNotificationGroup(
    private val context: Context,
    private val config: NotificationConfig,
    private val groupKey: String,
    private val tagPrefix: String,
) {
    private val manager = NotificationManagerCompat.from(context)

    /** 通知を表示できるか。通知の権限があり、[config] の通知チャンネルが無効にされていなければ true を返す。チャンネルがまだなければ、有効として扱う。 */
    fun canNotify(): Boolean {
        val channel = manager.getNotificationChannel(config.channelId)
        val isChannelBlocked = channel?.importance == NotificationManager.IMPORTANCE_NONE

        return manager.areNotificationsEnabled() && !isChannelBlocked
    }

    /** [config] の通知チャンネルがなければ作る。 */
    suspend fun createChannel(name: StringResource, description: StringResource) {
        if (manager.getNotificationChannel(config.channelId) != null) return

        val channel = NotificationChannel(
            config.channelId,
            getString(name),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            this.description = getString(description)
        }

        manager.createNotificationChannel(channel)
    }

    /** グループに属する通知の Builder を作る。タップで [contentIntent] を開き、自動で消える。[isSilent] が true なら、音と振動なしで表示する。 */
    fun builder(contentIntent: PendingIntent, isSilent: Boolean): NotificationCompat.Builder = NotificationCompat.Builder(context, config.channelId)
        .setSmallIcon(R.drawable.vec_app_icon_foreground)
        .setAutoCancel(true)
        .setCategory(NotificationCompat.CATEGORY_SOCIAL)
        .setGroup(groupKey)
        .setContentIntent(contentIntent)
        .setSilent(isSilent)

    /** [itemId] の通知を表示し、表示したら true を返す。権限がないとき、通知チャンネルが無効のときは表示しない。 */
    // 権限: 直前に areNotificationsEnabled で、Android 13 以上の通知の権限を含めて確かめている
    @SuppressLint("MissingPermission")
    fun show(itemId: String, notification: Notification): Boolean {
        if (!canNotify()) return false

        manager.notify(tagPrefix + itemId, config.notifyId, notification)

        return true
    }

    /**
     * 表示中の 1 件ごとの通知をまとめる、グループの親の通知を表示する。
     *
     * [shownItemIds] は直前に表示した対象。[title] には表示中の件数を渡し、[contentIntentOf] は表示中の件数からタップで開く intent を作る。
     */
    // 権限: 直前に areNotificationsEnabled で、Android 13 以上の通知の権限を含めて確かめている
    @SuppressLint("MissingPermission")
    suspend fun showSummary(
        shownItemIds: List<String>,
        title: StringResource,
        contentIntentOf: (Int) -> PendingIntent,
    ) {
        if (!canNotify()) return

        // 表示の要求は非同期に処理されるため、直前に表示した通知は、表示中の通知の一覧にまだないことがある。
        val itemCount = (activeItemTags() + shownItemIds.map { tagPrefix + it }).size
        val notification = builder(contentIntentOf(itemCount), isSilent = true)
            .setContentTitle(getString(title, itemCount))
            .setGroupSummary(true)
            .build()

        manager.notify(config.notifyId, notification)
    }

    /** アプリの起動 Activity を開く [PendingIntent] を作る。[configure] で、遷移先や計測に使う値を intent に足す。 */
    fun launchIntent(requestCode: Int, configure: Intent.() -> Unit): PendingIntent {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)

        // 起動 Activity を明示し、起動中なら onNewIntent で受け取らせる。暗黙の起動では、既存の画面が前面に出るだけで intent が届かない。
        val intent = Intent(Intent.ACTION_MAIN)
            .setComponent(launchIntent?.component)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            .apply(configure)

        return PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun activeItemTags(): Set<String> = manager.activeNotifications
        .filter { it.id == config.notifyId && it.tag?.startsWith(tagPrefix) == true }
        .mapNotNull { it.tag }
        .toSet()
}

/** [name] の int の extra があれば値を返し、同じ intent で二度数えないよう取り除く。なければ null を返す。 */
internal fun Intent.consumeIntExtra(name: String): Int? {
    if (!hasExtra(name)) return null

    val value = getIntExtra(name, 0)
    removeExtra(name)

    return value
}
