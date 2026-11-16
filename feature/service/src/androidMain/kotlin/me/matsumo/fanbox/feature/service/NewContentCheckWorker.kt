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
import me.matsumo.fanbox.core.model.NewContentType
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
        // 確認の途中で記録が消されたら、この確認の結果を記録しないよう、始める前の記録の世代を控える。
        val generations = NewContentType.entries.associateWith { newContentCheckDataStore.getRecordGeneration(it) }

        // 購読のない SettingRepository.setting は既定値を返すため、保存済みの値を直接読む。
        val setting = settingDataStore.loadStoredSetting()

        // 画面を開いていないプロセスでは、ログの設定がまだない。Cookie の移行処理もログを送るため、最初に設定する。
        LogConfigurator.configure(pixiViewConfig, setting)

        val readyTypes = resolveReadyTypes(setting)
        if (readyTypes.isEmpty()) return

        val config = resolveNewContentCheckConfig(fanboxRepository.getGuestMetadata())
        val enabledTypes = readyTypes.filter { type ->
            when (type) {
                NewContentType.POST -> config.isPostCheckEnabled
                NewContentType.MESSAGE -> config.isMessageCheckEnabled
            }
        }
        if (enabledTypes.isEmpty()) return

        scheduler.updateInterval(config.intervalHours)

        // 片方の失敗が、もう片方の確認を止めないよう、確認ごとに失敗を受け止める。
        for (type in enabledTypes) {
            suspendRunCatching { runCheck(type, setting, generations.getValue(type)) }.onFailure { failure ->
                Napier.w(failure) { "Failed to check new contents. type=$type" }
            }
        }
    }

    /** 通信の前の判定で、遠隔の設定の確認に進める確認を返す。通知を表示できない確認は、記録を消す。 */
    private suspend fun resolveReadyTypes(setting: Setting): List<NewContentType> {
        // FanboxRepository は生成時に guest をロードして通信するため、ここでは Cookie の保存先を直接読む。
        val hasStoredSession = cookieStorage.hasStoredSession()

        return NewContentType.entries.filter { type ->
            val precondition = resolveNewContentCheckPrecondition(
                isNotificationEnabled = type.isNotificationEnabled(setting),
                isTestUser = setting.isTestUser,
                canNotify = type.canNotify(),
                hasStoredSession = hasStoredSession,
            )

            if (precondition == NewContentCheckPrecondition.CLEAR_RECORD_AND_SKIP) newContentCheckDataStore.clearLastSeenCreatedAt(type)

            precondition == NewContentCheckPrecondition.PROCEED
        }
    }

    private fun NewContentType.isNotificationEnabled(setting: Setting): Boolean = when (this) {
        NewContentType.POST -> setting.isNewPostNotificationEnabled
        NewContentType.MESSAGE -> setting.isNewMessageNotificationEnabled
    }

    private fun NewContentType.canNotify(): Boolean = when (this) {
        NewContentType.POST -> postNotifier.canNotify()
        NewContentType.MESSAGE -> messageNotifier.canNotify()
    }

    /**
     * [type] の確認の記録を [latestCreatedAt] にする。
     *
     * 確認の途中で設定が OFF にされると、Scheduler が記録を消す。消された記録をこの確認が書き戻すと、次に ON にしたときの確認が初回にならない。
     * これを避けるため、確認を始める前の記録の世代 [generation] から変わっていれば保存しない。
     */
    private suspend fun saveRecord(
        type: NewContentType,
        latestCreatedAt: Instant,
        generation: Int,
    ) {
        newContentCheckDataStore.setLastSeenCreatedAtIfGeneration(type, latestCreatedAt, generation)
    }

    private suspend fun runCheck(
        type: NewContentType,
        setting: Setting,
        generation: Int,
    ) = when (type) {
        NewContentType.POST -> checkNewPosts(setting, generation)
        NewContentType.MESSAGE -> checkNewMessages(generation)
    }

    private suspend fun checkNewPosts(setting: Setting, generation: Int) {
        val posts = fetchHomePosts(setting) ?: return
        val detection = detectNewPosts(posts, newContentCheckDataStore.getLastSeenCreatedAt(NewContentType.POST))

        when (detection) {
            is NewContentDetection.FirstCheck -> saveRecord(NewContentType.POST, detection.latestCreatedAt, generation)
            is NewContentDetection.Found -> notifyNewPosts(detection, setting, generation)
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

    private suspend fun notifyNewPosts(
        detection: NewContentDetection.Found<Post>,
        setting: Setting,
        generation: Int,
    ) {
        // 新しい順に表示し、音と振動は最初の 1 件だけにする。1 回の確認で、新着の件数だけ鳴らないようにするため。
        val shownPostIds = detection.newContents.mapIndexedNotNull { index, post ->
            showNewPost(post, setting, isSilent = index > 0)
        }

        // 表示し終えてから記録する。途中で中断されたときは次の確認で全件を表示し直すが、同じ投稿の通知は置き換えるため重ならない。
        saveRecord(NewContentType.POST, detection.latestCreatedAt, generation)

        if (shownPostIds.isEmpty()) return

        postNotifier.showSummary(shownPostIds)
        NotificationsLog.newPostShown(shownPostIds.size).send()
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

    private suspend fun checkNewMessages(generation: Int) {
        // 初回にメッセージがないときの記録に使う。取得の途中で届いたメッセージを、初回の記録より前にしないよう、取得の前に控える。
        val checkedAt = Clock.System.now()

        // セッション切れの 401 を含め、取得に失敗したときは通知せずに終える。画面へは認証切れを伝えない。
        val messages = suspendRunCatching { fanboxRepository.getNewsLettersInBackground() }.getOrElse { failure ->
            Napier.w(failure) { "Failed to fetch messages for the new message check." }
            return
        }
        val detection = detectNewMessages(
            messages = messages,
            lastSeenCreatedAt = newContentCheckDataStore.getLastSeenCreatedAt(NewContentType.MESSAGE),
            blockedCreatorIds = fanboxRepository.blockedCreators.first(),
            checkedAt = checkedAt,
        )

        when (detection) {
            is NewContentDetection.FirstCheck -> saveRecord(NewContentType.MESSAGE, detection.latestCreatedAt, generation)
            is NewContentDetection.Found -> notifyNewMessages(detection, generation)
            NewContentDetection.NotFound -> Unit
        }
    }

    private suspend fun notifyNewMessages(detection: NewContentDetection.Found<NewsLetter>, generation: Int) {
        // 新しい順に表示し、音と振動は最初の 1 件だけにする。1 回の確認で、新着の件数だけ鳴らないようにするため。
        val shownMessageIds = detection.newContents.mapIndexedNotNull { index, message ->
            showNewMessage(message, isSilent = index > 0)
        }

        // 表示し終えてから記録する。途中で中断されたときは次の確認で全件を表示し直すが、同じメッセージの通知は置き換えるため重ならない。
        saveRecord(NewContentType.MESSAGE, detection.latestCreatedAt, generation)

        if (shownMessageIds.isEmpty()) return

        messageNotifier.showSummary(shownMessageIds)
        NotificationsLog.newMessageShown(shownMessageIds.size).send()
    }

    private suspend fun showNewMessage(message: NewsLetter, isSilent: Boolean): NewsLetterId? {
        val notification = messageNotifier.build(message, isSilent)

        return message.id.takeIf { messageNotifier.show(message.id, notification) }
    }
}
