package me.matsumo.fanbox.core.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.serialization.NavBackStackSerializer
import me.matsumo.fanbox.core.model.Destination

/**
 * [PixiViewNavigator.navigateForResult] で積んだ [DestinationKey.id] と、結果を受け取るコールバックの対応。
 *
 * 構成変更で Activity と Navigator が作り直されても、復元したバックスタックの id から結果を返せるよう、プロセス単位で持つ。
 */
private val pendingResultCallbacks = mutableMapOf<String, (Any?) -> Unit>()

/**
 * 画面遷移のバックスタックを操作する。
 *
 * 遷移先から結果を受け取る場合は [navigateForResult] で遷移し、遷移先は [backWithResult] で結果を返して戻る。
 * 結果を返さずに戻った場合、その遷移先のコールバックは破棄する。
 */
@Stable
class PixiViewNavigator(val backStack: NavBackStack<DestinationKey>) {

    /** バックスタックの一番上にある遷移先。 */
    val currentDestination: Destination
        get() = backStack.last().destination

    fun navigate(destination: Destination) {
        backStack.add(DestinationKey.of(destination))
    }

    /** 1つ前の画面へ戻る。NavDisplay は空のバックスタックを表示できないため、根の画面は取り除かない。 */
    fun back() {
        if (backStack.size <= 1) return

        val removedKey = backStack.removeAt(backStack.lastIndex)

        pendingResultCallbacks -= removedKey.id
    }

    fun <T> navigateForResult(destination: Destination, onResult: (T) -> Unit) {
        val key = DestinationKey.of(destination)

        pendingResultCallbacks[key.id] = { result ->
            @Suppress("UNCHECKED_CAST")
            onResult(result as T)
        }

        backStack.add(key)
    }

    /** 1つ前の画面へ戻り、[navigateForResult] の呼び出し元へ [result] を返す。 */
    fun <T> backWithResult(result: T) {
        val callback = pendingResultCallbacks.remove(backStack.last().id)

        back()
        callback?.invoke(result)
    }
}

/** [startDestination] を根に持つ [PixiViewNavigator] を作る。バックスタックは構成変更やプロセスの再生成をまたいで復元する。 */
@Composable
fun rememberPixiViewNavigator(startDestination: Destination): PixiViewNavigator {
    val backStack = rememberSerializable(serializer = NavBackStackSerializer(DestinationKey.serializer())) {
        NavBackStack(DestinationKey.of(startDestination))
    }

    return remember(backStack) { PixiViewNavigator(backStack) }
}
