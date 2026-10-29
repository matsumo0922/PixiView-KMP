package me.matsumo.fanbox.feature.post.image.items

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.persistentListOf
import me.matsumo.fanbox.core.resources.Res
import me.matsumo.fanbox.core.resources.post_image_all_download
import me.matsumo.fanbox.core.resources.post_image_download
import me.matsumo.fanbox.core.ui.view.Action
import me.matsumo.fanbox.core.ui.view.ActionSheet

@Composable
internal fun PostImageMenuDialog(
    onClickDownload: () -> Unit,
    onClickAllDownload: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val actions = persistentListOf(
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
        modifier = modifier,
        actions = actions,
        onDismissRequest = onDismissRequest,
    )
}
