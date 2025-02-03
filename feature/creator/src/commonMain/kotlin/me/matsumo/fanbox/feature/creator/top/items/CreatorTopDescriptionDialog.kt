package me.matsumo.fanbox.feature.creator.top.items

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import me.matsumo.fanbox.core.model.TranslationState
import me.matsumo.fanbox.core.ui.extensition.getScreenSizeDp
import sh.calvin.autolinktext.rememberAutoLinkText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CreatorTopDescriptionDialog(
    description: String,
    translationState: TranslationState<String>,
    onTranslateClicked: (String) -> Unit,
    onDismissRequest: () -> Unit,
) {
    BasicAlertDialog(onDismissRequest) {
        Column(
            modifier = Modifier
                .heightIn(max = getScreenSizeDp().height * 0.7f)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .weight(1f, false)
                    .verticalScroll(rememberScrollState()),
                text = AnnotatedString.rememberAutoLinkText(description),
                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                IconButton(onClick = { onTranslateClicked(description) }) {
                    if (translationState is TranslationState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(4.dp),
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Translate,
                            tint = MaterialTheme.colorScheme.onSurface,
                            contentDescription = null,
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                TextButton(onDismissRequest) {
                    Text("OK")
                }
            }
        }
    }
}
