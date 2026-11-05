package me.matsumo.fanbox.core.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.common.util.recordException
import me.matsumo.fanbox.core.common.util.setCrashReportKey
import me.matsumo.fanbox.core.logs.category.GuestLog
import me.matsumo.fanbox.core.logs.logger.LogConfigurator
import me.matsumo.fanbox.core.logs.logger.send
import me.matsumo.fanbox.core.model.GuestSource
import me.matsumo.fankt.fanbox.FanboxGuestEvent

/**
 * guest で起きた出来事を Firebase へ送る。
 *
 * ロードの成功は回数を見るため Analytics のイベントにし、失敗は原因を見るため Crashlytics の non-fatal にする。
 * ロードした bundle の版はクラッシュレポートと突き合わせられるよう custom key にも設定する。
 * どの guest で処理しているかは [source] で画面に渡す。
 *
 * fankt はこの処理を背景の thread から呼び、ここで投げた例外は記録して捨てる。
 */
internal class GuestEventReporter(private val scope: CoroutineScope) {

    private val _source = MutableStateFlow<GuestSource>(GuestSource.Loading)

    val source: StateFlow<GuestSource> = _source.asStateFlow()

    /** guest を読み込み直す前に呼び、ロードが終わるまで出所を [GuestSource.Loading] にする。 */
    fun markLoading() {
        _source.value = GuestSource.Loading
    }

    fun report(event: FanboxGuestEvent) {
        _source.update { it.updatedBy(event) }

        when (event) {
            is FanboxGuestEvent.Loaded -> reportLoaded(event.version ?: "local")

            is FanboxGuestEvent.LoadFailed -> recordException(GuestEventException("Guest load failed (${event.errorType})"))

            is FanboxGuestEvent.SchemaMismatch -> recordException(GuestEventException("Guest rejected the ${event.endpoint} response"))

            is FanboxGuestEvent.CallFailed -> recordException(
                GuestEventException("Guest ${event.operation} failed for ${event.endpoint} (${event.errorType})"),
            )
        }
    }

    // guest はログの送信先の設定より先にロードされうる。設定前に送ると失敗するため、設定を待ってから送る。
    private fun reportLoaded(version: String) {
        setCrashReportKey("guestBundleVersion", version)

        scope.launch {
            LogConfigurator.isConfigured.first { it }
            GuestLog.loaded(version).send()
        }
    }
}

/** [event] を受けた後の guest の出所。ロードの結果を表さない event では変わらない。 */
internal fun GuestSource.updatedBy(event: FanboxGuestEvent): GuestSource = when (event) {
    is FanboxGuestEvent.Loaded -> GuestSource.Delivered(version = event.version, isFromCache = event.isFromCache)
    is FanboxGuestEvent.LoadFailed -> GuestSource.Native
    is FanboxGuestEvent.SchemaMismatch, is FanboxGuestEvent.CallFailed -> this
}

/** Crashlytics に non-fatal として記録する guest の失敗。本文と認証情報は含まない。 */
internal class GuestEventException(message: String) : Exception(message)
