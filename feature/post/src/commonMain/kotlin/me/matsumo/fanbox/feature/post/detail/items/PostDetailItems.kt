package me.matsumo.fanbox.feature.post.detail.items

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.ImmutableList
import me.matsumo.fanbox.core.model.Setting
import me.matsumo.fanbox.core.model.fanbox.CreatorId
import me.matsumo.fanbox.core.model.fanbox.Post
import me.matsumo.fanbox.core.model.fanbox.PostDetail
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
        is PostDetail.Body.Article -> {
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

        is PostDetail.Body.Image -> {
            postDetailImageHeader(
                content = content,
                isAdultContents = post.hasAdultContent,
                isOverrideAdultContents = setting.isAllowedShowAdultContents,
                isTestUser = setting.isTestUser,
                onClickImage = onClickImage,
                onClickDownload = onClickDownload,
            )
        }

        is PostDetail.Body.File -> {
            postDetailFileHeader(
                content = content,
                isAutoImagePreview = setting.isAutoImagePreview,
                onClickFile = onClickFile,
                onClickImage = onClickImage,
                onClickDownload = onClickDownload,
            )
        }

        is PostDetail.Body.Text -> {
            item {
                ArticleUrlItem(
                    modifier = Modifier.fillMaxWidth(),
                    url = content.text,
                )
            }
        }

        is PostDetail.Body.Video -> {
            item {
                // fankt は既知のサービスについてのみ URL を復元する。復元できない場合は開く先が
                // 無いため、未対応の要素として扱う。
                val url = content.url

                if (url != null) {
                    ArticleUrlItem(
                        modifier = Modifier.fillMaxWidth(),
                        url = url,
                    )
                } else {
                    ArticleUnsupportedItem(modifier = Modifier.fillMaxWidth())
                }
            }
        }

        is PostDetail.Body.Html -> {
            item {
                // html は検証されていないネットワークデータで、そのまま描画するとスクリプトや
                // 外部リソースの読み込みを許すことになる。描画方法が決まるまで未対応として扱う。
                ArticleUnsupportedItem(modifier = Modifier.fillMaxWidth())
            }
        }

        is PostDetail.Body.Unknown -> {
            item {
                // rawBodyJson は検証されていないネットワークデータのため表示しない。
                ArticleUnsupportedItem(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}
