package me.matsumo.fanbox.feature.setting.top.items

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import me.matsumo.fanbox.core.common.PixiViewConfig
import me.matsumo.fanbox.core.model.GuestSource
import me.matsumo.fanbox.core.model.Setting
import me.matsumo.fanbox.core.model.fanbox.MetaData
import me.matsumo.fanbox.core.resources.Res
import me.matsumo.fanbox.core.resources.setting_top_information
import me.matsumo.fanbox.core.resources.setting_top_information_csrf_token
import me.matsumo.fanbox.core.resources.setting_top_information_fanbox_session_id
import me.matsumo.fanbox.core.resources.setting_top_information_guest
import me.matsumo.fanbox.core.resources.setting_top_information_id
import me.matsumo.fanbox.core.resources.setting_top_information_version
import me.matsumo.fanbox.core.ui.component.SettingTextItem
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

@Composable
internal fun SettingTopInformationSection(
    setting: Setting,
    fanboxMetaData: MetaData,
    fanboxSessionId: String,
    guestSource: GuestSource,
    config: PixiViewConfig,
    onTripleClickGuest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val clipboard = LocalClipboardManager.current
    val onClickGuest = rememberTripleClickHandler(onTripleClickGuest)

    Column(modifier) {
        SettingTopTitleItem(
            modifier = Modifier.fillMaxWidth(),
            text = Res.string.setting_top_information,
        )

        SettingTextItem(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(Res.string.setting_top_information_version),
            description = "${config.versionName}:${config.versionCode}" + when {
                setting.isPlusMode && setting.isDeveloperMode -> " [P+D]"
                setting.isPlusMode -> " [Premium]"
                setting.isDeveloperMode -> " [Developer]"
                else -> ""
            } + if (setting.isTestUser) " [Test]" else "",
            onClick = { /* do nothing */ },
        )

        SettingTextItem(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(Res.string.setting_top_information_id),
            description = setting.pixiViewId,
            onLongClick = { clipboard.setText(AnnotatedString(setting.pixiViewId)) },
        )

        SettingTextItem(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(Res.string.setting_top_information_fanbox_session_id),
            description = fanboxSessionId,
            onLongClick = { clipboard.setText(AnnotatedString(fanboxSessionId)) },
        )

        SettingTextItem(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(Res.string.setting_top_information_csrf_token),
            description = fanboxMetaData.csrfToken,
            onLongClick = { clipboard.setText(AnnotatedString(fanboxMetaData.csrfToken)) },
        )

        SettingTextItem(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(Res.string.setting_top_information_guest),
            description = guestSource.label(),
            onClick = onClickGuest,
            onLongClick = { guestSource.version()?.let { clipboard.setText(AnnotatedString(it)) } },
        )
    }
}

// 隠し操作のため、間隔を空けずに 3 回続けて押したときだけ反応する。
@Composable
private fun rememberTripleClickHandler(onTripleClick: () -> Unit): () -> Unit {
    val currentOnTripleClick by rememberUpdatedState(onTripleClick)
    val counter = remember { TripleClickCounter() }

    return { if (counter.click()) currentOnTripleClick() }
}

/** 続けて押された回数を数え、3 回目で true を返す。押す間隔が空くと数え直す。 */
private class TripleClickCounter {
    private var count = 0
    private var lastClick: TimeMark? = null

    fun click(): Boolean {
        val isConsecutive = lastClick?.let { it.elapsedNow() < 500.milliseconds } ?: false

        count = if (isConsecutive) count + 1 else 1
        lastClick = TimeSource.Monotonic.markNow()

        if (count < 3) return false

        count = 0

        return true
    }
}

private fun GuestSource.version(): String? = (this as? GuestSource.Delivered)?.version

// ローカルでビルドした bundle は commit を持たない。
private fun GuestSource.label(): String = when (this) {
    GuestSource.Loading -> "Loading"
    is GuestSource.Delivered -> (version ?: "local") + if (isFromCache) " (cache)" else ""
    GuestSource.Native -> "Native"
}
