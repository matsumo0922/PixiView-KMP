package me.matsumo.fanbox.core.ui.navigation

import androidx.navigation3.runtime.NavBackStack
import me.matsumo.fanbox.core.model.Destination
import me.matsumo.fanbox.core.model.SimpleAlertContents
import kotlin.test.Test
import kotlin.test.assertEquals

/** [PixiViewNavigator] のバックスタック操作と結果の受け渡しを検証する。 */
class PixiViewNavigatorTest {

    private fun navigatorOf(vararg destinations: Destination): PixiViewNavigator {
        val keys = destinations.map { DestinationKey.of(it) }

        return PixiViewNavigator(NavBackStack(*keys.toTypedArray()))
    }

    @Test
    fun backKeepsRootDestination() {
        val navigator = navigatorOf(Destination.Library)

        navigator.back()

        assertEquals(listOf<Destination>(Destination.Library), navigator.backStack.map { it.destination })
    }

    @Test
    fun backRemovesTopDestination() {
        val navigator = navigatorOf(Destination.Library, Destination.About)

        navigator.back()

        assertEquals(Destination.Library, navigator.currentDestination)
    }

    @Test
    fun backWithResultDeliversResultToCallerAndGoesBack() {
        val navigator = navigatorOf(Destination.Library)
        var receivedResult: Boolean? = null

        navigator.navigateForResult<Boolean>(Destination.SimpleAlertDialog(SimpleAlertContents.Logout)) { receivedResult = it }
        navigator.backWithResult(true)

        assertEquals(true, receivedResult)
        assertEquals(Destination.Library, navigator.currentDestination)
    }

    @Test
    fun resultIsDeliveredOnlyOnce() {
        val navigator = navigatorOf(Destination.Library)
        var deliveredCount = 0

        navigator.navigateForResult<Boolean>(Destination.SimpleAlertDialog(SimpleAlertContents.Logout)) { deliveredCount++ }
        navigator.backWithResult(true)
        navigator.navigate(Destination.About)
        navigator.backWithResult(false)

        assertEquals(1, deliveredCount)
    }

    @Test
    fun resultIsDeliveredAfterNavigatorIsRecreated() {
        val navigator = navigatorOf(Destination.Library)
        var receivedResult: Boolean? = null

        navigator.navigateForResult<Boolean>(Destination.SimpleAlertDialog(SimpleAlertContents.CommentDelete)) { receivedResult = it }

        // 構成変更では Navigator が作り直され、バックスタックは同じ id のまま復元される
        val restoredBackStack = NavBackStack(*navigator.backStack.toTypedArray())
        val recreatedNavigator = PixiViewNavigator(restoredBackStack)

        recreatedNavigator.backWithResult(true)

        assertEquals(true, receivedResult)
    }

    @Test
    fun backWithoutPendingResultOnlyGoesBack() {
        val navigator = navigatorOf(Destination.Library, Destination.About)

        navigator.backWithResult(true)

        assertEquals(Destination.Library, navigator.currentDestination)
    }
}
