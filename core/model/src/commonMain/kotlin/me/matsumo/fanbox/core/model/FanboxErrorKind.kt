package me.matsumo.fanbox.core.model

import me.matsumo.fanbox.core.resources.Res
import me.matsumo.fanbox.core.resources.error_blocked
import me.matsumo.fanbox.core.resources.error_challenged
import me.matsumo.fanbox.core.resources.error_forbidden
import me.matsumo.fanbox.core.resources.error_guest_failure
import me.matsumo.fanbox.core.resources.error_network
import me.matsumo.fanbox.core.resources.error_network_description
import me.matsumo.fanbox.core.resources.error_no_data
import me.matsumo.fanbox.core.resources.error_rate_limited
import me.matsumo.fanbox.core.resources.error_region_blocked
import me.matsumo.fanbox.core.resources.error_schema_mismatch
import me.matsumo.fanbox.core.resources.error_server
import me.matsumo.fanbox.core.resources.error_session_expired
import me.matsumo.fankt.fanbox.FanboxException
import org.jetbrains.compose.resources.StringResource

/**
 * FANBOX API の失敗を UI が扱える粒度へ分類した種別。
 *
 * 各分類は [FanboxException] の subtype に次のように対応する。
 *
 * - [Unauthorized]: [FanboxException.Unauthorized]（HTTP 401）
 * - [Forbidden]: [FanboxException.Forbidden]（HTTP 403）のうち、下の 3 つに当たらないもの
 * - [Challenged]: Cloudflare が通信の前に確認を求めた 403
 * - [RegionBlocked]: Cloudflare が接続元の国や地域を理由に拒否した 403
 * - [Blocked]: Cloudflare が上記以外の理由で拒否した 403
 * - [NotFound]: [FanboxException.NotFound]（HTTP 404）
 * - [RateLimited]: [FanboxException.RateLimited]（HTTP 429）
 * - [ServerError]: [FanboxException.ServerError]（HTTP 5xx）
 * - [Network]: [FanboxException.Network]（応答を受け取る前の通信失敗）
 * - [SchemaMismatch]: [FanboxException.SchemaMismatch]（応答の形式が期待と異なる）
 * - [GuestFailure]: [FanboxException.GuestFailure]（解析処理が request の組み立てか response の解析に失敗した）
 * - [Unknown]: 上記以外。[FanboxException.UnexpectedHttpError] と FANBOX 由来でない失敗を含む
 */
enum class FanboxErrorKind {
    Unauthorized,
    Forbidden,
    Challenged,
    RegionBlocked,
    Blocked,
    NotFound,
    RateLimited,
    ServerError,
    Network,
    SchemaMismatch,
    GuestFailure,
    Unknown,
}

/**
 * HTTP ステータスから分類を決める。
 *
 * いずれの分類にも当てはまらないステータスと、ステータスを伴わない失敗には null を返す。
 */
internal fun fanboxErrorKindOf(statusCode: Int?): FanboxErrorKind? = when (statusCode) {
    null -> null
    401 -> FanboxErrorKind.Unauthorized
    403 -> FanboxErrorKind.Forbidden
    404 -> FanboxErrorKind.NotFound
    429 -> FanboxErrorKind.RateLimited
    in 500..599 -> FanboxErrorKind.ServerError
    else -> null
}

/**
 * HTTP 403 の本文から、Cloudflare に止められたのかを判定する。
 *
 * Cloudflare は確認画面（`Just a moment...`）か、エラーコード付きの拒否画面を返す。HTML を返さない
 * API のホストでは `error code: 1020` のような 1 行になる。どれにも当たらない場合は FANBOX 自身の
 * 拒否として [FanboxErrorKind.Forbidden] を返す。
 */
internal fun forbiddenKindOf(rawBody: String?): FanboxErrorKind {
    val body = rawBody.orEmpty()

    // 確認画面に必ず含まれる title と script の識別子。
    val isChallenge = body.containsAny(
        "<title>Just a moment...</title>",
        "challenge-platform",
        "cf_chl_opt",
    )

    // 国や地域による拒否（エラー 1009）。HTML の画面と、API のホストが返す 1 行の両方。
    val isRegionBlocked = body.containsAny("error code: 1009", "banned the country or region")

    // それ以外の Cloudflare による拒否（エラー 1020 などのファイアウォールのルール）。
    val isBlocked = body.containsAny(
        "error code: 10",
        "used Cloudflare to restrict access",
        "Sorry, you have been blocked",
    )

    return when {
        isChallenge -> FanboxErrorKind.Challenged
        isRegionBlocked -> FanboxErrorKind.RegionBlocked
        isBlocked -> FanboxErrorKind.Blocked
        else -> FanboxErrorKind.Forbidden
    }
}

/**
 * FANBOX API の失敗を分類する。
 *
 * 中断（`CancellationException`）は呼び出し側の `suspendRunCatching` で除外済みであることを前提とする。
 */
fun Throwable.toFanboxErrorKind(): FanboxErrorKind = when (this) {
    is FanboxException.SchemaMismatch -> FanboxErrorKind.SchemaMismatch
    is FanboxException.GuestFailure -> FanboxErrorKind.GuestFailure
    is FanboxException.Network -> FanboxErrorKind.Network
    is FanboxException.Forbidden -> forbiddenKindOf(rawBody)
    is FanboxException -> fanboxErrorKindOf(statusCode) ?: FanboxErrorKind.Unknown
    else -> FanboxErrorKind.Unknown
}

/**
 * FANBOX API の失敗を、分類に応じたエラー状態へ変換する。
 *
 * 分類が [FanboxErrorKind.Unknown] のときだけ [fallbackMessage] と [fallbackRetryTitle] を使う。
 * 固有の再試行ボタン文言を持つ画面は [fallbackRetryTitle] で従来の表示を保てる。
 */
fun Throwable.toScreenStateError(
    fallbackMessage: StringResource = Res.string.error_network,
    fallbackRetryTitle: StringResource? = null,
): ScreenState.Error {
    val message = toFanboxErrorKind().messageOrNull() ?: return ScreenState.Error(
        message = fallbackMessage,
        retryTitle = fallbackRetryTitle,
    )

    return ScreenState.Error(message = message)
}

/** 分類に応じて利用者に見せる文言。[FanboxErrorKind.Unknown] は画面ごとに文言が違うため null を返す。 */
fun FanboxErrorKind.messageOrNull(): StringResource? = when (this) {
    FanboxErrorKind.Unauthorized -> Res.string.error_session_expired
    FanboxErrorKind.Forbidden -> Res.string.error_forbidden
    FanboxErrorKind.Challenged -> Res.string.error_challenged
    FanboxErrorKind.RegionBlocked -> Res.string.error_region_blocked
    FanboxErrorKind.Blocked -> Res.string.error_blocked
    FanboxErrorKind.NotFound -> Res.string.error_no_data
    FanboxErrorKind.RateLimited -> Res.string.error_rate_limited
    FanboxErrorKind.ServerError -> Res.string.error_server
    FanboxErrorKind.Network -> Res.string.error_network_description
    FanboxErrorKind.SchemaMismatch -> Res.string.error_schema_mismatch
    FanboxErrorKind.GuestFailure -> Res.string.error_guest_failure
    FanboxErrorKind.Unknown -> null
}

private fun String.containsAny(vararg markers: String): Boolean = markers.any { contains(it, ignoreCase = true) }
