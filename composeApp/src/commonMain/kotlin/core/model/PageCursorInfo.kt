package core.model

import core.model.fanbox.FanboxCursor

data class PageCursorInfo<T>(
    val contents: List<T>,
    val cursor: FanboxCursor?,
)

data class PageNumberInfo<T>(
    val contents: List<T>,
    val nextPage: Int?,
)

data class PageOffsetInfo<T>(
    val contents: List<T>,
    val offset: Int?,
)
