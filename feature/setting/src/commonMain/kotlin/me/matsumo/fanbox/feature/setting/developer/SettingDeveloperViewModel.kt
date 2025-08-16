package me.matsumo.fanbox.feature.setting.developer

import androidx.lifecycle.ViewModel
import me.matsumo.fanbox.core.common.PixiViewConfig
import me.matsumo.fanbox.core.repository.SettingRepository

class SettingDeveloperViewModel(
    private val pixiViewConfig: PixiViewConfig,
    private val settingRepository: SettingRepository,
) : ViewModel() {

    suspend fun submitPassword(password: String): Boolean {
        if (password == pixiViewConfig.developerPassword) {
            settingRepository.setDeveloperMode(true)
            return true
        }

        return false
    }
}
