package me.matsumo.fanbox.feature.creator.di

import me.matsumo.fanbox.feature.creator.download.CreatorPostsDownloadViewModel
import me.matsumo.fanbox.feature.creator.fancard.FanCardViewModel
import me.matsumo.fanbox.feature.creator.follow.FollowingCreatorsViewModel
import me.matsumo.fanbox.feature.creator.payment.PaymentsViewModel
import me.matsumo.fanbox.feature.creator.support.SupportingCreatorsViewModel
import me.matsumo.fanbox.feature.creator.top.CreatorTopViewModel
import org.koin.dsl.module

val creatorModule = module {

    factory {
        CreatorTopViewModel(
            userDataRepository = get(),
            fanboxRepository = get(),
            pixiViewConfig = get(),
        )
    }

    factory {
        SupportingCreatorsViewModel(
            fanboxRepository = get(),
        )
    }

    factory {
        PaymentsViewModel(
            fanboxRepository = get(),
        )
    }

    factory {
        FollowingCreatorsViewModel(
            fanboxRepository = get(),
        )
    }

    factory {
        FanCardViewModel(
            fanboxRepository = get(),
        )
    }

    factory {
        CreatorPostsDownloadViewModel(
            fanboxRepository = get(),
            ioDispatcher = get(),
        )
    }
}
