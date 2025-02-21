package me.matsumo.fanbox.core.model

import kotlinx.serialization.Serializable
import me.matsumo.fankt.fanbox.domain.model.id.FanboxCreatorId
import me.matsumo.fankt.fanbox.domain.model.id.FanboxPostId

@Serializable
sealed interface Destination {

    @Serializable
    data object Library : Destination

    @Serializable
    data class PostDetail(
        val postId: FanboxPostId,
        val pagingType: PagingType = PagingType.Unknown,
    ) : Destination {
        @Serializable
        enum class PagingType {
            Home,
            Supported,
            Creator,
            Search,
            Unknown,
        }
    }

    @Serializable
    data class PostImage(
        val postId: FanboxPostId,
        val index: Int,
    ) : Destination

    @Serializable
    data class PostSearch(
        val creatorId: FanboxCreatorId,
        val creatorQuery: String?,
        val tag: String?,
    ) : Destination

    @Serializable
    data class PostByCreatorSearch(
        val creatorId: FanboxCreatorId,
    ) : Destination

    @Serializable
    data object BookmarkedPosts : Destination

    @Serializable
    data class CreatorTop(
        val creatorId: FanboxCreatorId,
        val isPosts: Boolean = true,
    ) : Destination

    @Serializable
    data class CreatorPostsDownload(
        val creatorId: FanboxCreatorId,
    ) : Destination

    @Serializable
    data object SupportingCreators : Destination

    @Serializable
    data object FollowingCreators : Destination

    @Serializable
    data object Payments : Destination

    @Serializable
    data class FanCard(
        val creatorId: FanboxCreatorId,
    ) : Destination

    @Serializable
    data object DownloadQueue : Destination

    @Serializable
    data object About : Destination

    @Serializable
    data object SettingTop : Destination

    @Serializable
    data object SettingTheme : Destination

    @Serializable
    data object SettingLicense : Destination

    @Serializable
    data object SettingDirectory : Destination

    @Serializable
    data object WelcomeTop : Destination

    @Serializable
    data object WelcomeLogin : Destination

    @Serializable
    data object WelcomeWeb : Destination

    // Dialog

    @Serializable
    data class SimpleAlertDialog(
        val content: SimpleAlertContents,
    ) : Destination

    @Serializable
    data object SettingDeveloperDialog : Destination

    @Serializable
    data class SettingTranslationDialog(
        val language: String,
    ) : Destination

    // Bottom Sheet

    @Serializable
    data object VersionHistoryBottomSheet : Destination

    @Serializable
    data class BillingPlusBottomSheet(
        val referrer: String?,
    ) : Destination

    // Empty

    @Serializable
    data object Empty : Destination
}
