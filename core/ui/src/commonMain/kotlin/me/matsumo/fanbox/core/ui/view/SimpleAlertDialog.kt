@file:Suppress("MatchingDeclarationName")

package me.matsumo.fanbox.core.ui.view

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.dialog
import androidx.navigation.navArgument
import me.matsumo.fanbox.core.ui.Res
import me.matsumo.fanbox.core.ui.billing_plus_cancel_message
import me.matsumo.fanbox.core.ui.billing_plus_cancel_title
import me.matsumo.fanbox.core.ui.billing_plus_purchase_message
import me.matsumo.fanbox.core.ui.billing_plus_purchase_title
import me.matsumo.fanbox.core.ui.common_block
import me.matsumo.fanbox.core.ui.common_cancel
import me.matsumo.fanbox.core.ui.common_delete
import me.matsumo.fanbox.core.ui.common_ok
import me.matsumo.fanbox.core.ui.common_unblock
import me.matsumo.fanbox.core.ui.creator_block_dialog_message
import me.matsumo.fanbox.core.ui.creator_block_dialog_title
import me.matsumo.fanbox.core.ui.creator_posts_download_alert_message
import me.matsumo.fanbox.core.ui.creator_posts_download_alert_stop
import me.matsumo.fanbox.core.ui.creator_posts_download_alert_title
import me.matsumo.fanbox.core.ui.creator_unblock_dialog_message
import me.matsumo.fanbox.core.ui.creator_unblock_dialog_title
import me.matsumo.fanbox.core.ui.extensition.navigateForResult
import me.matsumo.fanbox.core.ui.post_detail_comment_delete_message
import me.matsumo.fanbox.core.ui.post_detail_comment_delete_title
import me.matsumo.fanbox.core.ui.setting_top_others_logout
import me.matsumo.fanbox.core.ui.setting_top_others_logout_dialog_description
import me.matsumo.fanbox.core.ui.welcome_login_debug_dialog_message
import me.matsumo.fanbox.core.ui.welcome_login_debug_dialog_title
import me.matsumo.fanbox.core.ui.welcome_login_dialog_message
import me.matsumo.fanbox.core.ui.welcome_login_dialog_title
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

enum class SimpleAlertContents(
    val titleRes: StringResource,
    val descriptionRes: StringResource,
    val positiveTextRes: StringResource? = null,
    val negativeTextRes: StringResource? = null,
    val isCaution: Boolean = false,
) {
    Login(
        titleRes = Res.string.welcome_login_dialog_title,
        descriptionRes = Res.string.welcome_login_dialog_message,
        positiveTextRes = Res.string.common_ok,
    ),
    Logout(
        titleRes = Res.string.setting_top_others_logout,
        descriptionRes = Res.string.setting_top_others_logout_dialog_description,
        positiveTextRes = Res.string.setting_top_others_logout,
        negativeTextRes = Res.string.common_cancel,
    ),
    LoginDebug(
        titleRes = Res.string.welcome_login_debug_dialog_title,
        descriptionRes = Res.string.welcome_login_debug_dialog_message,
        positiveTextRes = Res.string.common_ok,
        negativeTextRes = Res.string.common_cancel,
    ),
    CommentDelete(
        titleRes = Res.string.post_detail_comment_delete_title,
        descriptionRes = Res.string.post_detail_comment_delete_message,
        positiveTextRes = Res.string.common_delete,
        negativeTextRes = Res.string.common_cancel,
        isCaution = true,
    ),
    PurchasePlus(
        titleRes = Res.string.billing_plus_purchase_title,
        descriptionRes = Res.string.billing_plus_purchase_message,
        positiveTextRes = Res.string.common_ok,
    ),
    CancelPlus(
        titleRes = Res.string.billing_plus_cancel_title,
        descriptionRes = Res.string.billing_plus_cancel_message,
        positiveTextRes = Res.string.common_ok,
    ),
    CancelDownload(
        titleRes = Res.string.creator_posts_download_alert_title,
        descriptionRes = Res.string.creator_posts_download_alert_message,
        positiveTextRes = Res.string.creator_posts_download_alert_stop,
        negativeTextRes = Res.string.common_cancel,
    ),
    CreatorBlock(
        titleRes = Res.string.creator_block_dialog_title,
        descriptionRes = Res.string.creator_block_dialog_message,
        positiveTextRes = Res.string.common_block,
        negativeTextRes = Res.string.common_cancel,
    ),
    CreatorUnblock(
        titleRes = Res.string.creator_unblock_dialog_title,
        descriptionRes = Res.string.creator_unblock_dialog_message,
        positiveTextRes = Res.string.common_unblock,
        negativeTextRes = Res.string.common_cancel,
    ),
}

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
    dialog(
        route = SimpleAlertDialog,
        arguments = listOf(navArgument(SimpleAlertDialogContent) { type = NavType.StringType }),
    ) { entry ->
        val content = SimpleAlertContents.valueOf(entry.arguments?.getString(SimpleAlertDialogContent).orEmpty())

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
