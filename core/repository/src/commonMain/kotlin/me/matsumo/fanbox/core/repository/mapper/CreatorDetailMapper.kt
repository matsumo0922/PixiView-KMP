package me.matsumo.fanbox.core.repository.mapper

import me.matsumo.fanbox.core.model.fanbox.CreatorDetail
import me.matsumo.fanbox.core.model.fanbox.CreatorDetail.Platform
import me.matsumo.fanbox.core.model.fanbox.CreatorDetail.ProfileItem
import me.matsumo.fankt.fanbox.domain.model.FanboxCreatorDetail
import me.matsumo.fankt.fanbox.domain.model.FanboxCreatorDetail.Platform as FanktPlatform
import me.matsumo.fankt.fanbox.domain.model.FanboxCreatorDetail.ProfileItem as FanktProfileItem
import me.matsumo.fankt.fanbox.domain.model.FanboxCreatorDetail.ProfileLink as FanktProfileLink

fun FanboxCreatorDetail.toCreatorDetail(): CreatorDetail {
    return CreatorDetail(
        creatorId = creatorId.toCreatorId(),
        coverImageUrl = coverImageUrl,
        description = description,
        hasAdultContent = hasAdultContent,
        hasBoothShop = hasBoothShop,
        isAcceptingRequest = isAcceptingRequest,
        isFollowed = isFollowed,
        isStopped = isStopped,
        isSupported = isSupported,
        profileItems = profileItems.map { it.toProfileItem() },
        profileLinks = profileLinks.map { it.toProfileLink() },
        user = user?.toUser(),
    )
}

private fun FanktProfileItem.toProfileItem(): ProfileItem {
    return when (this) {
        is FanktProfileItem.Image -> ProfileItem.Image(
            id = id,
            imageUrl = imageUrl,
            thumbnailUrl = thumbnailUrl,
        )

        is FanktProfileItem.Video -> ProfileItem.Video(
            id = id,
            serviceProvider = serviceProvider,
            videoId = videoId,
            thumbnailUrl = thumbnailUrl,
        )

        is FanktProfileItem.Unknown -> ProfileItem.Unknown(
            id = id,
            type = type,
            rawJson = rawJson,
        )
    }
}

private fun FanktProfileLink.toProfileLink(): CreatorDetail.ProfileLink {
    return CreatorDetail.ProfileLink(
        url = url,
        link = link.toPlatform(),
    )
}

private fun FanktPlatform.toPlatform(): Platform {
    return when (this) {
        FanktPlatform.BOOTH -> Platform.BOOTH
        FanktPlatform.FACEBOOK -> Platform.FACEBOOK
        FanktPlatform.FANZA -> Platform.FANZA
        FanktPlatform.INSTAGRAM -> Platform.INSTAGRAM
        FanktPlatform.LINE -> Platform.LINE
        FanktPlatform.PIXIV -> Platform.PIXIV
        FanktPlatform.TUMBLR -> Platform.TUMBLR
        FanktPlatform.TWITTER -> Platform.TWITTER
        FanktPlatform.YOUTUBE -> Platform.YOUTUBE
        FanktPlatform.UNKNOWN -> Platform.UNKNOWN
    }
}
