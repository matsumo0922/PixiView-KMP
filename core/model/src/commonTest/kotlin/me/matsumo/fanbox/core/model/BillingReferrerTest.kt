package me.matsumo.fanbox.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** 課金画面の入口としてログに送る値を検証するテスト。 */
class BillingReferrerTest {

    @Test
    fun logValuesAreUnique() {
        val logValues = BillingReferrer.entries.map { referrer -> referrer.logValue }

        assertEquals(logValues.size, logValues.toSet().size)
    }

    @Test
    fun logValuesFitLogDefinition() {
        // scripts/daifuku/log_definitions/billing.md の referrer は !string 32
        val invalidValues = BillingReferrer.entries
            .map { referrer -> referrer.logValue }
            .filter { logValue -> logValue.isBlank() || logValue.length > 32 }

        assertTrue(invalidValues.isEmpty(), "invalid log values: $invalidValues")
    }
}
