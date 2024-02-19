package me.matsumo.fanbox

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.ui.animation.NavigateAnimation
import me.matsumo.fanbox.core.ui.component.EmptyDetailRoute
import me.matsumo.fanbox.core.ui.component.emptyDetailScreen
import me.matsumo.fanbox.core.ui.extensition.LocalSnackbarHostState
import me.matsumo.fanbox.core.ui.extensition.PixiViewNavigationType
import me.matsumo.fanbox.core.ui.extensition.rememberNavigator
import me.matsumo.fanbox.core.ui.view.navigateToSimpleAlertDialog
import me.matsumo.fanbox.core.ui.view.simpleAlertDialogDialog
import me.matsumo.fanbox.feature.about.about.aboutScreen
import me.matsumo.fanbox.feature.about.about.navigateToAbout
import me.matsumo.fanbox.feature.about.billing.billingPlusBottomSheet
import me.matsumo.fanbox.feature.about.billing.navigateToBillingPlus
import me.matsumo.fanbox.feature.about.versions.navigateToVersionHistory
import me.matsumo.fanbox.feature.about.versions.versionHistoryBottomSheet
import me.matsumo.fanbox.feature.creator.download.creatorPostsDownloadDialog
import me.matsumo.fanbox.feature.creator.download.navigateToCreatorPostsDownload
import me.matsumo.fanbox.feature.creator.fancard.fanCardScreen
import me.matsumo.fanbox.feature.creator.fancard.navigateToFanCard
import me.matsumo.fanbox.feature.creator.follow.followingCreatorsScreen
import me.matsumo.fanbox.feature.creator.follow.navigateToFollowingCreators
import me.matsumo.fanbox.feature.creator.payment.navigateToPayments
import me.matsumo.fanbox.feature.creator.payment.paymentsScreen
import me.matsumo.fanbox.feature.creator.support.navigateToSupportingCreators
import me.matsumo.fanbox.feature.creator.support.supportingCreatorsScreen
import me.matsumo.fanbox.feature.creator.top.creatorTopScreen
import me.matsumo.fanbox.feature.creator.top.navigateToCreatorTop
import me.matsumo.fanbox.feature.library.LibraryNavHost
import me.matsumo.fanbox.feature.library.LibraryRoute
import me.matsumo.fanbox.feature.library.component.LibraryPermanentDrawer
import me.matsumo.fanbox.feature.library.libraryScreen
import me.matsumo.fanbox.feature.library.navigateToLibraryDestination
import me.matsumo.fanbox.feature.post.bookmark.bookmarkedPostsScreen
import me.matsumo.fanbox.feature.post.bookmark.navigateToBookmarkedPosts
import me.matsumo.fanbox.feature.post.detail.PostDetailPagingType
import me.matsumo.fanbox.feature.post.detail.navigateToPostDetail
import me.matsumo.fanbox.feature.post.detail.postDetailScreen
import me.matsumo.fanbox.feature.post.image.navigateToPostImage
import me.matsumo.fanbox.feature.post.image.postImageScreen
import me.matsumo.fanbox.feature.post.search.navigateToPostSearch
import me.matsumo.fanbox.feature.post.search.postSearchScreen
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
import moe.tlaster.precompose.navigation.SwipeProperties
import moe.tlaster.precompose.navigation.transition.NavTransition

