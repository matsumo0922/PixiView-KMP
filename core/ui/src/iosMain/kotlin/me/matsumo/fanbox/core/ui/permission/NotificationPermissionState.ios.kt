package me.matsumo.fanbox.core.ui.permission

import androidx.compose.runtime.Composable

// 新着投稿の通知は Android のみの機能のため、iOS では許可済みとして扱い、案内を出さない。
@Composable
actual fun rememberNotificationPermissionState(referrer: String): NotificationPermissionState = GrantedNotificationPermissionState

private object GrantedNotificationPermissionState : NotificationPermissionState {
    override val isGranted: Boolean = true

    override suspend fun reportPromptShown() = Unit

    override fun request() = Unit
}
