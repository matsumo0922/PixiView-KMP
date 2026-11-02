package me.matsumo.fanbox.core.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.serialization.NavBackStackSerializer
import me.matsumo.fanbox.core.model.Destination

/**
 * 画面遷移のバックスタックを操作する。
 *
 * 遷移先から結果を受け取る場合は [navigateForResult] で遷移し、遷移先は [backWithResult] で結果を返して戻る。
 */
@Stable
class PixiViewNavigator(val backStack: NavBackStack<DestinationKey>) {

    private var resultCallback: ((Any?) -> Unit)? = null

    /** バックスタックの一番上にある遷移先。 */
    val currentDestination: Destination
        get() = backStack.last().destination

    fun navigate(destination: Destination) {
        backStack.add(DestinationKey.of(destination))
    }

    /** 1つ前の画面へ戻る。NavDisplay は空のバックスタックを表示できないため、根の画面は取り除かない。 */
    fun back() {
        if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
    }

    fun <T> navigateForResult(destination: Destination, onResult: (T) -> Unit) {
        resultCallback = { result ->
            @Suppress("UNCHECKED_CAST")
            onResult(result as T)
        }

        navigate(destination)
    }

    /** [navigateForResult] の呼び出し元へ [result] を返してから戻る。 */
    fun <T> backWithResult(result: T) {
        val callback = resultCallback

        resultCallback = null
        callback?.invoke(result)

        back()
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
