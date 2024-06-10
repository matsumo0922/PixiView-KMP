package me.matsumo.fanbox.feature.creator.di

import me.matsumo.fanbox.feature.creator.download.CreatorPostsDownloadViewModel
import me.matsumo.fanbox.feature.creator.fancard.FanCardViewModel
import me.matsumo.fanbox.feature.creator.follow.FollowingCreatorsViewModel
import me.matsumo.fanbox.feature.creator.payment.PaymentsViewModel
import me.matsumo.fanbox.feature.creator.support.SupportingCreatorsViewModel
import me.matsumo.fanbox.feature.creator.top.CreatorTopViewModel
import org.koin.compose.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val creatorModule = module {
    viewModelOf(::CreatorTopViewModel)
    viewModelOf(::SupportingCreatorsViewModel)
    viewModelOf(::PaymentsViewModel)
    viewModelOf(::FollowingCreatorsViewModel)
    viewModelOf(::FanCardViewModel)
    viewModelOf(::CreatorPostsDownloadViewModel)
}
