package me.matsumo.fanbox.feature.library.notify

import androidx.compose.runtime.Stable
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.matsumo.fanbox.core.model.fanbox.FanboxBell
import me.matsumo.fanbox.core.repository.FanboxRepository
import me.matsumo.fanbox.core.ui.extensition.emptyPaging
import me.matsumo.fanbox.feature.library.notify.paging.LibraryNotifyPagingSource
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope

class LibraryNotifyViewModel(
    private val fanboxRepository: FanboxRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryNotifyUiState(emptyPaging()))

    val uiState = _uiState.asStateFlow()

    init {
        Pager(
            config = PagingConfig(pageSize = 10),
            initialKey = null,
            pagingSourceFactory = {
                LibraryNotifyPagingSource(fanboxRepository)
            },
        ).flow.cachedIn(viewModelScope).also {
            _uiState.value = LibraryNotifyUiState(it)
        }
    }
}

@Stable
data class LibraryNotifyUiState(
    val paging: Flow<PagingData<FanboxBell>>,
)
