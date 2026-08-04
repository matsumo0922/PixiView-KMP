package me.matsumo.fanbox.core.datastore.cookie

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.matsumo.fankt.fanbox.FanboxCookieRecord

/**
 * secure store へ 1 つの値として書き込む Cookie の保存形式。
 *
 * レコードと移行状態を 1 つの値にまとめることで、片方だけが書かれた状態を作らない。
 *
 * [records] は認証情報を含むため、ログや telemetry に出さないこと。
 */
@Serializable
internal data class SecureCookiePayload(
    @SerialName("records") val records: List<SecureCookieRecord> = emptyList(),
    @SerialName("isMigrationCompleted") val isMigrationCompleted: Boolean = false,
    @SerialName("isLogoutInProgress") val isLogoutInProgress: Boolean = false,
) {
    /** ログアウト途中でも移行済みでもない、まだ何も書かれていない状態か。 */
    val isEmpty: Boolean
        get() = records.isEmpty() && !isMigrationCompleted && !isLogoutInProgress

    override fun toString(): String =
        "SecureCookiePayload(records=${records.size} 件, isMigrationCompleted=$isMigrationCompleted, " +
            "isLogoutInProgress=$isLogoutInProgress)"
}

/**
 * secure store に保存する Cookie 1 件。
 *
 * [FanboxCookieRecord] をそのまま保存すると fankt の変更が保存形式に波及するため、
 * 同じ 7 フィールドを持つ独自の型に写して保存する。
 *
 * [value] は認証情報のため、ログや telemetry に出さないこと。
 */
@Serializable
internal data class SecureCookieRecord(
    @SerialName("domain") val domain: String,
    @SerialName("path") val path: String,
    @SerialName("name") val name: String,
    @SerialName("value") val value: String,
    @SerialName("expiresAtEpochMilliseconds") val expiresAtEpochMilliseconds: Long? = null,
    @SerialName("secure") val secure: Boolean = false,
    @SerialName("hostOnly") val hostOnly: Boolean = false,
) {
    override fun toString(): String =
        "SecureCookieRecord(domain=$domain, path=$path, name=$name, " +
            "expiresAtEpochMilliseconds=$expiresAtEpochMilliseconds, secure=$secure, " +
            "hostOnly=$hostOnly, value=<redacted>)"
}

internal fun FanboxCookieRecord.toSecureCookieRecord(): SecureCookieRecord = SecureCookieRecord(
    domain = domain,
    path = path,
    name = name,
    value = value,
    expiresAtEpochMilliseconds = expiresAtEpochMilliseconds,
    secure = secure,
    hostOnly = hostOnly,
)

internal fun SecureCookieRecord.toFanboxCookieRecord(): FanboxCookieRecord = FanboxCookieRecord(
    domain = domain,
    path = path,
    name = name,
    value = value,
    expiresAtEpochMilliseconds = expiresAtEpochMilliseconds,
    secure = secure,
    hostOnly = hostOnly,
)
