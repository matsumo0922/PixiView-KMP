package me.matsumo.fanbox

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.ui.animation.NavigateAnimation
import me.matsumo.fanbox.core.ui.component.emptyDetailScreen
import me.matsumo.fanbox.core.ui.component.sheet.ModalBottomSheetLayout
import me.matsumo.fanbox.core.ui.component.sheet.rememberBottomSheetNavigator
import me.matsumo.fanbox.core.ui.extensition.PixiViewNavigationType
import me.matsumo.fanbox.core.ui.extensition.popBackStackWithResult
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
import me.matsumo.fanbox.feature.library.LibraryRoute
import me.matsumo.fanbox.feature.library.libraryScreen
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

@Composable
internal fun PixiViewNavHost(
    navigationType: PixiViewNavigationType,
    modifier: Modifier = Modifier,
    startDestination: String = LibraryRoute,
) {
    val bottomSheetNavigator = rememberBottomSheetNavigator()
    val navController = rememberNavController(bottomSheetNavigator)
    val scope = rememberCoroutineScope()

    ModalBottomSheetLayout(
        modifier = modifier,
        bottomSheetNavigator = bottomSheetNavigator,
    ) {
        NavHost(
            modifier = Modifier.fillMaxSize(),
            navController = navController,
            startDestination = startDestination,
            enterTransition = { NavigateAnimation.Horizontal.enter },
            exitTransition = { NavigateAnimation.Horizontal.exit },
            popEnterTransition = { NavigateAnimation.Horizontal.popEnter },
            popExitTransition = { NavigateAnimation.Horizontal.popExit },
        ) {
            applyNavGraph(scope, navController)
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
    mainNavController: NavHostController,
    subNavController: NavHostController = mainNavController,
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
        navigateToBillingPlus = { mainNavController.navigateToBillingPlus(it) },
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
        navigateToBillingPlus = { mainNavController.navigateToBillingPlus(it) },
        navigateToDownloadAll = { mainNavController.navigateToCreatorPostsDownload(it) },
        navigateToAlertDialog = { contents, onPositive, onNegative -> scope.launch { mainNavController.navigateToSimpleAlertDialog(contents, onPositive, onNegative) } },
        terminate = { mainNavController.popBackStack() },
    )

    supportingCreatorsScreen(
        navigateToCreatorPosts = { mainNavController.navigateToCreatorTop(it, isPosts = true) },
        navigateToFanCard = { subNavController.navigateToFanCard(it) },
        terminate = { mainNavController.popBackStack() },
    )

    followingCreatorsScreen(
        navigateToCreatorPosts = { mainNavController.navigateToCreatorTop(it, true) },
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
        navigateToDonate = { mainNavController.navigateToBillingPlus("donate") },
        terminate = { subNavController.popBackStack() },
    )

    settingTopScreen(
        navigateToThemeSetting = { mainNavController.navigateToSettingTheme() },
        navigateToBillingPlus = { mainNavController.navigateToBillingPlus(it) },
        navigateToSettingDeveloper = { mainNavController.navigateToSettingDeveloper() },
        navigateToLogoutDialog = { contents, onResult -> scope.launch { mainNavController.navigateToSimpleAlertDialog(contents, onResult) } },
        navigateToOpenSourceLicense = { mainNavController.navigateToSettingLicense() },
        terminate = { mainNavController.popBackStack() },
    )

    settingThemeScreen(
        navigateToBillingPlus = { mainNavController.navigateToBillingPlus(it) },
        terminate = { mainNavController.popBackStack() },
    )

    settingLicenseScreen(
        terminate = { mainNavController.popBackStack() },
    )

    // dialog

    simpleAlertDialogDialog(
        onResult = { mainNavController.popBackStackWithResult(it) },
    )

    creatorPostsDownloadDialog(
        navigateToCancelDownloadAlert = { contents, onResult -> scope.launch { mainNavController.navigateToSimpleAlertDialog(contents, onResult) } },
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
