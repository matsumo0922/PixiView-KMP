package me.matsumo.fanbox.feature.about.versions

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import me.matsumo.fanbox.core.ui.component.sheet.bottomSheet

const val VersionHistoryRoute = "versionHistory"

fun NavController.navigateToVersionHistory() {
    this.navigate("versionHistory")
}

fun NavGraphBuilder.versionHistoryBottomSheet(
    terminate: () -> Unit,
) {
    bottomSheet(VersionHistoryRoute) {
        VersionHistoryDialog(
            modifier = Modifier.fillMaxSize(),
            terminate = terminate,
        )
    }
}
