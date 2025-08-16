package me.matsumo.fanbox.feature.library.notify

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import me.matsumo.fanbox.core.model.ScreenState
import me.matsumo.fanbox.core.model.Setting
import me.matsumo.fanbox.core.repository.FanboxRepository
import me.matsumo.fanbox.core.repository.SettingRepository
import me.matsumo.fanbox.feature.library.notify.paging.LibraryNotifyPagingSource
import me.matsumo.fankt.fanbox.domain.model.FanboxBell

class LibraryNotifyViewModel(
    private val fanboxRepository: FanboxRepository,
    private val settingRepository: SettingRepository,
) : ViewModel() {

    val screenState = settingRepository.setting.map {
        val pager = Pager(
            config = PagingConfig(pageSize = 20),
            initialKey = null,
            pagingSourceFactory = {
                LibraryNotifyPagingSource(fanboxRepository)
            },
        )
            .flow
            .cachedIn(viewModelScope)

        ScreenState.Idle(
            LibraryNotifyUiState(
                paging = pager,
                setting = it,
            ),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ScreenState.Loading,
    )
}

@Stable
data class LibraryNotifyUiState(
    val paging: Flow<PagingData<FanboxBell>>,
    val setting: Setting,
)
