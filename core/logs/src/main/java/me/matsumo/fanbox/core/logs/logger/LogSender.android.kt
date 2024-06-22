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
import me.matsumo.fanbox.core.model.UserData

@SuppressLint("MissingPermission")
class LogSenderImpl(context: Context) : LogSender {

    private val analytics = FirebaseAnalytics.getInstance(context)

    override fun init(userData: UserData) {
        Firebase.crashlytics.setCustomKeys {
            key("pixiViewId", userData.pixiViewId)
            key("isUseAppLock", userData.isUseAppLock)
            key("isUseDynamicColor", userData.isUseDynamicColor)
            key("isUseGridMode", userData.isUseGridMode)
            key("isUseInfinityPostDetail", userData.isUseInfinityPostDetail)
            key("isDefaultFollowTabInHome", userData.isDefaultFollowTabInHome)
            key("isHideAdultContents", userData.isHideAdultContents)
            key("isOverrideAdultContents", userData.isOverrideAdultContents)
            key("isHideRestricted", userData.isHideRestricted)
            key("isTestUser", userData.isTestUser)
            key("isPlusMode", userData.isPlusMode)
            key("isDeveloperMode", userData.isDeveloperMode)
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
