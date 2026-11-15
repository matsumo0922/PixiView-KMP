package me.matsumo.fanbox.core.model

import me.matsumo.fanbox.core.model.fanbox.CreatorId
import me.matsumo.fanbox.core.model.fanbox.Post
import me.matsumo.fanbox.core.model.fanbox.PostId
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/** 新着投稿の定期確認の、遠隔から変えられる設定。[intervalHours] は確認の間隔（時間）。 */
data class NewPostCheckConfig(
    val isEnabled: Boolean,
    val intervalHours: Int,
) {
    /** 遠隔の値がないときに使う設定を持つオブジェクト。 */
    companion object {
        /** 遠隔の値がない、または読めないときの設定。 */
        val Default = NewPostCheckConfig(isEnabled = true, intervalHours = 3)
    }
}

/** 新着投稿の定期確認で、FANBOX へ取得する前に決める進め方。 */
enum class NewPostCheckPrecondition {
    /** 通信せずに終える。 */
    SKIP,

    /** 確認の記録を消して、通信せずに終える。通知を表示できるようになった後の最初の確認を、記録だけ行う初回にするため。 */
    CLEAR_RECORD_AND_SKIP,

    /** 遠隔の設定の確認と、投稿一覧の取得に進む。 */
    PROCEED,
}

/**
 * 新着投稿の定期確認を、通信の前に進めるかを決める。
 *
 * 新着投稿の通知が OFF、またはテストユーザーなら終える。通知を表示できないなら記録を消して終える。
 * 保存済みのセッションがなければ終える。どれにも当たらなければ進める。
 */
fun resolveNewPostCheckPrecondition(
    isNotificationEnabled: Boolean,
    isTestUser: Boolean,
    canNotify: Boolean,
    hasStoredSession: Boolean,
): NewPostCheckPrecondition {
    val isCheckAllowed = isNotificationEnabled && !isTestUser

    return when {
        !isCheckAllowed -> NewPostCheckPrecondition.SKIP
        !canNotify -> NewPostCheckPrecondition.CLEAR_RECORD_AND_SKIP
        !hasStoredSession -> NewPostCheckPrecondition.SKIP
        else -> NewPostCheckPrecondition.PROCEED
    }
}

/**
 * guest の manifest の metadata から、新着投稿の定期確認の設定を決める。
 *
 * 項目ごとに、値がないとき、真偽値・整数として読めないとき、間隔が 1〜168 時間の範囲外のときは既定値を使う。
 */
fun resolveNewPostCheckConfig(metadata: Map<String, String>): NewPostCheckConfig {
    val isEnabled = metadata["newPostCheck.enabled"]
        ?.trim()
        ?.toBooleanStrictOrNull()
    val intervalHours = metadata["newPostCheck.intervalHours"]
        ?.trim()
        ?.toIntOrNull()
        ?.takeIf { it in 1..168 }

    return NewPostCheckConfig(
        isEnabled = isEnabled ?: NewPostCheckConfig.Default.isEnabled,
        intervalHours = intervalHours ?: NewPostCheckConfig.Default.intervalHours,
    )
}

/** 前回の確認の記録と比べた、新着投稿の判定結果。 */
@OptIn(ExperimentalTime::class)
sealed interface NewPostDetection {

    /** 記録がない初回の確認。通知せず、[latestPublishedAt] を記録する。 */
    data class FirstCheck(val latestPublishedAt: Instant) : NewPostDetection

    /** 記録より新しい投稿がある。[newPosts] は新しい順で、[latestPublishedAt] を記録する。 */
    data class Found(
        val newPosts: List<Post>,
        val latestPublishedAt: Instant,
    ) : NewPostDetection

    /** 記録より新しい投稿がない、または投稿が 1 件もない。記録は変えない。 */
    data object NotFound : NewPostDetection
}

/**
 * [posts] のうち、前回までに確認した最新投稿の公開日時 [lastSeenPublishedAt] より後に公開された投稿を新着とする。
 *
 * [lastSeenPublishedAt] が null のときは初回の確認として扱い、新着を返さない。
 */
@OptIn(ExperimentalTime::class)
fun detectNewPosts(posts: List<Post>, lastSeenPublishedAt: Instant?): NewPostDetection {
    val latestPublishedAt = posts.maxOfOrNull { it.publishedDatetime } ?: return NewPostDetection.NotFound

    if (lastSeenPublishedAt == null) return NewPostDetection.FirstCheck(latestPublishedAt)

    val newPosts = posts
        .filter { it.publishedDatetime > lastSeenPublishedAt }
        .sortedByDescending { it.publishedDatetime }

    if (newPosts.isEmpty()) return NewPostDetection.NotFound

    return NewPostDetection.Found(newPosts, latestPublishedAt)
}

/** 新着投稿の通知に表示する内容。 */
sealed interface NewPostNotificationContent {

    /** 通知にまとめた新着投稿の件数。 */
    val postCount: Int

    /** 1 件の新着投稿。タイトルとサムネイルを表示する。 */
    data class Single(
        val postId: PostId,
        val creatorId: CreatorId?,
        val creatorName: String,
        val title: String,
        val thumbnailUrl: String?,
    ) : NewPostNotificationContent {
        override val postCount: Int = 1
    }

    /** タイトルとサムネイルを隠す、成人向けの 1 件の新着投稿。 */
    data class SingleHidden(
        val postId: PostId,
        val creatorId: CreatorId?,
        val creatorName: String,
    ) : NewPostNotificationContent {
        override val postCount: Int = 1
    }

    /** 1 つの通知にまとめた複数件の新着投稿。[creatorNames] は重複を除いた、新しい投稿の順のクリエイター名。 */
    data class Multiple(
        override val postCount: Int,
        val creatorNames: List<String>,
    ) : NewPostNotificationContent
}

/**
 * 新しい順の [newPosts] から、通知に表示する内容を決める。
 *
 * 複数件はクリエイター名だけを表示するため、成人向けかどうかによらない。
 * 1 件で、[isHideAdultContents] が true かつ成人向けの投稿のときは、タイトルとサムネイルを隠す。
 */
fun resolveNewPostNotificationContent(newPosts: List<Post>, isHideAdultContents: Boolean): NewPostNotificationContent {
    require(newPosts.isNotEmpty()) { "newPosts must not be empty" }

    if (newPosts.size > 1) {
        return NewPostNotificationContent.Multiple(
            postCount = newPosts.size,
            creatorNames = newPosts.mapNotNull { it.user?.name }.distinct(),
        )
    }

    val post = newPosts.single()
    val creatorName = post.user?.name.orEmpty()

    if (isHideAdultContents && post.hasAdultContent) {
        return NewPostNotificationContent.SingleHidden(
            postId = post.id,
            creatorId = post.user?.creatorId,
            creatorName = creatorName,
        )
    }

    return NewPostNotificationContent.Single(
        postId = post.id,
        creatorId = post.user?.creatorId,
        creatorName = creatorName,
        title = post.title,
        thumbnailUrl = post.cover?.url,
    )
}
