package me.matsumo.fanbox

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.matsumo.fanbox.core.repository.SettingRepository

class MainViewModel(
    private val settingRepository: SettingRepository,
) : ViewModel() {

    val setting = settingRepository.setting

    private val _isAdsSdkInitialized = MutableStateFlow(false)
    val isAdsSdkInitialized = _isAdsSdkInitialized.asStateFlow()

    fun setAdsSdkInitialized() {
        _isAdsSdkInitialized.value = true
    }
}
