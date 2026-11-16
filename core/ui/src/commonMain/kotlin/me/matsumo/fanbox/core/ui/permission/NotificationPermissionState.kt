package me.matsumo.fanbox.core.ui.permission

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable

/**
 * 通知を表示できるかと、通知の許可を求める操作。
 *
 * 案内を表示したことと、許可を求めた結果は、案内の種類を表す referrer 付きで Firebase Analytics へ送る。
 */
@Stable
interface NotificationPermissionState {

    /** 通知を表示できるか。Android 12 以下では、端末の設定で通知が有効かを表す。 */
    val isGranted: Boolean

    /** 案内を表示したことを記録する。 */
    suspend fun reportPromptShown()

    /**
     * 通知の許可を求める。
     *
     * Android 13 以上で権限のダイアログを出せるときはダイアログを出す。出せないとき、または Android 12 以下では、
     * 端末のアプリの通知の設定画面を開き、アプリに戻った時点の状態を結果とする。
     */
    fun request()
}

/** 通知の権限の状態を保持する。[referrer] は案内の種類（dialog、notify_tab）で、ログに付ける。 */
@Composable
expect fun rememberNotificationPermissionState(referrer: String): NotificationPermissionState
