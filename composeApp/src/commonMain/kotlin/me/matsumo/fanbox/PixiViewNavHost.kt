package me.matsumo.fanbox

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.model.Destination
import me.matsumo.fanbox.core.ui.animation.NavigateAnimation
import me.matsumo.fanbox.core.ui.component.emptyDetailScreen
import me.matsumo.fanbox.core.ui.component.sheet.ModalBottomSheetLayout
import me.matsumo.fanbox.core.ui.component.sheet.rememberBottomSheetNavigator
import me.matsumo.fanbox.core.ui.customNavTypes
import me.matsumo.fanbox.core.ui.extensition.popBackStackWithResult
import me.matsumo.fanbox.core.ui.view.navigateToSimpleAlertDialog
import me.matsumo.fanbox.core.ui.view.simpleAlertDialogDialog
import me.matsumo.fanbox.feature.about.about.aboutScreen
import me.matsumo.fanbox.feature.about.billing.billingPlusBottomSheet
import me.matsumo.fanbox.feature.about.versions.versionHistoryBottomSheet
import me.matsumo.fanbox.feature.creator.download.creatorPostsDownloadDialog
import me.matsumo.fanbox.feature.creator.fancard.fanCardScreen
import me.matsumo.fanbox.feature.creator.follow.followingCreatorsScreen
import me.matsumo.fanbox.feature.creator.payment.paymentsScreen
import me.matsumo.fanbox.feature.creator.support.supportingCreatorsScreen
import me.matsumo.fanbox.feature.creator.top.creatorTopScreen
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

@Composable
internal fun PixiViewNavHost(
    modifier: Modifier = Modifier,
    startDestination: Destination = Destination.Library,
) {
    val bottomSheetNavigator = rememberBottomSheetNavigator()
    val navController = rememberNavController(bottomSheetNavigator)
    val bottomNavigationNavController = rememberNavController()
    val scope = rememberCoroutineScope()

    HandleDeepLink(navController)

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

    // bottom sheet

    versionHistoryBottomSheet(
        terminate = { mainNavController.popBackStack() },
    )

    billingPlusBottomSheet(
        terminate = { mainNavController.popBackStack() },
    )

    // empty for start destination

    emptyDetailScreen()
}

@Composable
expect fun HandleDeepLink(navController: NavController)