@Composable
internal fun PixiViewNavHost(
    navigationType: PixiViewNavigationType,
    modifier: Modifier = Modifier,
    startDestination: String = LibraryRoute,
) {
    val mainNavigator = rememberNavigator("Main")
    val subNavigator = rememberNavigator("Sub")

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    CompositionLocalProvider(LocalSnackbarHostState provides snackbarHostState) {
        Scaffold(
            modifier = modifier,
            snackbarHost = {
                SnackbarHost(
                    modifier = Modifier.navigationBarsPadding(),
                    hostState = snackbarHostState,
                )
            },
        ) {
            when (navigationType) {
                PixiViewNavigationType.PermanentNavigationDrawer -> {
                    ExpandedNavHost(
                        modifier = Modifier.fillMaxSize(),
                        mainNavigator = mainNavigator,
                        subNavigator = subNavigator,
                        scope = scope,
                    )
                }

                PixiViewNavigationType.NavigationRail -> {
                    MediumNavHost(
                        modifier = Modifier.fillMaxSize(),
                        navigator = mainNavigator,
                        scope = scope,
                        startDestination = startDestination,
                    )
                }

                PixiViewNavigationType.BottomNavigation -> {
                    CompactNavHost(
                        modifier = Modifier.fillMaxSize(),
                        navigator = mainNavigator,
                        scope = scope,
                        startDestination = startDestination,
                    )
                }
            }
        }
    }
}

@Composable
private fun CompactNavHost(
    navigator: Navigator,
    scope: CoroutineScope,
    modifier: Modifier = Modifier,
    startDestination: String = LibraryRoute,
) {
    NavHost(
        modifier = modifier,
        navigator = navigator,
        initialRoute = startDestination,
        swipeProperties = SwipeProperties(
            spaceToSwipe = 16.dp,
            positionalThreshold = { it * 0.3f },
        ),
        navTransition = remember {
            NavTransition(
                createTransition = slideInHorizontally { it },
                destroyTransition = slideOutHorizontally { it },
                pauseTransition = slideOutHorizontally { -it / 4 },
                resumeTransition = slideInHorizontally { -it / 4 },
                exitTargetContentZIndex = 1f
            )
        },
    ) {
        applyNavGraph(scope, navigator)
    }
}

@Composable
private fun MediumNavHost(
    navigator: Navigator,
    scope: CoroutineScope,
    modifier: Modifier = Modifier,
    startDestination: String = LibraryRoute,
) {
    NavHost(
        modifier = modifier,
        navigator = navigator,
        initialRoute = startDestination,
        swipeProperties = SwipeProperties(
            spaceToSwipe = 16.dp,
            positionalThreshold = { it * 0.3f },
        ),
        navTransition = remember {
            NavTransition(
                createTransition = slideInHorizontally { it },
                destroyTransition = slideOutHorizontally { it },
                pauseTransition = slideOutHorizontally { -it / 4 },
                resumeTransition = slideInHorizontally { -it / 4 },
                exitTargetContentZIndex = 1f
            )
        }
    ) {
        applyNavGraph(scope, navigator)
    }
}

