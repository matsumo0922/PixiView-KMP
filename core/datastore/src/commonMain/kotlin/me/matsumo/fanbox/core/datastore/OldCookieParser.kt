package me.matsumo.fanbox.core.datastore

/**
 * Cookie ヘッダ形式の文字列から `FANBOXSESSID` の値を取り出す。
 *
 * 対象は [OldCookieDataStore] が保持する、Room 導入より前の保存形式である。`名前=値` を `;` で
 * 連結した Cookie ヘッダそのものが入っている。
 *
 * 名前と値は最初の `=` だけで区切る。FANBOX のセッション値は base64 風の文字列で `=` を含みうるため、
 * すべての `=` で分割すると値が途中で切れる。また 2 番目以降の要素は `; ` 区切りで前に空白が入るので、
 * 要素ごとに trim してから名前を比較する。
 *
 * `=` を含まない要素、名前が空の要素、値が空の要素は無視する。該当する Cookie が無ければ null を返す。
 */
internal fun parseOldSessionId(cookieHeader: String?): String? {
    if (cookieHeader.isNullOrBlank()) return null

    return cookieHeader
        .split(';')
        .firstNotNullOfOrNull { element -> element.toSessionIdOrNull() }
}

private fun String.toSessionIdOrNull(): String? {
    val separatorIndex = indexOf('=')

    if (separatorIndex <= 0) return null

    val name = substring(0, separatorIndex).trim()

    if (name != FANBOX_SESSION_ID_NAME) return null

    return substring(separatorIndex + 1).trim().takeIf { it.isNotEmpty() }
}

/** FANBOX のセッション Cookie の名前。 */
internal const val FANBOX_SESSION_ID_NAME: String = "FANBOXSESSID"
