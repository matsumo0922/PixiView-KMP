@file:Suppress("ModifierReused")

package me.matsumo.fanbox.core.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import me.matsumo.fanbox.core.model.ScreenState
import me.matsumo.fanbox.core.ui.extensition.LocalSnackbarHostState
import me.matsumo.fanbox.core.ui.view.ErrorView
import me.matsumo.fanbox.core.ui.view.LoadingView

@Composable
fun <T> AsyncLoadContents(
    screenState: ScreenState<T>,
    modifier: Modifier = Modifier,
    otherModifier: Modifier = modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    cornerShape: RoundedCornerShape = RoundedCornerShape(0.dp),
    retryAction: () -> Unit = {},
    content: @Composable (T) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        snackbarHost = {
            SnackbarHost(
                modifier = Modifier.navigationBarsPadding(),
                hostState = LocalSnackbarHostState.current,
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) {
        AnimatedContent(
            modifier = Modifier
                .clip(cornerShape)
                .background(containerColor),
            targetState = screenState,
            transitionSpec = { fadeIn().togetherWith(fadeOut()) },
            contentKey = { it::class.simpleName },
            label = "AsyncLoadContents",
        ) { state ->
            when (state) {
                is ScreenState.Idle -> {
                    content.invoke(state.data)
                }

                is ScreenState.Loading -> {
                    LoadingView(
                        modifier = otherModifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.2f)),
                    )
                }

                is ScreenState.Error -> {
                    ErrorView(
                        modifier = otherModifier.fillMaxWidth(),
                        errorState = state,
                        retryAction = retryAction,
                    )
                }
            }
        }
    }
}

@Composable
fun <T> AsyncLoadContentsWithoutAnimation(
    screenState: ScreenState<T>,
    modifier: Modifier = Modifier,
    otherModifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    cornerShape: RoundedCornerShape = RoundedCornerShape(0.dp),
    retryAction: () -> Unit = {},
    content: @Composable (T) -> Unit,
) {
    Box(
        modifier = modifier
            .clip(cornerShape)
            .background(containerColor),
    ) {
        when (screenState) {
            is ScreenState.Idle -> {
                content.invoke(screenState.data)
            }
            is ScreenState.Loading -> {
                LoadingView(
                    modifier = otherModifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.2f)),
                )
            }
            is ScreenState.Error -> {
                ErrorView(
                    modifier = otherModifier.fillMaxWidth(),
                    errorState = screenState,
                    retryAction = retryAction,
                )
            }
        }
    }
}

@Composable
fun <T> AsyncNoLoadContents(
    screenState: ScreenState<T>,
    modifier: Modifier = Modifier,
    otherModifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    cornerShape: RoundedCornerShape = RoundedCornerShape(0.dp),
    retryAction: () -> Unit = {},
    content: @Composable (T?) -> Unit,
) {
    Box(
        modifier = modifier
            .clip(cornerShape)
            .background(containerColor),
    ) {
        when (screenState) {
            is ScreenState.Idle, is ScreenState.Loading -> {
                content.invoke((screenState as? ScreenState.Idle)?.data)
            }
            is ScreenState.Error -> {
                ErrorView(
                    modifier = otherModifier.fillMaxWidth(),
                    errorState = screenState,
                    retryAction = retryAction,
                )
            }
        }
    }
}
