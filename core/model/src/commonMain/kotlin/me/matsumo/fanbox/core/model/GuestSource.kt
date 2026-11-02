package me.matsumo.fanbox.core.model

import androidx.compose.runtime.Immutable

/** FANBOX の request の組み立てと response の解析を担っている guest の出所。 */
@Immutable
sealed interface GuestSource {

    /** guest をロードしている途中。 */
    data object Loading : GuestSource

    /**
     * 配信された bundle。[version] は bundle のビルド元の commit で、ローカルでビルドした bundle では `null`。
     * [isFromCache] は、配信先からロードできず、検証済みのキャッシュからロードしたときに `true`。
     */
    @Immutable
    data class Delivered(
        val version: String?,
        val isFromCache: Boolean,
    ) : GuestSource

    /** bundle をロードできず、fankt に組み込まれたリリース時点の guest で処理している。 */
    data object Native : GuestSource
}
