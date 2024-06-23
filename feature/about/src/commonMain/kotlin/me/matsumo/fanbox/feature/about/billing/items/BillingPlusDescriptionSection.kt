package me.matsumo.fanbox.feature.about.billing.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DesignServices
import androidx.compose.material.icons.filled.DoNotDisturb
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.outlined.HideImage
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import me.matsumo.fanbox.core.ui.Res
import me.matsumo.fanbox.core.ui.billing_plus_item_accent_color
import me.matsumo.fanbox.core.ui.billing_plus_item_accent_color_description
import me.matsumo.fanbox.core.ui.billing_plus_item_download
import me.matsumo.fanbox.core.ui.billing_plus_item_download_description
import me.matsumo.fanbox.core.ui.billing_plus_item_feature
import me.matsumo.fanbox.core.ui.billing_plus_item_feature_description
import me.matsumo.fanbox.core.ui.billing_plus_item_hide_ads
import me.matsumo.fanbox.core.ui.billing_plus_item_hide_ads_description
import me.matsumo.fanbox.core.ui.billing_plus_item_hide_restricted
import me.matsumo.fanbox.core.ui.billing_plus_item_hide_restricted_description
import me.matsumo.fanbox.core.ui.billing_plus_item_lock
import me.matsumo.fanbox.core.ui.billing_plus_item_lock_description
import me.matsumo.fanbox.core.ui.billing_plus_item_material_you
import me.matsumo.fanbox.core.ui.billing_plus_item_material_you_description
import me.matsumo.fanbox.core.ui.billing_plus_item_support
import me.matsumo.fanbox.core.ui.billing_plus_item_support_description
import me.matsumo.fanbox.core.ui.billing_plus_item_widget
import me.matsumo.fanbox.core.ui.billing_plus_item_widget_description
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

internal fun LazyListScope.billingPlusDescriptionSection() {
    item {
        PlusItem(
            modifier = Modifier.fillMaxWidth(),
            title = Res.string.billing_plus_item_hide_ads,
            description = Res.string.billing_plus_item_hide_ads_description,
            icon = Icons.Default.DoNotDisturb,
        )
    }

    item {
        PlusItem(
            modifier = Modifier.fillMaxWidth(),
            title = Res.string.billing_plus_item_download,
            description = Res.string.billing_plus_item_download_description,
            icon = Icons.Default.Download,
        )
    }

    item {
        PlusItem(
            modifier = Modifier.fillMaxWidth(),
            title = Res.string.billing_plus_item_lock,
            description = Res.string.billing_plus_item_lock_description,
            icon = Icons.Default.Lock,
        )
    }

    item {
        PlusItem(
            modifier = Modifier.fillMaxWidth(),
            title = Res.string.billing_plus_item_hide_restricted,
            description = Res.string.billing_plus_item_hide_restricted_description,
            icon = Icons.Outlined.HideImage,
        )
    }

    item {
        PlusItem(
            modifier = Modifier.fillMaxWidth(),
            title = Res.string.billing_plus_item_widget,
            description = Res.string.billing_plus_item_widget_description,
            icon = Icons.Default.Widgets,
        )
    }

    item {
        PlusItem(
            modifier = Modifier.fillMaxWidth(),
            title = Res.string.billing_plus_item_material_you,
            description = Res.string.billing_plus_item_material_you_description,
            icon = Icons.Default.DesignServices,
        )
    }

    item {
        PlusItem(
            modifier = Modifier.fillMaxWidth(),
            title = Res.string.billing_plus_item_accent_color,
            description = Res.string.billing_plus_item_accent_color_description,
            icon = Icons.Default.ColorLens,
        )
    }

    item {
        PlusItem(
            modifier = Modifier.fillMaxWidth(),
            title = Res.string.billing_plus_item_feature,
            description = Res.string.billing_plus_item_feature_description,
            icon = Icons.Default.MoreHoriz,
        )
    }

    item {
        PlusItem(
            modifier = Modifier.fillMaxWidth(),
            title = Res.string.billing_plus_item_support,
            description = Res.string.billing_plus_item_support_description,
            icon = Icons.AutoMirrored.Outlined.HelpOutline,
        )
    }
}

@Composable
private fun PlusItem(
    title: StringResource,
    description: StringResource,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}