package me.matsumo.fanbox.core.repository.mapper

import me.matsumo.fanbox.core.model.fanbox.CreatorPlan
import me.matsumo.fankt.fanbox.domain.model.FanboxCreatorPlan

fun FanboxCreatorPlan.toCreatorPlan(): CreatorPlan {
    return CreatorPlan(
        id = id.toPlanId(),
        title = title,
        description = description,
        fee = fee,
        coverImageUrl = coverImageUrl,
        hasAdultContent = hasAdultContent,
        paymentMethod = paymentMethod.toPaymentMethod(),
        user = user?.toUser(),
    )
}
