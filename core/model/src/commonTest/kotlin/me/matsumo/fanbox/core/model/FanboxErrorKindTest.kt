package me.matsumo.fanbox.core.model

import me.matsumo.fanbox.core.resources.Res
import me.matsumo.fanbox.core.resources.common_back
import me.matsumo.fanbox.core.resources.creator_fan_card_not_supported
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** FANBOX API の失敗分類とエラー状態への変換を検証するテスト。 */
class FanboxErrorKindTest {

    @Test
    fun fanboxErrorKindOfClassifiesKnownStatusCodes() {
        assertEquals(FanboxErrorKind.Unauthorized, fanboxErrorKindOf(401))
        assertEquals(FanboxErrorKind.Forbidden, fanboxErrorKindOf(403))
        assertEquals(FanboxErrorKind.NotFound, fanboxErrorKindOf(404))
        assertEquals(FanboxErrorKind.RateLimited, fanboxErrorKindOf(429))
        assertEquals(FanboxErrorKind.ServerError, fanboxErrorKindOf(500))
        assertEquals(FanboxErrorKind.ServerError, fanboxErrorKindOf(503))
    }

    @Test
    fun fanboxErrorKindOfReturnsNullForUnknownStatusCodeAndMissingStatusCode() {
        assertNull(fanboxErrorKindOf(400))
        assertNull(fanboxErrorKindOf(null))
    }

    @Test
    fun toFanboxErrorKindReturnsUnknownForNonFanboxException() {
        assertEquals(FanboxErrorKind.Unknown, IllegalStateException().toFanboxErrorKind())
    }

    @Test
    fun toScreenStateErrorKeepsFallbackResourcesForUnknownError() {
        val error = IllegalStateException().toScreenStateError(
            fallbackMessage = Res.string.creator_fan_card_not_supported,
            fallbackRetryTitle = Res.string.common_back,
        )

        assertEquals(Res.string.creator_fan_card_not_supported, error.message)
        assertEquals(Res.string.common_back, error.retryTitle)
    }
}
