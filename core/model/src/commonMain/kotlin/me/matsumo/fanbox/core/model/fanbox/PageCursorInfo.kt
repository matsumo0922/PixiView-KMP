package me.matsumo.fanbox.core.model.fanbox

import androidx.compose.runtime.Immutable

/** カーソルでページングされる一覧。 */
@Immutable
data class PageCursorInfo<T>(
    val contents: List<T>,
    val cursor: Cursor?,
)

/** ページ番号でページングされる一覧。 */
@Immutable
data class PageNumberInfo<T>(
    val contents: List<T>,
    val nextPage: Int?,
)

/** オフセットでページングされる一覧。 */
@Immutable
data class PageOffsetInfo<T>(
    val contents: List<T>,
    val offset: Int?,
)
