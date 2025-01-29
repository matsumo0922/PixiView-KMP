package me.matsumo.fanbox.feature.creator.support

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import me.matsumo.fanbox.core.model.Destination
import me.matsumo.fanbox.core.ui.extensition.navigateWithLog
import me.matsumo.fankt.fanbox.domain.model.id.FanboxCreatorId

fun NavGraphBuilder.supportingCreatorsScreen(
    navigateToCreatorPosts: (FanboxCreatorId) -> Unit,
    navigateToFanCard: (FanboxCreatorId) -> Unit,
    terminate: () -> Unit,
) {
    composable<Destination.SupportingCreators> {
        SupportingCreatorsRoute(
            modifier = Modifier.fillMaxSize(),
            navigateToCreatorPosts = navigateToCreatorPosts,
            navigateToFanCard = navigateToFanCard,
            terminate = terminate,
        )
    }
}
