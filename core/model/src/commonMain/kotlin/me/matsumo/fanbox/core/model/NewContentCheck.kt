package me.matsumo.fanbox.core.model

import me.matsumo.fanbox.core.model.fanbox.CreatorId
import me.matsumo.fanbox.core.model.fanbox.NewsLetter
import me.matsumo.fanbox.core.model.fanbox.Post
import me.matsumo.fanbox.core.model.fanbox.PostId
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * 新着の定期確認の、遠隔から変えられる設定。
 *
 * [isPostCheckEnabled] は新着投稿の確認、[isMessageCheckEnabled] は新着メッセージの確認を行ってよいか。[intervalHours] は、両方の確認で共有する定期確認の間隔（時間）。
 */
data class NewContentCheckConfig(
    val isPostCheckEnabled: Boolean,
    val isMessageCheckEnabled: Boolean,
    val intervalHours: Int,
) {
    /** 遠隔の値がないときに使う設定を持つオブジェクト。 */
    companion object {
        /** 遠隔の値がない、または読めないときの設定。 */
        val Default = NewContentCheckConfig(
            isPostCheckEnabled = true,
            isMessageCheckEnabled = true,
            intervalHours = 3,
        )
    }
}

/** 新着の定期確認で、新着投稿・新着メッセージのそれぞれについて、FANBOX へ取得する前に決める進め方。 */
enum class NewContentCheckPrecondition {
    /** 通信せずに終える。 */
    SKIP,

    /** 確認の記録を消して、通信せずに終える。通知を表示できるようになった後の最初の確認を、記録だけ行う初回にするため。 */
    CLEAR_RECORD_AND_SKIP,

    /** 遠隔の設定の確認と、一覧の取得に進む。 */
    PROCEED,
}

/**
 * 新着投稿・新着メッセージの一方の確認を、通信の前に進めるかを決める。
 *
 * その通知の設定が OFF、またはテストユーザーなら終える。その通知を表示できないなら記録を消して終える。
 * 保存済みのセッションがなければ終える。どれにも当たらなければ進める。
 */
fun resolveNewContentCheckPrecondition(
    isNotificationEnabled: Boolean,
    isTestUser: Boolean,
    canNotify: Boolean,
    hasStoredSession: Boolean,
): NewContentCheckPrecondition {
    val isCheckAllowed = isNotificationEnabled && !isTestUser

    return when {
        !isCheckAllowed -> NewContentCheckPrecondition.SKIP
        !canNotify -> NewContentCheckPrecondition.CLEAR_RECORD_AND_SKIP
        !hasStoredSession -> NewContentCheckPrecondition.SKIP
        else -> NewContentCheckPrecondition.PROCEED
    }
}

/**
 * guest の manifest の metadata から、新着の定期確認の設定を決める。
 *
 * 項目ごとに、値がないとき、真偽値・整数として読めないとき、間隔が 1〜168 時間の範囲外のときは既定値を使う。
 */
fun resolveNewContentCheckConfig(metadata: Map<String, String>): NewContentCheckConfig {
    val intervalHours = metadata["newContentCheck.intervalHours"]
        ?.trim()
        ?.toIntOrNull()
        ?.takeIf { it in 1..168 }

    return NewContentCheckConfig(
        isPostCheckEnabled = metadata.booleanOf("newPostCheck.enabled") ?: NewContentCheckConfig.Default.isPostCheckEnabled,
        isMessageCheckEnabled = metadata.booleanOf("newMessageCheck.enabled") ?: NewContentCheckConfig.Default.isMessageCheckEnabled,
        intervalHours = intervalHours ?: NewContentCheckConfig.Default.intervalHours,
    )
}

private fun Map<String, String>.booleanOf(key: String): Boolean? = get(key)
    ?.trim()
    ?.toBooleanStrictOrNull()

/** 前回の確認の記録と比べた、新着の判定結果。[T] は投稿またはメッセージ。 */
@OptIn(ExperimentalTime::class)
sealed interface NewContentDetection<out T> {

    /** 記録がない初回の確認。通知せず、[latestCreatedAt] を記録する。 */
    data class FirstCheck(val latestCreatedAt: Instant) : NewContentDetection<Nothing>

    /**
     * 記録より新しいものがある。[newContents] はそのうち通知する対象で、新しい順に並ぶ。[latestCreatedAt] を記録する。
     *
     * 既読やブロックで、記録より新しいものがすべて通知の対象外のときは、[newContents] が空になる。
     */
    data class Found<T>(
        val newContents: List<T>,
        val latestCreatedAt: Instant,
    ) : NewContentDetection<T>

    /** 記録より新しいものがない。記録は変えない。 */
    data object NotFound : NewContentDetection<Nothing>
}

/**
 * [posts] のうち、前回までに確認した最新投稿の公開日時 [lastSeenPublishedAt] より後に公開された投稿を新着とする。
 *
 * [lastSeenPublishedAt] が null のときは初回の確認として扱い、新着を返さない。
 */
