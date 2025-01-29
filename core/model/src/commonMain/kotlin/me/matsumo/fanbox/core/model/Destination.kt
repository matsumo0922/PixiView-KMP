package me.matsumo.fanbox.core.model

import me.matsumo.fankt.fanbox.domain.model.id.FanboxCreatorId
import me.matsumo.fankt.fanbox.domain.model.id.FanboxPostId

sealed interface Destination {

    data object Library : Destination

    data class PostDetail(
        val postId: FanboxPostId,
        val pagingType: PagingType,
    ) : Destination {
        enum class PagingType {
            Home,
            Supported,
            Creator,
            Search,
            Unknown,
        }
    }

    data class PostImage(
        val postId: FanboxPostId,
        val index: Int,
    ) : Destination

    data class PostSearch(
        val creatorId: FanboxCreatorId?,
        val creatorQuery: String?,
        val tag: String?,
    ) : Destination

    data class PostByCreatorSearch(
        val creatorId: FanboxCreatorId,
    ) : Destination

    data object BookmarkedPosts: Destination

    data class CreatorTop(
        val creatorId: FanboxCreatorId,
        val isPosts: Boolean,
    ) : Destination

    data class CreatorPostsDownload(
        val creatorId: FanboxCreatorId,
    ) : Destination

    data object SupportingCreators: Destination

    data object FollowerCreators: Destination

    data object Payments: Destination

    data class FanCard(
        val creatorId: FanboxCreatorId,
    ) : Destination

    data object DownloadQueue: Destination

    data object About: Destination

    data object SettingTop: Destination

    data object SettingTheme: Destination

    data object SettingLicense: Destination

    data object SettingDirectory: Destination


    // Dialog

    data class SimpleAlertDialog(
        val content: SimpleAlertContents,
    ) : Destination

    data object SettingDeveloperDialog: Destination


    // Bottom Sheet

    data object VersionHistoryBottomSheet: Destination

    data class BillingPlusBottomSheet(
        val referrer: String?,
    ) : Destination


    // Empty

    data object Empty: Destination
}
