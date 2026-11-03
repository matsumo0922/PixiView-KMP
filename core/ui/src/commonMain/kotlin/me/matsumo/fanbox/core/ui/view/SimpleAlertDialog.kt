package me.matsumo.fanbox.core.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.scene.DialogSceneStrategy
import me.matsumo.fanbox.core.model.Destination
import me.matsumo.fanbox.core.model.SimpleAlertContents
import me.matsumo.fanbox.core.ui.navigation.PixiViewNavigator
import org.jetbrains.compose.resources.stringResource

fun PixiViewNavigator.navigateToSimpleAlertDialog(
    content: SimpleAlertContents,
    onClickPositive: (() -> Unit)? = null,
    onClickNegative: (() -> Unit)? = null,
) {
    navigateForResult<Boolean>(Destination.SimpleAlertDialog(content)) { result ->
        if (result) {
            onClickPositive?.invoke()
        } else {
            onClickNegative?.invoke()
        }
    }
}

fun EntryProviderScope<Destination>.simpleAlertDialogEntry(onResult: (Boolean) -> Unit) {
    entry<Destination.SimpleAlertDialog>(
        metadata = DialogSceneStrategy.dialog(),
    ) { destination ->
        val content = destination.content

        SimpleAlertDialog(
            title = stringResource(content.titleRes),
            description = stringResource(content.descriptionRes),
            positiveText = content.positiveTextRes?.let { stringResource(it) },
            negativeText = content.negativeTextRes?.let { stringResource(it) },
            onClickPositive = { onResult.invoke(true) },
            onClickNegative = { onResult.invoke(false) },
            isCaution = content.isCaution,
        )
    }
}

@Composable
expect fun SimpleAlertDialog(
    title: String,
    description: String,
    positiveText: String?,
    negativeText: String?,
    onClickPositive: () -> Unit,
    onClickNegative: () -> Unit,
    isCaution: Boolean,
    modifier: Modifier = Modifier,
)
