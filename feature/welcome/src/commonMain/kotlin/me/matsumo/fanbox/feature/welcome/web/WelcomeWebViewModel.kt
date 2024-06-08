package me.matsumo.fanbox.feature.welcome.web

import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.repository.FanboxRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

class WelcomeWebViewModel(
    private val fanboxRepository: FanboxRepository,
): ViewModel() {

    fun saveCookie(cookie: String) {
        viewModelScope.launch {
            fanboxRepository.updateCookie(cookie)
        }
    }
}
