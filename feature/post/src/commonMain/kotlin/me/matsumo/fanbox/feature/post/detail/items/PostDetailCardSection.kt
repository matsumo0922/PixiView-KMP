package me.matsumo.fanbox.feature.post.detail.items

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.matsumo.fanbox.core.model.fanbox.FanboxPostDetail
import me.matsumo.fanbox.core.model.fanbox.id.CreatorId
import me.matsumo.fanbox.core.ui.component.RestrictCardItem
import me.matsumo.fanbox.core.ui.extensition.padding

internal fun LazyListScope.postDetailCardSection(
    postDetail: FanboxPostDetail,
    onClickCreatorPlans: (CreatorId) -> Unit,
    onClickDownloadImages: (List<FanboxPostDetail.ImageItem>) -> Unit,
) {
    if (postDetail.isRestricted) {
        item {
            RestrictCardItem(
                modifier = Modifier
                    .padding(horizontal = 16.dp, top = 16.dp)
                    .fillMaxWidth(),
                feeRequired = postDetail.feeRequired,
                backgroundColor = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp),
                onClickPlanList = { onClickCreatorPlans.invoke(postDetail.user.creatorId) },
            )
        }
    } else if (postDetail.body.imageItems.isNotEmpty()) {
        item {
            PostDetailDownloadSection(
                modifier = Modifier
                    .padding(horizontal = 16.dp, top = 16.dp)
                    .fillMaxWidth(),
                postDetail = postDetail,
                onClickDownload = onClickDownloadImages,
            )
        }
    }
}