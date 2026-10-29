package me.matsumo.fanbox.feature.creator.top.items

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.persistentListOf
import me.matsumo.fanbox.core.resources.Res
import me.matsumo.fanbox.core.resources.common_block
import me.matsumo.fanbox.core.ui.view.Action
import me.matsumo.fanbox.core.ui.view.ActionSheet

@Composable
internal fun CreatorTopMenuDialog(
    onClickBlock: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val actions = persistentListOf(
        Action(
            text = Res.string.common_block,
            onClick = onClickBlock,
        ),
    )

    ActionSheet(
        modifier = modifier,
        actions = actions,
        onDismissRequest = onDismissRequest,
    )
}
