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
import me.matsumo.fanbox.core.model.fanbox.CreatorId
import me.matsumo.fanbox.core.model.fanbox.Post
import me.matsumo.fanbox.core.model.fanbox.PostDetail.Body
import me.matsumo.fanbox.core.model.fanbox.PostDetail.Body.Article.Block
import me.matsumo.fanbox.core.model.fanbox.PostDetail.FileItem
import me.matsumo.fanbox.core.model.fanbox.PostDetail.ImageItem
import me.matsumo.fanbox.core.model.fanbox.PostId
import me.matsumo.fanbox.core.resources.Res
import me.matsumo.fanbox.core.resources.post_detail_unsupported_content
import me.matsumo.fanbox.core.ui.component.AdultContentThumbnail
import me.matsumo.fanbox.core.ui.component.PostItem
import me.matsumo.fanbox.core.ui.extensition.LocalFanboxMetadata
import org.jetbrains.compose.resources.stringResource
import sh.calvin.autolinktext.rememberAutoLinkText

// LazyListScope の項目宣言へ、記事の表示データと画面イベントをそのまま渡す。
@Suppress("LongParameterList")
internal fun LazyListScope.postDetailArticleHeader(
    content: Body.Article,
    setting: Setting,
    bookmarkedPostIds: ImmutableList<PostId>,
    isAdultContents: Boolean,
    isAutoImagePreview: Boolean,
    onClickPost: (PostId) -> Unit,
    onClickPostLike: (PostId) -> Unit,
    onClickPostBookmark: (Post, Boolean) -> Unit,
    onClickCreator: (CreatorId) -> Unit,
    onClickImage: (ImageItem) -> Unit,
    onClickFile: (FileItem) -> Unit,
    onClickDownload: (List<ImageItem>) -> Unit,
) {
    items(content.blocks) { block ->
        PostDetailArticleBlock(
            modifier = Modifier.fillMaxWidth(),
            block = block,
            content = content,
            setting = setting,
            bookmarkedPostIds = bookmarkedPostIds,
            isAdultContents = isAdultContents,
            isAutoImagePreview = isAutoImagePreview,
            onClickPost = onClickPost,
            onClickPostLike = onClickPostLike,
            onClickPostBookmark = onClickPostBookmark,
            onClickCreator = onClickCreator,
            onClickImage = onClickImage,
            onClickFile = onClickFile,
            onClickDownload = onClickDownload,
        )
    }
}

@Composable
private fun PostDetailArticleBlock(
    block: Block,
    content: Body.Article,
    setting: Setting,
    bookmarkedPostIds: ImmutableList<PostId>,
    isAdultContents: Boolean,
    isAutoImagePreview: Boolean,
    onClickPost: (PostId) -> Unit,
    onClickPostLike: (PostId) -> Unit,
    onClickPostBookmark: (Post, Boolean) -> Unit,
    onClickCreator: (CreatorId) -> Unit,
    onClickImage: (ImageItem) -> Unit,
    onClickFile: (FileItem) -> Unit,
    onClickDownload: (List<ImageItem>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val metadata = LocalFanboxMetadata.current

    when (block) {
        is Block.Text -> {
            ArticleTextItem(
                modifier = modifier,
                item = block,
            )
        }

        is Block.Image -> {
            val shouldHideAdultContent = !setting.isAllowedShowAdultContents && metadata.context?.user?.showAdultContent == false && isAdultContents
            if (shouldHideAdultContent) {
                AdultContentThumbnail(
                    modifier = modifier
                        .fillMaxWidth()
                        .aspectRatio(block.item.aspectRatio),
                    coverImageUrl = block.item.thumbnailUrl,
                    isTestUser = setting.isTestUser,
                )
            } else {
                PostDetailImageItem(
                    modifier = modifier,
                    item = block.item,
                    onClickImage = onClickImage,
                    onClickDownload = { onClickDownload.invoke(listOf(block.item)) },
                    onClickAllDownload = { onClickDownload.invoke(content.imageItems) },
                )
            }
        }

        is Block.File -> {
            val imageItem = block.item.asImageItem()

            if (isAutoImagePreview && imageItem != null) {
                PostDetailImageItem(
                    modifier = modifier,
                    item = imageItem,
                    onClickImage = onClickImage,
                    onClickDownload = { onClickDownload.invoke(listOf(imageItem)) },
                    onClickAllDownload = { onClickDownload.invoke(content.imageItems) },
                )
            } else {
                PostDetailFileItem(
                    modifier = modifier,
                    item = block.item,
                    onClickDownload = onClickFile,
                )
            }
        }

        is Block.Link -> {
            ArticleLinkItem(
                modifier = modifier,
                item = block,
                isHideAdultContents = setting.isHideAdultContents,
                isOverrideAdultContents = setting.isAllowedShowAdultContents,
                isTestUser = setting.isTestUser,
                isBookmarked = bookmarkedPostIds.contains(block.post?.id),
                onClickPost = onClickPost,
                onClickPostLike = onClickPostLike,
                onClickPostBookmark = { _, isLiked -> block.post?.let { onClickPostBookmark.invoke(it, isLiked) } },
                onClickCreator = onClickCreator,
            )
        }

        is Block.Embed -> {
            // fankt は既知のサービスについてのみ URL を復元する。復元できない場合は開く先が
            // 無いため、未対応の要素として扱う。
            val embedUrl = block.url

            if (embedUrl != null) {
                ArticleUrlItem(
                    modifier = modifier,
                    url = embedUrl,
                )
            } else {
                ArticleUnsupportedItem(modifier = modifier)
            }
        }

        is Block.Unknown -> {
            // rawJson は検証されていないネットワークデータのため表示しない。
            ArticleUnsupportedItem(modifier = modifier)
        }
    }
}

/** 装飾情報を持たない文字列を、自動リンク付きのテキストとして表示する。 */
@Composable
internal fun ArticleUrlItem(
    url: String,
    modifier: Modifier = Modifier,
) {
    Text(
        modifier = modifier.padding(16.dp),
        text = AnnotatedString.rememberAutoLinkText(url),
        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
    )
}

/** 描画方法が決まっていない要素の存在を示す。 */
@Composable
internal fun ArticleUnsupportedItem(
    modifier: Modifier = Modifier,
) {
    Text(
        modifier = modifier.padding(16.dp),
        text = stringResource(Res.string.post_detail_unsupported_content),
        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
    )
}

@Composable
private fun ArticleTextItem(
    item: Block.Text,
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
    item: Block.Link,
    isHideAdultContents: Boolean,
    isOverrideAdultContents: Boolean,
    isTestUser: Boolean,
    isBookmarked: Boolean,
    onClickPost: (PostId) -> Unit,
    onClickPostLike: (PostId) -> Unit,
    onClickPostBookmark: (PostId, Boolean) -> Unit,
    onClickCreator: (CreatorId) -> Unit,
    modifier: Modifier = Modifier,
) {
    val post = item.post
    val url = item.url

    when {
        // FANBOX の投稿へのリンクはカードとして描画する。
        post != null -> {
            PostItem(
                modifier = modifier.padding(16.dp),
                post = post,
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

        // 外部サイトへのリンクは投稿として解決できない。fankt 0.1.0 が URL を保持するように
        // なったため、リンク先を表示できる。html は未信頼のため使わない。
        url != null -> ArticleUrlItem(modifier = modifier, url = url)

        else -> ArticleUnsupportedItem(modifier = modifier)
    }
}
