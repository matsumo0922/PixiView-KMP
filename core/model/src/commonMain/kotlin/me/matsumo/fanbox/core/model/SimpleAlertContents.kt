package me.matsumo.fanbox.core.model

import kotlinx.serialization.Serializable
import me.matsumo.fanbox.core.resources.Res
import me.matsumo.fanbox.core.resources.billing_plus_cancel_message
import me.matsumo.fanbox.core.resources.billing_plus_cancel_title
import me.matsumo.fanbox.core.resources.billing_plus_purchase_message
import me.matsumo.fanbox.core.resources.billing_plus_purchase_title
import me.matsumo.fanbox.core.resources.common_block
import me.matsumo.fanbox.core.resources.common_cancel
import me.matsumo.fanbox.core.resources.common_delete
import me.matsumo.fanbox.core.resources.common_ok
import me.matsumo.fanbox.core.resources.common_unblock
import me.matsumo.fanbox.core.resources.creator_block_dialog_message
import me.matsumo.fanbox.core.resources.creator_block_dialog_title
import me.matsumo.fanbox.core.resources.creator_posts_download_alert_message
import me.matsumo.fanbox.core.resources.creator_posts_download_alert_stop
import me.matsumo.fanbox.core.resources.creator_posts_download_alert_title
import me.matsumo.fanbox.core.resources.creator_unblock_dialog_message
import me.matsumo.fanbox.core.resources.creator_unblock_dialog_title
import me.matsumo.fanbox.core.resources.post_detail_comment_delete_message
import me.matsumo.fanbox.core.resources.post_detail_comment_delete_title
import me.matsumo.fanbox.core.resources.setting_top_others_logout
import me.matsumo.fanbox.core.resources.setting_top_others_logout_dialog_description
import me.matsumo.fanbox.core.resources.welcome_login_debug_dialog_message
import me.matsumo.fanbox.core.resources.welcome_login_debug_dialog_title
import me.matsumo.fanbox.core.resources.welcome_login_dialog_message
import me.matsumo.fanbox.core.resources.welcome_login_dialog_title
import org.jetbrains.compose.resources.StringResource

@Serializable
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
