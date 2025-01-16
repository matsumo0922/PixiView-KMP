package me.matsumo.fanbox.feature.post.detail.items

import androidx.compose.foundation.lazy.LazyListScope
import me.matsumo.fanbox.core.model.UserData
import me.matsumo.fanbox.core.model.fanbox.FanboxPost
import me.matsumo.fanbox.core.model.fanbox.FanboxPostDetail
import me.matsumo.fanbox.core.model.fanbox.id.FanboxCreatorId
import me.matsumo.fanbox.core.model.fanbox.id.FanboxPostId

internal fun LazyListScope.postDetailItems(
    post: FanboxPostDetail,
    userData: UserData,
    onClickPost: (FanboxPostId) -> Unit,
    onClickPostLike: (FanboxPostId) -> Unit,
    onClickPostBookmark: (FanboxPost, Boolean) -> Unit,
    onClickCreator: (FanboxCreatorId) -> Unit,
    onClickImage: (FanboxPostDetail.ImageItem) -> Unit,
    onClickFile: (FanboxPostDetail.FileItem) -> Unit,
    onClickDownload: (List<FanboxPostDetail.ImageItem>) -> Unit,
) {
    when (val content = post.body) {
        is FanboxPostDetail.Body.Article -> {
            postDetailArticleHeader(
                content = content,
                userData = userData,
                isAdultContents = post.hasAdultContent,
                isAutoImagePreview = userData.isAutoImagePreview,
                onClickPost = onClickPost,
                onClickPostLike = onClickPostLike,
                onClickPostBookmark = onClickPostBookmark,
                onClickCreator = onClickCreator,
                onClickImage = onClickImage,
                onClickFile = onClickFile,
                onClickDownload = onClickDownload,
            )
        }

        is FanboxPostDetail.Body.Image -> {
            postDetailImageHeader(
                content = content,
                isAdultContents = post.hasAdultContent,
                isOverrideAdultContents = userData.isAllowedShowAdultContents,
                isTestUser = userData.isTestUser,
                onClickImage = onClickImage,
                onClickDownload = onClickDownload,
            )
        }

        is FanboxPostDetail.Body.File -> {
            postDetailFileHeader(
                content = content,
                isAutoImagePreview = userData.isAutoImagePreview,
                onClickFile = onClickFile,
                onClickImage = onClickImage,
                onClickDownload = onClickDownload,
            )
        }

        is FanboxPostDetail.Body.Unknown -> {
            // do nothing
        }
    }
}
