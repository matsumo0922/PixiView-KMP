package me.matsumo.fanbox.core.ui.component.sheet

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.util.fastForEach
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavDestinationBuilder
import androidx.navigation.NavDestinationDsl
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.get
import kotlin.jvm.JvmSuppressWildcards
import kotlin.reflect.KClass
import kotlin.reflect.KType

/**
 * Add the [content] [Composable] as bottom sheet content to the [NavGraphBuilder]
 *
 * @param route route for the destination
 * @param arguments list of arguments to associate with destination
 * @param deepLinks list of deep links to associate with the destinations
 * @param content the sheet content at the given destination
 */

fun NavGraphBuilder.bottomSheet(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    content: @Composable ColumnScope.(backstackEntry: NavBackStackEntry) -> Unit
) {
    addDestination(
        BottomSheetNavigator.Destination(
            provider[BottomSheetNavigator::class], content
        ).apply {
            this.route = route
            arguments.fastForEach { (argumentName, argument) ->
                addArgument(argumentName, argument)
            }
            deepLinks.fastForEach { deepLink ->
                addDeepLink(deepLink)
            }
        })
}


inline fun <reified T : Any> NavGraphBuilder.bottomSheet(
    deepLinks: List<NavDeepLink> = emptyList(),
    noinline content: @Composable ColumnScope.(backstackEntry: NavBackStackEntry) -> Unit
) {
    destination(
        BottomSheetNavigatorDestinationBuilder(
            provider[BottomSheetNavigator::class],
            T::class,
            emptyMap(),
            content
        )
            .apply {
                deepLinks.forEach { deepLink -> deepLink(deepLink) }
            }
    )
}


/** DSL for constructing a new [ComposeNavigator.Destination] */
@NavDestinationDsl
class BottomSheetNavigatorDestinationBuilder :
    NavDestinationBuilder<BottomSheetNavigator.Destination> {

    private val composeNavigator: BottomSheetNavigator
    private val content: @Composable ColumnScope.(NavBackStackEntry) -> Unit

    constructor(
        navigator: BottomSheetNavigator,
        route: String,
        content: @Composable ColumnScope.(NavBackStackEntry) -> Unit
    ) : super(navigator, route) {
        this.composeNavigator = navigator
        this.content = content
    }

    constructor(
        navigator: BottomSheetNavigator,
        route: KClass<*>,
        typeMap: Map<KType, @JvmSuppressWildcards NavType<*>>,
        content: @Composable ColumnScope.(NavBackStackEntry) -> Unit
    ) : super(navigator, route, typeMap) {
        this.composeNavigator = navigator
        this.content = content
    }

    override fun instantiateDestination(): BottomSheetNavigator.Destination {
        return BottomSheetNavigator.Destination(composeNavigator, content)
    }

    override fun build(): BottomSheetNavigator.Destination {
        return super.build()
    }
}
