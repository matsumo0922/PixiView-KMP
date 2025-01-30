package me.matsumo.fanbox.feature.creator.payment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import me.matsumo.fanbox.core.common.util.format
import me.matsumo.fanbox.core.model.Destination
import me.matsumo.fanbox.core.resources.Res
import me.matsumo.fanbox.core.resources.error_no_data
import me.matsumo.fanbox.core.resources.error_no_data_payments
import me.matsumo.fanbox.core.resources.library_navigation_payments
import me.matsumo.fanbox.core.ui.AsyncLoadContents
import me.matsumo.fanbox.core.ui.component.PixiViewTopBar
import me.matsumo.fanbox.core.ui.extensition.drawVerticalScrollbar
import me.matsumo.fanbox.core.ui.view.EmptyView
import me.matsumo.fanbox.feature.creator.payment.items.MonthItem
import me.matsumo.fanbox.feature.creator.payment.items.PaymentItem
import me.matsumo.fankt.fanbox.domain.model.id.FanboxCreatorId
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun PaymentsRoute(
    navigateTo: (Destination) -> Unit,
    terminate: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PaymentsViewModel = koinViewModel(),
) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()

    AsyncLoadContents(
        modifier = modifier,
        screenState = screenState,
        retryAction = { viewModel.fetch() },
        terminate = { terminate.invoke() },
    ) { uiState ->
        PaymentsScreen(
            modifier = Modifier.fillMaxSize(),
            payments = uiState.payments.toImmutableList(),
            onClickCreatorPosts = { navigateTo(Destination.CreatorTop(it, true)) },
            onTerminate = terminate,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentsScreen(
    onClickCreatorPosts: (FanboxCreatorId) -> Unit,
    onTerminate: () -> Unit,
    payments: ImmutableList<Payment>,
    modifier: Modifier = Modifier,
) {
    val state = rememberLazyListState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            PixiViewTopBar(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(Res.string.library_navigation_payments),
                onClickNavigation = onTerminate,
                scrollBehavior = scrollBehavior,
            )
        },
        bottomBar = {
            HorizontalDivider()
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { padding ->
        if (payments.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .drawVerticalScrollbar(state),
                state = state,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                itemsIndexed(payments) { index, item ->
                    val prevPayment = payments.elementAtOrNull(index - 1)
                    val isMonthDifferent = rememberSaveable { isMonthDifferent(prevPayment, item) }

                    if (isMonthDifferent) {
                        MonthItem(
                            modifier = Modifier
                                .padding(
                                    top = if (prevPayment != null) 24.dp else 0.dp,
                                    bottom = 16.dp,
                                )
                                .fillMaxWidth(),
                            payments = payments.getFromYearMonth(item),
                        )
                    }

                    PaymentItem(
                        modifier = Modifier.fillMaxWidth(),
                        payment = item,
                        onClickCreator = onClickCreatorPosts,
                    )
                }

                item {
                    Spacer(modifier = Modifier.navigationBarsPadding())
                }
            }
        } else {
            EmptyView(
                modifier = Modifier.fillMaxSize(),
                titleRes = Res.string.error_no_data,
                messageRes = Res.string.error_no_data_payments,
            )
        }
    }
}

private fun isMonthDifferent(prev: Payment?, current: Payment): Boolean {
    return prev?.paymentDateTime?.format("MM") != current.paymentDateTime.format("MM")
}

private fun List<Payment>.getFromYearMonth(current: Payment): ImmutableList<Payment> {
    return filter { it.paymentDateTime.format("yyyy-MM") == current.paymentDateTime.format("yyyy-MM") }.toImmutableList()
}
