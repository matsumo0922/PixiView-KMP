package me.matsumo.fanbox.feature.welcome.web

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.repository.FanboxRepository
import me.matsumo.fanbox.core.repository.UserDataRepository

class WelcomeWebViewModel(
    private val fanboxRepository: FanboxRepository,
    private val userDataRepository: UserDataRepository,
) : ViewModel() {

    fun saveCookie(cookie: String) {
        viewModelScope.launch {
            fanboxRepository.updateCookie(cookie)
        }
    }

    fun debugLogin() {
        viewModelScope.launch {
            userDataRepository.setTestUser(true)
            userDataRepository.setFollowTabDefaultHome(true)
        }
    }
}
