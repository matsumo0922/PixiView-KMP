package me.matsumo.fanbox.core.ui.component

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import me.matsumo.fanbox.core.ui.MR
import me.matsumo.fanbox.core.ui.view.EmptyView

const val EmptyDetailRoute = "Empty"

fun NavGraphBuilder.emptyDetailScreen() {
    composable(EmptyDetailRoute) {
        EmptyView(
            modifier = Modifier.fillMaxSize(),
            titleRes = MR.strings.empty_detail_title,
            messageRes = MR.strings.empty_detail_description,
        )
    }
}
