package me.matsumo.fanbox.feature.welcome

import me.matsumo.fanbox.core.logs.category.WelcomeLog
import me.matsumo.fanbox.core.logs.logger.send
import me.matsumo.fanbox.core.model.LoginState

/**
 * ログイン画面で、ログインできたかを確かめた契機。
 *
 * [method] はログインの方法、[logName] はログに載せる契機の名前。
 */
internal enum class LoginCheckTrigger(val method: String, val logName: String) {
    /** WebView で、`return_to` に渡した URL を開いた。 */
    Redirect("web", "redirect"),

    /** WebView で、`return_to` 以外の FANBOX のページを開いた。 */
    FanboxPage("web", "fanbox_page"),

    /** WebView のヘルプから、ログインを押した。 */
    Help("web", "help"),

    /** FANBOXSESSID を直接入力した。 */
    Input("session_id", "input"),
}

/** ログインできたかを確かめた結果を記録する。どの段階で失敗したかを、利用者の報告に頼らず数えるため。 */
internal fun LoginState.sendLoginChecked(trigger: LoginCheckTrigger) {
    val result = when (this) {
        is LoginState.LoggedIn -> "logged_in"
        LoginState.LoggedOut -> "logged_out"
        LoginState.SessionUnreadable -> "session_unreadable"
        is LoginState.Unverified -> "unverified"
    }
    val errorKind = (this as? LoginState.Unverified)?.errorKind?.name.orEmpty()

    WelcomeLog.loginChecked(
        method = trigger.method,
        trigger = trigger.logName,
        result = result,
        errorKind = errorKind,
    ).send()
}
