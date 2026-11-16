package me.matsumo.fanbox.feature.service

import android.content.Intent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

/** 新着の通知のタップで開く画面。 */
sealed interface NotificationOpenRequest {

    /** 新着投稿の通知のタップで開く、投稿詳細の [url]。 */
    data class PostDetail(val url: String) : NotificationOpenRequest

    /** 新着メッセージの通知のタップで開く、ライブラリの「メッセージ」タブ。 */
    data object LibraryMessage : NotificationOpenRequest
}

/**
 * 新着の通知のタップで届いた、画面を開く要求。
 *
 * プロセスが終了した後のタップでは、Activity が作り直された直後、画面を組み立てる前に intent が届く。
 * 画面が受け取れるようになるまで要求を失わないよう、プロセスで保持する。
 */
object NotificationOpenRequests {
    private val channel = Channel<NotificationOpenRequest>(Channel.CONFLATED)

    /** 受け取られていない要求。要求は 1 回だけ受け取れる。 */
    val requests: Flow<NotificationOpenRequest> = channel.receiveAsFlow()

    fun send(request: NotificationOpenRequest) {
        channel.trySend(request)
    }
}

/**
 * 新着の通知から開いた intent なら、開く画面の要求を返す。新着の通知でなければ null を返す。
 *
 * 要求の印は intent から取り除く。投稿ごとの通知の投稿詳細の URL も取り除き、画面の deep link の処理が同じ投稿詳細をもう一度開かないようにする。
 * グループの親の通知はアプリを開くだけのため、要求を返さない。計測に使う件数は取り除かない。
 */
fun Intent.consumeNotificationOpenRequest(): NotificationOpenRequest? {
    if (getBooleanExtra(EXTRA_OPEN_LIBRARY_MESSAGE, false)) {
        removeExtra(EXTRA_OPEN_LIBRARY_MESSAGE)
        return NotificationOpenRequest.LibraryMessage
    }

    val postUrl = dataString?.takeIf { hasExtra(EXTRA_NEW_POST_COUNT) } ?: return null
    data = null

    return NotificationOpenRequest.PostDetail(postUrl)
}
