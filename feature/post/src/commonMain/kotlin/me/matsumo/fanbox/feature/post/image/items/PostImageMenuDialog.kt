package me.matsumo.fanbox.feature.post.image.items

import androidx.compose.runtime.Composable
import me.matsumo.fanbox.core.ui.MR
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
            text = MR.strings.post_image_download,
            onClick = onClickDownload,
        ),
        Action(
            text = MR.strings.post_image_all_download,
            onClick = onClickAllDownload,
        ),
    )

    ActionSheet(
        isVisible = isVisible,
        actions = actions,
        onDismissRequest = onDismissRequest,
    )
}
