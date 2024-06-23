package me.matsumo.fanbox.feature.post.detail.items

import androidx.compose.runtime.Composable
import me.matsumo.fanbox.core.ui.Res
import me.matsumo.fanbox.core.ui.post_detail_open_browser
import me.matsumo.fanbox.core.ui.post_image_all_download
import me.matsumo.fanbox.core.ui.view.Action
import me.matsumo.fanbox.core.ui.view.ActionSheet

@Composable
internal fun PostDetailMenuDialog(
    isVisible: Boolean,
    onClickOpenBrowser: () -> Unit,
    onClickAllDownload: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    val actions = listOf(
        Action(
            text = Res.string.post_detail_open_browser,
            onClick = onClickOpenBrowser,
        ),
        Action(
            text = Res.string.post_image_all_download,
            onClick = onClickAllDownload,
        ),
    )

    ActionSheet(
        isVisible = isVisible,
        actions = actions,
        onDismissRequest = onDismissRequest,
    )
}
