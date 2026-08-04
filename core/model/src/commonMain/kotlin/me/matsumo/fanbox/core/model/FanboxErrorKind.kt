package me.matsumo.fanbox.core.model

import me.matsumo.fanbox.core.resources.Res
import me.matsumo.fanbox.core.resources.error_forbidden
import me.matsumo.fanbox.core.resources.error_network
import me.matsumo.fanbox.core.resources.error_network_description
import me.matsumo.fanbox.core.resources.error_no_data
import me.matsumo.fanbox.core.resources.error_rate_limited
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
 * - [Forbidden]: [FanboxException.Forbidden]（HTTP 403）
 * - [NotFound]: [FanboxException.NotFound]（HTTP 404）
 * - [RateLimited]: [FanboxException.RateLimited]（HTTP 429）
 * - [ServerError]: [FanboxException.ServerError]（HTTP 5xx）
 * - [Network]: [FanboxException.Network]（応答を受け取る前の通信失敗）
 * - [SchemaMismatch]: [FanboxException.SchemaMismatch]（応答の形式が期待と異なる）
 * - [Unknown]: 上記以外。[FanboxException.UnexpectedHttpError] と FANBOX 由来でない失敗を含む
 */
enum class FanboxErrorKind {
    Unauthorized,
    Forbidden,
    NotFound,
    RateLimited,
    ServerError,
    Network,
    SchemaMismatch,
    Unknown,
}

/**
 * HTTP ステータスから分類を決める。
 *
 * いずれの分類にも当てはまらないステータスと、ステータスを伴わない失敗には null を返す。
 */
internal fun fanboxErrorKindOf(statusCode: Int?): FanboxErrorKind? {
    return when (statusCode) {
        null -> null
        401 -> FanboxErrorKind.Unauthorized
        403 -> FanboxErrorKind.Forbidden
        404 -> FanboxErrorKind.NotFound
        429 -> FanboxErrorKind.RateLimited
        in 500..599 -> FanboxErrorKind.ServerError
        else -> null
    }
}

/**
 * FANBOX API の失敗を分類する。
 *
 * 中断（`CancellationException`）は呼び出し側の `suspendRunCatching` で除外済みであることを前提とする。
 */
fun Throwable.toFanboxErrorKind(): FanboxErrorKind = when (this) {
    is FanboxException.SchemaMismatch -> FanboxErrorKind.SchemaMismatch
    is FanboxException.Network -> FanboxErrorKind.Network
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
    val message: StringResource = when (toFanboxErrorKind()) {
        FanboxErrorKind.Unauthorized -> Res.string.error_session_expired
        FanboxErrorKind.Forbidden -> Res.string.error_forbidden
        FanboxErrorKind.NotFound -> Res.string.error_no_data
        FanboxErrorKind.RateLimited -> Res.string.error_rate_limited
        FanboxErrorKind.ServerError -> Res.string.error_server
        FanboxErrorKind.Network -> Res.string.error_network_description
        FanboxErrorKind.SchemaMismatch -> Res.string.error_schema_mismatch
        FanboxErrorKind.Unknown -> return ScreenState.Error(
            message = fallbackMessage,
            retryTitle = fallbackRetryTitle,
        )
    }

    return ScreenState.Error(message = message)
}
