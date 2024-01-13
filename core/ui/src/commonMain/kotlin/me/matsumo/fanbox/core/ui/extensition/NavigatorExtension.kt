package me.matsumo.fanbox.core.ui.extensition

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import moe.tlaster.precompose.navigation.BackStackEntry
import moe.tlaster.precompose.navigation.NavOptions
import moe.tlaster.precompose.navigation.Navigator
import moe.tlaster.precompose.navigation.RouteBuilder
import moe.tlaster.precompose.stateholder.LocalStateHolder
import moe.tlaster.precompose.stateholder.StateHolder

@Composable
fun rememberNavigator(key: String): Navigator {
    val stateHolder = LocalStateHolder.current
    return stateHolder.getOrPut("Navigator-$key") {
        Navigator()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
fun RouteBuilder.bottomSheet(
    route: String,
    onDismissRequest: () -> Unit,
    skipPartiallyExpanded: Boolean = false,
    windowInsets: WindowInsets = WindowInsets(0, 0, 0, 0),
    content: @Composable (BackStackEntry) -> Unit,
) {
    dialog(route) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded)

        ModalBottomSheet(
            modifier = Modifier.fillMaxWidth(),
            sheetState = sheetState,
            shape = RectangleShape,
            dragHandle = null,
            windowInsets = windowInsets,
            onDismissRequest = onDismissRequest,
        ) {
            content(it)
        }
    }
}

private typealias NavResultCallback<T> = (T) -> Unit
private const val NavResultCallbackKey = "NavResultCallbackKey"

fun <T> Navigator.navigateForResult(
    route: String,
    stateHolder: StateHolder,
    navResultCallback: NavResultCallback<T>,
    navOptions: NavOptions? = null,
) {
    stateHolder.set(NavResultCallbackKey, navResultCallback)
    navigate(route, navOptions)
}

fun <T> Navigator.popBackStackWithResult(result: T, stateHolder: StateHolder) {
    stateHolder.get<NavResultCallback<T>>(NavResultCallbackKey)?.invoke(result)
    stateHolder.remove(NavResultCallbackKey)
    popBackStack()
}
