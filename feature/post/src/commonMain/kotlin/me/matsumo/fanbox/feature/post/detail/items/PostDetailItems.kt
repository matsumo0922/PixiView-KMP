package me.matsumo.fanbox.feature.post.detail.items

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.ImmutableList
import me.matsumo.fanbox.core.model.Setting
import me.matsumo.fanbox.core.model.fanbox.CreatorId
import me.matsumo.fanbox.core.model.fanbox.Post
import me.matsumo.fanbox.core.model.fanbox.PostDetail
import me.matsumo.fanbox.core.model.fanbox.PostDetail.Body
import me.matsumo.fanbox.core.model.fanbox.PostId

internal fun LazyListScope.postDetailItems(
    post: PostDetail,
    setting: Setting,
    bookmarkedPostIds: ImmutableList<PostId>,
    onClickPost: (PostId) -> Unit,
    onClickPostLike: (PostId) -> Unit,
    onClickPostBookmark: (Post, Boolean) -> Unit,
    onClickCreator: (CreatorId) -> Unit,
    onClickImage: (PostDetail.ImageItem) -> Unit,
    onClickFile: (PostDetail.FileItem) -> Unit,
    onClickDownload: (List<PostDetail.ImageItem>) -> Unit,
) {
    when (val content = post.body) {
        is Body.Article -> {
            postDetailArticleHeader(
                content = content,
                setting = setting,
                isAdultContents = post.hasAdultContent,
                isAutoImagePreview = setting.isAutoImagePreview,
                bookmarkedPostIds = bookmarkedPostIds,
                onClickPost = onClickPost,
                onClickPostLike = onClickPostLike,
                onClickPostBookmark = onClickPostBookmark,
                onClickCreator = onClickCreator,
                onClickImage = onClickImage,
                onClickFile = onClickFile,
                onClickDownload = onClickDownload,
            )
        }

        is Body.Image -> {
            postDetailImageHeader(
                content = content,
                isAdultContents = post.hasAdultContent,
                isOverrideAdultContents = setting.isAllowedShowAdultContents,
                isTestUser = setting.isTestUser,
                onClickImage = onClickImage,
                onClickDownload = onClickDownload,
            )
        }

        is Body.File -> {
            postDetailFileHeader(
                content = content,
                isAutoImagePreview = setting.isAutoImagePreview,
                onClickFile = onClickFile,
                onClickImage = onClickImage,
                onClickDownload = onClickDownload,
            )
        }

        is Body.Text, is Body.Video, is Body.Html, is Body.Unknown -> item {
            PostDetailOtherContent(
                modifier = Modifier.fillMaxWidth(),
                content = content,
            )
        }
    }
}

@Composable
private fun PostDetailOtherContent(
    content: Body,
    modifier: Modifier = Modifier,
) {
    val url = when (content) {
        is Body.Text -> content.text
        is Body.Video -> content.url
        else -> null
    }

    // HTML や未知の本文は検証されていないネットワークデータのため描画しない。
    if (url != null) {
        ArticleUrlItem(
            modifier = modifier,
            url = url,
        )
    } else {
        ArticleUnsupportedItem(modifier)
    }
}
