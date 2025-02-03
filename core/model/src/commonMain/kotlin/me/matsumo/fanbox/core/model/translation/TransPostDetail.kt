package me.matsumo.fanbox.core.model.translation

import kotlinx.serialization.Serializable
import me.matsumo.fankt.fanbox.domain.model.FanboxPostDetail

@Serializable
data class TransPostDetail(
    val title: String,
    val textBody: List<String>,
    val imageBody: String,
    val fileBody: String,
    val excerpt: String,
)

fun FanboxPostDetail.toTrans(): TransPostDetail {
    val textBody = (body as? FanboxPostDetail.Body.Article)?.blocks?.mapNotNull {
        (it as? FanboxPostDetail.Body.Article.Block.Text)?.text
    }

    return TransPostDetail(
        title = title,
        textBody = textBody.orEmpty(),
        imageBody = (body as? FanboxPostDetail.Body.Image)?.text.orEmpty(),
        fileBody = (body as? FanboxPostDetail.Body.File)?.text.orEmpty(),
        excerpt = excerpt,
    )
}

fun TransPostDetail.toFanboxPostDetail(original: FanboxPostDetail): FanboxPostDetail {
    var index = 0
    val newBody = when (val originalBody = original.body) {
        is FanboxPostDetail.Body.Article -> {
            originalBody.copy(
                blocks = originalBody.blocks.map { block ->
                    if (block is FanboxPostDetail.Body.Article.Block.Text) {
                        block.copy(text = textBody[index]).also { index++ }
                    } else {
                        block
                    }
                },
            )
        }

        is FanboxPostDetail.Body.Image -> originalBody.copy(text = imageBody)
        is FanboxPostDetail.Body.File -> originalBody.copy(text = fileBody)
        else -> originalBody
    }

    return original.copy(
        title = title,
        body = newBody,
        excerpt = excerpt,
    )
}
