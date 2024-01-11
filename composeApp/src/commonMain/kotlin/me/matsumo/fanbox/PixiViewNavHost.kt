package me.matsumo.fanbox

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import me.matsumo.fanbox.core.ui.extensition.PixiViewNavigationType
import me.matsumo.fanbox.core.ui.extensition.rememberNavigator
import me.matsumo.fanbox.core.ui.view.navigateToSimpleAlertDialog
import me.matsumo.fanbox.feature.library.LibraryNavHost
import me.matsumo.fanbox.feature.library.LibraryRoute
import me.matsumo.fanbox.feature.library.component.LibraryPermanentDrawer
import me.matsumo.fanbox.feature.library.libraryScreen
import me.matsumo.fanbox.feature.library.navigateToLibraryDestination
import me.matsumo.fanbox.feature.setting.developer.navigateToSettingDeveloper
import me.matsumo.fanbox.feature.setting.developer.settingDeveloperDialog
import me.matsumo.fanbox.feature.setting.oss.navigateToSettingLicense
import me.matsumo.fanbox.feature.setting.oss.settingLicenseScreen
import me.matsumo.fanbox.feature.setting.theme.navigateToSettingTheme
import me.matsumo.fanbox.feature.setting.theme.settingThemeScreen
import me.matsumo.fanbox.feature.setting.top.navigateToSettingTop
import me.matsumo.fanbox.feature.setting.top.settingTopScreen
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import moe.tlaster.precompose.navigation.NavHost
import moe.tlaster.precompose.navigation.Navigator
import moe.tlaster.precompose.navigation.RouteBuilder

@Composable
internal fun PixiViewNavHost(
    navigationType: PixiViewNavigationType,
    modifier: Modifier = Modifier,
    startDestination: String = LibraryRoute,
) {
    val mainNavigator = rememberNavigator("Main")
    val subNavigator = rememberNavigator("Sub")

    Box(modifier) {
        when (navigationType) {
            PixiViewNavigationType.PermanentNavigationDrawer -> {
                ExpandedNavHost(
                    modifier = Modifier.fillMaxSize(),
                    mainNavigator = mainNavigator,
                    subNavigator = subNavigator,
                )
            }

            PixiViewNavigationType.NavigationRail -> {
                MediumNavHost(
                    modifier = Modifier.fillMaxSize(),
                    navigator = mainNavigator,
                    startDestination = startDestination,
                )
            }

            PixiViewNavigationType.BottomNavigation -> {
                CompactNavHost(
                    modifier = Modifier.fillMaxSize(),
                    navigator = mainNavigator,
                    startDestination = startDestination,
                )
            }
        }
    }
}

@Composable
private fun CompactNavHost(
    navigator: Navigator,
    modifier: Modifier = Modifier,
    startDestination: String = LibraryRoute,
) {
    NavHost(
        modifier = modifier,
        navigator = navigator,
        initialRoute = startDestination,
    ) {
        applyNavGraph(navigator)
    }
}

@Composable
private fun MediumNavHost(
    navigator: Navigator,
    modifier: Modifier = Modifier,
    startDestination: String = LibraryRoute,
) {
    NavHost(
        modifier = modifier,
        navigator = navigator,
        initialRoute = startDestination,
    ) {
        applyNavGraph(navigator)
    }
}

