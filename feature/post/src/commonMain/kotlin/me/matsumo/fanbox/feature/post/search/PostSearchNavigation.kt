package me.matsumo.fanbox.feature.post.search

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import io.ktor.http.decodeURLPart
import io.ktor.http.encodeURLPathPart
import me.matsumo.fanbox.core.model.fanbox.id.CreatorId
import me.matsumo.fanbox.core.model.fanbox.id.PostId
import me.matsumo.fanbox.core.ui.extensition.navigateWithLog

const val PostSearchQueryStr = "postSearchQuery"
const val PostSearchRoute = "postSearch/{$PostSearchQueryStr}"

fun NavController.navigateToPostSearch(creatorId: CreatorId? = null, creatorQuery: String? = null, tag: String? = null) {
    val query = buildQuery(creatorId, creatorQuery, tag).encodeURLPathPart()
    val encodedQuery = query.encodeURLPathPart()
    val route = if (parseQuery(query).mode != PostSearchMode.Unknown) "postSearch/$encodedQuery" else "postSearch/pixiViewUnknown"

    this.navigateWithLog(route)
}

fun NavGraphBuilder.postSearchScreen(
    navigateToPostSearch: (CreatorId?, String?, String?) -> Unit,
    navigateToPostDetail: (PostId) -> Unit,
    navigateToCreatorPosts: (CreatorId) -> Unit,
    navigateToCreatorPlans: (CreatorId) -> Unit,
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
