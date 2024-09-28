package me.matsumo.fanbox.feature.welcome.top

import androidx.lifecycle.ViewModel
import me.matsumo.fanbox.core.repository.UserDataRepository

class WelcomeTopViewModel(
    private val userDataRepository: UserDataRepository,
) : ViewModel() {
    suspend fun setAgreedPrivacyPolicy() {
        userDataRepository.setAgreedPrivacyPolicy(true)
    }

    suspend fun setAgreedTermsOfService() {
        userDataRepository.setAgreedTermsOfService(true)
    }
}
