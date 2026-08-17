package me.matsumo.fanbox.core.model.fanbox

import androidx.compose.runtime.Immutable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/** 投稿一覧に表示される投稿。 */
@Immutable
@OptIn(ExperimentalTime::class)
data class Post(
    val id: PostId,
    val title: String,
    val cover: Cover?,
    val user: User?,
    val excerpt: String,
    val feeRequired: Int,
    val hasAdultContent: Boolean,
    val isLiked: Boolean,
    val isRestricted: Boolean,
    val likeCount: Int,
    val commentCount: Int,
    val tags: List<String>,
    val publishedDatetime: Instant,
    val updatedDatetime: Instant,
)
