package me.matsumo.fanbox.feature.welcome.web

/**
 * WebView でのログインが終わったとみなし、セッションを確かめに行くかを決める。
 *
 * FANBOX はログインを終えると `return_to` の URL へ戻すが、二段階認証やセキュリティ設定のお知らせなどの
 * 途中の画面を挟むと、別の FANBOX のページへ戻ることがある。戻り先の URL に一致した場合に加えて、
 * ログイン画面以外の FANBOX のページで、まだ確かめていない FANBOXSESSID がある場合も確かめる。
 *
 * [url] は WebView が最後に読み込んだ URL、[redirectUrl] は `return_to` に渡した URL、[sessionId] は
 * そのときの FANBOXSESSID、[checkedSessionId] は前に確かめた FANBOXSESSID である。同じセッションを
 * 確かめ直さないため、FANBOX のページを移るたびに確かめることはない。
 */
internal fun shouldCheckWebLogin(
    url: String?,
    redirectUrl: String,
    sessionId: String?,
    checkedSessionId: String?,
): Boolean {
    if (url == null) return false
    if (url == redirectUrl) return true

    val isUncheckedSession = sessionId != null && sessionId != checkedSessionId

    return isUncheckedSession && url.isFanboxPageAfterLogin()
}

/** URL のホストを返す。`https://` で始まらない場合は null を返す。 */
internal fun String.urlHost(): String? {
    if (!startsWith("https://")) return null

    return removePrefix("https://")
        .substringBefore('/')
        .substringBefore('?')
        .substringBefore('#')
}

private fun String.isFanboxPageAfterLogin(): Boolean {
    val host = urlHost() ?: return false
    val path = removePrefix("https://").removePrefix(host)
    val isFanboxHost = host == "fanbox.cc" || host.endsWith(".fanbox.cc")

    return isFanboxHost && !path.startsWith("/login")
}
