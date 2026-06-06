package me.matsumo.fanbox.feature.about.billing.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import me.matsumo.fanbox.core.resources.Res
import me.matsumo.fanbox.core.resources.billing_plus_description
import me.matsumo.fanbox.core.ui.appName
import me.matsumo.fanbox.core.ui.theme.bold
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun BillingTopSection(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        val titleStyle = MaterialTheme.typography.headlineLarge.bold()
        val annotatedString = buildAnnotatedString {
            append("Buy ")

            withStyle(titleStyle.copy(color = MaterialTheme.colorScheme.primary).toSpanStyle()) {
                append("$appName+")
            }
        }

        Text(
            modifier = Modifier.fillMaxWidth(),
            text = annotatedString,
            style = titleStyle,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(Res.string.billing_plus_description, appName),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
