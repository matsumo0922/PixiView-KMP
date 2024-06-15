package me.matsumo.fanbox.feature.creator.support

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import me.matsumo.fanbox.core.model.fanbox.FanboxCreatorPlan
import me.matsumo.fanbox.core.model.fanbox.id.CreatorId
import me.matsumo.fanbox.core.ui.AsyncLoadContents
import me.matsumo.fanbox.core.ui.MR
import me.matsumo.fanbox.core.ui.component.PixiViewTopBar
import me.matsumo.fanbox.core.ui.extensition.LocalNavigationType
import me.matsumo.fanbox.core.ui.extensition.NavigatorExtension
import me.matsumo.fanbox.core.ui.extensition.PixiViewNavigationType
import me.matsumo.fanbox.core.ui.extensition.drawVerticalScrollbar
import me.matsumo.fanbox.core.ui.view.EmptyView
import me.matsumo.fanbox.feature.creator.support.item.SupportingCreatorItem
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun SupportingCreatorsRoute(
    navigateToCreatorPosts: (CreatorId) -> Unit,
    navigateToFanCard: (CreatorId) -> Unit,
    terminate: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SupportingCreatorsViewModel = koinViewModel(),
    navigatorExtension: NavigatorExtension = koinInject(),
) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()

    AsyncLoadContents(
        modifier = modifier,
        screenState = screenState,
        retryAction = { viewModel.fetch() },
    ) { uiState ->
        SupportingCreatorsScreen(
            modifier = Modifier.fillMaxSize(),
            supportedCreators = uiState.supportedPlans.toImmutableList(),
            onClickPlanDetail = { navigatorExtension.navigateToWebPage(it, SupportingCreatorsRoute) },
            onClickFanCard = navigateToFanCard,
            onClickCreatorPosts = navigateToCreatorPosts,
            terminate = terminate,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SupportingCreatorsScreen(
    supportedCreators: ImmutableList<FanboxCreatorPlan>,
    onClickPlanDetail: (String) -> Unit,
    onClickFanCard: (CreatorId) -> Unit,
    onClickCreatorPosts: (CreatorId) -> Unit,
    terminate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state = rememberLazyGridState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val columns = when (LocalNavigationType.current.type) {
        PixiViewNavigationType.BottomNavigation -> 1
        PixiViewNavigationType.NavigationRail -> 2
        PixiViewNavigationType.PermanentNavigationDrawer -> 2
        else -> 1
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            PixiViewTopBar(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(MR.strings.library_navigation_supporting),
                onClickNavigation = terminate,
                scrollBehavior = scrollBehavior,
            )
        },
        bottomBar = {
            HorizontalDivider()
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { padding ->
        if (supportedCreators.isNotEmpty()) {
            LazyVerticalGrid(
                modifier = Modifier
                    .padding(padding)
                    .drawVerticalScrollbar(state, columns),
                state = state,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                columns = GridCells.Fixed(columns),
            ) {
                items(supportedCreators.toList()) { supportingPlan ->
                    SupportingCreatorItem(
                        modifier = Modifier.fillMaxWidth(),
                        supportingPlan = supportingPlan,
                        onClickPlanDetail = onClickPlanDetail,
                        onClickFanCard = onClickFanCard,
                        onClickCreatorPosts = onClickCreatorPosts,
                    )
                }

                item(span = { GridItemSpan(maxCurrentLineSpan) }) {
                    Spacer(modifier = Modifier.navigationBarsPadding())
                }
            }
        } else {
            EmptyView(
                modifier = Modifier.fillMaxSize(),
                titleRes = MR.strings.error_no_data,
                messageRes = MR.strings.error_no_data_supported,
            )
        }
    }
}
