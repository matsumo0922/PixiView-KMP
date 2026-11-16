package me.matsumo.fanbox

import android.content.Intent
import android.content.pm.verify.domain.DomainVerificationManager
import android.content.pm.verify.domain.DomainVerificationUserState
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.util.Consumer
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.model.Destination
import me.matsumo.fanbox.core.model.Flag
import me.matsumo.fanbox.core.repository.FlagRepository
import me.matsumo.fanbox.core.resources.Res
import me.matsumo.fanbox.core.resources.common_cancel
import me.matsumo.fanbox.core.resources.domain_validation_request_button
import me.matsumo.fanbox.core.resources.domain_validation_request_message
import me.matsumo.fanbox.core.resources.domain_validation_request_never_ask
import me.matsumo.fanbox.core.resources.domain_validation_request_title
import me.matsumo.fanbox.core.resources.error_no_available_this_device
import me.matsumo.fanbox.core.resources.notification_permission_request_button
import me.matsumo.fanbox.core.resources.notification_permission_request_message
import me.matsumo.fanbox.core.resources.notification_permission_request_title
import me.matsumo.fanbox.core.ui.extensition.padding
import me.matsumo.fanbox.core.ui.navigation.deepLinkDestinationOf
import me.matsumo.fanbox.core.ui.permission.NotificationPermissionState
import me.matsumo.fanbox.core.ui.permission.rememberNotificationPermissionState
import me.matsumo.fanbox.core.ui.theme.bold
import me.matsumo.fanbox.feature.service.NotificationOpenRequest
import me.matsumo.fanbox.feature.service.NotificationOpenRequests
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
actual fun HandleDeepLink(
    onDeepLink: (Destination) -> Unit,
    onOpenLibraryMessage: () -> Unit,
) {
    val activity = LocalActivity.current as FragmentActivity
    val currentOnDeepLink by rememberUpdatedState(onDeepLink)
    val currentOnOpenLibraryMessage by rememberUpdatedState(onOpenLibraryMessage)
    var isLaunchIntentHandled by rememberSaveable { mutableStateOf(false) }

    // 起動した intent は、構成変更やプロセスの再生成で作り直されても1回だけ扱う
    SideEffect {
        if (isLaunchIntentHandled) return@SideEffect

        isLaunchIntentHandled = true
        activity.intent.deepLinkDestination()?.let(currentOnDeepLink)
    }

    DisposableEffect(activity) {
        val listener = Consumer<Intent> { intent ->
            intent.deepLinkDestination()?.let(currentOnDeepLink)
        }

        activity.addOnNewIntentListener(listener)
        onDispose {
            activity.removeOnNewIntentListener(listener)
        }
    }

    // 新着の通知のタップは、画面ができる前に届くことがあるため、Activity が預けた要求を受け取る。
    LaunchedEffect(Unit) {
        NotificationOpenRequests.requests.collect { request ->
            when (request) {
                is NotificationOpenRequest.PostDetail -> deepLinkDestinationOf(request.url)?.let(currentOnDeepLink)
                NotificationOpenRequest.LibraryMessage -> currentOnOpenLibraryMessage()
            }
        }
    }

    RequestStartupPermission(activity)
}

/** 起動時に出す権限の案内。1 回の起動で 1 つだけ出す。 */
private enum class StartupPermissionPrompt {
    NOTIFICATION,
    DOMAIN_VERIFICATION,
}

/**
 * 起動時の権限の案内を、このプロセスで判定したか。
 *
 * ログアウトしてログインし直すと、案内を出す画面が作り直される。画面の状態に持つと判定をやり直すため、プロセスに持つ。
 */
private object StartupPermissionPromptHistory {
    var isChecked = false
}

/**
 * 通知の権限と、FANBOX のリンクを開く設定の案内のうち、条件を満たすものを 1 つだけ表示する。
 *
 * 両方の条件を満たすときは、新着投稿の通知に必要な通知の権限を優先する。
 */
@Composable
private fun RequestStartupPermission(
    activity: FragmentActivity,
    flagRepository: FlagRepository = koinInject(),
) {
    val notificationPermission = rememberNotificationPermissionState(referrer = "dialog")
    var prompt by rememberSaveable { mutableStateOf<StartupPermissionPrompt?>(null) }

    LaunchedEffect(Unit) {
        if (StartupPermissionPromptHistory.isChecked) return@LaunchedEffect

        // 選んでいる途中で Activity が作り直されると、この処理は取り消される。案内を失わないよう、選んだ結果を反映してから判定済みにする。
        prompt = selectStartupPermissionPrompt(
            activity = activity,
            flagRepository = flagRepository,
            isNotificationGranted = notificationPermission.isGranted,
        )
        StartupPermissionPromptHistory.isChecked = true

        if (prompt == StartupPermissionPrompt.NOTIFICATION) notificationPermission.reportPromptShown()
    }

    when (prompt) {
        StartupPermissionPrompt.NOTIFICATION -> RequestNotificationPermission(
            notificationPermission = notificationPermission,
            flagRepository = flagRepository,
            onClose = { prompt = null },
        )

        StartupPermissionPrompt.DOMAIN_VERIFICATION -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            RequestDomainVerification(
                activity = activity,
                flagRepository = flagRepository,
                onClose = { prompt = null },
            )
        }

        null -> Unit
    }
}

