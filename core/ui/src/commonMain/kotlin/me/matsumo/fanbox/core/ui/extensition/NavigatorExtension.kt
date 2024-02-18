package me.matsumo.fanbox.core.ui.extensition

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.matsumo.fanbox.core.ui.view.SimpleBottomSheet
import moe.tlaster.precompose.navigation.BackStackEntry
import moe.tlaster.precompose.navigation.Navigator
import moe.tlaster.precompose.navigation.RouteBuilder
import moe.tlaster.precompose.stateholder.LocalStateHolder

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
    content: @Composable (BackStackEntry) -> Unit,
) {
    dialog(route) {
        SimpleBottomSheet(
            modifier = Modifier.fillMaxWidth(),
            onDismissRequest = onDismissRequest,
            skipPartiallyExpanded = skipPartiallyExpanded,
        ) {
            content(it)
        }
    }
}

interface NavigatorExtension {
    fun navigateToWebPage(url: String)
    fun killApp()
}
