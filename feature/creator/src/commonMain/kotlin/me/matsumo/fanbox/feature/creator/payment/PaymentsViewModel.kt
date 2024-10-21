package me.matsumo.fanbox.feature.creator.payment

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import me.matsumo.fanbox.core.common.util.format
import me.matsumo.fanbox.core.common.util.suspendRunCatching
import me.matsumo.fanbox.core.model.ScreenState
import me.matsumo.fanbox.core.model.fanbox.FanboxPaidRecord
import me.matsumo.fanbox.core.repository.FanboxRepository
import me.matsumo.fanbox.core.resources.Res
import me.matsumo.fanbox.core.resources.error_network

class PaymentsViewModel(
    private val fanboxRepository: FanboxRepository,
) : ViewModel() {

    private val _screenState = MutableStateFlow<ScreenState<PaymentsUiState>>(ScreenState.Loading)

    val screenState = _screenState.asStateFlow()

    init {
        fetch()
    }

    fun fetch() {
        viewModelScope.launch {
            _screenState.value = ScreenState.Loading
            _screenState.value = suspendRunCatching {
                PaymentsUiState(
                    payments = fanboxRepository.getPaidRecords().translate(),
                )
            }.fold(
                onSuccess = { ScreenState.Idle(it) },
                onFailure = { ScreenState.Error(Res.string.error_network) },
            )
        }
    }

    private fun List<FanboxPaidRecord>.translate(): List<Payment> {
        val paymentDates = map { it.paymentDateTime }.distinctBy { it.format("yyyy-MM-dd") }

        return paymentDates.map { paymentDate ->
            Payment(
                paymentDateTime = paymentDate,
                paidRecords = filter { it.paymentDateTime.format("yyyy-MM-dd") == paymentDate.format("yyyy-MM-dd") },
            )
        }
    }
}

@Stable
data class PaymentsUiState(
    val payments: List<Payment>,
)

@Stable
data class Payment(
    val paymentDateTime: Instant,
    val paidRecords: List<FanboxPaidRecord>,
)