@OptIn(ExperimentalTime::class)
fun detectNewPosts(posts: List<Post>, lastSeenPublishedAt: Instant?): NewContentDetection<Post> = detectNewContents(
    contents = posts,
    lastSeenCreatedAt = lastSeenPublishedAt,
    createdAtOf = { it.publishedDatetime },
    isNotifiable = { true },
)

/**
 * [messages] のうち、前回までに確認した最新メッセージの作成日時 [lastSeenCreatedAt] より後に作成された、未読のメッセージを新着とする。
 *
 * [blockedCreatorIds] のクリエイターのメッセージは新着にしない。[lastSeenCreatedAt] が null のときは初回の確認として扱い、新着を返さない。
 * 記録する作成日時は、既読やブロックで新着にしないメッセージも含めた最新の作成日時とする。
 *
 * 初回の確認でメッセージが 1 件もないときは、確認した日時 [checkedAt] を記録する。メッセージがまだないアカウントでも、最初に届いたメッセージを通知できるようにするため。
 */
@OptIn(ExperimentalTime::class)
fun detectNewMessages(
    messages: List<NewsLetter>,
    lastSeenCreatedAt: Instant?,
    blockedCreatorIds: Set<CreatorId>,
    checkedAt: Instant,
): NewContentDetection<NewsLetter> {
    val isFirstCheckWithoutMessages = messages.isEmpty() && lastSeenCreatedAt == null
    if (isFirstCheckWithoutMessages) return NewContentDetection.FirstCheck(checkedAt)

    return detectNewContents(
        contents = messages,
        lastSeenCreatedAt = lastSeenCreatedAt,
        createdAtOf = { it.createdAt },
        isNotifiable = { message -> !message.isRead && message.creator.creatorId !in blockedCreatorIds },
    )
}

@OptIn(ExperimentalTime::class)
private fun <T> detectNewContents(
    contents: List<T>,
    lastSeenCreatedAt: Instant?,
    createdAtOf: (T) -> Instant,
    isNotifiable: (T) -> Boolean,
): NewContentDetection<T> {
    val latestCreatedAt = contents.maxOfOrNull(createdAtOf) ?: return NewContentDetection.NotFound

    if (lastSeenCreatedAt == null) return NewContentDetection.FirstCheck(latestCreatedAt)
    if (latestCreatedAt <= lastSeenCreatedAt) return NewContentDetection.NotFound

    // 通知の対象外のものも記録に含めるため、通知する対象がなくても Found を返して記録を進める。
    val newContents = contents
        .filter { createdAtOf(it) > lastSeenCreatedAt && isNotifiable(it) }
        .sortedByDescending(createdAtOf)

    return NewContentDetection.Found(newContents, latestCreatedAt)
}

/** 新着投稿 1 件の通知に表示する内容。新着投稿は 1 件ずつ通知し、複数の通知は OS のグループでまとめる。 */
@OptIn(ExperimentalTime::class)
sealed interface NewPostNotificationContent {

    /** 通知する投稿。 */
    val postId: PostId

    /** 投稿したクリエイターの ID。ないときは、通知のタップで投稿詳細ではなくアプリを開く。 */
    val creatorId: CreatorId?

    /** 通知のタイトルに表示する、投稿したクリエイターの名前。 */
    val creatorName: String

    /** 投稿の公開日時。通知の時刻として表示し、通知の並び順にも使う。 */
    val publishedAt: Instant

    /** タイトルとサムネイルを表示する新着投稿。 */
    data class Visible(
        override val postId: PostId,
        override val creatorId: CreatorId?,
        override val creatorName: String,
        override val publishedAt: Instant,
        val title: String,
        val thumbnailUrl: String?,
    ) : NewPostNotificationContent

    /** タイトルとサムネイルを隠す、成人向けの新着投稿。 */
    data class Hidden(
        override val postId: PostId,
        override val creatorId: CreatorId?,
        override val creatorName: String,
        override val publishedAt: Instant,
    ) : NewPostNotificationContent
}

/** 新着投稿 [post] の通知に表示する内容を決める。[isHideAdultContents] が true かつ成人向けの投稿のときは、タイトルとサムネイルを隠す。 */
@OptIn(ExperimentalTime::class)
fun resolveNewPostNotificationContent(post: Post, isHideAdultContents: Boolean): NewPostNotificationContent {
    val creatorName = post.user?.name.orEmpty()

    if (isHideAdultContents && post.hasAdultContent) {
        return NewPostNotificationContent.Hidden(
            postId = post.id,
            creatorId = post.user?.creatorId,
            creatorName = creatorName,
            publishedAt = post.publishedDatetime,
        )
    }

    return NewPostNotificationContent.Visible(
        postId = post.id,
        creatorId = post.user?.creatorId,
        creatorName = creatorName,
        publishedAt = post.publishedDatetime,
        title = post.title,
        thumbnailUrl = post.cover?.url,
    )
}
