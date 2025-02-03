package me.matsumo.fanbox.core.repository

import androidx.compose.ui.text.intl.Locale
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatResponseFormat
import com.aallam.openai.api.http.Timeout
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import me.matsumo.fanbox.core.common.PixiViewConfig
import me.matsumo.fanbox.core.model.translation.toFanboxComments
import me.matsumo.fanbox.core.model.translation.toFanboxPostDetail
import me.matsumo.fanbox.core.model.translation.toTrans
import me.matsumo.fankt.fanbox.domain.PageOffsetInfo
import me.matsumo.fankt.fanbox.domain.model.FanboxComment
import me.matsumo.fankt.fanbox.domain.model.FanboxPostDetail
import kotlin.time.Duration.Companion.seconds

class TranslationRepository(
    private val formatter: Json,
    private val pixiViewConfig: PixiViewConfig,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    private val client = OpenAI(
        token = pixiViewConfig.openaiApiKey,
        timeout = Timeout(60.seconds, 60.seconds, 60.seconds),
    )

    private suspend inline fun <reified T> translate(data: T, locale: Locale): T {
        val requestJson = formatter.encodeToString(data)
        val request = buildChatCompletionRequest(requestJson, locale)

        val completion = client.chatCompletion(request)
        val resultJson = completion.choices.first().message.content.orEmpty()

        return formatter.decodeFromString(resultJson)
    }

    suspend fun translate(postDetail: FanboxPostDetail, locale: Locale): FanboxPostDetail = withContext(ioDispatcher) {
        val transPostDetail = postDetail.toTrans()
        val result = translate(transPostDetail, locale)

        result.toFanboxPostDetail(postDetail)
    }

    suspend fun translate(comments: PageOffsetInfo<FanboxComment>, locale: Locale): PageOffsetInfo<FanboxComment> = withContext(ioDispatcher) {
        val transComments = comments.toTrans()
        val result = translate(transComments, locale)

        result.toFanboxComments(comments)
    }

    private fun buildChatCompletionRequest(json: String, locale: Locale): ChatCompletionRequest {
        return ChatCompletionRequest(
            model = ModelId("gpt-4o-mini"),
            messages = listOf(
                ChatMessage.System("You are an API that translates JSON to \"${locale.language}\". All responses must be in JSON, and the structure of the passed JSON must be retained, translating only the value part."),
                ChatMessage.User(json),
            ),
            responseFormat = ChatResponseFormat.JsonObject,
        )
    }
}
