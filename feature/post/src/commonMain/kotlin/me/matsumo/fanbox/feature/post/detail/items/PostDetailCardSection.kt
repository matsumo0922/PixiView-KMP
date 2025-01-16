package me.matsumo.fanbox.feature.post.detail.items

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.matsumo.fanbox.core.ui.component.RestrictCardItem
import me.matsumo.fanbox.core.ui.extensition.padding
import me.matsumo.fankt.fanbox.domain.model.FanboxPostDetail
import me.matsumo.fankt.fanbox.domain.model.id.FanboxCreatorId

internal fun LazyListScope.postDetailCardSection(
    postDetail: FanboxPostDetail,
    onClickCreatorPlans: (FanboxCreatorId) -> Unit,
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
                onClickPlanList = { postDetail.user?.creatorId?.let(onClickCreatorPlans) },
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
