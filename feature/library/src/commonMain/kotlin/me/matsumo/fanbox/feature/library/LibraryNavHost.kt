package me.matsumo.fanbox.feature.library

import androidx.compose.animation.ContentTransform
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import me.matsumo.fanbox.core.logs.category.NavigationLog
import me.matsumo.fanbox.core.logs.logger.send
import me.matsumo.fanbox.core.model.Destination
import me.matsumo.fanbox.core.ui.animation.NavigateAnimation
import me.matsumo.fanbox.feature.library.component.LibraryDestination
import me.matsumo.fanbox.feature.library.discovery.LibraryDiscoveryRoute
import me.matsumo.fanbox.feature.library.home.LibraryHomeScreen
import me.matsumo.fanbox.feature.library.message.LibraryMessageRoute
import me.matsumo.fanbox.feature.library.notify.LibraryNotifyRoute

/**
 * Library のタブの選択状態と、タブごとの画面の状態を持つ。
 *
 * 開始タブ以外を選ぶと開始タブの上に重ね、戻る操作で開始タブへ戻す。選んでいないタブの
 * rememberSaveable の状態と ViewModel も [entryDecorators] が保持し、再び選んだときに復元する。
 */
@Stable
internal class LibraryTabState(
    val startTab: LibraryDestination,
    currentTab: MutableState<LibraryDestination>,
    val entryDecorators: List<NavEntryDecorator<LibraryDestination>>,
) {
    var currentTab by currentTab
        private set

    /** 表示するタブ。開始タブを常に先頭に置き、戻る操作の行き先にする。 */
    val visibleTabs: List<LibraryDestination>
        get() = listOf(startTab, currentTab).distinct()

    fun select(tab: LibraryDestination) {
        NavigationLog.navigate(
            screenRoute = tab.route,
            referer = currentTab.route,
        ).send()

        currentTab = tab
    }

    fun back() {
        currentTab = startTab
    }
}

/**
 * [startTab] から始まる [LibraryTabState] を作る。
 *
 * 画面サイズに応じてレイアウトを切り替えてもタブの状態を失わないよう、レイアウトの分岐より上で呼ぶ。
 */
@Composable
internal fun rememberLibraryTabState(startTab: LibraryDestination): LibraryTabState {
    val currentTab = rememberSaveable(startTab) { mutableStateOf(startTab) }
    val saveableStateDecorator = rememberSaveableStateHolderNavEntryDecorator<LibraryDestination>()
    val viewModelStoreDecorator = rememberViewModelStoreNavEntryDecorator<LibraryDestination>()

    return remember(startTab, currentTab, saveableStateDecorator, viewModelStoreDecorator) {
        LibraryTabState(
            startTab = startTab,
            currentTab = currentTab,
            entryDecorators = listOf(saveableStateDecorator, viewModelStoreDecorator),
        )
    }
}

@Composable
internal fun LibraryNavHost(
    tabState: LibraryTabState,
    openDrawer: () -> Unit,
    navigateTo: (Destination) -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentOpenDrawer by rememberUpdatedState(openDrawer)
    val currentNavigateTo by rememberUpdatedState(navigateTo)

    // タブごとに1件だけのバックスタックを持つ。どのタブも取り除かないため、decorator の状態は破棄されない
    val entriesByTab = LibraryDestination.entries.associateWith { tab ->
        rememberDecoratedNavEntries(
            backStack = listOf(tab),
            entryDecorators = tabState.entryDecorators,
            entryProvider = { key ->
                libraryTabEntry(
                    tab = key,
                    openDrawer = { currentOpenDrawer() },
                    navigateTo = { currentNavigateTo(it) },
                )
            },
        )
    }

    NavDisplay(
        modifier = modifier,
        entries = tabState.visibleTabs.flatMap { entriesByTab.getValue(it) },
        transitionSpec = { libraryTabTransition() },
        popTransitionSpec = { libraryTabTransition() },
        predictivePopTransitionSpec = { libraryTabTransition() },
        onBack = tabState::back,
    )
}

private fun libraryTabTransition(): ContentTransform = ContentTransform(
    targetContentEnter = NavigateAnimation.Home.enter,
    initialContentExit = NavigateAnimation.Home.exit,
)

private fun libraryTabEntry(
    tab: LibraryDestination,
    openDrawer: () -> Unit,
    navigateTo: (Destination) -> Unit,
): NavEntry<LibraryDestination> = NavEntry(
    key = tab,
    contentKey = tab.name,
) {
    when (tab) {
        LibraryDestination.Home -> {
            LibraryHomeScreen(
                modifier = Modifier.fillMaxSize(),
                openDrawer = openDrawer,
                navigateTo = navigateTo,
            )
        }

        LibraryDestination.Discovery -> {
            LibraryDiscoveryRoute(
                modifier = Modifier.fillMaxSize(),
                openDrawer = openDrawer,
                navigateTo = navigateTo,
            )
        }

        LibraryDestination.Notify -> {
            LibraryNotifyRoute(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                openDrawer = openDrawer,
                navigateTo = navigateTo,
            )
        }

        LibraryDestination.Message -> {
            LibraryMessageRoute(
                modifier = Modifier.fillMaxSize(),
                openDrawer = openDrawer,
                navigateTo = navigateTo,
            )
        }
    }
}
