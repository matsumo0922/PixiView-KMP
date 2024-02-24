package me.matsumo.fanbox.core.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.compose.stringResource
import io.github.alexzhirkevich.cupertino.CupertinoActionSheet
import io.github.alexzhirkevich.cupertino.ExperimentalCupertinoApi
import io.github.alexzhirkevich.cupertino.cancel
import io.github.alexzhirkevich.cupertino.default
import me.matsumo.fanbox.core.ui.MR
import me.matsumo.fanbox.core.ui.extensition.Platform
import me.matsumo.fanbox.core.ui.extensition.currentPlatform

@OptIn(ExperimentalCupertinoApi::class)
@Composable
fun ActionSheet(
    isVisible: Boolean,
    actions: List<Action>,
    onDismissRequest: () -> Unit,
) {
    if (currentPlatform == Platform.Android) {
        if (!isVisible) return

        Dialog(onDismissRequest = onDismissRequest) {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(vertical = 8.dp),
            ) {
                for (action in actions) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                action.onClick.invoke()
                                onDismissRequest.invoke()
                            }
                            .padding(20.dp, 16.dp),
                        text = stringResource(action.text),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    } else {
        CupertinoActionSheet(
            visible = isVisible,
            onDismissRequest = onDismissRequest,
        ) {
            for (action in actions) {
                default(action.onClick) {
                    Text(stringResource(action.text))
                }
            }

            cancel(onDismissRequest) {
                Text(stringResource(MR.strings.common_cancel))
            }
        }
    }
}

@Stable
data class Action(
    val text: StringResource,
    val onClick: () -> Unit,
)
