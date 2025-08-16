package me.matsumo.fanbox.feature.setting.translate

import androidx.lifecycle.ViewModel
import me.matsumo.fanbox.core.common.util.adjustLanguageTag
import me.matsumo.fanbox.core.repository.SettingRepository

class SettingTranslationViewModel(
    private val settingRepository: SettingRepository,
) : ViewModel() {

    suspend fun setTranslateLanguage(code: String): Boolean {
        val tag = adjustLanguageTag(code) ?: return false
        settingRepository.setTranslateLanguage(tag)
        return true
    }
}
