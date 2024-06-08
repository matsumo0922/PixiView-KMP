package me.matsumo.fanbox.core.ui.extensition

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.Navigator
import androidx.navigation.compose.dialog
import androidx.navigation.navOptions
import me.matsumo.fanbox.core.ui.view.SimpleBottomSheet

interface NavigatorExtension {
    fun navigateToWebPage(url: String)
    fun killApp()
}

@OptIn(ExperimentalMaterial3Api::class)
fun NavGraphBuilder.bottomSheet(
    route: String,
    onDismissRequest: () -> Unit,
    skipPartiallyExpanded: Boolean = false,
    content: @Composable () -> Unit,
) {
    dialog(route) {
        SimpleBottomSheet(
            modifier = Modifier.fillMaxWidth(),
            onDismissRequest = onDismissRequest,
            skipPartiallyExpanded = skipPartiallyExpanded,
        ) {
            content()
        }
    }
}

/**
 * The navigation result callback between two call screens.
 */
typealias NavResultCallback<T> = (T) -> Unit

// A SavedStateHandle key is used to set/get NavResultCallback<T>
private const val NavResultCallbackKey = "NavResultCallbackKey"
private val navResultLambdas = mutableMapOf<String, NavResultCallback<*>>()

/**
 * Set the navigation result callback on calling screen.
 *
 * @param callback The navigation result callback.
 */
fun <T> NavController.setNavResultCallback(callback: NavResultCallback<T>) {
   navResultLambdas[NavResultCallbackKey] = callback
}

/**
 *  Get the navigation result callback on called screen.
 *
 * @return The navigation result callback if the previous backstack entry exists
 */
fun <T> NavController.getNavResultCallback(): NavResultCallback<T>? {
    return navResultLambdas.remove(NavResultCallbackKey) as? NavResultCallback<T>
}

/**
 *  Attempts to pop the controller's back stack and returns the result.
 *
 * @param result the navigation result
 */
fun <T> NavController.popBackStackWithResult(result: T) {
    getNavResultCallback<T>()?.invoke(result)
    popBackStack()
}

/**
 * Navigate to a route in the current NavGraph. If an invalid route is given, an
 * [IllegalArgumentException] will be thrown.
 *
 * @param route route for the destination
 * @param navResultCallback the navigation result callback
 * @param navOptions special options for this navigation operation
 * @param navigatorExtras extras to pass to the [Navigator]
 *
 * @throws IllegalArgumentException if the given route is invalid
 */
fun <T> NavController.navigateForResult(
    route: String,
    navResultCallback: NavResultCallback<T>,
    navOptions: NavOptions? = null,
    navigatorExtras: Navigator.Extras? = null,
) {
    setNavResultCallback(navResultCallback)
    navigate(route, navOptions, navigatorExtras)
}

/**
 * Navigate to a route in the current NavGraph. If an invalid route is given, an
 * [IllegalArgumentException] will be thrown.
 *
 * @param route route for the destination
 * @param navResultCallback the navigation result callback
 * @param builder DSL for constructing a new [NavOptions]
 *
 * @throws IllegalArgumentException if the given route is invalid
 */
fun <T> NavController.navigateForResult(
    route: String,
    navResultCallback: NavResultCallback<T>,
    builder: NavOptionsBuilder.() -> Unit,
) {
    setNavResultCallback(navResultCallback)
    navigate(route, navOptions(builder))
}