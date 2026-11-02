package me.matsumo.fanbox.feature.welcome

import me.matsumo.fanbox.core.model.LoginState
import me.matsumo.fanbox.core.model.messageOrNull
import me.matsumo.fanbox.core.resources.Res
import me.matsumo.fanbox.core.resources.welcome_login_toast_failed
import org.jetbrains.compose.resources.StringResource

/** ログインできなかったときに利用者へ見せる文言。確かめられなかった場合は、その理由を伝える。 */
internal fun LoginState.loginFailureMessage(): StringResource = (this as? LoginState.Unverified)?.errorKind?.messageOrNull()
    ?: Res.string.welcome_login_toast_failed
