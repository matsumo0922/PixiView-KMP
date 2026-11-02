package me.matsumo.fanbox.core.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavMetadataKey
import androidx.navigation3.runtime.get
import androidx.navigation3.runtime.metadata
import androidx.navigation3.scene.OverlayScene
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import kotlinx.coroutines.flow.first
import kotlin.coroutines.cancellation.CancellationException

/**
 * metadata に [bottomSheet] を持つ entry を、material の [ModalBottomSheetLayout] で表示する。
 *
 * 下にある画面の上に重ねて表示するため、overlay でない SceneStrategy より前に置く。
 */
class BottomSheetSceneStrategy<T : Any> : SceneStrategy<T> {

    override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {
        val lastEntry = entries.lastOrNull() ?: return null
        val sheetMetadata = lastEntry.metadata[BottomSheetKey] ?: return null
        val previousEntries = entries.dropLast(1)

        return BottomSheetScene(
            key = lastEntry.contentKey,
            previousEntries = previousEntries,
            overlaidEntries = previousEntries,
            entry = lastEntry,
            onUserDismiss = {
                sheetMetadata.onUserDismiss()
                onBack()
            },
        )
    }

    companion object {
        /**
         * entry を BottomSheet として表示するための metadata。
         *
         * @param onUserDismiss scrim のタップやスワイプでユーザーが閉じたときに呼ぶ。戻る操作や画面からの pop では呼ばない。
         */
        fun bottomSheet(onUserDismiss: () -> Unit = {}): Map<String, Any> = metadata {
            put(BottomSheetKey, BottomSheetMetadata(onUserDismiss))
        }
    }
}

/** [BottomSheetSceneStrategy] が entry の metadata から読む値。 */
private class BottomSheetMetadata(val onUserDismiss: () -> Unit)

private object BottomSheetKey : NavMetadataKey<BottomSheetMetadata>

/**
 * [entry] を BottomSheet で表示する [OverlayScene]。
 *
 * 表示したときに sheet を開き、pop されたときは [onRemove] で sheet を閉じきってから composition を離れる。
 * sheet がそれ以外の理由で閉じた場合はユーザーが閉じたとみなし、[onUserDismiss] を呼ぶ。
 */
@OptIn(ExperimentalMaterialApi::class)
private class BottomSheetScene<T : Any>(
    override val key: Any,
    override val previousEntries: List<NavEntry<T>>,
    override val overlaidEntries: List<NavEntry<T>>,
    private val entry: NavEntry<T>,
    private val onUserDismiss: () -> Unit,
) : OverlayScene<T> {

    override val entries: List<NavEntry<T>> = listOf(entry)

    private var sheetState: ModalBottomSheetState? = null
    private var isRemoving = false

    override val content: @Composable () -> Unit = {
        val state = rememberModalBottomSheetState(
            initialValue = ModalBottomSheetValue.Hidden,
            skipHalfExpanded = true,
        )

        SideEffect { sheetState = state }

        LaunchedEffect(state) {
            showAndAwaitHidden(state)

            if (!isRemoving) onUserDismiss()
        }

        ModalBottomSheetLayout(
            modifier = Modifier.fillMaxSize(),
            sheetState = state,
            sheetContent = { entry.Content() },
            content = {},
        )
    }

    override suspend fun onRemove() {
        isRemoving = true
        sheetState?.hide()
    }

    private suspend fun showAndAwaitHidden(state: ModalBottomSheetState) {
        try {
            state.show()
        } catch (_: CancellationException) {
            // 開いている途中にユーザーが閉じると show が中断される。閉じたことは下の待機で検知する
        }

        snapshotFlow { state.isVisible }.first { isVisible -> !isVisible }
    }
}
