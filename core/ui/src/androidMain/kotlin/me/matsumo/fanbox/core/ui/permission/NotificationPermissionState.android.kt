package me.matsumo.fanbox.core.ui.permission

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.logs.category.NotificationsLog
import me.matsumo.fanbox.core.logs.logger.LogConfigurator
import me.matsumo.fanbox.core.logs.logger.send
import me.matsumo.fanbox.core.model.Flag
import me.matsumo.fanbox.core.repository.FlagRepository
import org.koin.compose.koinInject

@Composable
actual fun rememberNotificationPermissionState(referrer: String): NotificationPermissionState {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val scope = rememberCoroutineScope()
    val flagRepository = koinInject<FlagRepository>()
    // 設定画面や権限のダイアログから戻る前に Activity が作り直されても、求めている途中であることを失わないよう保存する。
    val pendingRequest = rememberSaveable(referrer) { mutableStateOf(PendingPermissionRequest.NONE) }
    val state = remember(referrer) {
        AndroidNotificationPermissionState(
            context = context,
            referrer = referrer,
            scope = scope,
            flagRepository = flagRepository,
            pendingRequest = pendingRequest,
        )
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isPermitted ->
        state.onPermissionResult(isPermitted)
    }

    SideEffect {
        state.activity = activity
        state.launcher = launcher
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        state.onResume()
    }

    return state
}

/** 通知の許可を求めている途中の操作。権限のダイアログは、求める前に説明が必要と判定されていたかで分ける。 */
private enum class PendingPermissionRequest {
    NONE,
    DIALOG_AFTER_RATIONALE,
    DIALOG_WITHOUT_RATIONALE,
    SETTINGS,
}

@Stable
private class AndroidNotificationPermissionState(
    private val context: Context,
    private val referrer: String,
    private val scope: CoroutineScope,
    private val flagRepository: FlagRepository,
    private val pendingRequest: MutableState<PendingPermissionRequest>,
) : NotificationPermissionState {

    override var isGranted by mutableStateOf(areNotificationsEnabled())
        private set

    var activity: Activity? = null
    var launcher: ActivityResultLauncher<String>? = null

    override suspend fun reportPromptShown() {
        LogConfigurator.isConfigured.first { it }
        NotificationsLog.permissionPromptShown(referrer).send()
    }

    override fun request() {
        val currentLauncher = launcher
        val canShowPermissionDialog = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !isPermissionGranted()

        if (canShowPermissionDialog && currentLauncher != null) {
            val isRationaleShown = shouldShowRationale()

            if (isRationaleShown) recordRationaleShown()

            pendingRequest.value = if (isRationaleShown) PendingPermissionRequest.DIALOG_AFTER_RATIONALE else PendingPermissionRequest.DIALOG_WITHOUT_RATIONALE
            currentLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            openNotificationSettings()
        }
    }

    fun onPermissionResult(isPermitted: Boolean) {
        val request = pendingRequest.value
        val isRationaleShown = shouldShowRationale()

        isGranted = areNotificationsEnabled()
        pendingRequest.value = PendingPermissionRequest.NONE

        if (isRationaleShown) recordRationaleShown()

        // 前後とも説明が不要と判定される拒否は、初めて求めたダイアログを閉じただけの場合と、2 回拒否されてダイアログが出なかった場合がある。
        // 説明が必要と判定されたことがある（1 回拒否された）ときだけ後者として扱い、設定画面へ案内する。
        val mayBeSuppressed = !isPermitted && request == PendingPermissionRequest.DIALOG_WITHOUT_RATIONALE && !isRationaleShown

        if (!mayBeSuppressed) {
            reportResult(isGranted)
            return
        }

        scope.launch {
            if (flagRepository.getFlag(Flag.NOTIFICATION_PERMISSION_RATIONALE_SHOWN, false)) openNotificationSettings() else reportResult(false)
        }
    }

    fun onResume() {
        isGranted = areNotificationsEnabled()

        if (pendingRequest.value != PendingPermissionRequest.SETTINGS) return

        pendingRequest.value = PendingPermissionRequest.NONE
        reportResult(isGranted)
    }

    private fun openNotificationSettings() {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        runCatching { context.startActivity(intent) }
            .onSuccess { pendingRequest.value = PendingPermissionRequest.SETTINGS }
            .onFailure { failure ->
                Napier.w(failure) { "Failed to open the notification settings." }
                reportResult(false)
            }
    }

    private fun recordRationaleShown() {
        scope.launch { flagRepository.setFlag(Flag.NOTIFICATION_PERMISSION_RATIONALE_SHOWN, true) }
    }

    private fun reportResult(isSuccess: Boolean) {
        scope.launch {
            LogConfigurator.isConfigured.first { it }
            NotificationsLog.permissionResult(referrer, isSuccess).send()
        }
    }

    private fun areNotificationsEnabled(): Boolean = NotificationManagerCompat.from(context).areNotificationsEnabled()

    private fun isPermissionGranted(): Boolean = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    private fun shouldShowRationale(): Boolean = activity?.let {
        ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.POST_NOTIFICATIONS)
    } ?: false
}
