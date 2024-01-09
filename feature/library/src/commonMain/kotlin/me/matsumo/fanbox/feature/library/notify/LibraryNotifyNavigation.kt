package me.matsumo.fanbox.feature.library.notify

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import me.matsumo.fanbox.core.model.fanbox.id.PostId
import me.matsumo.fanbox.core.ui.animation.NavigateAnimation
import moe.tlaster.precompose.navigation.NavOptions
import moe.tlaster.precompose.navigation.Navigator
import moe.tlaster.precompose.navigation.RouteBuilder

const val LibraryNotifyRoute = "libraryNotify"

fun Navigator.navigateToLibraryNotify(navOptions: NavOptions? = null) {
    this.navigate(LibraryNotifyRoute, navOptions)
}

fun RouteBuilder.libraryNotifyScreen(
    openDrawer: () -> Unit,
    navigateToPostDetail: (PostId) -> Unit,
) {
    scene(
        route = LibraryNotifyRoute,
        navTransition = NavigateAnimation.Library.transition
    ) {
        LibraryNotifyRoute(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            openDrawer = openDrawer,
            navigateToPostDetail = navigateToPostDetail,
        )
    }
}
