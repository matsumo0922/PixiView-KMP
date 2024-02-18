package me.matsumo.fanbox.feature.welcome.top

import me.matsumo.fanbox.core.repository.UserDataRepository
import moe.tlaster.precompose.viewmodel.ViewModel

class WelcomeTopViewModel(
    private val userDataRepository: UserDataRepository
) : ViewModel() {
    suspend fun setAgreedPrivacyPolicy() {
        userDataRepository.setAgreedPrivacyPolicy(true)
    }

    suspend fun setAgreedTermsOfService() {
        userDataRepository.setAgreedTermsOfService(true)
    }
}
