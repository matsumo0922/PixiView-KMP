package me.matsumo.fanbox.core.repository.mapper

import me.matsumo.fanbox.core.model.fanbox.PageCursorInfo
import me.matsumo.fanbox.core.model.fanbox.PageNumberInfo
import me.matsumo.fanbox.core.model.fanbox.PageOffsetInfo
import me.matsumo.fankt.fanbox.domain.PageCursorInfo as FanktPageCursorInfo
import me.matsumo.fankt.fanbox.domain.PageNumberInfo as FanktPageNumberInfo
import me.matsumo.fankt.fanbox.domain.PageOffsetInfo as FanktPageOffsetInfo

fun <T, R> FanktPageCursorInfo<T>.toPageCursorInfo(contentMapper: (T) -> R): PageCursorInfo<R> {
    return PageCursorInfo(
        contents = contents.map(contentMapper),
        cursor = cursor?.toCursor(),
    )
}

fun <T, R> FanktPageNumberInfo<T>.toPageNumberInfo(contentMapper: (T) -> R): PageNumberInfo<R> {
    return PageNumberInfo(
        contents = contents.map(contentMapper),
        nextPage = nextPage,
    )
}

fun <T, R> FanktPageOffsetInfo<T>.toPageOffsetInfo(contentMapper: (T) -> R): PageOffsetInfo<R> {
    return PageOffsetInfo(
        contents = contents.map(contentMapper),
        offset = offset,
    )
}
