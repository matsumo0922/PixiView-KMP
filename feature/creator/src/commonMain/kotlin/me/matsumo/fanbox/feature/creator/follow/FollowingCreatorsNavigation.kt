package me.matsumo.fanbox.feature.creator.follow

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import me.matsumo.fanbox.core.model.Destination
import me.matsumo.fanbox.core.ui.extensition.navigateWithLog
import me.matsumo.fankt.fanbox.domain.model.id.FanboxCreatorId


fun NavGraphBuilder.followingCreatorsScreen(
    navigateTo: (Destination) -> Unit,
    terminate: () -> Unit,
) {
    composable<Destination.SupportingCreators> {
        FollowingCreatorsRoute(
            modifier = Modifier.fillMaxSize(),
            navigateTo = navigateTo,
            terminate = terminate,
        )
    }
}
