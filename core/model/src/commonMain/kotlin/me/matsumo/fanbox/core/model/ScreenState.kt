package me.matsumo.fanbox.core.model

import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.jetbrains.compose.resources.StringResource

@Stable
sealed class ScreenState<out T> {
    data object Loading : ScreenState<Nothing>()

    data class Error(
        val message: StringResource,
        val retryTitle: StringResource? = null,
    ) : ScreenState<Nothing>()

    data class Idle<T>(
        var data: T,
    ) : ScreenState<T>()
}

@Stable
sealed class TranslationState<out T> {
    data object Loading : TranslationState<Nothing>()
    data object None : TranslationState<Nothing>()

    data class Translated<T>(
        val data: T,
    ) : TranslationState<T>()
}

fun <T> ScreenState<T>.updateWhenIdle(action: (T) -> T): ScreenState<T> {
    return if (this is ScreenState.Idle) {
        val newData = action(data)
        ScreenState.Idle(newData)
    } else {
        this
    }
}

fun <T> MutableStateFlow<ScreenState<T>>.updateWhenIdle(action: (T) -> T) {
    update { this.value.updateWhenIdle(action) }
}
