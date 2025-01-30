package me.matsumo.fanbox.feature.post.search.common

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.toRoute
import io.ktor.http.decodeURLPart
import io.ktor.http.encodeURLPathPart
import me.matsumo.fanbox.core.model.Destination
import me.matsumo.fanbox.core.ui.customNavTypes
import me.matsumo.fanbox.core.ui.extensition.navigateWithLog
import me.matsumo.fankt.fanbox.domain.model.id.FanboxCreatorId
import me.matsumo.fankt.fanbox.domain.model.id.FanboxPostId

fun NavGraphBuilder.postSearchScreen(
    navigateTo: (Destination) -> Unit,
    terminate: () -> Unit,
) {
    composable<Destination.PostSearch>(
        typeMap = customNavTypes,
    ) { entry ->
        val args = entry.toRoute<Destination.PostSearch>()
        val query = buildQuery(args.creatorId, args.creatorQuery, args.tag)

        PostSearchRoute(
            query = query.takeIf { parseQuery(it).mode != PostSearchMode.Unknown }.orEmpty(),
            navigateTo = navigateTo,
            terminate = terminate,
        )
    }
}
