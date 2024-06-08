package me.matsumo.fanbox.feature.library.message

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import me.matsumo.fanbox.core.model.fanbox.FanboxNewsLetter
import me.matsumo.fanbox.core.model.fanbox.id.CreatorId
import me.matsumo.fanbox.core.ui.AsyncLoadContents
import me.matsumo.fanbox.core.ui.MR
import me.matsumo.fanbox.core.ui.component.PixiViewTopBar
import me.matsumo.fanbox.core.ui.extensition.LocalNavigationType
import me.matsumo.fanbox.core.ui.extensition.NavigatorExtension
import me.matsumo.fanbox.core.ui.extensition.PixiViewNavigationType
import me.matsumo.fanbox.core.ui.extensition.drawVerticalScrollbar
import me.matsumo.fanbox.core.ui.view.EmptyView
import me.matsumo.fanbox.feature.library.message.items.LibraryMessageItem
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun LibraryMessageRoute(
    openDrawer: () -> Unit,
    navigateToCreatorPosts: (CreatorId) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LibraryMessageViewModel = koinViewModel(),
    navigatorExtension: NavigatorExtension = koinInject(),
) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()

    AsyncLoadContents(
        modifier = modifier,
        screenState = screenState,
        retryAction = viewModel::fetch,
    ) { uiState ->
        LibraryMessageScreen(
            modifier = Modifier.fillMaxSize(),
            messages = uiState.messages.toImmutableList(),
            openDrawer = openDrawer,
            onClickCreator = navigateToCreatorPosts,
            onClickLink = navigatorExtension::navigateToWebPage,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LibraryMessageScreen(
    messages: ImmutableList<FanboxNewsLetter>,
    openDrawer: () -> Unit,
    onClickCreator: (CreatorId) -> Unit,
    onClickLink: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val navigationType = LocalNavigationType.current.type
    val state = rememberLazyListState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            PixiViewTopBar(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(MR.strings.library_navigation_message),
                navigationIcon = Icons.Default.Menu,
                onClickNavigation = if (navigationType != PixiViewNavigationType.PermanentNavigationDrawer) openDrawer else null,
                scrollBehavior = scrollBehavior,
            )
        },
        bottomBar = {
            Divider()
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            if (messages.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.drawVerticalScrollbar(state),
                    state = state,
                ) {
                    items(
                        items = messages,
                        key = { it.id.value },
                    ) {
                        LibraryMessageItem(
                            modifier = Modifier.fillMaxWidth(),
                            message = it,
                            onClickCreator = onClickCreator,
                            onClickLink = onClickLink,
                        )

                        Divider()
                    }
                }
            } else {
                EmptyView(
                    modifier = Modifier.fillMaxSize(),
                    titleRes = MR.strings.error_no_data,
                    messageRes = MR.strings.error_no_data_message,
                )
            }
        }
    }
}
