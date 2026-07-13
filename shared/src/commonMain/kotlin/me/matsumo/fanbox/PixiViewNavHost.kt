package me.matsumo.fanbox

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.model.Destination
import me.matsumo.fanbox.core.ui.animation.NavigateAnimation
import me.matsumo.fanbox.core.ui.component.emptyDetailScreen
import me.matsumo.fanbox.core.ui.component.sheet.BottomSheetNavigator
import me.matsumo.fanbox.core.ui.component.sheet.ModalBottomSheetLayout
import me.matsumo.fanbox.core.ui.component.sheet.rememberBottomSheetNavigator
import me.matsumo.fanbox.core.ui.customNavTypes
import me.matsumo.fanbox.core.ui.extensition.popBackStackWithResult
import me.matsumo.fanbox.core.ui.theme.LocalNavController
import me.matsumo.fanbox.core.ui.view.navigateToSimpleAlertDialog
import me.matsumo.fanbox.core.ui.view.simpleAlertDialogDialog
import me.matsumo.fanbox.feature.about.about.aboutScreen
import me.matsumo.fanbox.feature.about.billing.billingPlusBottomSheet
import me.matsumo.fanbox.feature.about.billing.billingRetentionBottomSheet
import me.matsumo.fanbox.feature.about.versions.versionHistoryBottomSheet
import me.matsumo.fanbox.feature.creator.download.creatorPostsDownloadDialog
import me.matsumo.fanbox.feature.creator.fancard.fanCardScreen
import me.matsumo.fanbox.feature.creator.follow.followingCreatorsScreen
import me.matsumo.fanbox.feature.creator.payment.paymentsScreen
import me.matsumo.fanbox.feature.creator.support.supportingCreatorsScreen
import me.matsumo.fanbox.feature.creator.top.creatorTopScreen
import me.matsumo.fanbox.feature.library.home.LibraryHomeRoute
import me.matsumo.fanbox.feature.library.libraryScreen
import me.matsumo.fanbox.feature.post.bookmark.bookmarkedPostsScreen
import me.matsumo.fanbox.feature.post.detail.postDetailScreen
import me.matsumo.fanbox.feature.post.image.postImageScreen
import me.matsumo.fanbox.feature.post.queue.downloadQueueScreen
import me.matsumo.fanbox.feature.post.search.common.postSearchScreen
import me.matsumo.fanbox.feature.post.search.creator.postByCreatorSearchScreen
import me.matsumo.fanbox.feature.setting.developer.settingDeveloperDialog
import me.matsumo.fanbox.feature.setting.directory.settingDirectoryScreen
import me.matsumo.fanbox.feature.setting.oss.settingLicenseScreen
import me.matsumo.fanbox.feature.setting.theme.settingThemeScreen
import me.matsumo.fanbox.feature.setting.top.settingTopScreen
import me.matsumo.fanbox.feature.setting.translate.settingTranslationDialog

