@file:Suppress("MatchingDeclarationName")

package me.matsumo.fanbox.core.ui.view

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.dialog
import androidx.navigation.toRoute
import me.matsumo.fanbox.core.model.Destination
import me.matsumo.fanbox.core.model.SimpleAlertContents
import me.matsumo.fanbox.core.ui.extensition.navigateForResult
import org.jetbrains.compose.resources.stringResource

const val SimpleAlertDialogContent = "simpleAlertDialogSongs"
const val SimpleAlertDialog = "simpleAlertDialog/{$SimpleAlertDialogContent}"

fun NavController.navigateToSimpleAlertDialog(
    content: SimpleAlertContents,
    onClickPositive: (() -> Unit)? = null,
    onClickNegative: (() -> Unit)? = null,
) {
    navigateForResult<Boolean>(
        route = "simpleAlertDialog/${content.name}",
        navResultCallback = { result ->
            if (result) {
                onClickPositive?.invoke()
            } else {
                onClickNegative?.invoke()
            }
        },
    )
}

fun NavGraphBuilder.simpleAlertDialogDialog(
    onResult: (Boolean) -> Unit,
) {
    dialog<Destination.SimpleAlertDialog> { entry ->
        val content = entry.toRoute<Destination.SimpleAlertDialog>().content

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
)
