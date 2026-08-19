package me.matsumo.fanbox.core.model.translation

import kotlinx.serialization.Serializable
import me.matsumo.fanbox.core.model.fanbox.PostDetail

@Serializable
data class TransPostDetail(
    val title: String,
    val textBody: List<String>,
    val imageBody: String,
    val fileBody: String,
    val excerpt: String,
)

fun PostDetail.toTrans(): TransPostDetail {
    val textBody = (body as? PostDetail.Body.Article)?.blocks?.mapNotNull {
        (it as? PostDetail.Body.Article.Block.Text)?.text
    }

    return TransPostDetail(
        title = title,
        textBody = textBody.orEmpty(),
        imageBody = (body as? PostDetail.Body.Image)?.text.orEmpty(),
        fileBody = (body as? PostDetail.Body.File)?.text.orEmpty(),
        excerpt = excerpt,
    )
}

fun TransPostDetail.toFanboxPostDetail(original: PostDetail): PostDetail {
    var index = 0
    val newBody = when (val originalBody = original.body) {
        is PostDetail.Body.Article -> {
            originalBody.copy(
                blocks = originalBody.blocks.map { block ->
                    if (block is PostDetail.Body.Article.Block.Text) {
                        block.copy(text = textBody[index]).also { index++ }
                    } else {
                        block
                    }
                },
            )
        }

        is PostDetail.Body.Image -> originalBody.copy(text = imageBody)
        is PostDetail.Body.File -> originalBody.copy(text = fileBody)
        else -> originalBody
    }

    return original.copy(
        title = title,
        body = newBody,
        excerpt = excerpt,
    )
}
