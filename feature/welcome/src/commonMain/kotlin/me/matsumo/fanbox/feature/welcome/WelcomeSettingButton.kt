package me.matsumo.fanbox.feature.welcome

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * ログイン前の画面から設定画面を開く、見えないボタン。
 *
 * 開発者がログインの前に developer mode を切り替えるためにあり、利用者に見せる導線ではないので、描画もタップの波紋も出さない。
 */
@Composable
internal fun WelcomeSettingButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.clickable(
            interactionSource = null,
            indication = null,
            onClick = onClick,
        ),
    )
}
