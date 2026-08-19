package me.matsumo.fanbox.core.model.translation

import kotlinx.serialization.Serializable
import me.matsumo.fanbox.core.model.fanbox.Comment
import me.matsumo.fanbox.core.model.fanbox.PageOffsetInfo

@Serializable
data class TransComments(
    val comments: List<String>,
)

private fun flatCooments(comments: List<Comment>): List<Comment> {
    return comments.flatMap {
        listOf(it) + flatCooments(it.replies)
    }
}

fun PageOffsetInfo<Comment>.toTrans(): TransComments {
    return TransComments(
        comments = flatCooments(contents).map { it.body },
    )
}

fun TransComments.toFanboxComments(original: PageOffsetInfo<Comment>): PageOffsetInfo<Comment> {
    var index = 0

    fun replaceCommentBody(comment: Comment): Comment {
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
