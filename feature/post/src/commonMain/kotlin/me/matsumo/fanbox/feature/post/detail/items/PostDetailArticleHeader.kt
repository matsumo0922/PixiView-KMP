package me.matsumo.fanbox.feature.post.detail.items

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import me.matsumo.fanbox.core.model.Setting
import me.matsumo.fanbox.core.ui.component.AdultContentThumbnail
import me.matsumo.fanbox.core.ui.component.PostItem
import me.matsumo.fanbox.core.ui.extensition.LocalFanboxMetadata
import me.matsumo.fankt.fanbox.domain.model.FanboxPost
import me.matsumo.fankt.fanbox.domain.model.FanboxPostDetail
import me.matsumo.fankt.fanbox.domain.model.id.FanboxCreatorId
import me.matsumo.fankt.fanbox.domain.model.id.FanboxPostId
import sh.calvin.autolinktext.rememberAutoLinkText

internal fun LazyListScope.postDetailArticleHeader(
    content: FanboxPostDetail.Body.Article,
    setting: Setting,
    bookmarkedPostIds: ImmutableList<FanboxPostId>,
    isAdultContents: Boolean,
    isAutoImagePreview: Boolean,
    onClickPost: (FanboxPostId) -> Unit,
    onClickPostLike: (FanboxPostId) -> Unit,
    onClickPostBookmark: (FanboxPost, Boolean) -> Unit,
    onClickCreator: (FanboxCreatorId) -> Unit,
    onClickImage: (FanboxPostDetail.ImageItem) -> Unit,
    onClickFile: (FanboxPostDetail.FileItem) -> Unit,
    onClickDownload: (List<FanboxPostDetail.ImageItem>) -> Unit,
) {
    items(content.blocks) {
        val metadata = LocalFanboxMetadata.current

        when (it) {
            is FanboxPostDetail.Body.Article.Block.Text -> {
                ArticleTextItem(
                    modifier = Modifier.fillMaxWidth(),
                    item = it,
                )
            }

            is FanboxPostDetail.Body.Article.Block.Image -> {
                if (!setting.isAllowedShowAdultContents && metadata.context?.user?.showAdultContent == false && isAdultContents) {
                    AdultContentThumbnail(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(it.item.aspectRatio),
                        coverImageUrl = it.item.thumbnailUrl,
                        isTestUser = setting.isTestUser,
                    )
                } else {
                    PostDetailImageItem(
                        modifier = Modifier.fillMaxWidth(),
                        item = it.item,
                        onClickImage = onClickImage,
                        onClickDownload = { onClickDownload.invoke(listOf(it.item)) },
                        onClickAllDownload = { onClickDownload.invoke(content.imageItems) },
                    )
                }
            }

            is FanboxPostDetail.Body.Article.Block.File -> {
                val imageItem = it.item.asImageItem()

                if (isAutoImagePreview && imageItem != null) {
                    PostDetailImageItem(
                        modifier = Modifier.fillMaxWidth(),
                        item = imageItem,
                        onClickImage = onClickImage,
                        onClickDownload = { onClickDownload.invoke(listOf(imageItem)) },
                        onClickAllDownload = { onClickDownload.invoke(content.imageItems) },
                    )
                } else {
                    PostDetailFileItem(
                        modifier = Modifier.fillMaxWidth(),
                        item = it.item,
                        onClickDownload = onClickFile,
                    )
                }
            }

            is FanboxPostDetail.Body.Article.Block.Link -> {
                ArticleLinkItem(
                    modifier = Modifier.fillMaxWidth(),
                    item = it,
                    isHideAdultContents = setting.isHideAdultContents,
                    isOverrideAdultContents = setting.isAllowedShowAdultContents,
                    isTestUser = setting.isTestUser,
                    isBookmarked = bookmarkedPostIds.contains(it.post?.id),
                    onClickPost = onClickPost,
                    onClickPostLike = onClickPostLike,
                    onClickPostBookmark = { _, isLiked -> it.post?.let { onClickPostBookmark.invoke(it, isLiked) } },
                    onClickCreator = onClickCreator,
                )
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
        text = AnnotatedString.rememberAutoLinkText(item.text),
        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
    )
}

@Composable
private fun ArticleLinkItem(
    item: FanboxPostDetail.Body.Article.Block.Link,
    isHideAdultContents: Boolean,
    isOverrideAdultContents: Boolean,
    isTestUser: Boolean,
    isBookmarked: Boolean,
    onClickPost: (FanboxPostId) -> Unit,
    onClickPostLike: (FanboxPostId) -> Unit,
    onClickPostBookmark: (FanboxPostId, Boolean) -> Unit,
    onClickCreator: (FanboxCreatorId) -> Unit,
    modifier: Modifier = Modifier,
) {
    item.post?.also {
        PostItem(
            modifier = modifier.padding(16.dp),
            post = it,
            isHideAdultContents = isHideAdultContents,
            isOverrideAdultContents = isOverrideAdultContents,
            isTestUser = isTestUser,
            isBookmarked = isBookmarked,
            onClickPost = onClickPost,
            onClickCreator = onClickCreator,
            onClickPlanList = {},
            onClickLike = onClickPostLike,
            onClickBookmark = onClickPostBookmark,
        )
    }
}
