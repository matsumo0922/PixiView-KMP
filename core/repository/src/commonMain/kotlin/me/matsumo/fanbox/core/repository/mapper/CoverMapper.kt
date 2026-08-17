package me.matsumo.fanbox.core.repository.mapper

import me.matsumo.fanbox.core.model.fanbox.Cover
import me.matsumo.fankt.fanbox.domain.model.FanboxCover

fun FanboxCover.toCover(): Cover {
    return Cover(
        url = url,
        type = type,
    )
}
