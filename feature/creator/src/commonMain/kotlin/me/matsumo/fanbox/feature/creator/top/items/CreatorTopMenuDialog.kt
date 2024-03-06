package me.matsumo.fanbox.feature.creator.top.items

import androidx.compose.runtime.Composable
import me.matsumo.fanbox.core.ui.MR
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
            text = MR.strings.common_block,
            onClick = onClickBlock,
        ),
    )

    ActionSheet(
        isVisible = isVisible,
        actions = actions,
        onDismissRequest = onDismissRequest,
    )
}
