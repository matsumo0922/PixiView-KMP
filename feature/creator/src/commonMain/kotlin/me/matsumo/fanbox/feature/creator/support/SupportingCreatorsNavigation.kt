package me.matsumo.fanbox.feature.creator.support

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import me.matsumo.fanbox.core.model.fanbox.id.CreatorId
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable

const val SupportingCreatorsRoute = "supportingCreators"

fun NavController.navigateToSupportingCreators(navOptions: NavOptions? = null) {
    this.navigate(SupportingCreatorsRoute, navOptions)
}

fun NavGraphBuilder.supportingCreatorsScreen(
    navigateToCreatorPlans: (CreatorId) -> Unit,
    navigateToCreatorPosts: (CreatorId) -> Unit,
    navigateToFanCard: (CreatorId) -> Unit,
    terminate: () -> Unit,
) {
    composable(SupportingCreatorsRoute) {
        SupportingCreatorsRoute(
            modifier = Modifier.fillMaxSize(),
            navigateToCreatorPlans = navigateToCreatorPlans,
            navigateToCreatorPosts = navigateToCreatorPosts,
            navigateToFanCard = navigateToFanCard,
            terminate = terminate,
        )
    }
}
