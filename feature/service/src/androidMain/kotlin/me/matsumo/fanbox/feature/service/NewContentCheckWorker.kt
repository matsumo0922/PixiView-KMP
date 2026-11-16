package me.matsumo.fanbox.feature.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.first
import me.matsumo.fanbox.core.common.PixiViewConfig
import me.matsumo.fanbox.core.common.util.suspendRunCatching
import me.matsumo.fanbox.core.datastore.NewContentCheckDataStore
import me.matsumo.fanbox.core.datastore.SettingDataStore
import me.matsumo.fanbox.core.datastore.cookie.MigratingFanboxCookieStorage
import me.matsumo.fanbox.core.logs.category.NotificationsLog
import me.matsumo.fanbox.core.logs.logger.LogConfigurator
import me.matsumo.fanbox.core.logs.logger.send
import me.matsumo.fanbox.core.model.NewContentCheckPrecondition
import me.matsumo.fanbox.core.model.NewContentDetection
import me.matsumo.fanbox.core.model.Setting
import me.matsumo.fanbox.core.model.detectNewMessages
import me.matsumo.fanbox.core.model.detectNewPosts
import me.matsumo.fanbox.core.model.fanbox.NewsLetter
import me.matsumo.fanbox.core.model.fanbox.NewsLetterId
import me.matsumo.fanbox.core.model.fanbox.Post
import me.matsumo.fanbox.core.model.fanbox.PostId
import me.matsumo.fanbox.core.model.resolveNewContentCheckConfig
import me.matsumo.fanbox.core.model.resolveNewContentCheckPrecondition
import me.matsumo.fanbox.core.model.resolveNewPostNotificationContent
import me.matsumo.fanbox.core.repository.FanboxRepository
import me.matsumo.fanbox.core.repository.hasStoredSession
import me.matsumo.fanbox.core.repository.paging.filterHomePosts
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * 支援中・フォロー中のクリエイターの新着投稿と、クリエイターからの新着メッセージを確かめ、通知する定期実行の処理。
 *
 * 新着投稿と新着メッセージは、設定・通知を表示できるか・遠隔の設定・取得の失敗を、それぞれ独立に扱う。片方が止まっても、もう片方は確かめる。
 * FANBOX への通信は、ホームの投稿一覧の先頭 1 ページの取得、新着投稿ごとのサムネイルの取得、メッセージ一覧の取得だけである。
 * どちらも通知を表示できないとき、保存済みのセッションがないときは、guest のロードを含めて通信しない。
 * 失敗しても再試行せず、次の定期実行を待つ。画面を一度も開いていないプロセスでも動く。
 */
