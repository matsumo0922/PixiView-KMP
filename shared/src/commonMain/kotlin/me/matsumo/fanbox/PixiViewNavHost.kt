package me.matsumo.fanbox

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavEntryDecorator
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.model.Destination
import me.matsumo.fanbox.core.ui.component.emptyDetailEntry
import me.matsumo.fanbox.core.ui.navigation.DestinationKey
import me.matsumo.fanbox.core.ui.navigation.PixiViewNavDisplay
import me.matsumo.fanbox.core.ui.navigation.PixiViewNavigator
import me.matsumo.fanbox.core.ui.navigation.destinationEntryProvider
import me.matsumo.fanbox.core.ui.navigation.rememberPixiViewNavigator
import me.matsumo.fanbox.core.ui.view.navigateToSimpleAlertDialog
import me.matsumo.fanbox.core.ui.view.simpleAlertDialogEntry
import me.matsumo.fanbox.feature.about.about.aboutEntry
import me.matsumo.fanbox.feature.about.billing.billingPlusBottomSheetEntry
import me.matsumo.fanbox.feature.about.billing.billingRetentionBottomSheetEntry
import me.matsumo.fanbox.feature.about.versions.versionHistoryBottomSheetEntry
import me.matsumo.fanbox.feature.creator.download.creatorPostsDownloadEntry
import me.matsumo.fanbox.feature.creator.fancard.fanCardEntry
import me.matsumo.fanbox.feature.creator.follow.followingCreatorsEntry
import me.matsumo.fanbox.feature.creator.payment.paymentsEntry
import me.matsumo.fanbox.feature.creator.support.supportingCreatorsEntry
import me.matsumo.fanbox.feature.creator.top.creatorTopEntry
import me.matsumo.fanbox.feature.library.libraryEntry
import me.matsumo.fanbox.feature.post.bookmark.bookmarkedPostsEntry
import me.matsumo.fanbox.feature.post.detail.postDetailEntry
import me.matsumo.fanbox.feature.post.image.postImageEntry
import me.matsumo.fanbox.feature.post.queue.downloadQueueEntry
import me.matsumo.fanbox.feature.post.search.common.postSearchEntry
import me.matsumo.fanbox.feature.post.search.creator.postByCreatorSearchEntry
import me.matsumo.fanbox.feature.setting.developer.settingDeveloperDialogEntry
import me.matsumo.fanbox.feature.setting.directory.settingDirectoryEntry
import me.matsumo.fanbox.feature.setting.oss.settingLicenseEntry
import me.matsumo.fanbox.feature.setting.theme.settingThemeEntry
import me.matsumo.fanbox.feature.setting.top.settingTopEntry
import me.matsumo.fanbox.feature.setting.translate.settingTranslationDialogEntry

@Composable
internal fun PixiViewNavHost(
    navigator: PixiViewNavigator,
    modifier: Modifier = Modifier,
    onPostDetailClose: suspend () -> Unit = {},
    onLibraryHomeVisibilityChange: (Boolean) -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val currentOnPostDetailClose by rememberUpdatedState(onPostDetailClose)
    val postDetailCloseDecorator = rememberPostDetailCloseDecorator(
        navigator = navigator,
        onClose = { scope.launch { currentOnPostDetailClose() } },
    )

    HandleDeepLink(onDeepLink = navigator::navigate)

    PixiViewNavDisplay(
        modifier = modifier,
        navigator = navigator,
        entryDecorators = persistentListOf(postDetailCloseDecorator),
        entryProvider = destinationEntryProvider {
            libraryEntry(
                navigateTo = navigator::navigate,
                onHomeVisibilityChange = onLibraryHomeVisibilityChange,
            )

            applyPostDestinations(navigator)
            applyCreatorDestinations(navigator)
            applySettingsDestinations(
                navigator = navigator,
                terminateSettingTop = navigator::back,
            )
        },
    )
}

/**
 * ログイン前の画面から開く設定画面。
 *
 * ログイン前は [PixiViewNavHost] を表示しないため、設定画面とそこから開く画面だけを持つバックスタックを別に用意する。
 * 設定画面はバックスタックの根にあり pop できないため、閉じる操作は [onClose] へ渡す。
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun PixiViewSettingNavHost(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val navigator = rememberPixiViewNavigator(Destination.SettingTop)

    // 根では NavDisplay が戻る操作を受け取らないため、ここで閉じる
    BackHandler(navigator.backStack.size == 1) {
        onClose()
    }

    PixiViewNavDisplay(
        modifier = modifier,
        navigator = navigator,
        entryProvider = destinationEntryProvider {
            applySettingsDestinations(
                navigator = navigator,
                terminateSettingTop = onClose,
            )
        },
    )
}

/**
 * 投稿詳細の画面が閉じきったときに [onClose] を呼ぶ decorator を作る。
 *
 * 画面がバックスタックから取り除かれ、退場アニメーションを終えて composition から外れたときに呼ぶ。
 * decorator が受け取るのは contentKey だけなので、投稿詳細の contentKey をバックスタックから集めておく。
 */
