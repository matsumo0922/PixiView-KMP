package me.matsumo.fanbox.core.model

import androidx.compose.runtime.Immutable

/** Plus 課金状態の設定更新内容を表すモデル。 */
@Immutable
data class SettingPlusStatusUpdate(
    val isPlusMode: Boolean,
    val isPlusTrial: Boolean,
    val isPlusModeChanged: Boolean,
    val isPlusTrialChanged: Boolean,
) {
    /** Plus 課金状態の設定に変更があるかどうか。 */
    val hasChanged get() = isPlusModeChanged || isPlusTrialChanged

    /** Plus 課金状態の設定更新内容を生成するオブジェクト。 */
    companion object {
        fun from(
            currentSetting: Setting,
            isPlusMode: Boolean,
            isPlusTrial: Boolean,
        ): SettingPlusStatusUpdate {
            val normalizedIsPlusTrial = isPlusMode && isPlusTrial

            return SettingPlusStatusUpdate(
                isPlusMode = isPlusMode,
                isPlusTrial = normalizedIsPlusTrial,
                isPlusModeChanged = currentSetting.isPlusMode != isPlusMode,
                isPlusTrialChanged = currentSetting.isPlusTrial != normalizedIsPlusTrial,
            )
        }
    }
}
