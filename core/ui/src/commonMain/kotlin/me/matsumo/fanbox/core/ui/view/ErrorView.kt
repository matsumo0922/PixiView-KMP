package me.matsumo.fanbox.core.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBackIos
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import me.matsumo.fanbox.core.model.ScreenState
import me.matsumo.fanbox.core.resources.Res
import me.matsumo.fanbox.core.resources.common_reload
import me.matsumo.fanbox.core.resources.error_executed
import me.matsumo.fanbox.core.resources.error_service_status
import me.matsumo.fanbox.core.ui.extensition.NavigatorExtension
import me.matsumo.fanbox.core.ui.theme.bold
import me.matsumo.fanbox.core.ui.theme.center
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ErrorView(
    title: StringResource,
    message: StringResource,
    modifier: Modifier = Modifier,
    serviceStatus: Boolean = true,
    retryTitle: StringResource? = null,
    retryAction: (() -> Unit)? = null,
    terminate: (() -> Unit)? = null,
) {
    val navigatorExtension = koinInject<NavigatorExtension>()

    Box(modifier) {
        TopAppBar(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth(),
            title = { /* nothing */ },
            navigationIcon = {
                if (terminate != null) {
                    IconButton(terminate) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBackIos,
                            contentDescription = null,
                        )
                    }
                }
            },
        )

        Column(
            modifier = Modifier.fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(
                space = 8.dp,
                alignment = Alignment.CenterVertically,
            ),
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(title),
                style = MaterialTheme.typography.titleMedium.bold().center(),
                color = MaterialTheme.colorScheme.onSurface,
            )

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(message),
                style = MaterialTheme.typography.bodyMedium.center(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (retryAction != null) {
                Button(
                    modifier = Modifier.padding(top = 24.dp),
                    onClick = { retryAction.invoke() },
                ) {
                    Text(
                        text = stringResource(retryTitle ?: Res.string.common_reload),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }

            if (serviceStatus) {
                TextButton(
                    modifier = Modifier.padding(top = 24.dp),
                    onClick = {
                        navigatorExtension.navigateToWebPage(
                            url = "https://github.com/matsumo0922/PixiView-KMP/blob/master/STATUS.md",
                            referrer = "ErrorView",
                        )
                    },
                ) {
                    Icon(
                        modifier = Modifier.size(14.dp),
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Text(
                        modifier = Modifier.padding(start = 4.dp),
                        text = stringResource(Res.string.error_service_status),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textDecoration = TextDecoration.Underline,
                    )
                }
            }
        }
    }
}

@Composable
fun ErrorView(
    errorState: ScreenState.Error,
    retryAction: (() -> Unit)?,
    terminate: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    ErrorView(
        modifier = modifier,
        title = Res.string.error_executed,
        message = errorState.message,
        retryTitle = errorState.retryTitle,
        retryAction = retryAction,
        terminate = terminate,
    )
}
