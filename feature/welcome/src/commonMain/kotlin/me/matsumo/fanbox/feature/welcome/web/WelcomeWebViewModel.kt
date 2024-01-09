package me.matsumo.fanbox.feature.welcome.web

import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.repository.FanboxRepository
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope

class WelcomeWebViewModel(
    private val fanboxRepository: FanboxRepository,
): ViewModel() {

    fun saveCookie(cookie: String) {
        viewModelScope.launch {
            fanboxRepository.updateCookie(cookie)
        }
    }
}
