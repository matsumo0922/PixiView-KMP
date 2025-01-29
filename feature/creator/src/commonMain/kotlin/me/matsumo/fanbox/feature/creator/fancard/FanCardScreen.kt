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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest
import me.matsumo.fanbox.core.model.ScreenState
import me.matsumo.fanbox.core.resources.Res
import me.matsumo.fanbox.core.resources.common_downloaded
import me.matsumo.fanbox.core.resources.error_network
import me.matsumo.fanbox.core.ui.AsyncLoadContents
import me.matsumo.fanbox.core.ui.animation.Zoomable
import me.matsumo.fanbox.core.ui.component.PixiViewTopBar
import me.matsumo.fanbox.core.ui.extensition.LocalSnackbarHostState
import me.matsumo.fanbox.core.ui.extensition.ToastExtension
import me.matsumo.fanbox.feature.creator.fancard.items.FanCardItem
import me.matsumo.fankt.fanbox.domain.model.FanboxCreatorPlanDetail
import me.matsumo.fankt.fanbox.domain.model.id.FanboxCreatorId
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun FanCardRoute(
    terminate: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FanCardViewModel = koinViewModel(),
    toastExtension: ToastExtension = koinInject(),
) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val snackHostState = LocalSnackbarHostState.current

    LaunchedEffect(true) {
        viewModel.downloadedEvent.collectLatest {
            toastExtension.show(snackHostState, if (it) Res.string.common_downloaded else Res.string.error_network)
        }
    }

    AsyncLoadContents(
        modifier = modifier,
        screenState = screenState,
        retryAction = viewModel::fetch,
        terminate = terminate,
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
