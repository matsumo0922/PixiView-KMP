package me.matsumo.fanbox.feature.post.image.items

import androidx.compose.runtime.Composable
import me.matsumo.fanbox.core.ui.Res
import me.matsumo.fanbox.core.ui.post_image_all_download
import me.matsumo.fanbox.core.ui.post_image_download
import me.matsumo.fanbox.core.ui.view.Action
import me.matsumo.fanbox.core.ui.view.ActionSheet

@Composable
internal fun PostImageMenuDialog(
    isVisible: Boolean,
    onClickDownload: () -> Unit,
    onClickAllDownload: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    val actions = listOf(
        Action(
            text = Res.string.post_image_download,
            onClick = onClickDownload,
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
