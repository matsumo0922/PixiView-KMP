package me.matsumo.fanbox.core.ui.component

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import me.matsumo.fanbox.core.ui.MR
import me.matsumo.fanbox.core.ui.view.EmptyView
import moe.tlaster.precompose.navigation.RouteBuilder

const val EmptyDetailRoute = "Empty"

fun RouteBuilder.emptyDetailScreen() {
    scene(EmptyDetailRoute) {
        EmptyView(
            modifier = Modifier.fillMaxSize(),
            titleRes = MR.strings.empty_detail_title,
            messageRes = MR.strings.empty_detail_description,
        )
    }
}
