package me.matsumo.fanbox.feature.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.first
import me.matsumo.fanbox.core.common.PixiViewConfig
import me.matsumo.fanbox.core.common.util.suspendRunCatching
import me.matsumo.fanbox.core.datastore.NewPostCheckDataStore
import me.matsumo.fanbox.core.datastore.SettingDataStore
import me.matsumo.fanbox.core.datastore.cookie.MigratingFanboxCookieStorage
import me.matsumo.fanbox.core.logs.logger.LogConfigurator
import me.matsumo.fanbox.core.model.NewPostCheckPrecondition
import me.matsumo.fanbox.core.model.NewPostDetection
import me.matsumo.fanbox.core.model.Setting
import me.matsumo.fanbox.core.model.detectNewPosts
import me.matsumo.fanbox.core.model.fanbox.Post
import me.matsumo.fanbox.core.model.resolveNewPostCheckConfig
import me.matsumo.fanbox.core.model.resolveNewPostCheckPrecondition
import me.matsumo.fanbox.core.model.resolveNewPostNotificationContent
import me.matsumo.fanbox.core.repository.FanboxRepository
import me.matsumo.fanbox.core.repository.hasStoredSession
import me.matsumo.fanbox.core.repository.paging.filterHomePosts
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * 支援中・フォロー中のクリエイターの新着投稿を確かめ、通知する定期実行の処理。
 *
 * FANBOX への通信は、ホームの投稿一覧の先頭 1 ページの取得と、新着が 1 件のときのサムネイルの取得だけである。
 * 通知を表示できないとき、保存済みのセッションがないときは、guest のロードを含めて通信しない。
 * 失敗しても再試行せず、次の定期実行を待つ。画面を一度も開いていないプロセスでも動く。
 */
class NewPostCheckWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params),
    KoinComponent {

    private val settingDataStore by inject<SettingDataStore>()
    private val newPostCheckDataStore by inject<NewPostCheckDataStore>()
    private val cookieStorage by inject<MigratingFanboxCookieStorage>()

    // 生成時に guest をロードして通信するため、取得に進むまで生成しない（by inject は最初の参照で生成する）。
    private val fanboxRepository by inject<FanboxRepository>()
    private val scheduler by inject<NewPostCheckScheduler>()
    private val pixiViewConfig by inject<PixiViewConfig>()
    private val notifier = NewPostNotifier(context)

    override suspend fun doWork(): Result {
        suspendRunCatching { checkNewPosts() }.onFailure { failure ->
            Napier.w(failure) { "Failed to check new posts." }
        }

        return Result.success()
    }

    private suspend fun checkNewPosts() {
        // 購読のない SettingRepository.setting は既定値を返すため、保存済みの値を直接読む。
        val setting = settingDataStore.loadStoredSetting()

        // 画面を開いていないプロセスでは、ログの設定がまだない。Cookie の移行処理もログを送るため、最初に設定する。
        LogConfigurator.configure(pixiViewConfig, setting)

        val precondition = resolveNewPostCheckPrecondition(
            isNotificationEnabled = setting.isNewPostNotificationEnabled,
            isTestUser = setting.isTestUser,
            canNotify = notifier.canNotify(),
            // FanboxRepository は生成時に guest をロードして通信するため、ここでは Cookie の保存先を直接読む。
            hasStoredSession = cookieStorage.hasStoredSession(),
        )

        when (precondition) {
            NewPostCheckPrecondition.SKIP -> return

            NewPostCheckPrecondition.CLEAR_RECORD_AND_SKIP -> {
                newPostCheckDataStore.clearLastSeenPublishedAt()
                return
            }

            NewPostCheckPrecondition.PROCEED -> Unit
        }

        val config = resolveNewPostCheckConfig(fanboxRepository.getGuestMetadata())
        if (!config.isEnabled) return

        scheduler.updateInterval(config.intervalHours)

        val posts = fetchHomePosts(setting) ?: return
        val detection = detectNewPosts(posts, newPostCheckDataStore.getLastSeenPublishedAt())

        when (detection) {
            is NewPostDetection.FirstCheck -> newPostCheckDataStore.setLastSeenPublishedAt(detection.latestPublishedAt)
            is NewPostDetection.Found -> notifyNewPosts(detection, setting)
            NewPostDetection.NotFound -> Unit
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

    private suspend fun notifyNewPosts(detection: NewPostDetection.Found, setting: Setting) {
        newPostCheckDataStore.setLastSeenPublishedAt(detection.latestPublishedAt)

        val content = resolveNewPostNotificationContent(detection.newPosts, setting.isHideAdultContentsInNotification)

        notifier.notify(content)
    }
}
