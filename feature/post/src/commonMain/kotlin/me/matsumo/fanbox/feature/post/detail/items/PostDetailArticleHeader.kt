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
import me.matsumo.fanbox.core.model.fanbox.PostDetail
import me.matsumo.fanbox.core.model.fanbox.PostId
import me.matsumo.fanbox.core.resources.Res
import me.matsumo.fanbox.core.resources.post_detail_unsupported_content
import me.matsumo.fanbox.core.ui.component.AdultContentThumbnail
import me.matsumo.fanbox.core.ui.component.PostItem
import me.matsumo.fanbox.core.ui.extensition.LocalFanboxMetadata
import org.jetbrains.compose.resources.stringResource
import sh.calvin.autolinktext.rememberAutoLinkText

internal fun LazyListScope.postDetailArticleHeader(
    content: PostDetail.Body.Article,
    setting: Setting,
    bookmarkedPostIds: ImmutableList<PostId>,
    isAdultContents: Boolean,
    isAutoImagePreview: Boolean,
    onClickPost: (PostId) -> Unit,
    onClickPostLike: (PostId) -> Unit,
    onClickPostBookmark: (Post, Boolean) -> Unit,
    onClickCreator: (CreatorId) -> Unit,
    onClickImage: (PostDetail.ImageItem) -> Unit,
    onClickFile: (PostDetail.FileItem) -> Unit,
    onClickDownload: (List<PostDetail.ImageItem>) -> Unit,
) {
    items(content.blocks) {
        val metadata = LocalFanboxMetadata.current

        when (it) {
            is PostDetail.Body.Article.Block.Text -> {
                ArticleTextItem(
                    modifier = Modifier.fillMaxWidth(),
                    item = it,
                )
            }

            is PostDetail.Body.Article.Block.Image -> {
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

            is PostDetail.Body.Article.Block.File -> {
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

            is PostDetail.Body.Article.Block.Link -> {
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

            is PostDetail.Body.Article.Block.Embed -> {
                // fankt は既知のサービスについてのみ URL を復元する。復元できない場合は開く先が
                // 無いため、未対応の要素として扱う。
                val embedUrl = it.url

                if (embedUrl != null) {
                    ArticleUrlItem(
                        modifier = Modifier.fillMaxWidth(),
                        url = embedUrl,
                    )
                } else {
                    ArticleUnsupportedItem(modifier = Modifier.fillMaxWidth())
                }
            }

            is PostDetail.Body.Article.Block.Unknown -> {
                // rawJson は検証されていないネットワークデータのため表示しない。
                ArticleUnsupportedItem(modifier = Modifier.fillMaxWidth())
            }
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
internal fun ArticleUnsupportedItem(modifier: Modifier = Modifier) {
    Text(
        modifier = modifier.padding(16.dp),
        text = stringResource(Res.string.post_detail_unsupported_content),
        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
    )
}

@Composable
private fun ArticleTextItem(
    item: PostDetail.Body.Article.Block.Text,
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
    item: PostDetail.Body.Article.Block.Link,
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
