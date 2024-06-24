package me.matsumo.fanbox.core.logs.category

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

// This class is automatically generated by generate-log-classes.
sealed class BillingLog : LogCategory {

    class Purchase internal constructor(
        private val referrer: String,
        private val isSuccess: Boolean
    ) : BillingLog() {
        override val properties: JsonObject = buildJsonObject {
            put("event_category", "billing")
            put("event_name", "purchase")
            put("referrer", referrer)
            put("is_success", isSuccess)
        }
    }

    class Consume internal constructor(
        private val isSuccess: Boolean
    ) : BillingLog() {
        override val properties: JsonObject = buildJsonObject {
            put("event_category", "billing")
            put("event_name", "consume")
            put("is_success", isSuccess)
        }
    }

    class Verify internal constructor(
        private val isSuccess: Boolean
    ) : BillingLog() {
        override val properties: JsonObject = buildJsonObject {
            put("event_category", "billing")
            put("event_name", "verify")
            put("is_success", isSuccess)
        }
    }

    companion object {
        // 購入リクエスト
        fun purchase(
            referrer: String,
            isSuccess: Boolean
        ) = Purchase(referrer, isSuccess)

        // 消費リクエスト
        fun consume(
            isSuccess: Boolean
        ) = Consume(isSuccess)

        // 検証リクエスト
        fun verify(
            isSuccess: Boolean
        ) = Verify(isSuccess)
    }
}