@Composable
private fun ExpandedNavHost(
    mainNavigator: Navigator,
    subNavigator: Navigator,
    modifier: Modifier = Modifier,
) {
    val backStackEntry by mainNavigator.currentEntry.collectAsStateWithLifecycle(null)

    PermanentNavigationDrawer(
        modifier = modifier,
        drawerContent = {
            LibraryPermanentDrawer(
                currentDestination = backStackEntry?.route?.route,
                onClickLibrary = mainNavigator::navigateToLibraryDestination,
                navigateToBookmarkedPosts = { /*mainNavController.navigateToBookmarkedPosts()*/ },
                navigateToFollowingCreators = { /*mainNavController.navigateToFollowingCreators()*/ },
                navigateToSupportingCreators = { /*mainNavController.navigateToSupportingCreators()*/ },
                navigateToPayments = { /*subNavigator.navigateToPayments()*/ },
                navigateToSetting = { /*mainNavController.navigateToSettingTop()*/ },
                navigateToAbout = { /*subNavigator.navigateToAbout()*/ },
                navigateToBillingPlus = { /*mainNavController.navigateToBillingPlus()*/ },
            )
        },
    ) {
        Row(Modifier.fillMaxSize()) {
            LibraryNavHost(
                modifier = Modifier.weight(1f),
                navController = mainNavigator,
                openDrawer = { /* do nothing */ },
                navigateToPostSearch = { /*mainNavController.navigateToPostSearch()*/ },
                navigateToPostDetailFromHome = { /*subNavigator.navigateToPostDetail(it, PostDetailPagingType.Home)*/ },
                navigateToPostDetailFromSupported = {/* subNavigator.navigateToPostDetail(it, PostDetailPagingType.Supported)*/ },
                navigateToCreatorPosts = { /*mainNavController.navigateToCreatorTop(it, isPosts = true)*/ },
                navigateToCreatorPlans = { /*mainNavController.navigateToCreatorTop(it)*/ },
                navigateToCancelPlus = { mainNavigator.navigateToSimpleAlertDialog(it) },
            ) {
                applyNavGraph(mainNavigator, subNavigator)
            }

            NavHost(
                modifier = Modifier.weight(1f),
                navigator = subNavigator,
                initialRoute = "",
            ) {
                applyNavGraph(mainNavigator, subNavigator)
            }
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
private fun RouteBuilder.applyNavGraph(
    mainNavController: Navigator,
    subNavController: Navigator = mainNavController,
) {
    // composable

    libraryScreen(
        navigateToPostSearch = { /*mainNavController.navigateToPostSearch()*/ },
        navigateToPostDetailFromHome = { /*subNavController.navigateToPostDetail(it, PostDetailPagingType.Home)*/ },
        navigateToPostDetailFromSupported = { /*subNavController.navigateToPostDetail(it, PostDetailPagingType.Supported)*/ },
        navigateToCreatorPosts = { /*mainNavController.navigateToCreatorTop(it, isPosts = true)*/ },
        navigateToCreatorPlans = { /*mainNavController.navigateToCreatorTop(it)*/ },
        navigateToBookmarkedPosts = { /*mainNavController.navigateToBookmarkedPosts()*/ },
        navigateToFollowerCreators = { /*mainNavController.navigateToFollowingCreators()*/ },
        navigateToSupportingCreators = { /*mainNavController.navigateToSupportingCreators()*/ },
        navigateToPayments = { /*subNavController.navigateToPayments()*/ },
        navigateToSettingTop = { subNavController.navigateToSettingTop() },
        navigateToAbout = { /*subNavController.navigateToAbout()*/ },
        navigateToBillingPlus = { /*mainNavController.navigateToBillingPlus()*/ },
        navigateToCancelPlus = { /*mainNavController.navigateToSimpleAlertDialog(it)*/ },
    )

    /*postDetailScreen(
        navigateToPostSearch = { query, creatorId -> mainNavController.navigateToPostSearch(tag = query, creatorId = creatorId) },
        navigateToPostDetail = { subNavController.navigateToPostDetail(it, PostDetailPagingType.Unknown) },
        navigateToPostImage = { postId, index -> subNavController.navigateToPostImage(postId, index) },
        navigateToCreatorPosts = { mainNavController.navigateToCreatorTop(it, isPosts = true) },
        navigateToCreatorPlans = { mainNavController.navigateToCreatorTop(it) },
        navigateToCommentDeleteDialog = { contents, onResult -> mainNavController.navigateToSimpleAlertDialog(contents, onResult) },
        terminate = { subNavController.popBackStack() },
    )

    postImageScreen(
        terminate = { subNavController.popBackStack() },
    )

    postSearchScreen(
        navigateToPostSearch = { creatorId, creatorQuery, tag -> mainNavController.navigateToPostSearch(creatorId, creatorQuery, tag) },
        navigateToPostDetail = { subNavController.navigateToPostDetail(it, PostDetailPagingType.Search) },
        navigateToCreatorPosts = { mainNavController.navigateToCreatorTop(it, isPosts = true) },
        navigateToCreatorPlans = { mainNavController.navigateToCreatorTop(it) },
        terminate = { mainNavController.popBackStack() },
    )

    bookmarkedPostsScreen(
        navigateToPostDetail = { subNavController.navigateToPostDetail(it, PostDetailPagingType.Unknown) },
        navigateToCreatorPosts = { mainNavController.navigateToCreatorTop(it, isPosts = true) },
        navigateToCreatorPlans = { mainNavController.navigateToCreatorTop(it) },
        terminate = { mainNavController.popBackStack() },
    )

    creatorTopScreen(
        navigateToPostDetail = { subNavController.navigateToPostDetail(it, PostDetailPagingType.Creator) },
        navigateToPostSearch = { query, creatorId -> mainNavController.navigateToPostSearch(tag = query, creatorId = creatorId) },
        navigateToBillingPlus = { mainNavController.navigateToBillingPlus() },
        navigateToDownloadAll = { mainNavController.navigateToCreatorPostsDownload(it) },
        terminate = { mainNavController.popBackStack() },
    )

    supportingCreatorsScreen(
        navigateToCreatorPlans = { mainNavController.navigateToCreatorTop(it) },
        navigateToCreatorPosts = { mainNavController.navigateToCreatorTop(it, isPosts = true) },
        navigateToFanCard = { subNavController.navigateToFanCard(it) },
        terminate = { mainNavController.popBackStack() },
    )

    followingCreatorsScreen(
        navigateToCreatorPlans = { mainNavController.navigateToCreatorTop(it) },
        terminate = { mainNavController.popBackStack() },
    )

    paymentsScreen(
        navigateToCreatorPosts = { mainNavController.navigateToCreatorTop(it, isPosts = true) },
        terminate = { subNavController.popBackStack() },
    )

    fanCardScreen(
        terminate = { subNavController.popBackStack() },
    )

    aboutScreen(
        navigateToVersionHistory = { mainNavController.navigateToVersionHistory() },
        navigateToDonate = { mainNavController.navigateToBillingPlus() },
        terminate = { subNavController.popBackStack() },
    )*/

    settingTopScreen(
        navigateToThemeSetting = { mainNavController.navigateToSettingTheme() },
        navigateToBillingPlus = { /*mainNavController.navigateToBillingPlus()*/ },
        navigateToSettingDeveloper = { mainNavController.navigateToSettingDeveloper() },
        navigateToLogoutDialog = { contents, onResult -> mainNavController.navigateToSimpleAlertDialog(contents, onResult) },
        navigateToOpenSourceLicense = { mainNavController.navigateToSettingLicense() },
        terminate = { mainNavController.popBackStack() },
    )

    settingThemeScreen(
        navigateToBillingPlus = { /*mainNavController.navigateToBillingPlus()*/ },
        terminate = { mainNavController.popBackStack() },
    )

    settingLicenseScreen(
        terminate = { mainNavController.popBackStack() },
    )

    settingDeveloperDialog(
        terminate = { mainNavController.popBackStack() },
    )

    // dialog

    /*simpleAlertDialogDialog(
        terminateWithResult = { mainNavController.popBackStackWithResult(it) },
    )

    creatorPostsDownloadDialog(
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

    emptyDetailScreen()*/
}
