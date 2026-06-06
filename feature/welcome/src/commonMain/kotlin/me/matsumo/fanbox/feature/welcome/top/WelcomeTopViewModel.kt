package me.matsumo.fanbox.feature.welcome.top

import androidx.lifecycle.ViewModel
import me.matsumo.fanbox.core.repository.SettingRepository

class WelcomeTopViewModel(
    private val settingRepository: SettingRepository,
) : ViewModel() {
    suspend fun setAgreedPrivacyPolicy() {
        settingRepository.setAgreedPrivacyPolicy(true)
    }

    suspend fun setAgreedTermsOfService() {
        settingRepository.setAgreedTermsOfService(true)
    }
}
