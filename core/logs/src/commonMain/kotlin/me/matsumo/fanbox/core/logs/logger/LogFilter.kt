package me.matsumo.fanbox.core.logs.logger

import kotlinx.datetime.TimeZone
import kotlinx.serialization.json.JsonObjectBuilder
import me.matsumo.fanbox.core.common.PixiViewConfig
import me.matsumo.fanbox.core.logs.CommonPayload
import me.matsumo.fanbox.core.model.Setting

class LogFilter(
    private val pixiViewConfig: PixiViewConfig,
    private val setting: Setting,
    private val userAgent: String,
) {
    fun applyFilter(builder: JsonObjectBuilder): JsonObjectBuilder {
        val commonPayload = CommonPayload(
            pixiviewId = setting.pixiViewId,
            userAgent = userAgent,
            isPlus = setting.isPlusMode,
            isDeveloper = setting.isDeveloperMode,
            isTester = setting.isTestUser,
            applicationVersionCode = pixiViewConfig.versionCode.toLong(),
            applicationVersionName = pixiViewConfig.versionName,
            platform = pixiViewConfig.platform,
            platformVersion = pixiViewConfig.platformVersion,
            device = pixiViewConfig.device,
            deviceAbis = pixiViewConfig.deviceAbis,
            timeZone = TimeZone.currentSystemDefault().id,
        )

        return commonPayload.applyToJsonObject(builder)
    }
}