@Composable
private fun rememberPostDetailCloseDecorator(
    navigator: PixiViewNavigator,
    onClose: () -> Unit,
): NavEntryDecorator<DestinationKey> {
    val currentOnClose by rememberUpdatedState(onClose)
    val postDetailContentKeys = remember(navigator) { mutableSetOf<Any>() }

    LaunchedEffect(navigator) {
        snapshotFlow { navigator.backStack.toList() }.collect { keys ->
            val postDetailKeys = keys
                .filter { it.destination is Destination.PostDetail }
                .map { it.id }

            postDetailContentKeys.addAll(postDetailKeys)
        }
    }

    return remember(navigator) {
        NavEntryDecorator(
            onPop = { contentKey ->
                if (postDetailContentKeys.remove(contentKey)) currentOnClose()
            },
            decorate = { entry -> entry.Content() },
        )
    }
}

private fun EntryProviderScope<Destination>.applyPostDestinations(navigator: PixiViewNavigator) {
    postDetailEntry(
        navigateTo = navigator::navigate,
        navigateToCommentDeleteDialog = { contents, onResult ->
            navigator.navigateToSimpleAlertDialog(contents, onResult)
        },
        terminate = navigator::back,
    )

    postImageEntry(
        navigateTo = navigator::navigate,
        terminate = navigator::back,
    )

    postSearchEntry(
        navigateTo = navigator::navigate,
        terminate = navigator::back,
    )

    postByCreatorSearchEntry(
        navigateTo = navigator::navigate,
        terminate = navigator::back,
    )

    bookmarkedPostsEntry(
        navigateTo = navigator::navigate,
        terminate = navigator::back,
    )
}

private fun EntryProviderScope<Destination>.applyCreatorDestinations(navigator: PixiViewNavigator) {
    creatorTopEntry(
        navigateTo = navigator::navigate,
        navigateToAlertDialog = { contents, onPositive, onNegative ->
            navigator.navigateToSimpleAlertDialog(contents, onPositive, onNegative)
        },
        terminate = navigator::back,
    )

    creatorPostsDownloadEntry(
        navigateTo = navigator::navigate,
        terminate = navigator::back,
    )

    supportingCreatorsEntry(
        navigateTo = navigator::navigate,
        terminate = navigator::back,
    )

    followingCreatorsEntry(
        navigateTo = navigator::navigate,
        terminate = navigator::back,
    )

    paymentsEntry(
        navigateTo = navigator::navigate,
        terminate = navigator::back,
    )

    fanCardEntry(
        terminate = navigator::back,
    )

    downloadQueueEntry(
        navigateTo = navigator::navigate,
        terminate = navigator::back,
    )
}

private fun EntryProviderScope<Destination>.applySettingsDestinations(
    navigator: PixiViewNavigator,
    terminateSettingTop: () -> Unit,
) {
    aboutEntry(
        navigateTo = navigator::navigate,
        terminate = navigator::back,
    )

    settingTopEntry(
        navigateTo = navigator::navigate,
        navigateToLogoutDialog = { contents, onResult ->
            navigator.navigateToSimpleAlertDialog(contents, onResult)
        },
        terminate = terminateSettingTop,
    )

    settingThemeEntry(
        navigateTo = navigator::navigate,
        terminate = navigator::back,
    )

    settingLicenseEntry(
        terminate = navigator::back,
    )

    settingDirectoryEntry(
        terminate = navigator::back,
    )

    // dialog

    simpleAlertDialogEntry(
        onResult = { navigator.backWithResult(it) },
    )

    settingDeveloperDialogEntry(
        terminate = navigator::back,
    )

    settingTranslationDialogEntry(
        terminate = navigator::back,
    )

    // bottom sheet

    versionHistoryBottomSheetEntry(
        terminate = navigator::back,
    )

    billingPlusBottomSheetEntry(
        terminate = navigator::back,
    )

    billingRetentionBottomSheetEntry(
        replaceTo = { destination ->
            navigator.back()
            navigator.navigate(destination)
        },
        terminate = navigator::back,
    )

    emptyDetailEntry()
}

@Composable
expect fun HandleDeepLink(onDeepLink: (Destination) -> Unit)
