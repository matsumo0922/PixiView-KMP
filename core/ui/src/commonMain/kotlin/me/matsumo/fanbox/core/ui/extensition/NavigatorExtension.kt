package me.matsumo.fanbox.core.ui.extensition

import androidx.compose.runtime.Composable
import moe.tlaster.precompose.navigation.Navigator
import moe.tlaster.precompose.stateholder.LocalStateHolder

@Composable
fun rememberNavigator(key: String): Navigator {
    val stateHolder = LocalStateHolder.current
    return stateHolder.getOrPut("Navigator-$key") {
        Navigator()
    }
}
