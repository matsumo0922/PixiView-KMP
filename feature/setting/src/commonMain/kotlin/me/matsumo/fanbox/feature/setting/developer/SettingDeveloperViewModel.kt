package me.matsumo.fanbox.feature.setting.developer

import androidx.lifecycle.ViewModel
import me.matsumo.fanbox.core.common.PixiViewConfig
import me.matsumo.fanbox.core.repository.UserDataRepository

class SettingDeveloperViewModel(
    private val pixiViewConfig: PixiViewConfig,
    private val userDataRepository: UserDataRepository,
) : ViewModel() {

    suspend fun submitPassword(password: String): Boolean {
        if (password == pixiViewConfig.developerPassword) {
            userDataRepository.setDeveloperMode(true)
            return true
        }

        return false
    }
}