@OptIn(ExperimentalTime::class)
class NewContentCheckWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params),
    KoinComponent {

    private val settingDataStore by inject<SettingDataStore>()
    private val newContentCheckDataStore by inject<NewContentCheckDataStore>()
    private val cookieStorage by inject<MigratingFanboxCookieStorage>()

    // 生成時に guest をロードして通信するため、取得に進むまで生成しない（by inject は最初の参照で生成する）。
    private val fanboxRepository by inject<FanboxRepository>()
    private val scheduler by inject<NewContentCheckScheduler>()
    private val pixiViewConfig by inject<PixiViewConfig>()
    private val postNotifier = NewPostNotifier(context)
    private val messageNotifier = NewMessageNotifier(context)

    override suspend fun doWork(): Result {
        suspendRunCatching { checkNewContents() }.onFailure { failure ->
            Napier.w(failure) { "Failed to check new contents." }
        }

        return Result.success()
    }

    private suspend fun checkNewContents() {
        // 購読のない SettingRepository.setting は既定値を返すため、保存済みの値を直接読む。
        val setting = settingDataStore.loadStoredSetting()

        // 画面を開いていないプロセスでは、ログの設定がまだない。Cookie の移行処理もログを送るため、最初に設定する。
        LogConfigurator.configure(pixiViewConfig, setting)

        val readyChecks = resolveReadyChecks(setting)
        if (readyChecks.isEmpty()) return

        val config = resolveNewContentCheckConfig(fanboxRepository.getGuestMetadata())
        val enabledChecks = readyChecks.filter { check ->
            when (check) {
                ContentCheck.POST -> config.isPostCheckEnabled
                ContentCheck.MESSAGE -> config.isMessageCheckEnabled
            }
        }
        if (enabledChecks.isEmpty()) return

        scheduler.updateInterval(config.intervalHours)

        // 片方の失敗が、もう片方の確認を止めないよう、確認ごとに失敗を受け止める。
        for (check in enabledChecks) {
            suspendRunCatching { runCheck(check, setting) }.onFailure { failure ->
                Napier.w(failure) { "Failed to check new contents. check=$check" }
            }
        }
    }

    /** 通信の前の判定で、遠隔の設定の確認に進める確認を返す。通知を表示できない確認は、記録を消す。 */
    private suspend fun resolveReadyChecks(setting: Setting): List<ContentCheck> {
        // FanboxRepository は生成時に guest をロードして通信するため、ここでは Cookie の保存先を直接読む。
        val hasStoredSession = cookieStorage.hasStoredSession()

        return ContentCheck.entries.filter { check ->
            val precondition = resolveNewContentCheckPrecondition(
                isNotificationEnabled = check.isNotificationEnabled(setting),
                isTestUser = setting.isTestUser,
                canNotify = check.canNotify(),
                hasStoredSession = hasStoredSession,
            )

            if (precondition == NewContentCheckPrecondition.CLEAR_RECORD_AND_SKIP) clearRecord(check)

            precondition == NewContentCheckPrecondition.PROCEED
        }
    }

    private fun ContentCheck.isNotificationEnabled(setting: Setting): Boolean = when (this) {
        ContentCheck.POST -> setting.isNewPostNotificationEnabled
        ContentCheck.MESSAGE -> setting.isNewMessageNotificationEnabled
    }

    private fun ContentCheck.canNotify(): Boolean = when (this) {
        ContentCheck.POST -> postNotifier.canNotify()
        ContentCheck.MESSAGE -> messageNotifier.canNotify()
    }

    private suspend fun clearRecord(check: ContentCheck) = when (check) {
        ContentCheck.POST -> newContentCheckDataStore.clearLastSeenPublishedAt()
        ContentCheck.MESSAGE -> newContentCheckDataStore.clearLastSeenMessageCreatedAt()
    }

    /**
     * [check] の確認の記録を [latestCreatedAt] にする。
     *
     * 確認の途中で設定が OFF にされると、Scheduler が消した記録をこの確認が書き戻し、次に ON にしたときの確認が初回にならない。
     * これを避けるため、保存の直前に保存済みの設定を読み直し、OFF になっていれば保存しない。
     */
    private suspend fun saveRecord(check: ContentCheck, latestCreatedAt: Instant) {
        if (!check.isNotificationEnabled(settingDataStore.loadStoredSetting())) return

        when (check) {
            ContentCheck.POST -> newContentCheckDataStore.setLastSeenPublishedAt(latestCreatedAt)
            ContentCheck.MESSAGE -> newContentCheckDataStore.setLastSeenMessageCreatedAt(latestCreatedAt)
        }
    }

    private suspend fun runCheck(check: ContentCheck, setting: Setting) = when (check) {
        ContentCheck.POST -> checkNewPosts(setting)
        ContentCheck.MESSAGE -> checkNewMessages()
    }

    private suspend fun checkNewPosts(setting: Setting) {
        val posts = fetchHomePosts(setting) ?: return
        val detection = detectNewPosts(posts, newContentCheckDataStore.getLastSeenPublishedAt())

        when (detection) {
            is NewContentDetection.FirstCheck -> saveRecord(ContentCheck.POST, detection.latestCreatedAt)
            is NewContentDetection.Found -> notifyNewPosts(detection, setting)
            NewContentDetection.NotFound -> Unit
        }
    }

    // セッション切れの 401 を含め、取得に失敗したときは通知せずに終える。画面へは認証切れを伝えない。
    private suspend fun fetchHomePosts(setting: Setting): List<Post>? {
        val page = suspendRunCatching { fanboxRepository.getHomePostsInBackground(cursor = null) }.getOrElse { failure ->
            Napier.w(failure) { "Failed to fetch home posts for the new post check." }
            return null
        }

        return filterHomePosts(
            posts = page.contents,
            isHideRestricted = setting.isHideRestricted,
            blockedCreators = fanboxRepository.blockedCreators.first(),
        )
    }

    private suspend fun notifyNewPosts(detection: NewContentDetection.Found<Post>, setting: Setting) {
        // 新しい順に表示し、音と振動は最初の 1 件だけにする。1 回の確認で、新着の件数だけ鳴らないようにするため。
        val shownPostIds = detection.newContents.mapIndexedNotNull { index, post ->
            showNewPost(post, setting, isSilent = index > 0)
        }

        // 表示し終えてから記録する。途中で中断されたときは次の確認で全件を表示し直すが、同じ投稿の通知は置き換えるため重ならない。
        saveRecord(ContentCheck.POST, detection.latestCreatedAt)

        if (shownPostIds.isEmpty()) return

        postNotifier.showSummary(shownPostIds)
        NotificationsLog.newPostNotificationShown(shownPostIds.size).send()
    }

    private suspend fun showNewPost(
        post: Post,
        setting: Setting,
        isSilent: Boolean,
    ): PostId? {
        val content = resolveNewPostNotificationContent(post, setting.isHideAdultContentsInNotification)
        val notification = postNotifier.build(content, isSilent)

        return post.id.takeIf { postNotifier.show(post.id, notification) }
    }

    private suspend fun checkNewMessages() {
        // セッション切れの 401 を含め、取得に失敗したときは通知せずに終える。画面へは認証切れを伝えない。
        val messages = suspendRunCatching { fanboxRepository.getNewsLettersInBackground() }.getOrElse { failure ->
            Napier.w(failure) { "Failed to fetch messages for the new message check." }
            return
        }
        val detection = detectNewMessages(
            messages = messages,
            lastSeenCreatedAt = newContentCheckDataStore.getLastSeenMessageCreatedAt(),
            blockedCreatorIds = fanboxRepository.blockedCreators.first(),
            checkedAt = Clock.System.now(),
        )

        when (detection) {
            is NewContentDetection.FirstCheck -> saveRecord(ContentCheck.MESSAGE, detection.latestCreatedAt)
            is NewContentDetection.Found -> notifyNewMessages(detection)
            NewContentDetection.NotFound -> Unit
        }
    }

    private suspend fun notifyNewMessages(detection: NewContentDetection.Found<NewsLetter>) {
        // 新しい順に表示し、音と振動は最初の 1 件だけにする。1 回の確認で、新着の件数だけ鳴らないようにするため。
        val shownMessageIds = detection.newContents.mapIndexedNotNull { index, message ->
            showNewMessage(message, isSilent = index > 0)
        }

        // 表示し終えてから記録する。途中で中断されたときは次の確認で全件を表示し直すが、同じメッセージの通知は置き換えるため重ならない。
        saveRecord(ContentCheck.MESSAGE, detection.latestCreatedAt)

        if (shownMessageIds.isEmpty()) return

        messageNotifier.showSummary(shownMessageIds)
        NotificationsLog.newMessageShown(shownMessageIds.size).send()
    }

    private suspend fun showNewMessage(message: NewsLetter, isSilent: Boolean): NewsLetterId? {
        val notification = messageNotifier.build(message, isSilent)

        return message.id.takeIf { messageNotifier.show(message.id, notification) }
    }

    /** 定期実行の中で、独立に判定・取得・通知する確認の種類。 */
    private enum class ContentCheck {
        POST,
        MESSAGE,
    }
}
