package me.matsumo.fanbox.core.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.scene.Scene
import androidx.navigation3.ui.NavDisplay
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * [navigator] のバックスタックを表示する。
 *
 * 各画面に rememberSaveable の状態と ViewModel を持たせ、metadata に応じて Dialog と BottomSheet を重ねて表示する。
 * 画面遷移には横軸の shared axis を使う。
 *
 * @param entryDecorators 標準の decorator に加えて適用する decorator。
 */
@Composable
fun PixiViewNavDisplay(
    navigator: PixiViewNavigator,
    entryProvider: (DestinationKey) -> NavEntry<DestinationKey>,
    modifier: Modifier = Modifier,
    entryDecorators: ImmutableList<NavEntryDecorator<DestinationKey>> = persistentListOf(),
) {
    val slideDistance = with(LocalDensity.current) { 30.dp.roundToPx() }
    val sceneStrategies = remember {
        listOf(
            BottomSheetSceneStrategy<DestinationKey>(),
            DialogSceneStrategy(),
        )
    }

    NavDisplay(
        modifier = modifier,
        backStack = navigator.backStack,
        onBack = navigator::back,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator<DestinationKey>(),
            rememberViewModelStoreNavEntryDecorator(),
        ) + entryDecorators,
        sceneStrategies = sceneStrategies,
        transitionSpec = { sharedAxisX(SlideDirection.Start, slideDistance) },
        popTransitionSpec = { sharedAxisX(SlideDirection.End, slideDistance) },
        predictivePopTransitionSpec = { sharedAxisX(SlideDirection.End, slideDistance) },
        entryProvider = entryProvider,
    )
}

/**
 * 出ていく画面と入ってくる画面が横軸を共有する遷移を返す。
 *
 * 予測型戻るは進捗を時間へ写して seek するため、時間に対して位置が単調に進む spec でなければ
 * ならない。`MotionScheme` の spatial spec は damping が 1 未満で行き過ぎてから戻るため、
 * 指を進めると画面が戻る向きへ動く。navigation3 の既定の予測型戻るが damping 1.0 の spring
 * だけで組まれているのも同じ理由である。ここでは時間基準の tween を使う。
 *
 * 移動量は Material の shared axis に合わせて [slideDistance] に抑える。全幅の移動量は方向で
 * 符号が変わるため、符号を保ったまま大きさだけを丸める。
 */
private fun <T : Any> AnimatedContentTransitionScope<Scene<T>>.sharedAxisX(
    towards: SlideDirection,
    slideDistance: Int,
): ContentTransform {
    val slideSpec = tween<IntOffset>(durationMillis = 300, easing = FastOutSlowInEasing)
    val fadeSpec = tween<Float>(durationMillis = 300, easing = FastOutSlowInEasing)
    val offset: (Int) -> Int = { it.coerceIn(-slideDistance, slideDistance) }

    return ContentTransform(
        slideIntoContainer(
            towards = towards,
            animationSpec = slideSpec,
            initialOffset = offset,
        ) + fadeIn(fadeSpec),
        slideOutOfContainer(
            towards = towards,
            animationSpec = slideSpec,
            targetOffset = offset,
        ) + fadeOut(fadeSpec),
    )
}