@Composable
private fun ExpandedNavHost(
    mainNavigator: Navigator,
    subNavigator: Navigator,
    scope: CoroutineScope,
    modifier: Modifier = Modifier,
) {
    val backStackEntry by mainNavigator.currentEntry.collectAsStateWithLifecycle(null)

    PermanentNavigationDrawer(
        modifier = modifier,
        drawerContent = {
            LibraryPermanentDrawer(
                currentDestination = backStackEntry?.route?.route,
                onClickLibrary = mainNavigator::navigateToLibraryDestination,
                navigateToBookmarkedPosts = { mainNavigator.navigateToBookmarkedPosts() },
                navigateToFollowingCreators = { mainNavigator.navigateToFollowingCreators() },
                navigateToSupportingCreators = { mainNavigator.navigateToSupportingCreators() },
                navigateToPayments = { subNavigator.navigateToPayments() },
                navigateToSetting = { mainNavigator.navigateToSettingTop() },
                navigateToAbout = { subNavigator.navigateToAbout() },
                navigateToBillingPlus = { mainNavigator.navigateToBillingPlus() },
            )
        },
    ) {
        Row(Modifier.fillMaxSize()) {
            LibraryNavHost(
                modifier = Modifier.weight(1f),
                navController = mainNavigator,
                openDrawer = { /* do nothing */ },
                navigateToPostSearch = { mainNavigator.navigateToPostSearch() },
                navigateToPostDetailFromHome = { subNavigator.navigateToPostDetail(it, PostDetailPagingType.Home) },
                navigateToPostDetailFromSupported = { subNavigator.navigateToPostDetail(it, PostDetailPagingType.Supported) },
                navigateToCreatorPosts = { mainNavigator.navigateToCreatorTop(it, isPosts = true) },
                navigateToCreatorPlans = { mainNavigator.navigateToCreatorTop(it) },
                navigateToSimpleAlert = { scope.launch { mainNavigator.navigateToSimpleAlertDialog(it) } },
            ) {
                applyNavGraph(scope, mainNavigator, subNavigator)
            }

            NavHost(
                modifier = Modifier.weight(1f),
                navigator = subNavigator,
                initialRoute = EmptyDetailRoute,
                navTransition = NavigateAnimation.Horizontal.transition
            ) {
                applyNavGraph(scope, mainNavigator, subNavigator)
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
    scope: CoroutineScope,
    mainNavController: Navigator,
    subNavController: Navigator = mainNavController,
) {
    // composable

    libraryScreen(
        navigateToPostSearch = { mainNavController.navigateToPostSearch() },
        navigateToPostDetailFromHome = { subNavController.navigateToPostDetail(it, PostDetailPagingType.Home) },
        navigateToPostDetailFromSupported = { subNavController.navigateToPostDetail(it, PostDetailPagingType.Supported) },
        navigateToCreatorPosts = { mainNavController.navigateToCreatorTop(it, isPosts = true) },
        navigateToCreatorPlans = { mainNavController.navigateToCreatorTop(it) },
        navigateToBookmarkedPosts = { mainNavController.navigateToBookmarkedPosts() },
        navigateToFollowerCreators = { mainNavController.navigateToFollowingCreators() },
        navigateToSupportingCreators = { mainNavController.navigateToSupportingCreators() },
        navigateToPayments = { subNavController.navigateToPayments() },
        navigateToSettingTop = { subNavController.navigateToSettingTop() },
        navigateToAbout = { subNavController.navigateToAbout() },
        navigateToBillingPlus = { mainNavController.navigateToBillingPlus() },
        navigateToCancelPlus = { scope.launch { mainNavController.navigateToSimpleAlertDialog(it) } },
    )

    postDetailScreen(
        navigateToPostSearch = { query, creatorId -> mainNavController.navigateToPostSearch(tag = query, creatorId = creatorId) },
        navigateToPostDetail = { subNavController.navigateToPostDetail(it, PostDetailPagingType.Unknown) },
        navigateToPostImage = { postId, index -> subNavController.navigateToPostImage(postId, index) },
        navigateToCreatorPosts = { mainNavController.navigateToCreatorTop(it, isPosts = true) },
        navigateToCreatorPlans = { mainNavController.navigateToCreatorTop(it) },
        navigateToCommentDeleteDialog = { contents, onResult -> scope.launch { mainNavController.navigateToSimpleAlertDialog(contents, onResult) } },
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
    )

    settingTopScreen(
        navigateToThemeSetting = { mainNavController.navigateToSettingTheme() },
        navigateToBillingPlus = { mainNavController.navigateToBillingPlus() },
        navigateToSettingDeveloper = { mainNavController.navigateToSettingDeveloper() },
        navigateToLogoutDialog = { contents, onResult -> scope.launch { mainNavController.navigateToSimpleAlertDialog(contents, onResult) } },
        navigateToOpenSourceLicense = { mainNavController.navigateToSettingLicense() },
        terminate = { mainNavController.popBackStack() },
    )

    settingThemeScreen(
        navigateToBillingPlus = { mainNavController.navigateToBillingPlus() },
        terminate = { mainNavController.popBackStack() },
    )

    settingLicenseScreen(
        terminate = { mainNavController.popBackStack() },
    )

    // dialog

    simpleAlertDialogDialog(
        onResult = { mainNavController.goBackWith() },
    )

    creatorPostsDownloadDialog(
        terminate = { mainNavController.popBackStack() },
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
