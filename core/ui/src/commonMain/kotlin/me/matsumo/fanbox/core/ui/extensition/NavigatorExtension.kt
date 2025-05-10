package me.matsumo.fanbox.core.ui.extensition

import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.Navigator
import androidx.navigation.navOptions
import me.matsumo.fanbox.core.logs.category.NavigationLog
import me.matsumo.fanbox.core.logs.logger.send

interface NavigatorExtension {
    fun navigateToWebPage(url: String, referrer: String)
    fun killApp()
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
    route: Any,
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
    navigateWithLog(route, navOptions(builder))
}

fun NavController.navigateWithLog(
    route: String,
    navOptions: NavOptions? = null,
    navigatorExtras: Navigator.Extras? = null,
) {
    NavigationLog.navigate(
        screenRoute = route,
        referer = this.currentDestination?.route.orEmpty(),
    ).send()

    navigate(route, navOptions, navigatorExtras)
}
