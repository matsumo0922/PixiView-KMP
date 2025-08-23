package me.matsumo.fanbox.feature.about.billing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.Flow
import me.matsumo.fanbox.core.ui.AsyncLoadContents
import me.matsumo.fanbox.core.ui.theme.LocalNavController
import me.matsumo.fanbox.feature.about.billing.items.BillingBottomBar
import me.matsumo.fanbox.feature.about.billing.items.BillingTopSection
import me.matsumo.fanbox.feature.about.billing.items.billingDescriptionSection
import org.jetbrains.compose.resources.getString
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun BillingPlusRoute(
    modifier: Modifier = Modifier,
    viewModel: BillingViewModel = koinViewModel(),
) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()

    AsyncLoadContents(
        modifier = modifier,
        screenState = screenState,
    ) {
        BillingScreen(
            modifier = Modifier.fillMaxSize(),
            uiState = it,
            onViewEvent = viewModel::onViewEvent,
            messageEventFlow = viewModel.messageEvent,
        )
    }
}

@Composable
private fun BillingScreen(
    uiState: BillingUiState,
    onViewEvent: (BillingViewEvent) -> Unit,
    messageEventFlow: Flow<BillingMessageEvent>,
    modifier: Modifier = Modifier,
) {
    val navController = LocalNavController.current
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            BillingBottomBar(
                modifier = Modifier.fillMaxWidth(),
                uiState = uiState,
                onViewEvent = onViewEvent,
            )
        },
        snackbarHost = {
            SnackbarHost(
                modifier = Modifier.fillMaxWidth(),
                hostState = snackbarHostState,
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = modifier,
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                BillingTopSection(
                    modifier = Modifier
                        .padding(top = 16.dp, bottom = 8.dp)
                        .fillMaxWidth(),
                )
            }

            billingDescriptionSection()
        }
    }

    LaunchedEffect(true) {
        messageEventFlow.collect {
            when (it) {
                is BillingMessageEvent.Purchased -> {
                    navController.popBackStack()
                }

                is BillingMessageEvent.SnackBar -> {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    snackbarHostState.showSnackbar(
                        message = getString(it.messageRes),
                        duration = SnackbarDuration.Short,
                    )
                }
            }
        }
    }
}
