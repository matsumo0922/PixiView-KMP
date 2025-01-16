package me.matsumo.fanbox.feature.post.search

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import io.ktor.http.decodeURLPart
import io.ktor.http.encodeURLPathPart
import me.matsumo.fanbox.core.model.fanbox.id.FanboxCreatorId
import me.matsumo.fanbox.core.model.fanbox.id.FanboxPostId
import me.matsumo.fanbox.core.ui.extensition.navigateWithLog

const val PostSearchQueryStr = "postSearchQuery"
const val PostSearchRoute = "postSearch/{$PostSearchQueryStr}"

fun NavController.navigateToPostSearch(creatorId: FanboxCreatorId? = null, creatorQuery: String? = null, tag: String? = null) {
    val query = buildQuery(creatorId, creatorQuery, tag).encodeURLPathPart()
    val encodedQuery = query.encodeURLPathPart()
    val route = if (parseQuery(query).mode != PostSearchMode.Unknown) "postSearch/$encodedQuery" else "postSearch/pixiViewUnknown"

    this.navigateWithLog(route)
}

fun NavGraphBuilder.postSearchScreen(
    navigateToPostSearch: (FanboxCreatorId?, String?, String?) -> Unit,
    navigateToPostDetail: (FanboxPostId) -> Unit,
    navigateToCreatorPosts: (FanboxCreatorId) -> Unit,
    navigateToCreatorPlans: (FanboxCreatorId) -> Unit,
    terminate: () -> Unit,
) {
    composable(
        route = PostSearchRoute,
        arguments = listOf(navArgument(PostSearchQueryStr) { type = NavType.StringType }),
    ) {
        var query = it.arguments?.getString(PostSearchQueryStr)?.decodeURLPart().orEmpty()

        if (query == "pixiViewUnknown") {
            query = ""
        }

        PostSearchRoute(
            query = query,
            navigateToPostSearch = navigateToPostSearch,
            navigateToPostDetail = navigateToPostDetail,
            navigateToCreatorPosts = navigateToCreatorPosts,
            navigateToCreatorPlans = navigateToCreatorPlans,
            terminate = terminate,
        )
    }
}
