package me.matsumo.fanbox.feature.about.billing.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import me.matsumo.fanbox.core.ui.Res
import me.matsumo.fanbox.core.ui.appName
import me.matsumo.fanbox.core.ui.billing_plus_description
import me.matsumo.fanbox.core.ui.extensition.Platform
import me.matsumo.fanbox.core.ui.extensition.currentPlatform
import me.matsumo.fanbox.core.ui.theme.bold
import org.jetbrains.compose.resources.stringResource

internal fun LazyListScope.billingPlusTitleSection(
    onTerminate: () -> Unit,
) {
    item {
        TitleItem(
            modifier = Modifier.fillMaxWidth(),
            onTerminate = onTerminate,
        )
    }

    item {
        Text(
            modifier = Modifier
                .padding(vertical = 16.dp)
                .fillMaxWidth(),
            text = stringResource(Res.string.billing_plus_description, appName),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun TitleItem(
    onTerminate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val titleStyle = MaterialTheme.typography.headlineLarge.bold()
    val annotatedString = buildAnnotatedString {
        append("Buy ")

        withStyle(titleStyle.copy(color = MaterialTheme.colorScheme.primary).toSpanStyle()) {
            append("$appName+")
        }
    }

    Row(
        modifier = modifier.padding(top = 24.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = annotatedString,
            style = titleStyle,
            color = MaterialTheme.colorScheme.onSurface,
        )

        if (currentPlatform != Platform.Android) {
            IconButton(
                modifier = Modifier.padding(end = 8.dp),
                onClick = onTerminate,
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                )
            }
        }
    }
}
