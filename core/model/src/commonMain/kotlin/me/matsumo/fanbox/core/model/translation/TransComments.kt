package me.matsumo.fanbox.core.model.translation

import kotlinx.serialization.Serializable
import me.matsumo.fankt.fanbox.domain.PageOffsetInfo
import me.matsumo.fankt.fanbox.domain.model.FanboxComment

@Serializable
data class TransComments(
    val comments: List<String>,
)

private fun flatCooments(comments: List<FanboxComment>): List<FanboxComment> {
    return comments.flatMap {
        listOf(it) + flatCooments(it.replies)
    }
}

fun PageOffsetInfo<FanboxComment>.toTrans(): TransComments {
    return TransComments(
        comments = flatCooments(contents).map { it.body },
    )
}

fun TransComments.toFanboxComments(original: PageOffsetInfo<FanboxComment>): PageOffsetInfo<FanboxComment> {
    var index = 0

    fun replaceCommentBody(comment: FanboxComment): FanboxComment {
        val newBody = comments[index++]

        return comment.copy(
            body = newBody,
            replies = comment.replies.map { replaceCommentBody(it) },
        )
    }

    return original.copy(
        contents = original.contents.map { replaceCommentBody(it) },
    )
}
