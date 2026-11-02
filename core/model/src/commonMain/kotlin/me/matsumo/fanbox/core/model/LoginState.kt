package me.matsumo.fanbox.core.model

import androidx.compose.runtime.Immutable
import me.matsumo.fanbox.core.model.fanbox.MetaData

/** 保存したセッションで FANBOX にログインできているかを確かめた結果。 */
@Immutable
sealed interface LoginState {

    /** ログインしている。[metaData] は確かめたときに FANBOX から取得した情報。 */
    @Immutable
    data class LoggedIn(val metaData: MetaData) : LoginState

    /** ログインしていない。セッションが無いか、FANBOX が無効と答えた。 */
    data object LoggedOut : LoginState

    /** 保存したセッションが壊れていて読めない。ログインし直す必要がある。 */
    data object SessionUnreadable : LoginState

    /** FANBOX に確かめられなかった。[errorKind] はその理由で、セッションは無効と決まっていない。 */
    @Immutable
    data class Unverified(val errorKind: FanboxErrorKind) : LoginState
}

/**
 * 確かめ直した結果 [checked] を受けた後の状態。
 *
 * ログイン中に確かめられなかっただけなら、ログイン中のままにする。通信が途切れたりメンテナンスが
 * 始まったりするたびに、使っている途中の画面を閉じないため。
 */
fun LoginState.updatedBy(checked: LoginState): LoginState = if (this is LoginState.LoggedIn && checked is LoginState.Unverified) this else checked

/** セッションを確かめる処理の失敗を、確かめた結果に変換する。FANBOX がセッションを無効と答えた場合だけログアウトとする。 */
fun Throwable.toLoginState(): LoginState = when (val errorKind = toFanboxErrorKind()) {
    FanboxErrorKind.Unauthorized -> LoginState.LoggedOut
    else -> LoginState.Unverified(errorKind)
}