private suspend fun selectStartupPermissionPrompt(
    activity: FragmentActivity,
    flagRepository: FlagRepository,
    isNotificationGranted: Boolean,
): StartupPermissionPrompt? {
    val shouldRequestNotification = !isNotificationGranted && flagRepository.getFlag(Flag.SHOULD_REQUEST_NOTIFICATION_PERMISSION, true)
    if (shouldRequestNotification) return StartupPermissionPrompt.NOTIFICATION

    val shouldRequestDomainVerification = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && shouldRequestDomainVerification(activity, flagRepository)
    if (shouldRequestDomainVerification) return StartupPermissionPrompt.DOMAIN_VERIFICATION

    return null
}

@RequiresApi(Build.VERSION_CODES.S)
private suspend fun shouldRequestDomainVerification(activity: FragmentActivity, flagRepository: FlagRepository): Boolean {
    val manager = activity.getSystemService(DomainVerificationManager::class.java)
    val userState = manager.getDomainVerificationUserState(activity.packageName)
    val selectedDomains = userState?.hostToStateMap?.filterValues {
        it == DomainVerificationUserState.DOMAIN_STATE_SELECTED
    }
    val enable = userState?.isLinkHandlingAllowed

    val flag = flagRepository.getFlag(Flag.SHOULD_REQUEST_DOMAIN_VERIFICATION, true)
    val isNotSelected = selectedDomains.isNullOrEmpty()

    return flag && (isNotSelected || enable == false)
}

@Composable
private fun RequestNotificationPermission(
    notificationPermission: NotificationPermissionState,
    flagRepository: FlagRepository,
    onClose: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    RequestPermissionDialog(
        title = Res.string.notification_permission_request_title,
        message = Res.string.notification_permission_request_message,
        requestButton = Res.string.notification_permission_request_button,
        onRequestClick = {
            notificationPermission.request()
            onClose()
        },
        onCancelClick = { isChecked ->
            if (isChecked) {
                scope.launch { flagRepository.setFlag(Flag.SHOULD_REQUEST_NOTIFICATION_PERMISSION, false) }
            }
            onClose()
        },
        onDismissRequest = onClose,
    )
}

private fun Intent?.deepLinkDestination(): Destination? = this?.dataString?.let(::deepLinkDestinationOf)

@RequiresApi(Build.VERSION_CODES.S)
@Composable
private fun RequestDomainVerification(
    activity: FragmentActivity,
    flagRepository: FlagRepository,
    onClose: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val errorMessage = stringResource(Res.string.error_no_available_this_device)

    RequestPermissionDialog(
        title = Res.string.domain_validation_request_title,
        message = Res.string.domain_validation_request_message,
        requestButton = Res.string.domain_validation_request_button,
        onRequestClick = {
            runCatching {
                activity.startActivity(Intent(Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS, Uri.parse("package:${activity.packageName}")))
            }.onFailure {
                Toast.makeText(activity, errorMessage, Toast.LENGTH_SHORT).show()
            }.onSuccess {
                onClose()
            }
        },
        onCancelClick = { isChecked ->
            if (isChecked) {
                scope.launch {
                    flagRepository.setFlag(Flag.SHOULD_REQUEST_DOMAIN_VERIFICATION, false)
                }
            }
            onClose()
        },
        onDismissRequest = onClose,
    )
}

@Composable
private fun RequestPermissionDialog(
    title: StringResource,
    message: StringResource,
    requestButton: StringResource,
    onRequestClick: () -> Unit,
    onCancelClick: (Boolean) -> Unit,
    onDismissRequest: () -> Unit,
) {
    val (isChecked, setChecked) = remember { mutableStateOf(false) }

    Dialog(onDismissRequest) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(title),
                style = MaterialTheme.typography.titleMedium.bold(),
                color = MaterialTheme.colorScheme.onSurface,
            )

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Column(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    modifier = Modifier.padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val interactionSource = remember { MutableInteractionSource() }

                    Checkbox(
                        modifier = Modifier.size(12.dp),
                        checked = isChecked,
                        onCheckedChange = setChecked,
                        interactionSource = interactionSource,
                    )

                    Text(
                        modifier = Modifier.clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = { setChecked(!isChecked) },
                        ),
                        text = stringResource(Res.string.domain_validation_request_never_ask),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onRequestClick,
                ) {
                    Text(text = stringResource(requestButton))
                }

                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onCancelClick.invoke(isChecked) },
                ) {
                    Text(text = stringResource(Res.string.common_cancel))
                }
            }
        }
    }
}
