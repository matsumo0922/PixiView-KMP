package me.matsumo.fanbox.core.model

import androidx.compose.runtime.Stable
import dev.icerock.moko.resources.StringResource
import kotlinx.coroutines.flow.StateFlow

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

fun <T> ScreenState<T>.updateWhenIdle(action: (T) -> T): ScreenState<T> {
    return if (this is ScreenState.Idle) ScreenState.Idle(action(data)) else this
}

fun <T> StateFlow<ScreenState<T>>.updateWhenIdle(action: (T) -> T): ScreenState<T> {
    return this.value.updateWhenIdle(action)
}