@Composable
internal fun PixiViewNavHost(
    modifier: Modifier = Modifier,
    bottomSheetNavigator: BottomSheetNavigator = rememberBottomSheetNavigator(),
    navController: NavHostController = rememberNavController(bottomSheetNavigator),
    startDestination: Destination = Destination.Library,
    onPostDetailClosed: suspend () -> Unit = {},
    onLibraryHomeVisibilityChanged: (Boolean) -> Unit = {},
) {
    val bottomNavigationNavController = rememberNavController()
    val bottomNavigationBackStackEntry by bottomNavigationNavController.currentBackStackEntryAsState()
    val scope = rememberCoroutineScope()
    val composeNavigator: ComposeNavigator = remember(navController) {
        navController.navigatorProvider.getNavigator(COMPOSE_NAVIGATOR_NAME)
    }
    val currentOnPostDetailClosed by rememberUpdatedState(onPostDetailClosed)
    val currentOnLibraryHomeVisibilityChanged by rememberUpdatedState(onLibraryHomeVisibilityChanged)

    LaunchedEffect(bottomNavigationBackStackEntry?.destination?.route) {
        currentOnLibraryHomeVisibilityChanged(bottomNavigationBackStackEntry?.destination?.route == LibraryHomeRoute)
    }

    HandleDeepLink(navController)

    LaunchedEffect(navController, composeNavigator) {
        observePostDetailClosedEntries(
            navController = navController,
            composeNavigator = composeNavigator,
            onPostDetailClosed = { currentOnPostDetailClosed() },
        )
    }

    CompositionLocalProvider(LocalNavController provides navController) {
        ModalBottomSheetLayout(bottomSheetNavigator) {
            NavHost(
                modifier = modifier,
                navController = navController,
                startDestination = startDestination,
                enterTransition = { NavigateAnimation.Horizontal.enter },
                exitTransition = { NavigateAnimation.Horizontal.exit },
                popEnterTransition = { NavigateAnimation.Horizontal.popEnter },
                popExitTransition = { NavigateAnimation.Horizontal.popExit },
                typeMap = customNavTypes,
            ) {
                applyNavGraph(
                    scope = scope,
                    mainNavController = navController,
                    bottomNavigationNavController = bottomNavigationNavController,
                )
            }
        }
    }
}

private suspend fun observePostDetailClosedEntries(
    navController: NavHostController,
    composeNavigator: ComposeNavigator,
    onPostDetailClosed: suspend () -> Unit,
) = coroutineScope {
    val pendingClosedEntryIds = mutableSetOf<String>()
    var previousBackStack = composeNavigator.backStack.value

    launch {
        composeNavigator.backStack.collect { currentBackStack ->
            pendingClosedEntryIds.addAll(previousBackStack.findClosedPostDetailEntryIds(currentBackStack))
            previousBackStack = currentBackStack
            dispatchCompletedPostDetailClosedEntries(
                pendingClosedEntryIds = pendingClosedEntryIds,
                visibleEntries = navController.visibleEntries.value,
                onPostDetailClosed = onPostDetailClosed,
            )
        }
    }

    launch {
        navController.visibleEntries.collect { visibleEntries ->
            dispatchCompletedPostDetailClosedEntries(
                pendingClosedEntryIds = pendingClosedEntryIds,
                visibleEntries = visibleEntries,
                onPostDetailClosed = onPostDetailClosed,
            )
        }
    }
}

private suspend fun dispatchCompletedPostDetailClosedEntries(
    pendingClosedEntryIds: MutableSet<String>,
    visibleEntries: List<NavBackStackEntry>,
    onPostDetailClosed: suspend () -> Unit,
) {
    val visibleEntryIds = visibleEntries.map { visibleEntry -> visibleEntry.id }.toSet()
    val completedEntryIds = pendingClosedEntryIds.filter { pendingEntryId ->
        !visibleEntryIds.contains(pendingEntryId)
    }

    for (completedEntryId in completedEntryIds) {
        pendingClosedEntryIds.remove(completedEntryId)
        onPostDetailClosed()
    }
}

private fun List<NavBackStackEntry>.findClosedPostDetailEntryIds(
    currentBackStack: List<NavBackStackEntry>,
): List<String> {
    val currentEntryIds = currentBackStack.map { currentEntry -> currentEntry.id }.toSet()

    return mapNotNull { previousEntry ->
        val wasRemoved = !currentEntryIds.contains(previousEntry.id)
        val isPostDetail = previousEntry.destination.hasRoute<Destination.PostDetail>()

        if (wasRemoved && isPostDetail) previousEntry.id else null
    }
}

/** Compose の画面遷移を管理する Navigator の登録名。 */
private const val COMPOSE_NAVIGATOR_NAME = "composable"

/**
 * mainNavController
 *   - Library*
 *   - CreatorTop
 *   - PostSearch
 *   - BookmarkedPosts
 *   - FollowingCreators
 *   - SupportingCreators
 *   - Setting*
 *   - Dialogs*
 */
