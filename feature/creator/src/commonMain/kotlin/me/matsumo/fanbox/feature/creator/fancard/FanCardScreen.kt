package me.matsumo.fanbox.feature.creator.fancard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.matsumo.fanbox.core.ui.animation.Zoomable
import kotlinx.coroutines.flow.collectLatest
import me.matsumo.fanbox.core.model.ScreenState
import me.matsumo.fanbox.core.model.fanbox.FanboxCreatorPlanDetail
import me.matsumo.fanbox.core.model.fanbox.id.CreatorId
import me.matsumo.fanbox.core.ui.AsyncLoadContents
import me.matsumo.fanbox.core.ui.MR
import me.matsumo.fanbox.core.ui.component.PixiViewTopBar
import me.matsumo.fanbox.core.ui.extensition.LocalSnackbarHostState
import me.matsumo.fanbox.core.ui.extensition.SnackbarExtension
import me.matsumo.fanbox.feature.creator.fancard.items.FanCardItem
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import moe.tlaster.precompose.koin.koinViewModel
import org.koin.compose.koinInject

@Composable
internal fun FanCardRoute(
    creatorId: CreatorId,
    terminate: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FanCardViewModel = koinViewModel(FanCardViewModel::class),
    snackbarExtension: SnackbarExtension = koinInject(),
) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val snackHostState = LocalSnackbarHostState.current

    LaunchedEffect(creatorId) {
        if (screenState !is ScreenState.Idle) {
            viewModel.fetch(creatorId)
        }
    }

    LaunchedEffect(true) {
        viewModel.downloadedEvent.collectLatest {
            snackbarExtension.showSnackbar(snackHostState, if (it) MR.strings.common_downloaded else MR.strings.error_network)
        }
    }

    AsyncLoadContents(
        modifier = modifier,
        screenState = screenState,
        retryAction = { terminate.invoke() },
    ) {
        FanCardScreen(
            modifier = Modifier.fillMaxWidth(),
            planDetail = it.planDetail,
            onTerminate = terminate,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FanCardScreen(
    planDetail: FanboxCreatorPlanDetail,
    onTerminate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isDisplayName by remember { mutableStateOf(true) }

    Box(modifier) {
        Zoomable(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
        ) {
            FanCardItem(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(),
                planDetail = planDetail,
                isDisplayName = isDisplayName,
            )
        }

        PixiViewTopBar(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth(),
            onClickNavigation = onTerminate,
            isTransparent = true,
        )
    }
}
