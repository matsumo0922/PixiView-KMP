package me.matsumo.fanbox.core.model

import androidx.compose.runtime.Immutable

/** FANBOX の request の組み立てと response の解析を担っている guest の出所。 */
@Immutable
sealed interface GuestSource {

    /** guest をロードしている途中。 */
    data object Loading : GuestSource

    /**
     * 配信された bundle（検証済みのキャッシュを含む）。[version] は bundle のビルド元の commit で、
     * ローカルでビルドした bundle では `null`。
     */
    data class Delivered(val version: String?) : GuestSource

    /** bundle をロードできず、fankt に組み込まれたリリース時点の guest で処理している。 */
    data object Native : GuestSource
}
