package me.matsumo.fanbox.feature.about.versions

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import me.matsumo.fanbox.core.ui.extensition.bottomSheet
import moe.tlaster.precompose.navigation.Navigator
import moe.tlaster.precompose.navigation.RouteBuilder

const val VersionHistoryRoute = "versionHistory"

fun Navigator.navigateToVersionHistory() {
    this.navigate("versionHistory")
}

fun RouteBuilder.versionHistoryBottomSheet(
    terminate: () -> Unit,
) {
    bottomSheet(
        route = VersionHistoryRoute,
        skipPartiallyExpanded = true,
        onDismissRequest = terminate,
    ) {
        VersionHistoryDialog(
            modifier = Modifier.fillMaxSize(),
        )
    }
}
