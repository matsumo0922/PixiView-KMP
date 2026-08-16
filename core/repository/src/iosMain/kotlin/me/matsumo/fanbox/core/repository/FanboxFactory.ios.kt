package me.matsumo.fanbox.core.repository

import kotlinx.coroutines.CoroutineDispatcher
import me.matsumo.fankt.fanbox.Fanbox
import me.matsumo.fankt.fanbox.FanboxCookieStorage
import me.matsumo.fankt.fanbox.FanboxLogLevel

/**
 * 配信先を渡さずに [Fanbox] を生成する。guest は起動せず、投稿詳細は組み込みの直接経路で処理される。
 *
 * 遠隔コードの実行を停止する手段が iOS には無いため、配信の対象を Android に限っている。FANBOX の
 * 仕様変更への追従は、iOS ではアプリの更新で届く。
 *
 * [isDeveloperMode] は配信先の選択に使う値であり、配信先を渡さない iOS では参照しない。宣言に現れるのは
 * expect の signature が共通であるためで、配信先を common へ移すと iOS のバイナリにも含まれてしまう。
 */
internal actual fun createFanbox(
    logLevel: FanboxLogLevel,
    isDeveloperMode: Boolean,
    ioDispatcher: CoroutineDispatcher,
    cookieStorage: FanboxCookieStorage,
): Fanbox {
    return Fanbox(
        logLevel = logLevel,
        ioDispatcher = ioDispatcher,
        cookieStorage = cookieStorage,
    )
}
