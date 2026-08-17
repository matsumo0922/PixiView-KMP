package me.matsumo.fanbox.core.repository.mapper

import me.matsumo.fanbox.core.model.fanbox.Tag
import me.matsumo.fankt.fanbox.domain.model.FanboxTag

fun FanboxTag.toTag(): Tag {
    return Tag(
        count = count,
        coverImageUrl = coverImageUrl,
        name = name,
    )
}
