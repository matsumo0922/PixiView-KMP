package me.matsumo.fanbox.core.ui.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import kotlinx.serialization.Serializable
import me.matsumo.fanbox.core.model.Destination
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * バックスタックに積む1件分の遷移先。
 *
 * Navigation 3 は contentKey ごとに画面の状態と ViewModel を持つ。同じ [destination] を複数回積んでも
 * 状態が混ざらないよう、積むたびに固有の [id] を振り、contentKey として使う。
 */
@Serializable
data class DestinationKey(
    val destination: Destination,
    val id: String,
) : NavKey {
    companion object {
        /** [destination] に新しい [id] を振った [DestinationKey] を作る。 */
        @OptIn(ExperimentalUuidApi::class)
        fun of(destination: Destination): DestinationKey = DestinationKey(destination, Uuid.random().toString())
    }
}

/**
 * [Destination] ごとの画面定義から、[DestinationKey] を表示する entryProvider を作る。
 *
 * 各 feature は `EntryProviderScope<Destination>` へ画面を登録する。返す [NavEntry] の contentKey には
 * [DestinationKey.id] を使い、metadata は登録した画面のものを引き継ぐ。
 */
fun destinationEntryProvider(builder: EntryProviderScope<Destination>.() -> Unit): (DestinationKey) -> NavEntry<DestinationKey> {
    val provider = entryProvider(builder = builder)

    return { key ->
        val entry = provider(key.destination)

        NavEntry(
            key = key,
            contentKey = key.id,
            metadata = entry.metadata,
        ) {
            entry.Content()
        }
    }
}
