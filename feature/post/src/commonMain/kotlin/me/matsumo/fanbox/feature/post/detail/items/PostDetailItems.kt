package me.matsumo.fanbox.feature.post.detail.items

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.ImmutableList
import me.matsumo.fanbox.core.model.Setting
import me.matsumo.fankt.fanbox.domain.model.FanboxPost
import me.matsumo.fankt.fanbox.domain.model.FanboxPostDetail
import me.matsumo.fankt.fanbox.domain.model.id.FanboxCreatorId
import me.matsumo.fankt.fanbox.domain.model.id.FanboxPostId

internal fun LazyListScope.postDetailItems(
    post: FanboxPostDetail,
    setting: Setting,
    bookmarkedPostIds: ImmutableList<FanboxPostId>,
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

        is FanboxPostDetail.Body.Image -> {
            postDetailImageHeader(
                content = content,
                isAdultContents = post.hasAdultContent,
                isOverrideAdultContents = setting.isAllowedShowAdultContents,
                isTestUser = setting.isTestUser,
                onClickImage = onClickImage,
                onClickDownload = onClickDownload,
            )
        }

        is FanboxPostDetail.Body.File -> {
            postDetailFileHeader(
                content = content,
                isAutoImagePreview = setting.isAutoImagePreview,
                onClickFile = onClickFile,
                onClickImage = onClickImage,
                onClickDownload = onClickDownload,
            )
        }

        is FanboxPostDetail.Body.Text -> {
            item {
                ArticleUrlItem(
                    modifier = Modifier.fillMaxWidth(),
                    url = content.text,
                )
            }
        }

        is FanboxPostDetail.Body.Video -> {
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

        is FanboxPostDetail.Body.Html -> {
            item {
                // html は検証されていないネットワークデータで、そのまま描画するとスクリプトや
                // 外部リソースの読み込みを許すことになる。描画方法が決まるまで未対応として扱う。
                ArticleUnsupportedItem(modifier = Modifier.fillMaxWidth())
            }
        }

        is FanboxPostDetail.Body.Unknown -> {
            item {
                // rawBodyJson は検証されていないネットワークデータのため表示しない。
                ArticleUnsupportedItem(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}
