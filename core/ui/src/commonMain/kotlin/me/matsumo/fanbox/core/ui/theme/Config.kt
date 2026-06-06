package me.matsumo.fanbox.core.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import me.matsumo.fanbox.core.common.PixiViewConfig

/** PixiView の実行時設定を提供する CompositionLocal。 */
val LocalPixiViewConfig = staticCompositionLocalOf { PixiViewConfig.dummy() }

/** Google Mobile Ads SDK の初期化完了状態を提供する CompositionLocal。 */
val LocalAdsSdkInitialized = staticCompositionLocalOf { true }
