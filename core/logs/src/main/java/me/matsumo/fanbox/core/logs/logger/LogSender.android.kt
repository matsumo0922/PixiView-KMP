package me.matsumo.fanbox.core.logs.logger

import android.annotation.SuppressLint
import android.content.Context
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ParametersBuilder
import com.google.firebase.analytics.logEvent
import com.google.firebase.crashlytics.crashlytics
import com.google.firebase.crashlytics.setCustomKeys
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import me.matsumo.fanbox.core.model.Setting

@SuppressLint("MissingPermission")
class LogSenderImpl(context: Context) : LogSender {

    private val analytics = FirebaseAnalytics.getInstance(context)

    override fun init(setting: Setting) {
        Firebase.crashlytics.setCustomKeys {
            key("pixiViewId", setting.pixiViewId)
            key("isUseAppLock", setting.isUseAppLock)
            key("isUseDynamicColor", setting.isUseDynamicColor)
            key("isUseGridMode", setting.isUseGridMode)
            key("isUseInfinityPostDetail", setting.isUseInfinityPostDetail)
            key("isDefaultFollowTabInHome", setting.isDefaultFollowTabInHome)
            key("isHideAdultContents", setting.isHideAdultContents)
            key("isOverrideAdultContents", setting.isOverrideAdultContents)
            key("isHideRestricted", setting.isHideRestricted)
            key("isTestUser", setting.isTestUser)
            key("isPlusMode", setting.isPlusMode)
            key("isDeveloperMode", setting.isDeveloperMode)
        }
    }

    override fun sendLog(log: JsonObject) {
        val eventName = listOf(
            log["event_category"]?.jsonPrimitive?.content,
            log["event_name"]?.jsonPrimitive?.content,
        ).joinToString("_")

        analytics.logEvent(eventName) {
            for ((key, value) in log) {
                param(key, value)
            }
        }
    }

    private fun ParametersBuilder.param(key: String, value: JsonElement) {
        val longValue = value.jsonPrimitive.longOrNull
        val doubleValue = value.jsonPrimitive.doubleOrNull

        when {
            longValue != null -> param(key, longValue)
            doubleValue != null -> param(key, doubleValue)
            else -> param(key, value.jsonPrimitive.content)
        }
    }
}
