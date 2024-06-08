package me.matsumo.fanbox.core.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeUIViewController
import com.mohamedrejeb.calf.ui.sheet.BottomSheetControllerDelegate
import com.mohamedrejeb.calf.ui.sheet.BottomSheetTransitioningDelegate
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import me.matsumo.fanbox.core.ui.extensition.LocalSnackbarHostState
import platform.UIKit.UIApplication
import platform.UIKit.UIModalPresentationPopover
import platform.UIKit.UISheetPresentationControllerDetentIdentifierLarge
import platform.UIKit.UISheetPresentationControllerDetentIdentifierMedium
import platform.UIKit.presentationController
import platform.UIKit.sheetPresentationController
import platform.UIKit.transitioningDelegate
import kotlin.concurrent.Volatile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun SimpleBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier,
    skipPartiallyExpanded: Boolean,
    content: @Composable ColumnScope.() -> Unit
) {
    val adaptiveSheetState = rememberAdaptiveSheetState(skipPartiallyExpanded)
    val typography = MaterialTheme.typography.copy()
    val shapes = MaterialTheme.shapes.copy()
    val colorScheme = MaterialTheme.colorScheme.copy()

    val sheetManager = remember {
        BottomSheetManager(
            onDismiss = onDismissRequest,
            content = {
                val snackbarHostState = remember { SnackbarHostState() }

                MaterialTheme(
                    typography = typography,
                    shapes = shapes,
                    colorScheme = colorScheme,
                ) {
                    CompositionLocalProvider(LocalSnackbarHostState provides snackbarHostState) {
                        Scaffold(
                            modifier = modifier,
                            snackbarHost = {
                                SnackbarHost(
                                    modifier = Modifier.navigationBarsPadding(),
                                    hostState = snackbarHostState,
                                )
                            },
                        ) {
                            Column(modifier) {
                                content.invoke(this)
                            }
                        }
                    }
                }
            }
        )
    }

    LaunchedEffect(Unit) {
        adaptiveSheetState.show()
    }

    LaunchedEffect(adaptiveSheetState.sheetValue) {
        println(adaptiveSheetState.sheetValue)
        if (adaptiveSheetState.sheetValue == SheetValue.Hidden) {
            sheetManager.hide(
                completion = {
                    adaptiveSheetState.deferredUntilHidden.complete(Unit)
                }
            )
        } else {
            sheetManager.show()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            sheetManager.hide()
            adaptiveSheetState.sheetValue = SheetValue.Hidden
        }
    }
}

private class BottomSheetManager(
    private val onDismiss: () -> Unit,
    private val content: @Composable () -> Unit,
) {
    private var isPresented = false
    private var isAnimating = false

    private val bottomSheetUIViewController = ComposeUIViewController {
        content.invoke()
    }

    private val bottomSheetTransitioningDelegate = BottomSheetTransitioningDelegate()

    private val presentationControllerDelegate = BottomSheetControllerDelegate(
        onDismiss = {
            isPresented = false
            onDismiss.invoke()
        }
    )

    fun show() {
        if (isPresented || isAnimating) return

        isAnimating = true

        bottomSheetUIViewController.modalPresentationStyle = UIModalPresentationPopover
        bottomSheetUIViewController.transitioningDelegate = bottomSheetTransitioningDelegate
        bottomSheetUIViewController.presentationController?.setDelegate(presentationControllerDelegate)

        bottomSheetUIViewController.sheetPresentationController?.setDetents(
            listOf(
                UISheetPresentationControllerDetentIdentifierMedium,
                UISheetPresentationControllerDetentIdentifierLarge
            )
        )
        bottomSheetUIViewController.sheetPresentationController?.prefersGrabberVisible = true

        UIApplication.sharedApplication.keyWindow?.rootViewController?.presentViewController(
            viewControllerToPresent = bottomSheetUIViewController,
            animated = true,
            completion = {
                isPresented = true
                isAnimating = false
            }
        )
    }

    /**
     * Hides the bottom sheet.
     */
    fun hide(
        completion: (() -> Unit)? = null
    ) {
        if (!isPresented || isAnimating) return

        isAnimating = true

        bottomSheetUIViewController.dismissViewControllerAnimated(
            flag = true,
            completion = {
                isPresented = false
                isAnimating = false
                completion?.invoke()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Stable
private class AdaptiveSheetState(
    skipPartiallyExpanded: Boolean,
    initialValue: SheetValue,
    confirmValueChange: (SheetValue) -> Boolean,
    skipHiddenState: Boolean,
) {
    init {
        if (skipPartiallyExpanded) {
            require(initialValue != SheetValue.PartiallyExpanded) {
                "The initial value must not be set to PartiallyExpanded if skipPartiallyExpanded " +
                        "is set to true."
            }
        }
        if (skipHiddenState) {
            require(initialValue != SheetValue.Hidden) {
                "The initial value must not be set to Hidden if skipHiddenState is set to true."
            }
        }
    }

    var sheetValue by mutableStateOf(initialValue)
    val currentValue: SheetValue get() = sheetValue
    val isVisible get() = currentValue != SheetValue.Hidden

    @Volatile
    internal var deferredUntilHidden = CompletableDeferred<Unit>()

    /**
     * Expand the bottom sheet with animation and suspend until it is [PartiallyExpanded] if defined
     * else [Expanded].
     * @throws [CancellationException] if the animation is interrupted
     */
    suspend fun show() {
        sheetValue = SheetValue.Expanded
    }

    /**
     * Hide the bottom sheet with animation and suspend until it is fully hidden or animation has
     * been cancelled.
     * @throws [CancellationException] if the animation is interrupted
     */
    suspend fun hide() {
        sheetValue = SheetValue.Hidden
        deferredUntilHidden.await()
        deferredUntilHidden = CompletableDeferred()
    }

    companion object {
        /**
         * The default [Saver] implementation for [AdaptiveSheetState].
         */
        fun Saver(
            skipPartiallyExpanded: Boolean,
            confirmValueChange: (SheetValue) -> Boolean
        ) = Saver<AdaptiveSheetState, SheetValue>(
            save = { it.currentValue },
            restore = { savedValue ->
                AdaptiveSheetState(skipPartiallyExpanded, savedValue, confirmValueChange, false)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun rememberAdaptiveSheetState(
    skipPartiallyExpanded: Boolean = false,
    confirmValueChange: (SheetValue) -> Boolean = { true },
): AdaptiveSheetState {
    return rememberSaveable(
        skipPartiallyExpanded, confirmValueChange,
        saver = AdaptiveSheetState.Saver(
            skipPartiallyExpanded = skipPartiallyExpanded,
            confirmValueChange = confirmValueChange
        )
    ) {
        AdaptiveSheetState(skipPartiallyExpanded, SheetValue.Hidden, confirmValueChange, false)
    }
}
