package me.matsumo.fanbox.core.ui.navigation

import me.matsumo.fanbox.core.model.Destination
import me.matsumo.fanbox.core.model.fanbox.CreatorId
import me.matsumo.fanbox.core.model.fanbox.PostId

/**
 * FANBOX の URL を、それを表示する画面の [Destination] に変換する。対応しない URL には null を返す。
 *
 * - `https://www.fanbox.cc/@{creatorId}/posts/{postId}` と `https://{creatorId}.fanbox.cc/posts/{postId}` は投稿詳細
 * - `https://www.fanbox.cc/@{creatorId}` と `https://{creatorId}.fanbox.cc`（末尾の `/` は任意）はクリエイターのトップ
 *
 * どの URL も、末尾に query と fragment を付けてよい。
 */
fun deepLinkDestinationOf(url: String): Destination? = deepLinkPatterns.firstNotNullOfOrNull { (pattern, toDestination) ->
    pattern.matchEntire(url)?.let { match -> toDestination(match.groupValues) }
}

// 上から順に照合する。投稿の URL はクリエイターのトップの URL より先に照合する
private val deepLinkPatterns: List<Pair<Regex, (List<String>) -> Destination>> = listOf(
    deepLinkPattern("""https://www\.fanbox\.cc/@[^/?#]+/posts/([^/?#]+)""") { Destination.PostDetail(PostId(it[1])) },
    deepLinkPattern("""https://[^/?#]+\.fanbox\.cc/posts/([^/?#]+)""") { Destination.PostDetail(PostId(it[1])) },
    deepLinkPattern("""https://www\.fanbox\.cc/@([^/?#]+)""") { Destination.CreatorTop(CreatorId(it[1])) },
    deepLinkPattern("""https://([^/?#]+)\.fanbox\.cc/?""") { Destination.CreatorTop(CreatorId(it[1])) },
)

private fun deepLinkPattern(
    path: String,
    toDestination: (List<String>) -> Destination,
): Pair<Regex, (List<String>) -> Destination> = Regex("$path(?:[?#].*)?", RegexOption.IGNORE_CASE) to toDestination
