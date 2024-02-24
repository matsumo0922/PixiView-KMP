@file:Suppress("MatchingDeclarationName")

package me.matsumo.fanbox.core.ui.view

import androidx.compose.runtime.Composable
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.compose.stringResource
import me.matsumo.fanbox.core.ui.MR
import moe.tlaster.precompose.navigation.Navigator
import moe.tlaster.precompose.navigation.RouteBuilder
import moe.tlaster.precompose.navigation.path

enum class SimpleAlertContents(
    val titleRes: StringResource,
    val descriptionRes: StringResource,
    val positiveTextRes: StringResource? = null,
    val negativeTextRes: StringResource? = null,
    val isCaution: Boolean = false,
) {
    FOR_IOS(
        titleRes = MR.strings.about_for_ios_title,
        descriptionRes = MR.strings.about_for_ios_description,
        positiveTextRes = MR.strings.common_ok,
    ),
    Login(
        titleRes = MR.strings.welcome_login_dialog_title,
        descriptionRes = MR.strings.welcome_login_dialog_message,
        positiveTextRes = MR.strings.common_ok,
    ),
    Logout(
        titleRes = MR.strings.setting_top_others_logout,
        descriptionRes = MR.strings.setting_top_others_logout_dialog_description,
        positiveTextRes = MR.strings.setting_top_others_logout,
        negativeTextRes = MR.strings.common_cancel,
    ),
    CommentDelete(
        titleRes = MR.strings.post_detail_comment_delete_title,
        descriptionRes = MR.strings.post_detail_comment_delete_message,
        positiveTextRes = MR.strings.common_delete,
        negativeTextRes = MR.strings.common_cancel,
        isCaution = true,
    ),
    PurchasePlus(
        titleRes = MR.strings.billing_plus_purchase_title,
        descriptionRes = MR.strings.billing_plus_purchase_message,
        positiveTextRes = MR.strings.common_ok,
    ),
    CancelPlus(
        titleRes = MR.strings.billing_plus_cancel_title,
        descriptionRes = MR.strings.billing_plus_cancel_message,
        positiveTextRes = MR.strings.common_ok,
    ),
    CancelDownload(
        titleRes = MR.strings.creator_posts_download_alert_title,
        descriptionRes = MR.strings.creator_posts_download_alert_message,
        positiveTextRes = MR.strings.creator_posts_download_alert_stop,
        negativeTextRes = MR.strings.common_cancel,
    )
}

const val SimpleAlertDialogContent = "simpleAlertDialogSongs"
const val SimpleAlertDialog = "simpleAlertDialog/{$SimpleAlertDialogContent}"

suspend fun Navigator.navigateToSimpleAlertDialog(
    content: SimpleAlertContents,
    onClickPositive: (() -> Unit)? = null,
    onClickNegative: (() -> Unit)? = null,
) {
    val result = navigateForResult("simpleAlertDialog/$content") as? Boolean ?: return

    if (result) {
        onClickPositive?.invoke()
    } else {
        onClickNegative?.invoke()
    }
}

fun RouteBuilder.simpleAlertDialogDialog(
    onResult: (Boolean) -> Unit,
) {
    dialog(SimpleAlertDialog) { entry ->
        val content = SimpleAlertContents.valueOf(entry.path<String>(SimpleAlertDialogContent).orEmpty())

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
