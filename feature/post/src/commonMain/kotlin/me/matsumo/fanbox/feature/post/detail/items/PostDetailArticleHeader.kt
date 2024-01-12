package me.matsumo.fanbox.feature.post.detail.items

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.matsumo.fanbox.core.model.UserData
import me.matsumo.fanbox.core.model.fanbox.FanboxPost
import me.matsumo.fanbox.core.model.fanbox.FanboxPostDetail
import me.matsumo.fanbox.core.model.fanbox.id.CreatorId
import me.matsumo.fanbox.core.model.fanbox.id.PostId
import me.matsumo.fanbox.core.ui.component.AdultContentThumbnail
import me.matsumo.fanbox.core.ui.component.PostItem
import me.matsumo.fanbox.core.ui.extensition.LocalFanboxMetadata

@Composable
internal fun PostDetailArticleHeader(
    content: FanboxPostDetail.Body.Article,
    userData: UserData,
    isAdultContents: Boolean,
    onClickPost: (PostId) -> Unit,
    onClickPostLike: (PostId) -> Unit,
    onClickPostBookmark: (FanboxPost, Boolean) -> Unit,
    onClickCreator: (CreatorId) -> Unit,
    onClickImage: (FanboxPostDetail.ImageItem) -> Unit,
    onClickFile: (FanboxPostDetail.FileItem) -> Unit,
    onClickDownload: (List<FanboxPostDetail.ImageItem>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val metadata = LocalFanboxMetadata.current

    Column(modifier) {
        for (item in content.blocks) {
            when (item) {
                is FanboxPostDetail.Body.Article.Block.Text -> {
                    ArticleTextItem(
                        modifier = Modifier.fillMaxWidth(),
                        item = item,
                    )
                }

                is FanboxPostDetail.Body.Article.Block.Image -> {
                    if (!userData.isAllowedShowAdultContents && !metadata.context.user.showAdultContent && isAdultContents) {
                        AdultContentThumbnail(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(item.item.aspectRatio),
                            coverImageUrl = item.item.thumbnailUrl,
                            isTestUser = userData.isTestUser,
                        )
                    } else {
                        PostDetailImageItem(
                            modifier = Modifier.fillMaxWidth(),
                            item = item.item,
                            onClickImage = onClickImage,
                            onClickDownload = { onClickDownload.invoke(listOf(item.item)) },
                            onClickAllDownload = { onClickDownload.invoke(content.imageItems) },
                        )
                    }
                }

                is FanboxPostDetail.Body.Article.Block.File -> {
                    PostDetailFileItem(
                        modifier = Modifier.fillMaxWidth(),
                        item = item.item,
                        onClickDownload = onClickFile,
                    )
                }

                is FanboxPostDetail.Body.Article.Block.Link -> {
                    ArticleLinkItem(
                        modifier = Modifier.fillMaxWidth(),
                        item = item,
                        isHideAdultContents = userData.isHideAdultContents,
                        isOverrideAdultContents = userData.isAllowedShowAdultContents,
                        isTestUser = userData.isTestUser,
                        onClickPost = onClickPost,
                        onClickPostLike = onClickPostLike,
                        onClickPostBookmark = { _, isLiked -> item.post?.let { onClickPostBookmark.invoke(it, isLiked) } },
                        onClickCreator = onClickCreator,
                    )
                }
            }
        }
    }
}

@Composable
private fun ArticleTextItem(
    item: FanboxPostDetail.Body.Article.Block.Text,
    modifier: Modifier = Modifier,
) {
    Text(
        modifier = modifier.padding(16.dp),
        text = item.text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
    )
}

@Composable
private fun ArticleLinkItem(
    item: FanboxPostDetail.Body.Article.Block.Link,
    isHideAdultContents: Boolean,
    isOverrideAdultContents: Boolean,
    isTestUser: Boolean,
    onClickPost: (PostId) -> Unit,
    onClickPostLike: (PostId) -> Unit,
    onClickPostBookmark: (PostId, Boolean) -> Unit,
    onClickCreator: (CreatorId) -> Unit,
    modifier: Modifier = Modifier,
) {
    item.post?.also {
        PostItem(
            modifier = modifier.padding(16.dp),
            post = it,
            isHideAdultContents = isHideAdultContents,
            isOverrideAdultContents = isOverrideAdultContents,
            isTestUser = isTestUser,
            onClickPost = onClickPost,
            onClickCreator = onClickCreator,
            onClickPlanList = {},
            onClickLike = onClickPostLike,
            onClickBookmark = onClickPostBookmark,
        )
    }
}
