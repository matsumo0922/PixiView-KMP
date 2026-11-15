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
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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

@Composable
actual fun rememberNotificationPermissionState(referrer: String): NotificationPermissionState {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val scope = rememberCoroutineScope()
    val state = remember(referrer) { AndroidNotificationPermissionState(context, referrer, scope) }
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

@Stable
private class AndroidNotificationPermissionState(
    private val context: Context,
    private val referrer: String,
    private val scope: CoroutineScope,
) : NotificationPermissionState {

    override var isGranted by mutableStateOf(areNotificationsEnabled())
        private set

    var activity: Activity? = null
    var launcher: ActivityResultLauncher<String>? = null

    private var isWaitingForSettings = false
    private var shouldShowRationaleBeforeRequest = false

    override suspend fun reportPromptShown() {
        LogConfigurator.isConfigured.first { it }
        NotificationsLog.permissionPromptShown(referrer).send()
    }

    override fun request() {
        val currentLauncher = launcher
        val canShowPermissionDialog = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !isPermissionGranted()

        if (canShowPermissionDialog && currentLauncher != null) {
            shouldShowRationaleBeforeRequest = shouldShowRationale()
            currentLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            openNotificationSettings()
        }
    }

    fun onPermissionResult(isPermitted: Boolean) {
        isGranted = areNotificationsEnabled()

        // 2 回拒否された後は、ダイアログを出さずに拒否が返る。前後とも説明が不要と判定されるのはこの場合だけで、設定画面へ案内する。
        val isDialogSuppressed = !isPermitted && !shouldShowRationaleBeforeRequest && !shouldShowRationale()

        if (isDialogSuppressed) {
            openNotificationSettings()
        } else {
            reportResult(isGranted)
        }
    }

    fun onResume() {
        isGranted = areNotificationsEnabled()

        if (!isWaitingForSettings) return

        isWaitingForSettings = false
        reportResult(isGranted)
    }

    private fun openNotificationSettings() {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        runCatching { context.startActivity(intent) }
            .onSuccess { isWaitingForSettings = true }
            .onFailure { failure ->
                Napier.w(failure) { "Failed to open the notification settings." }
                reportResult(false)
            }
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
