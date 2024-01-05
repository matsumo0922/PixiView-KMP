package me.matsumo.fanbox.core.model

sealed class ScreenState<out T> {
    data object Loading : ScreenState<Nothing>()

    data class Error(
        val message: Int,
        val retryTitle: Int? = null,
    ) : ScreenState<Nothing>()

    data class Idle<T>(
        var data: T,
    ) : ScreenState<T>()
}

fun <T> ScreenState<T>.changeContent(action: (T) -> T): ScreenState<T> {
    return if (this is ScreenState.Idle) ScreenState.Idle(action(data)) else this
}
