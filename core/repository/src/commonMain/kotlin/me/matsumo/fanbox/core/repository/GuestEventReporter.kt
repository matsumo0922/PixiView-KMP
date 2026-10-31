package me.matsumo.fanbox.core.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.common.util.recordException
import me.matsumo.fanbox.core.common.util.setCrashReportKey
import me.matsumo.fanbox.core.logs.category.GuestLog
import me.matsumo.fanbox.core.logs.logger.LogConfigurator
import me.matsumo.fanbox.core.logs.logger.send
import me.matsumo.fankt.fanbox.FanboxGuestEvent

/**
 * guest で起きた出来事を Firebase へ送る。
 *
 * ロードの成功は回数を見るため Analytics のイベントにし、失敗は原因を見るため Crashlytics の non-fatal にする。
 * ロードした bundle の版はクラッシュレポートと突き合わせられるよう custom key にも設定する。
 *
 * fankt はこの処理を背景の thread から呼び、ここで投げた例外は記録して捨てる。
 */
internal class GuestEventReporter(private val scope: CoroutineScope) {

    fun report(event: FanboxGuestEvent) {
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

/** Crashlytics に non-fatal として記録する guest の失敗。本文と認証情報は含まない。 */
internal class GuestEventException(message: String) : Exception(message)
