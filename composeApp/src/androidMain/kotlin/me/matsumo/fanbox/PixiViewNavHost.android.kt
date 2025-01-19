package me.matsumo.fanbox

import android.content.Intent
import android.content.pm.verify.domain.DomainVerificationManager
import android.content.pm.verify.domain.DomainVerificationUserState
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.util.Consumer
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.model.Flag
import me.matsumo.fanbox.core.repository.FlagRepository
import me.matsumo.fanbox.core.resources.Res
import me.matsumo.fanbox.core.resources.common_cancel
import me.matsumo.fanbox.core.resources.domain_validation_request_button
import me.matsumo.fanbox.core.resources.domain_validation_request_message
import me.matsumo.fanbox.core.resources.domain_validation_request_never_ask
import me.matsumo.fanbox.core.resources.domain_validation_request_title
import me.matsumo.fanbox.core.ui.extensition.padding
import me.matsumo.fanbox.core.ui.theme.bold
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
actual fun HandleDeepLink(navController: NavController) {
    val activity = LocalActivity.current as FragmentActivity

    DisposableEffect(Unit) {
        val listener = Consumer<Intent> {
            navController.handleDeepLink(it)
        }

        activity.addOnNewIntentListener(listener)
        onDispose {
            activity.removeOnNewIntentListener(listener)
        }
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        RequestDomainVerification(activity)
    }
}

@RequiresApi(Build.VERSION_CODES.S)
@Composable
private fun RequestDomainVerification(
    activity: FragmentActivity,
    flagRepository: FlagRepository = koinInject(),
) {
    val scope = rememberCoroutineScope()
    var shouldRequest by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val manager = activity.getSystemService(DomainVerificationManager::class.java)
        val userState = manager.getDomainVerificationUserState(activity.packageName)
        val selectedDomains = userState?.hostToStateMap?.filterValues { it == DomainVerificationUserState.DOMAIN_STATE_SELECTED }
        val enable = userState?.isLinkHandlingAllowed

        val flag = flagRepository.getFlag(Flag.SHOULD_REQUEST_DOMAIN_VERIFICATION, true)
        val isNotSelected = selectedDomains.isNullOrEmpty()

        shouldRequest = flag && (isNotSelected || enable == false)
    }

    if (shouldRequest) {
        RequestDomainVerificationDialog(
            onRequestClicked = {
                activity.startActivity(Intent(Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS, Uri.parse("package:${activity.packageName}")))
                shouldRequest = false
            },
            onCancelClicked = { isChecked ->
                if (isChecked) {
                    scope.launch {
                        flagRepository.setFlag(Flag.SHOULD_REQUEST_DOMAIN_VERIFICATION, false)
                    }
                }
                shouldRequest = false
            },
            onDismissRequest = {
                shouldRequest = false
            },
        )
    }
}

@Composable
private fun RequestDomainVerificationDialog(
    onRequestClicked: () -> Unit,
    onCancelClicked: (Boolean) -> Unit,
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
                text = stringResource(Res.string.domain_validation_request_title),
                style = MaterialTheme.typography.titleMedium.bold(),
                color = MaterialTheme.colorScheme.onSurface,
            )

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(Res.string.domain_validation_request_message),
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
                    onClick = onRequestClicked,
                ) {
                    Text(text = stringResource(Res.string.domain_validation_request_button))
                }

                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onCancelClicked.invoke(isChecked) },
                ) {
                    Text(text = stringResource(Res.string.common_cancel))
                }
            }
        }
    }
}
