package me.matsumo.fanbox.feature.setting.translate

import androidx.compose.ui.text.intl.Locale
import androidx.lifecycle.ViewModel
import me.matsumo.fanbox.core.common.PixiViewConfig
import me.matsumo.fanbox.core.common.util.adjustLanguageTag
import me.matsumo.fanbox.core.repository.UserDataRepository

class SettingTranslationViewModel(
    private val userDataRepository: UserDataRepository,
) : ViewModel() {

    suspend fun setTranslateLanguage(code: String): Boolean {
        val tag = adjustLanguageTag(code) ?: return false
        userDataRepository.setTranslateLanguage(tag)
        return true
    }
}
