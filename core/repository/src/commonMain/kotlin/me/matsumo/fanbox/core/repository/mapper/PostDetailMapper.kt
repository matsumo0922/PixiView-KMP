package me.matsumo.fanbox.core.repository.mapper

import me.matsumo.fanbox.core.model.fanbox.PostDetail
import me.matsumo.fanbox.core.model.fanbox.PostDetail.Body
import me.matsumo.fanbox.core.model.fanbox.PostDetail.Body.Article.Block
import me.matsumo.fanbox.core.model.fanbox.PostDetail.Body.Article.Block.Text
import me.matsumo.fankt.fanbox.domain.model.FanboxPostDetail
import kotlin.time.ExperimentalTime
import me.matsumo.fankt.fanbox.domain.model.FanboxPostDetail.Body as FanktBody
import me.matsumo.fankt.fanbox.domain.model.FanboxPostDetail.Body.Article.Block as FanktBlock
import me.matsumo.fankt.fanbox.domain.model.FanboxPostDetail.Body.Article.Block.Text as FanktBlockText
import me.matsumo.fankt.fanbox.domain.model.FanboxPostDetail.FileItem as FanktFileItem
import me.matsumo.fankt.fanbox.domain.model.FanboxPostDetail.ImageItem as FanktImageItem
import me.matsumo.fankt.fanbox.domain.model.FanboxPostDetail.OtherPost as FanktOtherPost

@OptIn(ExperimentalTime::class)
fun FanboxPostDetail.toPostDetail(): PostDetail {
    return PostDetail(
        id = id.toPostId(),
        title = title,
        body = body.toBody(),
        coverImageUrl = coverImageUrl,
        commentCount = commentCount,
        excerpt = excerpt,
        feeRequired = feeRequired,
        hasAdultContent = hasAdultContent,
        imageForShare = imageForShare,
        isLiked = isLiked,
        isRestricted = isRestricted,
        likeCount = likeCount,
        tags = tags,
        updatedDatetime = updatedDatetime,
        publishedDatetime = publishedDatetime,
        nextPost = nextPost?.toOtherPost(),
        prevPost = prevPost?.toOtherPost(),
        user = user?.toUser(),
    )
}

@OptIn(ExperimentalTime::class)
private fun FanktOtherPost.toOtherPost(): PostDetail.OtherPost {
    return PostDetail.OtherPost(
        id = id.toPostId(),
        title = title,
        publishedDatetime = publishedDatetime,
    )
}

private fun FanktBody.toBody(): Body {
    return when (this) {
        is FanktBody.Article -> Body.Article(
            blocks = blocks.map { it.toBlock() },
        )

        is FanktBody.Image -> Body.Image(
            text = text,
            images = images.map { it.toImageItem() },
        )

        is FanktBody.File -> Body.File(
            text = text,
            files = files.map { it.toFileItem() },
        )

        is FanktBody.Text -> Body.Text(text = text)

        is FanktBody.Video -> Body.Video(
            serviceProvider = serviceProvider,
            videoId = videoId,
        )

        is FanktBody.Html -> Body.Html(html = html)

        is FanktBody.Unknown -> Body.Unknown(
            type = type,
            rawBodyJson = rawBodyJson,
        )
    }
}

private fun FanktBlock.toBlock(): Block {
    return when (this) {
        is FanktBlock.Text -> Block.Text(
            text = text,
            styles = styles.map { it.toStyleSpan() },
            links = links.map { it.toLinkSpan() },
            isHeader = isHeader,
        )

        is FanktBlock.Image -> Block.Image(item = item.toImageItem())

        is FanktBlock.File -> Block.File(item = item.toFileItem())

        is FanktBlock.Link -> Block.Link(
            html = html,
            post = post?.toPost(),
            type = type,
            url = url,
        )

        is FanktBlock.Embed -> Block.Embed(
            serviceProvider = serviceProvider,
            contentId = contentId,
        )

        is FanktBlock.Unknown -> Block.Unknown(rawJson = rawJson)
    }
}

private fun FanktBlockText.StyleSpan.toStyleSpan(): Text.StyleSpan {
    return Text.StyleSpan(
        type = type,
        offset = offset,
        length = length,
    )
}

private fun FanktBlockText.LinkSpan.toLinkSpan(): Text.LinkSpan {
    return Text.LinkSpan(
        offset = offset,
        length = length,
        url = url,
    )
}

private fun FanktImageItem.toImageItem(): PostDetail.ImageItem {
    return PostDetail.ImageItem(
        id = id.toPostItemId(),
        postId = postId.toPostId(),
        extension = extension,
        originalUrl = originalUrl,
        thumbnailUrl = thumbnailUrl,
        aspectRatio = aspectRatio,
    )
}

private fun FanktFileItem.toFileItem(): PostDetail.FileItem {
    return PostDetail.FileItem(
        id = id.toPostItemId(),
        postId = postId.toPostId(),
        name = name,
        extension = extension,
        size = size,
        url = url,
    )
}
