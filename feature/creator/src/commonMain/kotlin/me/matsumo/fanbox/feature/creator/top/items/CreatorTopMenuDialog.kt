package me.matsumo.fanbox.feature.creator.top.items

import androidx.compose.runtime.Composable
import me.matsumo.fanbox.core.ui.Res
import me.matsumo.fanbox.core.ui.common_block
import me.matsumo.fanbox.core.ui.view.Action
import me.matsumo.fanbox.core.ui.view.ActionSheet

@Composable
internal fun CreatorTopMenuDialog(
    isVisible: Boolean,
    onClickBlock: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    val actions = listOf(
        Action(
            text = Res.string.common_block,
            onClick = onClickBlock,
        ),
    )

    ActionSheet(
        isVisible = isVisible,
        actions = actions,
        onDismissRequest = onDismissRequest,
    )
}
