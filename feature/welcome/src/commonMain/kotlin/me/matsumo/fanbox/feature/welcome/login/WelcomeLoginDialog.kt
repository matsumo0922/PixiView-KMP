package me.matsumo.fanbox.feature.welcome.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import me.matsumo.fanbox.core.resources.Res
import me.matsumo.fanbox.core.resources.common_cancel
import me.matsumo.fanbox.core.resources.welcome_login_other_message
import me.matsumo.fanbox.core.resources.welcome_login_other_title
import me.matsumo.fanbox.core.resources.welcome_login_title
import me.matsumo.fanbox.core.ui.theme.bold
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun WelcomeLoginDialog(
    onClickHelp: (String) -> Unit,
    onClickLogin: (String) -> Unit,
    onDismissRequest: () -> Unit,
) {
    val (sessionId, setSessionId) = remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismissRequest,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(Res.string.welcome_login_other_title),
                style = MaterialTheme.typography.titleMedium.bold(),
                color = MaterialTheme.colorScheme.onSurface,
            )

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(Res.string.welcome_login_other_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onClickHelp.invoke("https://github.com/matsumo0922/PixiView-KMP/blob/master/FANBOXSESSID.md") }
                    .fillMaxWidth()
                    .padding(8.dp),
                text = "https://github.com/matsumo0922/PixiView-KMP/blob/master/FANBOXSESSID.md",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
            )

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = sessionId,
                onValueChange = setSessionId,
                label = { Text("FANBOXSESSID") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
            )

            Row(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(4.dp),
                    onClick = onDismissRequest,
                ) {
                    Text(text = stringResource(Res.string.common_cancel))
                }

                Button(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(4.dp),
                    enabled = sessionId.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary),
                    onClick = { onClickLogin.invoke(sessionId) },
                ) {
                    Text(text = stringResource(Res.string.welcome_login_title))
                }
            }
        }
    }
}