private fun NavGraphBuilder.applyNavGraph(
    scope: CoroutineScope,
    bottomNavigationNavController: NavHostController,
    mainNavController: NavHostController,
    subNavController: NavHostController = mainNavController,
) {
    // composable

    libraryScreen(
        navHostController = bottomNavigationNavController,
        navigateTo = { mainNavController.navigate(it) },
    )

    postDetailScreen(
        navigateTo = { mainNavController.navigate(it) },
        navigateToCommentDeleteDialog = { contents, onResult ->
            scope.launch {
                mainNavController.navigateToSimpleAlertDialog(contents, onResult)
            }
        },
        terminate = { subNavController.popBackStack() },
    )

    postImageScreen(
        navigateTo = { mainNavController.navigate(it) },
        terminate = { subNavController.popBackStack() },
    )

    postSearchScreen(
        navigateTo = { mainNavController.navigate(it) },
        terminate = { mainNavController.popBackStack() },
    )

    postByCreatorSearchScreen(
        navigateTo = { mainNavController.navigate(it) },
        terminate = { mainNavController.popBackStack() },
    )

    bookmarkedPostsScreen(
        navigateTo = { mainNavController.navigate(it) },
        terminate = { mainNavController.popBackStack() },
    )

    creatorTopScreen(
        navigateTo = { mainNavController.navigate(it) },
        navigateToAlertDialog = { contents, onPositive, onNegative ->
            scope.launch {
                mainNavController.navigateToSimpleAlertDialog(contents, onPositive, onNegative)
            }
        },
        terminate = { mainNavController.popBackStack() },
    )

    creatorPostsDownloadDialog(
        navigateTo = { mainNavController.navigate(it) },
        terminate = { mainNavController.popBackStack() },
    )

    supportingCreatorsScreen(
        navigateTo = { mainNavController.navigate(it) },
        terminate = { mainNavController.popBackStack() },
    )

    followingCreatorsScreen(
        navigateTo = { mainNavController.navigate(it) },
        terminate = { mainNavController.popBackStack() },
    )

    paymentsScreen(
        navigateTo = { mainNavController.navigate(it) },
        terminate = { subNavController.popBackStack() },
    )

    fanCardScreen(
        terminate = { subNavController.popBackStack() },
    )

    downloadQueueScreen(
        navigateTo = { mainNavController.navigate(it) },
        terminate = { subNavController.popBackStack() },
    )

    aboutScreen(
        navigateTo = { mainNavController.navigate(it) },
        terminate = { subNavController.popBackStack() },
    )

    settingTopScreen(
        navigateTo = { mainNavController.navigate(it) },
        navigateToLogoutDialog = { contents, onResult ->
            scope.launch {
                mainNavController.navigateToSimpleAlertDialog(contents, onResult)
            }
        },
        terminate = { mainNavController.popBackStack() },
    )

    settingThemeScreen(
        navigateTo = { mainNavController.navigate(it) },
        terminate = { mainNavController.popBackStack() },
    )

    settingLicenseScreen(
        terminate = { mainNavController.popBackStack() },
    )

    settingDirectoryScreen(
        terminate = { mainNavController.popBackStack() },
    )

    // dialog

    simpleAlertDialogDialog(
        onResult = { mainNavController.popBackStackWithResult(it) },
    )

    settingDeveloperDialog(
        terminate = { mainNavController.popBackStack() },
    )

    settingTranslationDialog(
        terminate = { mainNavController.popBackStack() },
    )

    // bottom sheet

    versionHistoryBottomSheet(
        terminate = { mainNavController.popBackStack() },
    )

    billingPlusBottomSheet(
        terminate = { mainNavController.popBackStack() },
    )

    billingRetentionBottomSheet(
        terminate = { mainNavController.popBackStack() },
    )

    // empty for start destination

    emptyDetailScreen()
}

@Composable
expect fun HandleDeepLink(navController: NavController)
