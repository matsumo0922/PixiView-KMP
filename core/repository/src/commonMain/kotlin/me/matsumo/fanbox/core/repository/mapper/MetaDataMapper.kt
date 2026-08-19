package me.matsumo.fanbox.core.repository.mapper

import me.matsumo.fanbox.core.model.fanbox.MetaData
import me.matsumo.fanbox.core.model.fanbox.MetaData.Context
import me.matsumo.fankt.fanbox.domain.model.FanboxMetaData
import me.matsumo.fankt.fanbox.domain.model.FanboxMetaData.Context as FanktContext

fun FanboxMetaData.toMetaData(): MetaData {
    return MetaData(
        apiUrl = apiUrl,
        context = context?.toContext(),
        csrfToken = csrfToken,
    )
}

private fun FanktContext.toContext(): Context {
    return Context(
        privacyPolicy = privacyPolicy.toPrivacyPolicy(),
        user = user.toContextUser(),
    )
}

private fun FanktContext.PrivacyPolicy.toPrivacyPolicy(): Context.PrivacyPolicy {
    return Context.PrivacyPolicy(
        policyUrl = policyUrl,
        revisionHistoryUrl = revisionHistoryUrl,
        shouldShowNotice = shouldShowNotice,
        updateDate = updateDate,
    )
}

private fun FanktContext.User.toContextUser(): Context.User {
    return Context.User(
        creatorId = creatorId?.toCreatorId(),
        fanboxUserStatus = fanboxUserStatus,
        hasAdultContent = hasAdultContent,
        hasUnpaidPayments = hasUnpaidPayments,
        iconUrl = iconUrl,
        isCreator = isCreator,
        isMailAddressOutdated = isMailAddressOutdated,
        isSupporter = isSupporter,
        lang = lang,
        name = name,
        planCount = planCount,
        showAdultContent = showAdultContent,
        userId = userId?.toUserId(),
    )
}
